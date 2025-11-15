package com.random_enchant.render.entity;

import com.random_enchant.entity.SlimeBallEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ThrowSlimeBallRender extends EntityRenderer<SlimeBallEntity> {
    private final ItemRenderer itemRenderer;
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/item/slime_ball.png");

    public ThrowSlimeBallRender(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    public void render(SlimeBallEntity slimeBallEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.multiply(this.dispatcher.getRotation());
        this.itemRenderer
                .renderItem(
                        slimeBallEntity.getStack(),
                        ModelTransformationMode.GROUND,
                        i,
                        OverlayTexture.DEFAULT_UV,
                        matrixStack,
                        vertexConsumerProvider,
                        slimeBallEntity.getWorld(),
                        slimeBallEntity.getId()
                );
        matrixStack.pop();
        super.render(slimeBallEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }


    @Override
    public Identifier getTexture(SlimeBallEntity slimeBallEntity) {
        return TEXTURE;
    }
}
