package com.random_enchant;

import com.random_enchant.command.ModCommands;
import com.random_enchant.enchantment.ModEnchantmentEffect;
import com.random_enchant.enchantment.ModEnchantmentTags;
import com.random_enchant.entity.ModEntities;
import com.random_enchant.event.AttackMobEvent;
import com.random_enchant.event.ModEvents;
import com.random_enchant.event.dispenser.SlimeBallDispenserBehavior;
import com.random_enchant.item.ModItemGroup;
import com.random_enchant.item.ModItemModelProperties;
import com.random_enchant.item.ModItemTags;
import com.random_enchant.item.ModItems;
import com.random_enchant.sound.ModSoundEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomEnchant implements ModInitializer {
    public static final String MOD_ID = "random-enchant";


    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ModEvents.registerEvents();
        LOGGER.info("Initialize Mod Register");
        ModCommands.registerModCommands();

		ModEntities.registerModEntities();

        ModItems.registerModItems();
        ModItemTags.registerModItemTags();
        ModEnchantmentEffect.registerModEnchantmentEffects();
        ModEnchantmentTags.registerModItemTags();
        ModItemGroup.registerModGroups();
        AttackMobEvent.registerAttackMobEvents();
        ModSoundEvents.registerModSoundEvents();

        ModItemModelProperties.registerModelProperties();

        DispenserBlock.registerBehavior(Items.SLIME_BALL, new SlimeBallDispenserBehavior());
    }
}