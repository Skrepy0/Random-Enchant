package com.random_enchant.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.random_enchant.RandomEnchant;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.ArrayList;

public class RandomEnchantCommand {
    public static boolean isRandomEnchant = true;
    public static boolean showFireChargeParticular = true;
    static Logger LOGGER = RandomEnchant.LOGGER;

    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("random_enchant")
                    .requires(source -> source.hasPermissionLevel(2)) // 主命令需要操作员权限
                    .then(CommandManager.literal("isRandomEnchant")
                            .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(context -> {
                                        boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                        isRandomEnchant = enabled;

                                        // 使用正确的翻译键方式
                                        Text message = enabled ?
                                                Text.translatable("command.random-enchant.randomEnchant.enable") :
                                                Text.translatable("command.random-enchant.randomEnchant.disable");

                                        // 使用LOGGER而不是System.out
                                        LOGGER.info(message.getString());

                                        // 反馈给玩家
                                        context.getSource().sendFeedback(() -> message, false);

                                        return 1;
                                    })
                            )
                    )
                    .then(CommandManager.literal("description")
                            .executes(context -> {
                                Text discription_1 = Text.of("  本mod(Random-Enchant)由§bSkrepy2233§r制作，以下是几点说明");
                                Text discription_2 = Text.of("1.可以使用/§arandom_enchant isRandomEnchant §b<true/false>§r 进行配置，默认是开启");
                                Text discription_3 = Text.of("2.§aisRandomEnchant§r开启后,玩家击打有生命实体后会对玩家主手物品进行随机附魔（等级也是随机）");
                                Text discription_4 = Text.of("3.已经启用本mod自带的材质包（修复§b附魔等级的罗马数字显示§r）");
                                Text discription_5 = Text.of("4.最重要的一点，附魔随机的范围是§d原版附魔§r（技术原因）");
                                Text discription_6 = Text.of("5.本mod对原版附魔添加了一些效果，如附魔有§a[无限]§r的食物使用后数量不会减少，方块、不死图腾亦同");
                                Text discription_7 = Text.of("最后，添加的附魔与对应的物品：\n §a[无限]§r投掷类物品，如坤蛋、末影珍珠、药水（饮用除外）、食物（蛋糕除外）等;各种方块、不死图腾\n §a[力量]§r 火焰弹、铲子、粘液球");
                                Text discription = Text.of("§c最后§r:按§d[T]§r查看全部\n§b张舒程§r好帅~~");
                                ArrayList<Text> message = new ArrayList<>();
                                message.add(discription_1);
                                message.add(discription_2);
                                message.add(discription_3);
                                message.add(discription_4);
                                message.add(discription_5);
                                message.add(discription_6);
                                message.add(discription_7);
                                message.add(discription);
                                for (int i = 0; i < 8; i++) {
                                    LOGGER.info(message.get(i).getString());
                                    // 反馈给玩家
                                    int finalI = i;
                                    context.getSource().sendFeedback(() -> message.get(finalI), true);
                                }
                                return 1;
                            })
                    )
                    .then(CommandManager.literal("showFireChargeParticle")
                            .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(context -> {
                                        boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                        showFireChargeParticular = enabled;

                                        // 使用正确的翻译键方式
                                        Text message = enabled ?
                                                Text.translatable("command.random-enchant.showFireChargeParticle.enable") :
                                                Text.translatable("command.random-enchant.showFireChargeParticle.disable");

                                        // 使用LOGGER而不是System.out
                                        LOGGER.info(message.getString());

                                        // 反馈给玩家
                                        context.getSource().sendFeedback(() -> message, false);

                                        return 1;
                                    })
                            )
                    )
            );
        });
    }
}
