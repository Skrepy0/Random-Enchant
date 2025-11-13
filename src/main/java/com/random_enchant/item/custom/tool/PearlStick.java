package com.random_enchant.item.custom.tool;

import com.random_enchant.item.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PearlStick extends Item {
    public PearlStick(Settings settings) {
        super(settings.maxDamage(128).rarity(Rarity.RARE).maxCount(1).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.pass(stack);
        if (user.getItemCooldownManager().isCoolingDown(this))return TypedActionResult.pass(stack);
        throwEnderPearl(user, world, stack, hand);
        user.getItemCooldownManager().set(ModItems.PEARL_STICK,30);
        return TypedActionResult.success(stack);
    }

    private void throwEnderPearl(PlayerEntity player, World world, ItemStack stack, Hand hand) {
        // 优化1：使用更简洁的方式初始化速度向量
        EnderPearlEntity enderPearlEntity = new EnderPearlEntity(world, player);

        // 优化2：提取常量，提高代码可读性
        final float SPAWN_DISTANCE = 0.5F;
        final float DEGREES_TO_RADIANS = 0.017453292F;

        // 优化3：使用更清晰的变量名
        float yawRadians = player.getYaw() * DEGREES_TO_RADIANS;
        double offsetX = -Math.sin(yawRadians) * SPAWN_DISTANCE;
        double offsetZ = Math.cos(yawRadians) * SPAWN_DISTANCE;

        // 优化4：简化位置计算
        double spawnX = player.getX() + offsetX;
        double spawnY = player.getEyeY();
        double spawnZ = player.getZ() + offsetZ;

        // 设置位置
        enderPearlEntity.setPosition(spawnX, spawnY, spawnZ);

        // 优化5：提取速度相关常量
        final float SPEED_MULTIPLIER = 2.5F;// 一坤
        final float DIVERGENCE = 0.5F;
        enderPearlEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, SPEED_MULTIPLIER, DIVERGENCE);

        // 播放音效和设置冷却
        world.playSound(null, BlockPos.ofFloored(player.getPos()), SoundEvents.ENTITY_ENDER_PEARL_THROW,
                SoundCategory.AMBIENT, 0.8F, 1.0F);
        player.getItemCooldownManager().set(Items.FIRE_CHARGE, 30);
        world.spawnEntity(enderPearlEntity);



        // 优化7：简化物品消耗逻辑
        if (!player.getAbilities().creativeMode) {
            stack.damage(1, player, LivingEntity.getSlotForHand(hand));
        }
    }
}
