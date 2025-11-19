package com.random_enchant.event.enchantments.power;

import com.random_enchant.RandomEnchant;
import com.random_enchant.command.RandomEnchantCommand;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.random_enchant.enchantment.ModEnchantHelper.getEnchantmentLevel;

public class ThrowFireBallEvent implements UseItemCallback {
    public void registerEvent(){UseItemCallback.EVENT.register(this);}

    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        int powerLevel = getEnchantmentLevel(stack,world, Enchantments.POWER);
        if (world.isClient()){
            return TypedActionResult.pass(stack);
        }
        if (stack.getItem()!= Items.FIRE_CHARGE){
            return TypedActionResult.pass(stack);
        }
        if (!(powerLevel >0)){
            return TypedActionResult.pass(stack);
        }
        if (player.getItemCooldownManager().isCoolingDown(Items.FIRE_CHARGE)){
            return TypedActionResult.pass(stack);
        }
        throwFireBall(player,hand,world,powerLevel,stack);
        return TypedActionResult.pass(stack);
    }

    public static void throwFireBall(PlayerEntity player, Hand hand, World world, int powerLevel, ItemStack stack) {
        // 优化1：使用更简洁的方式初始化速度向量
        FireballEntity fireballEntity = new FireballEntity(world, player, Vec3d.ZERO, powerLevel);

        // 优化2：提取常量，提高代码可读性
        final float SPAWN_DISTANCE = 1.5F;
        final float DEGREES_TO_RADIANS = 0.017453292F;

        // 优化3：使用更清晰的变量名
        float yawRadians = player.getYaw() * DEGREES_TO_RADIANS;
        double offsetX = -Math.sin(yawRadians) * SPAWN_DISTANCE;
        double offsetZ = Math.cos(yawRadians) * SPAWN_DISTANCE;

        // 优化4：简化位置计算
        double spawnX = player.getX() + offsetX;
        double spawnY = player.getEyeY();
        double spawnZ = player.getZ() + offsetZ;

        // 设置火球位置
        fireballEntity.setPosition(spawnX, spawnY, spawnZ);

        // 优化5：提取速度相关常量
        final float SPEED_MULTIPLIER = powerLevel >= 2? 2.0F:1.0F;
        final float DIVERGENCE = 0.5F;
        fireballEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, SPEED_MULTIPLIER, DIVERGENCE);

        // 播放音效和设置冷却
        world.playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.AMBIENT, 1.0F, 1.0F);
        player.getItemCooldownManager().set(Items.FIRE_CHARGE, 30);

        // 生成火球
        world.spawnEntity(fireballEntity);

        // 优化6：使用更简单的方法添加粒子效果
        if (!world.isClient) {
            if (RandomEnchantCommand.showFireChargeParticular &&powerLevel>=5){
                startFireballParticleEffect(world, fireballEntity);
            }

        }

        // 优化7：简化物品消耗逻辑
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
    }

    /**
     * 启动火球粒子效果 - 使用简单循环实现
     */
    private static void startFireballParticleEffect(World world, FireballEntity fireball) {
        if (world.isClient) return;

        // 创建一个简单的循环来生成粒子
        new Thread(() -> {
            AtomicInteger tickCount = new AtomicInteger(0);
            try {
                AtomicReference<Float> radius = new AtomicReference<>((float) 20);
                while (fireball.isAlive() && !fireball.isRemoved() && tickCount.get() < 200) {
                    // 在主线程中执行粒子生成
                    ((ServerWorld) world).getServer().execute(() -> {
                        if (fireball.isAlive() && !fireball.isRemoved()) {
                            spawnRingParticles(world, fireball.getPos(), radius.get(),fireball.getVelocity());
                            if (radius.get() > 3) radius.updateAndGet(v -> v - 2);
                        }
                    });

                    // 等待2 tick (100ms)
                    Thread.sleep(100);
                    tickCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 在指定位置生成环形粒子效果
     */
    private static void spawnRingParticles(World world, Vec3d center, float radius, Vec3d velocity) {
        // 环形粒子数量
        final int PARTICLE_COUNT = 80;
        // 获取火焰弹的速度方向
        Vec3d direction = velocity.normalize();
        direction.multiply(0.2);
        // 计算与速度方向垂直的基向量
        Vec3d base1, base2;

        // 选择一个与速度方向不平行的参考向量
        Vec3d reference = Math.abs(direction.y) < 0.9 ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);

        // 计算第一个垂直向量（通过叉积）
        base1 = direction.crossProduct(reference).normalize();

        // 计算第二个垂直向量（与第一个和速度方向都垂直）
        base2 = direction.crossProduct(base1).normalize();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // 计算环形上的点（在垂直于速度方向的平面上）
            double angle = 2 * Math.PI * i / PARTICLE_COUNT;
            double xOffset = radius * (Math.cos(angle) * base1.x + Math.sin(angle) * base2.x);
            double yOffset = radius * (Math.cos(angle) * base1.y + Math.sin(angle) * base2.y);
            double zOffset = radius * (Math.cos(angle) * base1.z + Math.sin(angle) * base2.z);

            double x = center.x + xOffset;
            double y = center.y + yOffset;
            double z = center.z + zOffset;

            // 在服务器端发送粒子数据包给所有客户端
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                double speed = 0.1; // 调整速度值，10太快了

                // 计算从中心指向粒子位置的方向（向外）
                Vec3d direction1 = new Vec3d(x - center.x, y - center.y, z - center.z).normalize();

                // 使用 spawnParticles 方法，通过速度参数设置粒子运动方向
                serverWorld.spawnParticles(
                        ParticleTypes.END_ROD,
                        x, y, z,                    // 粒子位置
                        10,                          // 粒子数量
                        direction1.x * speed,        // X方向速度
                        direction1.y * speed,        // Y方向速度
                        direction1.z * speed,        // Z方向速度
                        0.01                        // 基础速度（会被方向向量缩放）
                );
            }

// 额外在中心位置添加一些粒子
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;

                // 中心粒子向外扩散
                for (int j = 0; j < 2; j++) {
                    // 随机方向
                    double dirX = (world.random.nextDouble() - 0.5) * 2;
                    double dirY = (world.random.nextDouble() - 0.5) * 2;
                    double dirZ = (world.random.nextDouble() - 0.5) * 2;
                    Vec3d dir = new Vec3d(dirX, dirY, dirZ).normalize();

                    serverWorld.spawnParticles(
                            ParticleTypes.FIREWORK,
                            center.x, center.y, center.z,
                            1,
                            dir.x * 5,  // X方向速度
                            dir.y * 5,  // Y方向速度
                            dir.z * 5,  // Z方向速度
                            0.01
                    );
                    serverWorld.spawnParticles(
                            ParticleTypes.FLAME,
                            center.x, center.y, center.z,
                            1,
                            dir.x * 5,  // X方向速度
                            dir.y * 5,  // Y方向速度
                            dir.z * 5,  // Z方向速度
                            0.01
                    );
                }
            }
    }

}
}
