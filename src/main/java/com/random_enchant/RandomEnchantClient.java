package com.random_enchant;

import com.random_enchant.entity.SlimeBallEntity;
import com.random_enchant.render.entity.ThrowSlimeBallRender;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;


public class RandomEnchantClient implements ClientModInitializer {
    @Override
    public void onInitializeClient (){
        RandomEnchant.LOGGER.info("register render");
        EntityRendererRegistry.register(SlimeBallEntity.TYPE, ThrowSlimeBallRender::new);
    }
}
