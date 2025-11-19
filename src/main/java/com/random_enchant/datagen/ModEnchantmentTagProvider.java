package com.random_enchant.datagen;

import com.random_enchant.enchantment.ModEnchantmentTags;
import com.random_enchant.enchantment.ModEnchantments;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.EnchantmentTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentTagProvider extends EnchantmentTagProvider {
    public ModEnchantmentTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        getOrCreateTagBuilder(ModEnchantmentTags.FURY_OF_FLY)
                .add(ModEnchantments.FURY_OF_FLY);
        getOrCreateTagBuilder(EnchantmentTags.TREASURE)
                .add(ModEnchantments.FURY_OF_FLY);
        getOrCreateTagBuilder(EnchantmentTags.TRADEABLE)
                .add(ModEnchantments.FURY_OF_FLY);
        getOrCreateTagBuilder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .add(ModEnchantments.FURY_OF_FLY);

    }
}
