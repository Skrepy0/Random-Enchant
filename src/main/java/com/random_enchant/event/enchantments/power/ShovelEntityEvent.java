package com.random_enchant.event.enchantments.power;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ShovelEntityEvent implements UseEntityCallback{
    public void registerEvent(){
        UseEntityCallback.EVENT.register(this);
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        int powerLevel = getEnchantmentLevel(stack,world, Enchantments.POWER);
        if (world.isClient()){
            return ActionResult.PASS;
        }
        if (!(stack.getItem() instanceof ShovelItem)){
            return ActionResult.PASS;
        }
        if (!(powerLevel>0)){
            return ActionResult.PASS;
        }
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())){
            return ActionResult.PASS;
        }
        launchEntity(entity,powerLevel,player);
        setShovelDamage(stack,player,getEnchantmentLevel(stack,world,Enchantments.UNBREAKING));
        player.getItemCooldownManager().set(stack.getItem(),10);
        return ActionResult.SUCCESS;
    }
    private static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {

        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        int enchantmentLevel = enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
        return enchantmentLevel;
    }
    private static void setShovelDamage(ItemStack stack,PlayerEntity player,int UnbreakingLevel){
        if (!player.getAbilities().creativeMode) {
            // 减少耐久度
            if (stack.getDamage() + damage(UnbreakingLevel) >= stack.getMaxDamage()) {
                stack.setCount(0);
            } else {
                stack.setDamage(stack.getDamage() + damage(UnbreakingLevel));
            }
        }
    }
    private static void launchEntity(Entity entity, int power, PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        Vec3d targetPos = entity.getPos();
        Vec3d targetVec = new Vec3d(
                targetPos.getX() + 0.5,
                targetPos.getY() + 1.0,
                targetPos.getZ() + 0.5
        );

        Vec3d direction = targetVec.subtract(playerPos).normalize();

        // 稍微向上调整方向
        direction = new Vec3d(
                direction.x,
                direction.y + 0.2,
                direction.z
        ).normalize();

        // 根据附魔等级计算速度
        Vec3d velocity = direction.multiply(power * 0.6);
        entity.setVelocity(velocity);
    }
    public static int damage(int UnbreakingLevel){
        int damage_num=0;
        if (UnbreakingLevel==0){
            damage_num = 2;
        }else if (UnbreakingLevel == 1){
            damage_num = 1;
        }else if (UnbreakingLevel == 2){
            Random random = new Random();
            int r = random.nextInt(9);
            if (r<=4){
                damage_num = 1;
            }else {
                damage_num = 0;
            }

        }else if (UnbreakingLevel>=3){
            Random random = new Random();
            int r = random.nextInt(9);
            if (r<=2){
                damage_num = 1;
            }else {
                damage_num = 0;
            }
        }
        return damage_num;
    }

}
