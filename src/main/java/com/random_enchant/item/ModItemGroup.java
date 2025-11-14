package com.random_enchant.item;

import com.random_enchant.RandomEnchant;
import com.random_enchant.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;

public class ModItemGroup {

    // 定义要显示的特定附魔键列表
    private static final List<RegistryKey<Enchantment>> SPECIFIC_ENCHANTMENT_KEYS = List.of(
            Enchantments.INFINITY,
            Enchantments.PROTECTION,
            Enchantments.BLAST_PROTECTION,
            Enchantments.POWER,
            Enchantments.CHANNELING,
            Enchantments.UNBREAKING,
            ModEnchantments.FURY_OF_FLY
            // 在这里添加更多你想要的附魔
    );

    private static void addSpecificMaxLevelEnchantedBooks(
            ItemGroup.Entries entries, RegistryEntryLookup<Enchantment> registryLookup, ItemGroup.StackVisibility stackVisibility
    ) {
        for (RegistryKey<Enchantment> key : SPECIFIC_ENCHANTMENT_KEYS) {
            registryLookup.getOptional(key).ifPresent(enchantmentEntry -> {
                Enchantment enchantment = enchantmentEntry.value();
                ItemStack enchantedBook = EnchantedBookItem.forEnchantment(
                        new EnchantmentLevelEntry(enchantmentEntry, enchantment.getMaxLevel())
                );
                entries.add(enchantedBook, stackVisibility);
            });
        }
    }

    private static void addSpecificAllLevelEnchantedBooks(
            ItemGroup.Entries entries, RegistryEntryLookup<Enchantment> registryLookup, ItemGroup.StackVisibility stackVisibility
    ) {
        for (RegistryKey<Enchantment> key : SPECIFIC_ENCHANTMENT_KEYS) {
            registryLookup.getOptional(key).ifPresent(enchantmentEntry -> {
                Enchantment enchantment = enchantmentEntry.value();
                for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
                    ItemStack enchantedBook = EnchantedBookItem.forEnchantment(
                            new EnchantmentLevelEntry(enchantmentEntry, level)
                    );
                    entries.add(enchantedBook, stackVisibility);
                }
            });
        }
    }

    public static final ItemGroup RANDOM_ENCHANT_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(RandomEnchant.MOD_ID, "random_enchant_group"),
            ItemGroup.create(ItemGroup.Row.TOP, -1)
                    .displayName(Text.translatable("itemgroup.random_enchant_group"))
                    .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
                    .entries(((displayContext, entries) -> {

                        // 使用RegistryEntryLookup
                        displayContext.lookup().getOptionalWrapper(RegistryKeys.ENCHANTMENT).ifPresent(registryWrapper -> {
                            addSpecificMaxLevelEnchantedBooks(entries, registryWrapper, ItemGroup.StackVisibility.PARENT_TAB_ONLY);
                            addSpecificAllLevelEnchantedBooks(entries, registryWrapper, ItemGroup.StackVisibility.SEARCH_TAB_ONLY);
                        });
                        entries.add(Blocks.ANVIL);
                        entries.add(Blocks.WHITE_WOOL);
                        entries.add(Items.FIRE_CHARGE);
                        entries.add(Items.WIND_CHARGE);
                        entries.add(Items.ENDER_EYE);
                        entries.add(Items.ENDER_PEARL);
                        entries.add(Items.EGG);
                        entries.add(Items.SNOWBALL);
                        entries.add(Items.ENCHANTED_GOLDEN_APPLE);
                        entries.add(Items.TOTEM_OF_UNDYING);
                        entries.add(Items.DIAMOND_SHOVEL);
                        entries.add(Items.FIREWORK_ROCKET);
                        entries.add(ModItems.PEARL_STICK);
                        entries.add(ModItems.PEARL_SPEAR);
                        entries.add(ModItems.CURRY_STICK);
                        entries.add(ModItems.WATER_ITEM);
                        entries.add(ModItems.FIRE_ITEM);
                    }))

                    .build());

    public static void registerModGroups() {
        RandomEnchant.LOGGER.info("Registering Item Groups");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            entries.addAfter(Items.DEBUG_STICK,ModItems.WATER_ITEM);
            entries.addAfter(ModItems.WATER_ITEM,ModItems.FIRE_ITEM);
        });
    }
}