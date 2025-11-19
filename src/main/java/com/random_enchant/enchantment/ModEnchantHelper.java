package com.random_enchant.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ModEnchantHelper {
    public static int getEnchantmentLevel(PlayerEntity player, Hand hand, World world, RegistryKey<Enchantment> enchantment) {
        ItemStack stack = player.getStackInHand(hand);
        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        return enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
    public static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {

        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        return enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
}
