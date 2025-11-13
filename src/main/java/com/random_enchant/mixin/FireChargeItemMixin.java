package com.random_enchant.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireChargeItem.class)
public class FireChargeItemMixin {

    @Redirect(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;decrement(I)V"
            )
    )
    private void redirectDecrement(ItemStack stack, int amount, ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && !player.getAbilities().creativeMode) {
            World world = context.getWorld();
            int infinityLevel = getEnchantmentLevel(stack, world, Enchantments.INFINITY);
            if (infinityLevel > 0) {
                // 有无限附魔，不执行消耗
                return;
            }
        }
        // 没有无限附魔或者是创造模式，执行原版消耗
        stack.decrement(amount);
    }

    @Unique
    private static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        if (stack == null || stack.isEmpty()) return 0;

        RegistryEntry<Enchantment> enchantmentEntry = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(enchantment)
                .orElse(null);

        return enchantmentEntry != null ? EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
}