package com.random_enchant.event.dispenser;

import com.random_enchant.entity.SlimeBallEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class SlimeBallDispenserBehavior extends ItemDispenserBehavior {

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.world();
        if (world == null) return stack;
        Position position = DispenserBlock.getOutputLocation(pointer);
        Direction direction = pointer.state().get(DispenserBlock.FACING);
        int powerLevel = getEnchantmentLevel(stack, world, Enchantments.POWER);
        if (powerLevel > 0) {
            // 创建粘液球实体
            SlimeBallEntity slimeBallEntity = new SlimeBallEntity(world, world.getClosestPlayer(position.getX(), position.getY(), position.getZ(), 10, false), stack,powerLevel);
            slimeBallEntity.setItemStack(stack.copy().split(1)); // 使用一个粘液球
            slimeBallEntity.setPosition(position.getX(), position.getY(), position.getZ());
            // 设置速度方向
            double speed = powerLevel * 0.2+1;
            slimeBallEntity.setVelocity(
                    direction.getOffsetX() * speed,
                    (direction.getOffsetY() * speed) + 0.1,
                    direction.getOffsetZ() * speed
            );
            slimeBallEntity.setItem(stack);
            // 生成实体
            world.spawnEntity(slimeBallEntity);

            // 减少物品数量
            stack.decrement(1);
            return stack;
        } else {
            ItemStack itemStack = stack.split(1);
            spawnItem(pointer.world(), itemStack, 6, direction, position);
            return stack;
        }

    }

    private static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {

        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        return enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
}