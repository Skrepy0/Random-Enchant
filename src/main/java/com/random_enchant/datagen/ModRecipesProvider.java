package com.random_enchant.datagen;

import com.random_enchant.RandomEnchant;
import com.random_enchant.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends FabricRecipeProvider {
    public ModRecipesProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        //有序合成
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.CHAINMAIL_HELMET, 1)
                .pattern("###")
                .pattern("# #")
                .input('#', ModItems.FIRE_ITEM)
                .criterion("has_item", RecipeProvider.conditionsFromItem(ModItems.FIRE_ITEM))
                .offerTo(exporter, Identifier.of(RandomEnchant.MOD_ID, "fire_chainmail_helmet"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.CHAINMAIL_CHESTPLATE, 1)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.FIRE_ITEM)
                .criterion("has_item", RecipeProvider.conditionsFromItem(ModItems.FIRE_ITEM))
                .offerTo(exporter, Identifier.of(RandomEnchant.MOD_ID, "fire_chainmail_chestplate"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.CHAINMAIL_LEGGINGS, 1)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModItems.FIRE_ITEM)
                .criterion("has_item", RecipeProvider.conditionsFromItem(ModItems.FIRE_ITEM))
                .offerTo(exporter, Identifier.of(RandomEnchant.MOD_ID, "fire_chainmail_leggings"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.CHAINMAIL_BOOTS, 1)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModItems.FIRE_ITEM)
                .criterion("has_item", RecipeProvider.conditionsFromItem(ModItems.FIRE_ITEM))
                .offerTo(exporter, Identifier.of(RandomEnchant.MOD_ID, "fire_chainmail_boots"));
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.ENDER_PEARL, 1)
                .pattern(" # ")
                .pattern("# #")
                .input('#', ModItems.FIRE_ITEM)
                .criterion("has_item", RecipeProvider.conditionsFromItem(ModItems.FIRE_ITEM))
                .offerTo(exporter, Identifier.of(RandomEnchant.MOD_ID, "m_pearl"));
    }
}
