package com.random_enchant.event.enchantments.frost_walker;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.random_enchant.enchantment.ModEnchantHelper.getEnchantmentLevel;

public class ArrowEntityFlyEvent {
    private static final int DETECTION_RADIUS = 512;
    private static final Map<BlockPos, Long> TEMPORARY_ICE_BLOCKS = new HashMap<>();
    private static final int ICE_DURATION_TICKS = 180;

    // 存储每个箭矢的上次生成位置
    private static final Map<Integer, BlockPos> lastIcePositions = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient) return;


            for (PlayerEntity player : world.getPlayers()) {
                Vec3d playerPos = player.getPos();
                Box detectionArea = new Box(
                        playerPos.x - DETECTION_RADIUS,
                        playerPos.y - DETECTION_RADIUS,
                        playerPos.z - DETECTION_RADIUS,
                        playerPos.x + DETECTION_RADIUS,
                        playerPos.y + DETECTION_RADIUS,
                        playerPos.z + DETECTION_RADIUS
                );

                List<Entity> allEntities = world.getOtherEntities(null, detectionArea);
                for (Entity entity : allEntities) {
                    if (entity instanceof ArrowEntity arrow && !arrow.isOnGround()) {
                        handleFrostWalkerArrow(arrow, world);
                    }
                }
            }
        });
    }

    private static void handleFrostWalkerArrow(ArrowEntity arrow, World world) {
        Entity owner = arrow.getOwner();
        if (owner == null) return;

        ItemStack stack = arrow.getItemStack();
        int frostWalkerLevel = stack == null ? 0 : getEnchantmentLevel(stack, world, Enchantments.FROST_WALKER);

        if (frostWalkerLevel > 0) {
            // 在箭矢尾部生成霜冰
            createIceDiskBackup(arrow, frostWalkerLevel, world);
        }
    }

    /**
     * 备用方法：在箭矢周围生成球形霜冰
     */
    private static void createIceDiskBackup(ArrowEntity arrow, int frostWalkerLevel, World world) {
        int radius = Math.max((int) (frostWalkerLevel * 0.5), 1);
        Vec3d arrowTailPos = calculateArrowTailPosition(arrow, radius + 1.5);
        BlockPos centerPos = new BlockPos((int) arrowTailPos.x, (int) arrowTailPos.y, (int) arrowTailPos.z);
        long currentTime = world.getTime();
        int blocksPlaced = 0;
        Entity owner = arrow.getOwner();
        if (owner != null && !(owner.getPos().distanceTo(arrowTailPos) >= radius + 1.5)) {
            return;
        }


        // 在箭矢周围生成球形霜冰
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    // 如果距离在半径内，尝试放置霜冰
                    if (distance <= radius) {
                        BlockPos icePos = centerPos.add(x, y, z);

                        // 放宽条件检查
                        if (isValidIcePositionLoose(world, icePos)) {
                            Block block = null;
                            if (frostWalkerLevel <= 3) block = Blocks.FROSTED_ICE;
                            else if (frostWalkerLevel < 10) block = Blocks.ICE;
                            else if (frostWalkerLevel < 20) block = Blocks.PACKED_ICE;
                            else block = Blocks.BLUE_ICE;


                            if (world.setBlockState(icePos, block.getDefaultState())) {
                                TEMPORARY_ICE_BLOCKS.put(icePos, currentTime);
                                blocksPlaced++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 宽松的位置验证
     */
    private static boolean isValidIcePositionLoose(World world, BlockPos pos) {
        // 检查世界边界
        if (!world.isInBuildLimit(pos)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        // 允许在可替换的方块上生成
        return state.isReplaceable();
    }
//    private static void cleanupExpiredIceBlocks(World world) {
//        long currentTime = world.getTime();
//        Iterator<Map.Entry<BlockPos, Long>> iterator = TEMPORARY_ICE_BLOCKS.entrySet().iterator();
//
//        while (iterator.hasNext()) {
//            Map.Entry<BlockPos, Long> entry = iterator.next();
//            if (currentTime - entry.getValue() > ICE_DURATION_TICKS) {
//                BlockPos pos = entry.getKey();
//                if (world.getBlockState(pos).getBlock() == Blocks.FROSTED_ICE) {
//                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
//                    System.out.println("恢复霜冰为水: " + pos);
//                }
//                iterator.remove();
//            }
//        }
//
//        // 清理过期的箭矢位置记录
//        lastIcePositions.entrySet().removeIf(entry -> {
//            Entity arrow = world.getEntityById(entry.getKey());
//            return arrow == null || arrow.isRemoved() || (arrow instanceof ArrowEntity && ((ArrowEntity)arrow).isOnGround());
//        });
//    }

    private static Vec3d calculateArrowTailPosition(ArrowEntity arrow, double tailOffset) {
        // 获取箭矢的飞行方向（反向就是尾部方向）
        Vec3d velocity = arrow.getVelocity();
        if (velocity.lengthSquared() < 0.01) {
            // 如果箭矢几乎静止，直接返回当前位置
            return arrow.getPos();
        }

        // 归一化速度向量
        Vec3d direction = velocity.normalize();

        // 计算尾部位置（向后偏移一定距离）
        Vec3d tailPos = arrow.getPos().subtract(direction.multiply(tailOffset));

        return tailPos;
    }
}