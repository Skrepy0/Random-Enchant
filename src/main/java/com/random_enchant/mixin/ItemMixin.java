package com.random_enchant.mixin;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    // 1. 修复：提前在 HEAD 保存原栈（避免后续被修改），且不取消原返回（cancellable=false，原逻辑正常执行）
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsingHead(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
        if(foodComponent != null){
            // 保存原栈的副本（关键：此时 stack 还未被消耗，数据完整）
            ItemStack originalStack = stack.copy();
            // 调用核心逻辑，传入原栈
            handleInfiniteFood(originalStack, world, user);
        }

    }

    // 2. 核心逻辑：分离处理，便于维护
    @Unique
    private void handleInfiniteFood(ItemStack originalStack, World world, LivingEntity user) {
        // 检查：非玩家、客户端、空栈 → 跳过

        if (!(user instanceof PlayerEntity player) || world.isClient() || originalStack.isEmpty()) {
            return;
        }

        // 3. 修复：使用自定义的“无限食物”附魔（替换 Enchantments.INFINITY，因为它不支持食物）
        int infiniteLevel = getEnchantmentLevel(originalStack, world, Enchantments.INFINITY); // 改为你的自定义附魔

        if (infiniteLevel <= 0) {
            return;
        }

        // 创造模式无需补充 → 跳过
        if (player.getAbilities().creativeMode) {
            return;
        }
        Hand hand = user.getActiveHand();
        world.getServer().execute(() -> {
            ItemStack currentStack = player.getStackInHand(hand);
            if (currentStack.getItem() == originalStack.getItem()) {
                if (currentStack.getCount() !=1) {
                    // 直接增加当前堆栈的数量，而不是插入新堆栈
                    currentStack.increment(1);
                    player.getInventory().markDirty();
                }else {
                    // 如果手中的物品已经改变，创建新堆栈并尝试插入物品栏
                    ItemStack newStack = originalStack.copy();
                    newStack.setCount(1);

                    if (!player.getInventory().insertStack(newStack)) {
                        player.dropItem(newStack, false);
                    }
                }
            }
            player.currentScreenHandler.sendContentUpdates();
        });
    }

    // 附魔等级获取：逻辑正确，保留
    @Unique
    private int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        RegistryEntry<Enchantment> enchantmentEntry = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(enchantment)
                .orElse(null);

        int level = enchantmentEntry != null ? EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
        return level;
    }
}