package com.random_enchant.entity;

import com.random_enchant.RandomEnchant;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static void registerModEntities(){
        RandomEnchant.LOGGER.info("Register Mod Entities");
        SlimeBallEntity.TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(RandomEnchant.MOD_ID, "thrown_slime_ball_entity"),
                FabricEntityTypeBuilder.<SlimeBallEntity>create(SpawnGroup.MISC, SlimeBallEntity::new)
                        .trackRangeBlocks(64).trackedUpdateRate(10)
                        .build()
        );
//        FrozenArrowEntity.TYPE = Registry.register(
//                Registries.ENTITY_TYPE,
//                Identifier.of(RandomEnchant.MOD_ID, "frozen_arrow"),
//                FabricEntityTypeBuilder.<FrozenArrowEntity>create(SpawnGroup.MISC, FrozenArrowEntity::new)
//                        .trackRangeBlocks(64).trackedUpdateRate(10)
//                        .build()
//        );
    }
}
