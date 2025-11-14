package com.random_enchant;

import com.random_enchant.datagen.*;
import com.random_enchant.enchantment.ModEnchantments;
import com.random_enchant.entity.damage.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class RandomEnchantDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModZHCNLanProvider::new);
		pack.addProvider(ModENUSLanProvider::new);
		pack.addProvider(ModModelsProvider::new);
		pack.addProvider(ModRecipesProvider::new);
		pack.addProvider(ModItemTagProvider::new);
		pack.addProvider(ModDynamicRegistryProvider::new);
		pack.addProvider(ModEnchantmentTagProvider::new);


	}
	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.ENCHANTMENT, ModEnchantments::bootstrap);
	}
}
