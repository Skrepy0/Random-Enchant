package com.random_enchant.entity.damage;


import com.random_enchant.RandomEnchant;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public interface ModDamageTypes {
    RegistryKey<DamageType> PEARL_SPEAR = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(RandomEnchant.MOD_ID,"pearl_spear"));
    static void bootstrap(Registerable<DamageType> damageTypeRegisterable) {
        damageTypeRegisterable.register(PEARL_SPEAR, new DamageType("pearl_spear", 0.1F));

    }
}
