package com.random_enchant.event;

import com.random_enchant.command.RandomEnchantCommand;
import com.random_enchant.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class AttackMobEvent {
    public static void registerAttackMobEvents() {
        // 注册攻击实体事件回调
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity) && !player.getAbilities().creativeMode) {
                enchantItem(player, world, hand);

            }
            return ActionResult.PASS; // 允许正常攻击处理继续
        });
    }

    private static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        return enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }

    private static void addEnchantment(ItemStack stack, World world, RegistryKey<Enchantment> enchantment, int level) {
        int enchantmentLevel = getEnchantmentLevel(stack, world, enchantment);
        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        stack.addEnchantment(enchantmentEntry, level + enchantmentLevel);
    }

    private static RegistryKey<Enchantment> getRandomEnchant() {
        ArrayList<RegistryKey<Enchantment>> enchantments = new ArrayList<>();
        enchantments.add(Enchantments.PROTECTION);
        enchantments.add(Enchantments.FIRE_PROTECTION);
        enchantments.add(Enchantments.FEATHER_FALLING);
        enchantments.add(Enchantments.BLAST_PROTECTION);
        enchantments.add(Enchantments.PROJECTILE_PROTECTION);
        enchantments.add(Enchantments.RESPIRATION);
        enchantments.add(Enchantments.AQUA_AFFINITY);
        enchantments.add(Enchantments.THORNS);
        enchantments.add(Enchantments.DEPTH_STRIDER);
        enchantments.add(Enchantments.FROST_WALKER);
        enchantments.add(Enchantments.BINDING_CURSE);
        enchantments.add(Enchantments.SOUL_SPEED);
        enchantments.add(Enchantments.SWIFT_SNEAK);
        enchantments.add(Enchantments.SHARPNESS);
        enchantments.add(Enchantments.SMITE);
        enchantments.add(Enchantments.BANE_OF_ARTHROPODS);
        enchantments.add(Enchantments.KNOCKBACK);
        enchantments.add(Enchantments.FIRE_ASPECT);
        enchantments.add(Enchantments.SWEEPING_EDGE);
        enchantments.add(Enchantments.EFFICIENCY);
        enchantments.add(Enchantments.SILK_TOUCH);
        enchantments.add(Enchantments.UNBREAKING);
        enchantments.add(Enchantments.FORTUNE);
        enchantments.add(Enchantments.POWER);
        enchantments.add(Enchantments.PUNCH);
        enchantments.add(Enchantments.FLAME);
        enchantments.add(Enchantments.INFINITY);
        enchantments.add(Enchantments.LUCK_OF_THE_SEA);
        enchantments.add(Enchantments.LURE);
        enchantments.add(Enchantments.LOYALTY);
        enchantments.add(Enchantments.IMPALING);
        enchantments.add(Enchantments.RIPTIDE);
        enchantments.add(Enchantments.CHANNELING);
        enchantments.add(Enchantments.MULTISHOT);
        enchantments.add(Enchantments.QUICK_CHARGE);
        enchantments.add(Enchantments.PIERCING);
        enchantments.add(Enchantments.DENSITY);
        enchantments.add(Enchantments.BREACH);
        enchantments.add(Enchantments.WIND_BURST);
        enchantments.add(Enchantments.MENDING);
        enchantments.add(Enchantments.VANISHING_CURSE);
        enchantments.add(ModEnchantments.FURY_OF_FLY);

        java.util.Random random = new java.util.Random();
        int randomIndex = random.nextInt(enchantments.size());
        return enchantments.get(randomIndex);
    }

    private static void enchantItem(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand) {
        // 只在服务器端执行
        if (world.isClient) {
            return;
        }
        if (!RandomEnchantCommand.isRandomEnchant) {
            return; // 如果禁用，直接返回
        }
        ItemStack stack = player.getStackInHand(hand);
        // 如果物品为空则不处理
        if (stack.isEmpty()) {
            return;
        }
        java.util.Random random = new java.util.Random();
        int pro = random.nextInt(10);
        int level;
        if (pro <= 3) {
            level = random.nextInt(5) + 6;
        } else {
            level = random.nextInt(3);
        }
        RegistryKey<Enchantment> enchantment = getRandomEnchant();
        addEnchantment(stack, world, enchantment, level);
    }
}

