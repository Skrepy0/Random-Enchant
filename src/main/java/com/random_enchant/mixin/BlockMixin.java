package com.random_enchant.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        // 只在服务器端执行且放置者是玩家

        if (placer instanceof PlayerEntity player && !world.isClient) {
            // 检查创造模式
            if (player.getAbilities().creativeMode) {
                return;
            }
            // 检查主手和副手
            for (Hand hand : Hand.values()) {
                ItemStack handStack = player.getStackInHand(hand);
                if (hand == Hand.OFF_HAND){
                    if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof BlockItem){
                        continue;
                    }
                }
                // 如果手中的物品有无限附魔
                if (getEnchantmentLevel(handStack, world, Enchantments.INFINITY) > 0) {
                    // 恢复物品数量
                    if (handStack.getItem() instanceof BlockItem){
                        if (handStack.getCount() == 0) {
                            // 如果物品已耗尽，设置为1
                            player.setStackInHand(hand, new ItemStack(handStack.getItem(), 1));
                        } else {
                            // 否则增加1个
                            handStack.increment(1);
                        }
                        player.currentScreenHandler.sendContentUpdates();
                        break; // 只需要处理一个手
                    }
                }
            }
        }
    }

    @Unique
    private int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        RegistryEntry<Enchantment> enchantmentEntry = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(enchantment)
                .orElse(null);

        return enchantmentEntry != null ? EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
}