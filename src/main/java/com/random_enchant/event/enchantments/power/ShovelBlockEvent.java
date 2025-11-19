package com.random_enchant.event.enchantments.power;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;

import static com.random_enchant.enchantment.ModEnchantHelper.getEnchantmentLevel;

public class ShovelBlockEvent implements UseBlockCallback {
    public int UnbreakingLevel;
    public void registerEvent() {
        UseBlockCallback.EVENT.register(this);
    }



    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        // 仅在服务端处理
        if (world.isClient()) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);
        int powerLevel = getEnchantmentLevel(player, hand, world, Enchantments.POWER);
        UnbreakingLevel = getEnchantmentLevel(player,hand,world,Enchantments.UNBREAKING);

        // 检查是否手持铲子且有力量附魔
        if (!(stack.getItem() instanceof ShovelItem) || powerLevel <= 0) {
            return ActionResult.PASS;
        }

        BlockPos targetPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(targetPos);

        // 检查方块是否可挖掘
        if (blockState.getHardness(world, targetPos) < 0) {
            return ActionResult.PASS;
        }
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())){
            return ActionResult.PASS;
        }

        // 生成下落的方块
        generateFallingBlock(targetPos, blockState, world, powerLevel, player);

        // 移除原方块
        world.removeBlock(targetPos, false);
        setShovelDamage(stack,player);
        player.getItemCooldownManager().set(stack.getItem(),5);

        return ActionResult.SUCCESS;
    }

    private void generateFallingBlock(BlockPos targetPos, BlockState blockState, World world, int power, PlayerEntity player) {
        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(targetPos);
            FallingBlockEntity fallingBlockEntity = null;

            try {
                // 使用反射获取私有构造函数
                Constructor<FallingBlockEntity> constructor = FallingBlockEntity.class.getDeclaredConstructor(
                        World.class, double.class, double.class, double.class, BlockState.class
                );
                constructor.setAccessible(true); // 允许访问私有构造函数

                // 创建实体实例
                fallingBlockEntity = constructor.newInstance(
                        world,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 1.0,
                        targetPos.getZ() + 0.5,
                        blockState
                );
            } catch (Exception e) {
                e.printStackTrace();
                return; // 构造失败则退出
            }

            if (fallingBlockEntity != null) {
                fallingBlockEntity.timeFalling = 1;
                fallingBlockEntity.setNoGravity(false);
                fallingBlockEntity.canHit();
                fallingBlockEntity.setHurtEntities(50.0F, power * 2);


                // 发射方块
                launchBlock(targetPos, power, player, fallingBlockEntity);

                // 处理方块实体数据
                if (blockEntity != null) {
                    try {
                        Field blockEntityDataField = FallingBlockEntity.class.getDeclaredField("blockEntityData");
                        blockEntityDataField.setAccessible(true);
                        NbtCompound blockEntityData = blockEntity.createNbt(world.getRegistryManager());
                        blockEntityDataField.set(fallingBlockEntity, blockEntityData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 生成实体
                world.spawnEntity(fallingBlockEntity);

            }
        }
    }
    private void setShovelDamage(ItemStack stack,PlayerEntity player){
        if (!player.getAbilities().creativeMode) {
            // 减少耐久度
            if (stack.getDamage() + damage() >= stack.getMaxDamage()) {
                stack.setCount(0);
            } else {
                stack.setDamage(stack.getDamage() + damage());
            }
        }
    }
    public int damage(){
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

    private static void launchBlock(BlockPos targetPos, int power, PlayerEntity player, FallingBlockEntity fallingBlockEntity) {
        Vec3d playerPos = player.getPos();
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
        fallingBlockEntity.setVelocity(velocity);
    }
}
