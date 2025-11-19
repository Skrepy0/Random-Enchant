package com.random_enchant.enchantment;

import com.random_enchant.RandomEnchant;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModEnchantmentTags {
    public static final TagKey<Enchantment> FURY_OF_FLY= of("fury_of_fly");


    public static TagKey<Enchantment> of(String id){
        return TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(RandomEnchant.MOD_ID,id));
    }
    public static void registerModItemTags(){
        RandomEnchant.LOGGER.info("Register Mod Enchantment Tags");
    }
}