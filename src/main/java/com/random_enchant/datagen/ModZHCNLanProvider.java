package com.random_enchant.datagen;

import com.random_enchant.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModZHCNLanProvider extends FabricLanguageProvider {
    public ModZHCNLanProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
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
        for (int i = 11; i < 256; i++) {
            translationBuilder.add("enchantment.level." + i, toRoman(i));
        }
        translationBuilder.add(ModItems.WATER_ITEM, "水");
        translationBuilder.add(ModItems.FIRE_ITEM, "火");
        translationBuilder.add(ModItems.PEARL_STICK, "珍珠权杖");
        translationBuilder.add(ModItems.PEARL_SPEAR, "Pearl_M");
        translationBuilder.add(ModItems.CURRY_STICK, "咖喱棒");
        translationBuilder.add("command.random-enchant.randomEnchant.enable", "§a已启用随机附魔");
        translationBuilder.add("command.random-enchant.randomEnchant.disable", "§6已禁用随机附魔");
        translationBuilder.add("command.random-enchant.showFireChargeParticle.enable", "§a火焰弹发射粒子效果(§5[力量]§r>=V)§a已启用");
        translationBuilder.add("command.random-enchant.showFireChargeParticle.disable", "§6火焰弹发射粒子效果(§5[力量]§r>=V)§6已禁用");
        translationBuilder.add("itemgroup.random_enchant_group", "§5随机附魔");

        translationBuilder.add("enchantment.random-enchant.fury_of_fly","§aFly之怒");
        translationBuilder.add("entity.minecraft.bee.spawn_name","§aFly");


        translationBuilder.add("item.tooltip.random-enchant.for_shift_tooltip", "按下§6[SHIFT]§r查看详细信息");
        translationBuilder.add("item.tooltip.random-enchant.pearl_spear.detail_description_1", "§b左键攻击实体并瞬移，右键瞬移§r");
        translationBuilder.add("item.tooltip.random-enchant.pearl_spear.detail_description_2", "§b瞬移路径上的实体会受到8点基础伤害(实际伤害与§a[横扫之刃]§b等级有关)§r");
        translationBuilder.add("item.tooltip.random-enchant.pearl_spear.detail_description_3", "§b左键攻击时与目标的相对速度越大，伤害越高§r");
    }
}
