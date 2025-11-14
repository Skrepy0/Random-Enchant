package com.random_enchant.event.enchantments.infinity;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ThrowItemEvent implements UseItemCallback {
    public void registerEvents() {
        UseItemCallback.EVENT.register(this);
    }

    private static int getEnchantmentLevel(PlayerEntity player, Hand hand, World world, RegistryKey<Enchantment> enchantment) {
        ItemStack stack = player.getStackInHand(hand);
        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        int enchantmentLevel = enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
        return enchantmentLevel;
    }

    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        if (!(item == Items.ENDER_PEARL||item == Items.WIND_CHARGE||item == Items.EGG||item == Items.ENDER_EYE||item == Items.SNOWBALL||
              item == Items.FIREWORK_ROCKET || (item == Items.FIRE_CHARGE&&getEnchantmentLevel(player,hand,world,Enchantments.POWER)>0))) {
            return TypedActionResult.pass(stack);
        }

        if (item == Items.ENDER_PEARL && player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)){
            return TypedActionResult.pass(stack);
        }
        if (item == Items.WIND_CHARGE && player.getItemCooldownManager().isCoolingDown(Items.WIND_CHARGE)){
            return TypedActionResult.pass(stack);
        }
        if (item == Items.FIRE_CHARGE && player.getItemCooldownManager().isCoolingDown(Items.FIRE_CHARGE)){
            return TypedActionResult.pass(stack);
        }
        if (item == Items.FIREWORK_ROCKET && !player.isFallFlying()){
            return TypedActionResult.pass(stack);
        }


        int infinityLevel = getEnchantmentLevel(player, hand, world, Enchantments.INFINITY);
        if (infinityLevel <= 0) {
            return TypedActionResult.pass(stack);
        }


        if (world.isClient()) {
            return TypedActionResult.pass(stack);
        }


        if (player.getAbilities().creativeMode) {
            return TypedActionResult.pass(stack);
        }


        if (stack.isEmpty()) {
            return TypedActionResult.pass(stack);
        }
        ItemStack originalStack = stack.copy();
        int originalCount = stack.getCount();

        world.getServer().execute(() -> {
            // 检查玩家手中是否仍然是item且数量减少了
            ItemStack currentStack = player.getStackInHand(hand);

            if (currentStack.getItem() == originalStack.getItem()) {
                // 直接增加当前堆栈的数量，而不是插入新堆栈
                currentStack.increment(1);
                player.getInventory().markDirty();

            } else {
                ItemStack newStack = originalStack.copy();
                newStack.setCount(1);
                if (!player.getInventory().insertStack(newStack)) {
                    player.dropItem(newStack, false);
                }
            }
        });

        return TypedActionResult.pass(stack);
    }
}