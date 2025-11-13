package com.random_enchant.command;

import com.random_enchant.RandomEnchant;

public class ModCommands {
    public static void registerModCommands(){
        RandomEnchantCommand.registerCommand();
        RandomEnchant.LOGGER.info("Register Mod Commands");
    }
}
