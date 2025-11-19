package com.random_enchant.event.enchantments.infinity;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.random_enchant.enchantment.ModEnchantHelper.getEnchantmentLevel;

public class PotionThrowEvent implements UseItemCallback {
    public void registerEvents() {
        UseItemCallback.EVENT.register(this);
    }

    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // 检查是否是喷溅药水或滞留药水
        if (!(stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION)) {
            return TypedActionResult.pass(stack);
        }

        // 检查是否有无限附魔
        int infinityLevel = getEnchantmentLevel(player, hand, world, Enchantments.INFINITY);
        if (infinityLevel <= 0) {
            return TypedActionResult.pass(stack);
        }

        // 只在服务端处理
        if (world.isClient()) {
            return TypedActionResult.pass(stack);
        }

        // 创造模式不需要处理
        if (player.getAbilities().creativeMode) {
            return TypedActionResult.pass(stack);
        }

        // 检查物品是否为空
        if (stack.isEmpty()) {
            return TypedActionResult.pass(stack);
        }

        // 保存当前物品信息以便后续补充
        ItemStack originalStack = stack.copy();
        int originalCount = stack.getCount();

        // 使用服务器任务在下一个tick补充物品
        world.getServer().execute(() -> {
            // 检查玩家手中是否仍然是药水且数量减少了
            ItemStack currentStack = player.getStackInHand(hand);

            if (currentStack.getItem() == Items.SPLASH_POTION ||
                    currentStack.getItem() == Items.LINGERING_POTION) {

                if (currentStack.getCount() < originalCount) {
                    // 直接增加当前堆栈的数量，而不是插入新堆栈
                    currentStack.increment(1);
                    player.getInventory().markDirty();
                }
            } else {
                // 如果手中的物品已经改变，创建新堆栈并尝试插入物品栏
                ItemStack newStack = originalStack.copy();
                newStack.setCount(1);

                if (!player.getInventory().insertStack(newStack)) {
                    player.dropItem(newStack, false);
                }
            }
        });

        // 返回PASS让原版逻辑继续处理药水投掷
        return TypedActionResult.pass(stack);
    }
}