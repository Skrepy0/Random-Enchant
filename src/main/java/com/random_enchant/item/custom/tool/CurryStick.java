package com.random_enchant.item.custom.tool;

import com.random_enchant.RandomEnchant;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.Random;

public class CurryStick extends Item {
    private static final int MAX_USE_TIME = 60;
    public static final FoodComponent CURRY_STICK = new FoodComponent.Builder().build();

    public CurryStick(Settings settings) {
        super(settings.maxDamage(250).rarity(Rarity.RARE).maxCount(1)
                .attributeModifiers(createAttributeModifiers())
                .food(CURRY_STICK));
    }

    private static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()

                // 剑的基础属性 - 攻击伤害
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                ToolMaterials.IRON.getAttackDamage() + 4 + 1.14,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                // 剑的基础属性 - 攻击速度
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2.4F,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        new EntityAttributeModifier(
                                Identifier.of(RandomEnchant.MOD_ID, "curry_stick_speed_reduce"),
                                -0.114514,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ),
                        AttributeModifierSlot.HAND
                )
                .build();
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.clearStatusEffects();
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 2));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 2));
            if (user instanceof PlayerEntity player) {
                HungerManager hungerManager = player.getHungerManager();
                hungerManager.setFoodLevel(Math.min(hungerManager.getPrevFoodLevel() + 6, 20)); // 设置饱食度为20（最大为20）
                hungerManager.setSaturationLevel(Math.min(hungerManager.getSaturationLevel() + 8f, 20.0f)); // 设置饱和度为20.0f
            }
        }
        if (user != null) {
            int infinityLevel = getEnchantmentLevel(stack, world, Enchantments.INFINITY);
            if (infinityLevel == 0) {
                int unbreakingLevel = getEnchantmentLevel(stack, world, Enchantments.UNBREAKING);
                stack.damage(unbreakingLevel, user, LivingEntity.getSlotForHand(user.getActiveHand()));
            }

            return stack;
        } else {
            return null;
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE_TIME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    private static int getItemDamage(int unbreakingLevel) {
        Random random = new Random();
        int rand = random.nextInt(10);
        if (unbreakingLevel == 1) {
            if (rand <= 6) {
                return 25;
            }
            return 50;
        } else if (unbreakingLevel == 2) {
            if (rand <= 4) {
                return 25;
            }
            return 50;
        } else if (unbreakingLevel > 2) {
            if (rand <= 7) {
                return 20;
            }
            return 50;
        }
        return 50;
    }

    private static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        return enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
}