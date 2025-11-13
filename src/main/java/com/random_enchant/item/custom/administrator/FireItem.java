package com.random_enchant.item.custom.administrator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FireItem extends Item {
    public FireItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = null;
        if (player != null) {
            stack = player.getStackInHand(hand);
        }
        BlockPos clickedPos = context.getBlockPos();
        Direction side = context.getSide();

        // 只在服务器端处理
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        // 计算放置位置（基于点击的面）
        BlockPos placementPos = calculatePlacementPos(clickedPos, side);

        // 检查目标位置是否可以放置方块
        if (canPlaceAt(world, placementPos)) {
            // 放置火方块
            world.setBlockState(placementPos, Blocks.FIRE.getDefaultState());
            if (player != null && !player.isCreative()) {
                stack.decrement(1);
            }

            world.playSound(null, placementPos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            //粒子效果
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.FLAME,
                        placementPos.getX() + 0.5,
                        placementPos.getY() + 0.5,
                        placementPos.getZ() + 0.5,
                        10,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    /**
     * 根据点击的面计算放置位置
     */
    private BlockPos calculatePlacementPos(BlockPos clickedPos, Direction side) {
        return clickedPos.offset(side);
    }

    /**
     * 检查是否可以在此位置放置方块
     */
    private boolean canPlaceAt(World world, BlockPos pos) {
        // 检查位置是否在世界范围内
        if (!world.isInBuildLimit(pos)) {
            return false;
        }

        // 检查位置是否已有方块，且该方块可被替换
        BlockState existingState = world.getBlockState(pos);

        // 在 1.21.1 中，使用 isReplaceable() 方法替代 getMaterial().isReplaceable()
        return existingState.isAir() || existingState.isReplaceable();
    }
}
