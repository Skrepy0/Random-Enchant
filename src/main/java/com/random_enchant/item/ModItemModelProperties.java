package com.random_enchant.item;

import com.random_enchant.RandomEnchant;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ModItemModelProperties {
    public static void registerModelProperties() {
        ModelPredicateProviderRegistry.register(
                Identifier.of(RandomEnchant.MOD_ID, "cooldown"),
                (stack, world, entity, seed) -> {
                    if (entity instanceof PlayerEntity player) {
                        // 检查物品是否在冷却中
                        return player.getItemCooldownManager().isCoolingDown(stack.getItem()) ? 1.0F : 0.0F;
                    }
                    return 0.0F;
                }
        );
    }
}
