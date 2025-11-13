package com.random_enchant.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class FuryOfFly implements EnchantmentEntityEffect{
    public static final MapCodec<FuryOfFly> CODEC = MapCodec.unit(FuryOfFly::new);

    // 如果需要，可以添加构造函数参数
    public FuryOfFly() {
        // 空构造函数
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {

    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
