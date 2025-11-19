package com.random_enchant.item;

import com.random_enchant.RandomEnchant;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModItemTags {
    public static final TagKey<Item> FURY_OF_FLY_AVAILABLE = of("fury_of_fly_available");
    public static final TagKey<Item> QUICK_CHARGE_AVAILABLE = of("quick_charge_available");
    public static final TagKey<Item> WIND_BURST_AVAILABLE = of("wind_burst_available");
    public static final TagKey<Item> CHANNELING = of("channeling_available");
    public static final TagKey<Item> FROST_WALKER = of("frost_walker_available");
    public static TagKey<Item> of(String id){
        return TagKey.of(RegistryKeys.ITEM, Identifier.of(RandomEnchant.MOD_ID,id));
    }
    public static void registerModItemTags(){
        RandomEnchant.LOGGER.info("Register Mod Item Tags");
    }

}
