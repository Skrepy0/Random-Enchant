package com.random_enchant.entity;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SlimeBallEntity extends ThrownItemEntity {
    public static EntityType<SlimeBallEntity> TYPE;
    private ItemStack itemStack;
    private boolean canBeCatchUp = false;// 碰撞后才能被捡起
    private static final float COLLISION_WIDTH = 0.5f;
    private static final float COLLISION_HEIGHT = 0.5f;
    private int powerLevel;
    // 添加最大渲染距离
    private static final double MAX_RENDER_DISTANCE = 3000.0;
    private LivingEntity owner;

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    // 添加防止下沉的变量
    private static final int MAX_NO_COLLISION_TICKS = 5; // 防止刚生成时卡在方块里的ticks


    @Nullable
    @Override
    public LivingEntity getOwner() {
        return owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
    }

    public SlimeBallEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        this.itemStack = ItemStack.EMPTY;
        // 设置碰撞箱
        this.setBoundingBox(createBoundingBox());
    }

    @Override
    public void tick() {
        super.tick();
        this.setBoundingBox(createBoundingBox());

        if (this.owner != null && this.owner.isAlive() && this.canBeCatchUp) {
            double distance = this.getPos().distanceTo(this.owner.getPos());
            if (distance < 3.0) {
                if (owner instanceof PlayerEntity player) {
                    ItemStack stack = this.itemStack.copy();
                    stack.setCount(1);
                    if (!player.getAbilities().creativeMode && !stack.isEmpty() && !player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, false);
                    }
                }
                this.discard();
            }
        }
    }

    private Box createBoundingBox() {
        return new Box(
                this.getX() - COLLISION_WIDTH / 2,
                this.getY(),
                this.getZ() - COLLISION_WIDTH / 2,
                this.getX() + COLLISION_WIDTH / 2,
                this.getY() + COLLISION_HEIGHT,
                this.getZ() + COLLISION_WIDTH / 2
        );
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public SlimeBallEntity(World world, LivingEntity owner, ItemStack stack,int powerLevel) {
        super(TYPE, world);
        this.owner = owner;
        this.itemStack = stack.copy();
        // 提高生成位置，避免一开始就卡在地下
        this.powerLevel = powerLevel;
        if (owner != null){
            this.setPosition(owner.getX(), owner.getEyeY() + 0.2, owner.getZ());
        }
        this.setOwner(owner);

        // 初始速度稍微向上，避免直接下沉
        Vec3d lookVec = null;
        if (owner != null) {
            lookVec = owner.getRotationVector();
        }
        if (lookVec != null) {
            this.setVelocity(lookVec.multiply(1.5).add(0, 0.1, 0));
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0F);
        canBeCatchUp = true;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        // 获取碰撞面的法线方向
        Vec3d normal = new Vec3d(
                blockHitResult.getSide().getVector().getX(),
                blockHitResult.getSide().getVector().getY(),
                blockHitResult.getSide().getVector().getZ()
        );

        // 获取当前速度
        Vec3d currentVelocity = this.getVelocity();
        World world = this.getWorld();
        if (!world.isClient()) {
            if (!world.isClient()) {

                // 计算反弹方向（反射向量）
                // 公式：反射向量 = 入射向量 - 2 * (入射向量 · 法线) * 法线
                double dotProduct = currentVelocity.dotProduct(normal);
                Vec3d reflection = currentVelocity.subtract(normal.multiply(2 * dotProduct));
                double length = reflection.length();
                if (length <= 0.9) {
                    this.setVelocity(reflection.multiply(1.0));
                } else {
                    // 设置反弹后的速度（可以稍微减少一点能量）
                    this.setVelocity(reflection.multiply(0.85));
                }
                // 播放碰撞效果
                this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
                if (canBeCatchUp && reflection.multiply(0.85).length() <= 0.3) {
                    ItemStack newItemStack = this.itemStack.copy();
                    newItemStack.setCount(1);
                    this.dropStack(newItemStack);
                    this.discard();
                } else {
                    canBeCatchUp = true;
                }
                world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_FALL,
                        SoundCategory.BLOCKS, 0.2F, 1.4F);
            }
        }
    }


    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("SlimeBallItem") && this.getWorld() != null) {
            this.itemStack = ItemStack.fromNbt(this.getWorld().getRegistryManager(), nbt.getCompound("SlimeBallItem")).orElse(ItemStack.EMPTY);
        } else if (nbt.contains("SlimeBallItem")) {
            this.itemStack = ItemStack.EMPTY;
        }
        this.powerLevel = nbt.getInt("PowerLevel");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.itemStack != null && !this.itemStack.isEmpty() && this.getWorld() != null) {
            nbt.put("SlimeBallItem", this.itemStack.encode(this.getWorld().getRegistryManager()));
        }
        nbt.putInt("PowerLevel", this.powerLevel);
    }
}