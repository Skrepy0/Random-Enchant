package com.random_enchant.entity;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SlimeBallEntity extends ThrownItemEntity {
    public static EntityType<SlimeBallEntity> TYPE;
    private ItemStack itemStack;
    private boolean returning = false;
    private static final float COLLISION_WIDTH = 0.5f;
    private static final float COLLISION_HEIGHT = 0.5f;

    // 添加最大渲染距离
    private static final double MAX_RENDER_DISTANCE = 3000.0;
    private LivingEntity owner;

    // 添加防止下沉的变量
    private static final int MAX_NO_COLLISION_TICKS = 5; // 防止刚生成时卡在方块里的ticks

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
        if (owner != null && owner.isAlive() && this.returning) {
            double ox = owner.getX();
            double oy = owner.getY();
            double oz = owner.getZ();
            double ex = this.getX();
            double ey = this.getY();
            double ez = this.getZ();
            double distance = Math.sqrt((ox - ex) * (ox - ex) + (oy - ey) * (oy - ey) + (oz - ez) * (oz - ez));
            Vec3d direction = new Vec3d(
                    ox - ex,
                    oy - ey,
                    oz - ez
            );
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

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public SlimeBallEntity(World world, LivingEntity owner, ItemStack stack) {
        super(TYPE, world);
        this.owner = owner;
        this.itemStack = stack.copy();
        // 提高生成位置，避免一开始就卡在地下
        this.setPosition(owner.getX(), owner.getEyeY() + 0.2, owner.getZ());
        this.setOwner(owner);

        // 初始速度稍微向上，避免直接下沉
        Vec3d lookVec = owner.getRotationVector();
        this.setVelocity(lookVec.multiply(1.5).add(0, 0.1, 0));
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0F);
    }

    private static double getVec3dLength(Vec3d vec3d) {
        double length = 0;
        double x = vec3d.x;
        double y = vec3d.y;
        double z = vec3d.z;
        length = Math.sqrt(x * x + y * y + z * z);
        return length;
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

        if (!this.getWorld().isClient()) {
            if (!this.getWorld().isClient()) {

                // 计算反弹方向（反射向量）
                // 公式：反射向量 = 入射向量 - 2 * (入射向量 · 法线) * 法线
                double dotProduct = currentVelocity.dotProduct(normal);
                Vec3d reflection = currentVelocity.subtract(normal.multiply(2 * dotProduct));
                double length = getVec3dLength(reflection);
                if (length <= 0.9) {
                    this.setVelocity(reflection.multiply(1.0));
                } else {
                    // 设置反弹后的速度（可以稍微减少一点能量）
                    this.setVelocity(reflection.multiply(0.8));
                }
                // 播放碰撞效果
                this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
                if (returning && getVec3dLength(reflection.multiply(0.8)) <= 0.2) {
                    ItemStack newItemStack = this.itemStack.copy();
                    newItemStack.setCount(1);
                    this.dropStack(newItemStack);
                    this.discard();
                } else {
                    returning = true;
                }
            }
        }
    }


    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }
}