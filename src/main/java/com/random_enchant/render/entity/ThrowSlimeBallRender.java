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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@Environment(EnvType.CLIENT)
public class ThrowSlimeBallRender extends EntityRenderer<SlimeBallEntity> {
    private final ItemRenderer itemRenderer;
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/item/slime_ball.png");

    public ThrowSlimeBallRender(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    public void render(SlimeBallEntity slimeBallEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        // 检查实体是否在方块内部
        if (isOccludedBySolidBlock(slimeBallEntity)) {
            // 如果在方块内部，则不渲染
            return;
        }

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

    /**
     * 检查实体是否在固体方块内部
     */
    private boolean isInsideSolidBlock(SlimeBallEntity entity) {
        if (entity.getWorld() == null) return false;

        // 获取实体的边界框
        Box entityBox = entity.getBoundingBox();

        // 检查边界框内的所有方块
        BlockPos minPos = new BlockPos(
                (int)Math.floor(entityBox.minX),
                (int)Math.floor(entityBox.minY),
                (int)Math.floor(entityBox.minZ)
        );
        BlockPos maxPos = new BlockPos(
                (int)Math.floor(entityBox.maxX),
                (int)Math.floor(entityBox.maxY),
                (int)Math.floor(entityBox.maxZ)
        );

        // 遍历实体所在的所有方块位置
        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            if (entity.getWorld().getBlockState(pos).isSolidBlock(entity.getWorld(), pos)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 替代方法：使用光线追踪检查实体是否被方块遮挡
     */
    private boolean isOccludedBySolidBlock(SlimeBallEntity entity) {
        if (entity.getWorld() == null || this.dispatcher.camera == null) return false;

        // 获取相机位置
        Vec3d cameraPos = this.dispatcher.camera.getPos();
        // 获取实体位置
        Vec3d entityPos = entity.getPos();

        // 从相机到实体进行光线追踪
        BlockHitResult hitResult = entity.getWorld().raycast(new RaycastContext(
                cameraPos,
                entityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));

        // 如果光线被方块阻挡，并且该方块不是实体所在的方块，则认为是遮挡
        return hitResult.getType() == HitResult.Type.BLOCK &&
                !hitResult.getBlockPos().equals(new BlockPos((int)entityPos.x, (int)entityPos.y, (int)entityPos.z));
    }

    /**
     * 更简单的方法：只检查实体中心点是否在固体方块内
     */
    private boolean isCenterInsideSolidBlock(SlimeBallEntity entity) {
        if (entity.getWorld() == null) return false;

        BlockPos entityPos = entity.getBlockPos();
        return entity.getWorld().getBlockState(entityPos).isSolidBlock(entity.getWorld(), entityPos);
    }

    @Override
    public Identifier getTexture(SlimeBallEntity slimeBallEntity) {
        return TEXTURE;
    }
}