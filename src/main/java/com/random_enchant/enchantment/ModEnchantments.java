package com.random_enchant.enchantment;

import com.random_enchant.RandomEnchant;
import com.random_enchant.item.ModItemTags;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> FURY_OF_FLY = of("fury_of_fly");
    public static void bootstrap(Registerable<Enchantment> registry) {
        RegistryEntryLookup<Enchantment> registryEntryLookup2 = registry.getRegistryLookup(RegistryKeys.ENCHANTMENT);
        RegistryEntryLookup<Item> registryEntryLookup3 = registry.getRegistryLookup(RegistryKeys.ITEM);
        register(registry,FURY_OF_FLY,Enchantment.builder(Enchantment.definition(
                registryEntryLookup3.getOrThrow(ModItemTags.FURY_OF_FLY_AVAILABLE),
                3,5,
                Enchantment.leveledCost(10,5),
                Enchantment.leveledCost(27,10),5,
                AttributeModifierSlot.MAINHAND
        )));
    }
    public static void register(Registerable<Enchantment> registry, RegistryKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.getValue()));
    }
    public static RegistryKey<Enchantment> of(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(RandomEnchant.MOD_ID,id));
    }
}
