package com.random_enchant.enchantment;

import com.mojang.serialization.MapCodec;
import com.random_enchant.RandomEnchant;
import com.random_enchant.enchantment.custom.FuryOfFly;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantmentEffect {
    private static MapCodec<? extends EnchantmentEntityEffect> register(String name, MapCodec<? extends EnchantmentEntityEffect> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(RandomEnchant.MOD_ID,name),codec);
    }

    public static void registerModEnchantmentEffects() {
        RandomEnchant.LOGGER.info("Register Mod Enchantments");
        register("fury_of_fly", FuryOfFly.CODEC);
    }
}
