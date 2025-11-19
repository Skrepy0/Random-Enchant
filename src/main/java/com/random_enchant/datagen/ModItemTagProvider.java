package com.random_enchant.datagen;

import com.random_enchant.item.ModItemTags;
import com.random_enchant.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        getOrCreateTagBuilder(ModItemTags.FURY_OF_FLY_AVAILABLE)
                .add(ModItems.PEARL_SPEAR);
        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(ModItems.PEARL_SPEAR);
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ModItems.PEARL_SPEAR)
                .add(ModItems.PEARL_STICK)
                .add(ModItems.CURRY_STICK);
        getOrCreateTagBuilder(ModItemTags.WIND_BURST_AVAILABLE)
                .forceAddTag(ItemTags.MACE_ENCHANTABLE)
                .add(ModItems.PEARL_SPEAR);
        getOrCreateTagBuilder(ModItemTags.QUICK_CHARGE_AVAILABLE)
                .forceAddTag(ItemTags.CROSSBOW_ENCHANTABLE)
                .add(ModItems.PEARL_SPEAR);
        getOrCreateTagBuilder(ModItemTags.CHANNELING)
                .add(ModItems.PEARL_SPEAR)
                .forceAddTag(ItemTags.TRIDENT_ENCHANTABLE);
        getOrCreateTagBuilder(ItemTags.SWORD_ENCHANTABLE)
                .add(ModItems.PEARL_SPEAR);
        getOrCreateTagBuilder(ItemTags.WEAPON_ENCHANTABLE)
                .add(ModItems.PEARL_SPEAR);
        getOrCreateTagBuilder(ModItemTags.FROST_WALKER)
                .add(Items.ARROW)
                .forceAddTag(ItemTags.FOOT_ARMOR_ENCHANTABLE);

    }
}
