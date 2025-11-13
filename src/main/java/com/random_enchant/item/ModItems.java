package com.random_enchant.item;

import com.random_enchant.RandomEnchant;
import com.random_enchant.item.custom.administrator.FireItem;
import com.random_enchant.item.custom.administrator.WaterItem;
import com.random_enchant.item.custom.tool.CurryStick;
import com.random_enchant.item.custom.tool.PearlSpear;
import com.random_enchant.item.custom.tool.PearlStick;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item WATER_ITEM = registerItem("water", new WaterItem(new Item.Settings()));
    public static final Item FIRE_ITEM = registerItem("fire", new FireItem(new Item.Settings()));
    public static final Item PEARL_STICK = registerItem("pearl_stick", new PearlStick(new Item.Settings()));
    public static final Item PEARL_SPEAR = registerItem("pearl_spear", new PearlSpear(new Item.Settings()));
    public static final Item CURRY_STICK = registerItem("curry_stick",new CurryStick(new Item.Settings()));
    public static Item registerItem(String id, Item item) {
        //return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(Skrepy.MOD_ID,id)),item);
        return Registry.register(Registries.ITEM, Identifier.of(RandomEnchant.MOD_ID, id), item);
    }

    public static void addItemToIG(FabricItemGroupEntries fabricItemGroupEntries) {
        fabricItemGroupEntries.add(Items.ENCHANTED_BOOK);

    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemToIG);
        RandomEnchant.LOGGER.info("Register Mod Items");
    }
}
