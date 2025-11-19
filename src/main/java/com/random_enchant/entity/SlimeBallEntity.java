package com.random_enchant.entity;

import net.minecraft.block.Blocks;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SlimeBallEntity extends ThrownItemEntity {
    public static EntityType<SlimeBallEntity> TYPE;
    private ItemStack itemStack;
    private boolean canBeCatchUp = false;// 碰撞后才能被捡起
    private static final float COLLISION_WIDTH = 1f;
    private static final float COLLISION_HEIGHT = 1f;
    private int powerLevel;

    // 添加最大渲染距离
    private static final double MAX_RENDER_DISTANCE = 3000.0;
    private LivingEntity owner;

    // 防止穿过方块的设置
    private static final double MAX_SPEED = 2.0; // 最大速度限制
    private static final int MAX_NO_COLLISION_TICKS = 5;

    public int getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

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
        this.setBoundingBox(createBoundingBox());
    }

    @Override
    public void tick() {
        // 预碰撞检测 - 防止穿过方块
        if (!this.getWorld().isClient() && checkPreCollision()) {
            return; // 如果发生碰撞，不执行后续的移动逻辑
        }

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

    /**
     * 预碰撞检测 - 防止实体穿过方块
     */
    private boolean checkPreCollision() {
        Vec3d currentPos = this.getPos();
        Vec3d velocity = this.getVelocity();

        // 如果速度很小，不需要预检测
        if (velocity.lengthSquared() < 0.01) {
            return false;
        }

        Vec3d nextPos = currentPos.add(velocity);

        // 使用光线追踪检测碰撞
        BlockHitResult hitResult = this.getWorld().raycast(new RaycastContext(
                currentPos,
                nextPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                this
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 提前处理碰撞
            handlePreCollision(hitResult);
            return true;
        }

        return false;
    }

    /**
     * 处理预碰撞
     */
    private void handlePreCollision(BlockHitResult hitResult) {
        // 设置实体位置到碰撞点
        Vec3d collisionPoint = hitResult.getPos();
        this.setPosition(collisionPoint);

        // 调用碰撞处理
        this.onBlockHit(hitResult);
    }

    /**
     * 限制速度，防止穿过方块
     */
    @Override
    public void setVelocity(Vec3d velocity) {
        double currentSpeed = velocity.length();

        if (currentSpeed > MAX_SPEED) {
            velocity = velocity.normalize().multiply(MAX_SPEED);
        }

        super.setVelocity(velocity);
    }

    private Box createBoundingBox() {
        return new Box(
                this.getX() - COLLISION_WIDTH / 2,
                this.getY(),
                this.getZ() - COLLISION_WIDTH / 2,
                this.getX() + COLLISION_WIDTH / 2,
                this.getY() - COLLISION_HEIGHT,
                this.getZ() + COLLISION_WIDTH / 2
        );
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public SlimeBallEntity(World world, LivingEntity owner, ItemStack stack, int powerLevel) {
        super(TYPE, world);
        this.owner = owner;
        this.itemStack = stack.copy();
        this.powerLevel = powerLevel;

        if (owner != null){
            this.setPosition(owner.getX(), owner.getEyeY() + 0.2, owner.getZ());
        }
        this.setOwner(owner);

        // 初始速度限制
        Vec3d lookVec = null;
        if (owner != null) {
            lookVec = owner.getRotationVector();
        }
        if (lookVec != null) {
            Vec3d velocity = lookVec.multiply(1.5).add(0, 0.1, 0);
            // 应用速度限制
            double speed = velocity.length();
            if (speed > MAX_SPEED) {
                velocity = velocity.normalize().multiply(MAX_SPEED);
            }
            this.setVelocity(velocity);
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

        World world = this.getWorld();
        if (world.isClient()) {
            return; // 只在服务端处理
        }

        // 确保实体位置正确，防止穿过
        Vec3d collisionPoint = blockHitResult.getPos();
        this.setPosition(collisionPoint);

        // 获取碰撞面的法线方向
        Vec3d normal = new Vec3d(
                blockHitResult.getSide().getVector().getX(),
                blockHitResult.getSide().getVector().getY(),
                blockHitResult.getSide().getVector().getZ()
        );

        // 获取当前速度
        Vec3d currentVelocity = this.getVelocity();

        // 计算反弹方向（反射向量）
        double dotProduct = currentVelocity.dotProduct(normal);
        Vec3d reflection = currentVelocity.subtract(normal.multiply(2 * dotProduct));
        double length = reflection.length();

        // 根据速度和碰撞面类型调整反弹
        if (length <= 0.9) {
            this.setVelocity(reflection.multiply(1.0));
        } else if (world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.SLIME_BLOCK && length <= 3) {
            this.setVelocity(reflection.multiply(1.1));
        } else {
            this.setVelocity(reflection.multiply(0.85));
        }

        // 播放碰撞效果
        world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);

        // 处理可拾取状态
        if (canBeCatchUp && reflection.multiply(0.85).length() <= 0.3) {
            ItemStack newItemStack = this.itemStack.copy();
            newItemStack.setCount(1);
            this.dropStack(newItemStack);
            this.discard();
        } else {
            if (!canBeCatchUp) canBeCatchUp = true;
        }

        world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_FALL,
                SoundCategory.BLOCKS, 0.2F, 1.4F);
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

    // 发射器使用的构造函数
    public SlimeBallEntity(World world, double x, double y, double z) {
        super(TYPE, world);
        this.setPosition(x, y, z);
        this.itemStack = new ItemStack(Items.SLIME_BALL);
    }
}