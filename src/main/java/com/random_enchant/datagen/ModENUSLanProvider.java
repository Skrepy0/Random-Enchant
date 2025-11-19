package com.random_enchant.datagen;

import com.random_enchant.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModENUSLanProvider extends FabricLanguageProvider{
    public ModENUSLanProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput,"en_us", registryLookup);
    }

    public static String toRoman(int number) {
        if (number < 11 || number > 255) {
            throw new IllegalArgumentException("输入必须在11到255之间");
        }

        // 定义罗马数字的基本组成部分
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        // 分解数字的各个部分
        int thousandPart = number / 1000;
        int hundredPart = (number % 1000) / 100;
        int tenPart = (number % 100) / 10;
        int unitPart = number % 10;

        // 构建罗马数字字符串
        return thousands[thousandPart] +
                hundreds[hundredPart] +
                tens[tenPart] +
                units[unitPart];
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        for (int i = 11 ; i < 256;i++){
            translationBuilder.add("enchantment.level."+i,toRoman(i));
        }
        translationBuilder.add(ModItems.WATER_ITEM,"Water");
        translationBuilder.add(ModItems.FIRE_ITEM,"Fire");
        translationBuilder.add(ModItems.PEARL_STICK,"Pearl Stick");
        translationBuilder.add(ModItems.PEARL_SPEAR,"Pearl_M");
        translationBuilder.add(ModItems.CURRY_STICK, "Curry Stick");
        translationBuilder.add("command.random-enchant.randomEnchant.enable","§aEnabled Random Enchant");
        translationBuilder.add("command.random-enchant.randomEnchant.disable","§6Disabled Random Enchant");
        translationBuilder.add("command.random-enchant.showFireChargeParticle.enable","§aFire Charge launching particle(§5[Power]§r>=V)§a enabled");
        translationBuilder.add("command.random-enchant.showFireChargeParticle.disable","§6Fire Charge launching particle(§5[Power]§r>=V)§6 disabled");
        translationBuilder.add("itemgroup.random_enchant_group","§5Random Enchant");

        translationBuilder.add("enchantment.random-enchant.fury_of_fly","§aFury Of Fly");
        translationBuilder.add("entity.minecraft.bee.spawn_name","§aFly");

        translationBuilder.add("entity.slimeball.throw","SlimeBall flies");

        translationBuilder.add("item.tooltip.random-enchant.for_shift_tooltip","Press §6[SHIFT]§r show detail information");
        translationBuilder.add("item.tooltip.random-enchant.pearl_spear.detail_description_1","§bLeft-click to attack the entity and teleport, right-click to teleport§r");
        translationBuilder.add("item.tooltip.random-enchant.pearl_spear.detail_description_2","§bEntities in the teleportation path take 8 base damage(real damage is related to the level of §a[Sweep Edge]§b)§r");
        translationBuilder.add("item.tooltip.random-enchant.pearl_spear.detail_description_3","§bThe greater the relative speed to the target when left-clicking to attack,the higher the damage§r");
    }
}
