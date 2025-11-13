package com.random_enchant.entity.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public class ModDamageSources {
    public final Registry<DamageType> registry;

    public ModDamageSources(Registry<DamageType> registry) {
        this.registry = registry;
    }
    public DamageSource pearlSpear(PersistentProjectileEntity source, @Nullable Entity attacker) {
        return this.create(ModDamageTypes.PEARL_SPEAR, source, attacker);
    }
    public final DamageSource create(RegistryKey<DamageType> key) {
        return new DamageSource(this.registry.entryOf(key));
    }

    public final DamageSource create(RegistryKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(this.registry.entryOf(key), attacker);
    }

    public final DamageSource create(RegistryKey<DamageType> key, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(this.registry.entryOf(key), source, attacker);
    }
}
