package com.random_enchant.event.enchantments.power;

import com.random_enchant.entity.SlimeBallEntity;
import com.random_enchant.sound.ModSoundEvents;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.random_enchant.enchantment.ModEnchantHelper.getEnchantmentLevel;

public class ThrowSlimeBallEvent implements UseItemCallback {
    public void registerEvent(){UseItemCallback.EVENT.register(this);}
    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        int powerEnchantLevel = getEnchantmentLevel(stack,world, Enchantments.POWER);
        if (world.isClient){
            return TypedActionResult.pass(stack);
        }
        if (stack.getItem() != Items.SLIME_BALL){
            return TypedActionResult.pass(stack);
        }
        if (powerEnchantLevel <= 0){
            return TypedActionResult.pass(stack);
        }
        SlimeBallEntity slimeBallEntity = new SlimeBallEntity(world, player, stack,powerEnchantLevel);
        slimeBallEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);
        world.playSound(null,player.getX(), player.getY(), player.getZ(), ModSoundEvents.ENTITY_SLIMEBALL_THROW, SoundCategory.AMBIENT,1.0F,1.0F);
        slimeBallEntity.setItemStack(stack);
        slimeBallEntity.setOwner(player);
        slimeBallEntity.setItem(stack);
        world.spawnEntity(slimeBallEntity);
        if (!player.getAbilities().creativeMode) {
            ItemStack newItemStack = stack.copy();
            if (stack.getCount()-1>0){
                newItemStack.setCount(stack.getCount()-1);
                player.setStackInHand(hand,newItemStack);
            }else {
                player.setStackInHand(hand,ItemStack.EMPTY);
            }
        }
        return TypedActionResult.success(stack);
    }
}
