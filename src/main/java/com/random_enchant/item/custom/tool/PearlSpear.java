package com.random_enchant.item.custom.tool;

import com.random_enchant.RandomEnchant;
import com.random_enchant.enchantment.ModEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class PearlSpear extends Item {
    public PearlSpear(Settings settings) {
        super(settings.maxDamage(128).rarity(Rarity.EPIC).maxCount(1).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                .attributeModifiers(createAttributeModifiers()));
    }

    private static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                // 剑的基础属性 - 攻击伤害
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                ToolMaterials.DIAMOND.getAttackDamage() + 5 - 1,
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
                // 速度加成
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        new EntityAttributeModifier(
                                Identifier.of(RandomEnchant.MOD_ID, "pearl_spear_speed_boost"),
                                1.14514,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ),
                        AttributeModifierSlot.HAND
                )
                .build();
    }
    private static int getItemDamage(int unbreakingLevel){
        Random random = new Random();
        int rand = random.nextInt(10);
        if (unbreakingLevel == 1){
            if (rand <= 6){
                return 1;
            }
            return 0;
        } else if (unbreakingLevel == 2) {
            if (rand <= 4){
                return 1;
            }
            return 0;
        }else if (unbreakingLevel > 2) {
            if (rand <= 7){
                return 1;
            }
            return 0;
        }
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.pass(stack);
        if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.pass(stack);
        teleportUser(10, user, world, false);
        if (!user.getAbilities().creativeMode) {
            int unbreakingLevel = getEnchantmentLevel(stack,world,Enchantments.UNBREAKING);
            stack.damage(getItemDamage(unbreakingLevel), user, LivingEntity.getSlotForHand(hand));
        }
        world.playSound(null, BlockPos.ofFloored(user.getPos()), SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.AMBIENT, 0.2F, 1.4F);
        user.getItemCooldownManager().set(this, 200);
        showParticleEffect(world, user);
        return TypedActionResult.success(stack);
    }
    @Override
    public boolean postHit(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
        PlayerEntity user = (PlayerEntity) attacker;
        World world = user.getWorld();
        if (world.isClient()) return false;
        if (user.getItemCooldownManager().isCoolingDown(this)) return false;
        Vec3d playerVelocity = attacker.getVelocity();
        Vec3d entityVelocity = entity.getVelocity();
        Vec3d playerPos = attacker.getPos();
        Vec3d entityPos = entity.getPos();
        Vec3d playerToEntity = entityPos.subtract(playerPos); // 应该是减法，不是add乘-1
        // 计算投影长度
        double playerVeLength = playerVelocity.dotProduct(playerToEntity.normalize());
        double entityVeLength = entityVelocity.dotProduct(playerToEntity.normalize());
        double dV;
        if (attacker.isOnGround()) dV = entityVeLength - playerVeLength > 0 ? entityVeLength - playerVeLength : 0;
        else dV = playerVeLength - entityVeLength > 0 ? playerVeLength - entityVeLength : 0;

//        System.out.println(playerVeLength+" "+entityVeLength);
        float damage = (float) (dV * 10 + 9);
//        System.out.println("damage="+damage);
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
        teleportUser(10, user, world, true);
        int channelingLevel = getEnchantmentLevel(stack,world,Enchantments.CHANNELING);
        if (channelingLevel > 0){
            spawnLightningEntity(channelingLevel,world,entity);
        }
        world.playSound(null, BlockPos.ofFloored(user.getPos()), SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.AMBIENT, 0.2F, 1.0F);
        user.getItemCooldownManager().set(this, 200);
        if (!user.getAbilities().creativeMode) {
            int unbreakingLevel = getEnchantmentLevel(stack,world,Enchantments.UNBREAKING);
            stack.damage(getItemDamage(unbreakingLevel), user, LivingEntity.getSlotForHand(user.getActiveHand()));
        }
        showParticleEffect(world, user);
        entity.damage(user.getDamageSources().playerAttack(user), damage);
        int furyOfFlyLevel = getEnchantmentLevel(stack,world, ModEnchantments.FURY_OF_FLY);
        if (furyOfFlyLevel>0){
            spawnBee(world,entity,furyOfFlyLevel,user);
        }
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),               // 粒子位置
                10,                          // 粒子数量
                0.5,        // X方向速度
                0.5,        // Y方向速度
                0.5,        // Z方向速度
                0.03                      // 基础速度（会被方向向量缩放）
        );
        return true;
    }
    private BlockPos findGroundPosition(ServerWorld world, BlockPos pos) {
        // 从高空开始向下寻找第一个非空气方块
        for (int y = world.getTopY(); y >= world.getBottomY(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!world.getBlockState(checkPos).isAir()) {
                return checkPos.up(); // 返回到地面之上
            }
        }
        return pos; // 如果没有找到地面，返回原位置
    }
    private void spawnLightningEntity(int channelingLevel, World world, Entity entity){
        Random random = new Random();
        if (channelingLevel > 0 && world instanceof ServerWorld serverWorld) {
            // 限制最大闪电数量，防止性能问题
            int lightningCount = Math.min(channelingLevel, 8); // 最多8道闪电

            for (int i = 0; i < lightningCount; i++) {
                // 计算随机偏移位置，避免所有闪电都在同一点
                double offsetX = (random.nextDouble() - 0.5) * 10.0; // ±5格范围
                double offsetZ = (random.nextDouble() - 0.5) * 10.0;

                // 在目标实体位置附近生成闪电
                BlockPos lightningPos = entity.getBlockPos().add((int)offsetX, 0, (int)offsetZ);

                // 找到该位置的地面高度
                BlockPos groundPos = findGroundPosition(serverWorld, lightningPos);

                // 创建闪电实体
                LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
                lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(groundPos));
                lightningEntity.setChanneler(entity instanceof ServerPlayerEntity ? (ServerPlayerEntity) entity : null);

                // 在世界中生成闪电
                serverWorld.spawnEntity(lightningEntity);

                // 添加音效和粒子效果
                serverWorld.playSound(null, groundPos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                        SoundCategory.WEATHER, 5.0F, 1.0F);
            }
        }
    }
    private void damageEntitiesNearTrack(World world, Vec3d startPos, Vec3d endPos, PlayerEntity player) {
        final double DAMAGE_RADIUS = 3.0;
        final double SEARCH_RADIUS = 20.0;
        final float DAMAGE_AMOUNT = 8.0f; // 调整伤害值

        // 获取玩家周围20格范围内的所有生物
        Box searchBox = new Box(
                player.getX() - SEARCH_RADIUS, player.getY() - SEARCH_RADIUS, player.getZ() - SEARCH_RADIUS,
                player.getX() + SEARCH_RADIUS, player.getY() + SEARCH_RADIUS, player.getZ() + SEARCH_RADIUS
        );

        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive() // 排除玩家自己和死亡的生物
        );

        for (LivingEntity entity : nearbyEntities) {
            if (isEntityNearTrack(entity, startPos, endPos, DAMAGE_RADIUS)) {
                // 对生物造成伤害
                entity.damage(player.getDamageSources().playerAttack(player), DAMAGE_AMOUNT);

                // 可选：添加受伤效果
                if (!world.isClient) {
                    ((ServerWorld) world).spawnParticles(
                            ParticleTypes.DAMAGE_INDICATOR,
                            entity.getX(), entity.getBodyY(0.5), entity.getZ(),
                            5, 0.3, 0.3, 0.3, 0.02
                    );
                }
            }
        }
    }

    /**
     * 检查生物是否靠近轨迹
     */
    private boolean isEntityNearTrack(LivingEntity entity, Vec3d startPos, Vec3d endPos, double maxDistance) {
        Vec3d entityPos = entity.getPos();

        // 计算实体到线段的最短距离
        double distance = distanceToLineSegment(entityPos, startPos, endPos);

        return distance <= maxDistance;
    }

    /**
     * 计算点到线段的最短距离
     */
    private double distanceToLineSegment(Vec3d point, Vec3d lineStart, Vec3d lineEnd) {
        // 线段向量
        Vec3d lineVec = lineEnd.subtract(lineStart);
        // 点到线段起点的向量
        Vec3d pointToStart = point.subtract(lineStart);

        // 计算投影比例 t
        double lineLengthSquared = lineVec.lengthSquared();
        if (lineLengthSquared == 0.0) {
            // 线段退化为点
            return pointToStart.length();
        }

        double t = pointToStart.dotProduct(lineVec) / lineLengthSquared;

        // 将 t 限制在 [0,1] 范围内
        t = MathHelper.clamp(t, 0.0, 1.0);

        // 计算线段上最近的点
        Vec3d closestPoint = lineStart.add(lineVec.multiply(t));

        // 返回到最近点的距离
        return point.distanceTo(closestPoint);
    }

    private void teleportUser(float distance, PlayerEntity player, World world, boolean isPlane) {
        float spawnDistance = distance;
        final float DEGREES_TO_RADIANS = 0.017453292F;

        // 保存当前状态
        Vec3d originalVelocity = player.getVelocity();
        boolean wasOnGround = player.isOnGround();

        while (spawnDistance > 0) {
            float yawRadians = player.getYaw() * DEGREES_TO_RADIANS;
            float pitch = player.getPitch() * DEGREES_TO_RADIANS;
//            System.out.println(pitch);
            double offsetX = -Math.sin(yawRadians) * spawnDistance;
            double offsetY = isPlane ? 0 : -Math.sin(pitch) * spawnDistance;
            double offsetZ = Math.cos(yawRadians) * spawnDistance;

            double tpX = player.getX() + offsetX;
            double tpY = player.getY() + offsetY;
            double tpZ = player.getZ() + offsetZ;

            BlockPos targetPos = BlockPos.ofFloored(tpX, tpY, tpZ);
            if (world.getBlockState(targetPos).getBlock() == Blocks.AIR) {
                // 传送玩家
                showTrack(world, player.getPos(), new Vec3d(tpX, tpY, tpZ));
                // 对轨迹附近的生物造成伤害
                damageEntitiesNearTrack(world, player.getPos(), new Vec3d(tpX, tpY, tpZ), player);
                player.teleport((ServerWorld) world, tpX, tpY, tpZ,
                        EnumSet.noneOf(PositionFlag.class),
                        MathHelper.wrapDegrees(player.getYaw()),
                        MathHelper.wrapDegrees(player.getPitch()));

                // 恢复动量
                player.setVelocity(originalVelocity);
                player.velocityModified = true;
                player.setOnGround(wasOnGround);

                // 服务器同步
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
                }
                break;
            }
            spawnDistance -= 0.2;
        }
    }

    private void showParticleEffect(World world, PlayerEntity player) {
        final int PARTICLE_COUNT = 20;
        final double RADIUS = 2.0;
        Vec3d pos = player.getPos();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = 2 * Math.PI * i / PARTICLE_COUNT;
            double x = player.getX() + RADIUS * Math.sin(angle);
            double y = player.getY();
            double z = player.getZ() + RADIUS * Math.cos(angle);
            // 在服务器端发送粒子数据包给所有客户端
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                double speed = 0.08; // 调整速度值，10太快了

                // 计算从中心指向粒子位置的方向（向外）
                Vec3d direction1 = new Vec3d(x - pos.x, y - pos.y, z - pos.z).normalize();

                // 使用 spawnParticles 方法，通过速度参数设置粒子运动方向
                serverWorld.spawnParticles(
                        ParticleTypes.END_ROD,
                        x, y + 0.3, z,                    // 粒子位置
                        10,                          // 粒子数量
                        direction1.x * speed,        // X方向速度
                        direction1.y * speed,        // Y方向速度
                        direction1.z * speed,        // Z方向速度
                        0.01                        // 基础速度（会被方向向量缩放）
                );
                serverWorld.spawnParticles(
                        ParticleTypes.FLAME,
                        x, y + 0.3, z,                    // 粒子位置
                        10,                          // 粒子数量
                        direction1.x * speed,        // X方向速度
                        direction1.y * speed,        // Y方向速度
                        direction1.z * speed,        // Z方向速度
                        0.01                        // 基础速度（会被方向向量缩放）
                );
                serverWorld.spawnParticles(
                        ParticleTypes.DRAGON_BREATH,
                        x, y + 0.3, z,                    // 粒子位置
                        10,                          // 粒子数量
                        direction1.x * speed * 1.1,        // X方向速度
                        direction1.y * speed * 1.1,        // Y方向速度
                        direction1.z * speed * 1.1,        // Z方向速度
                        0.03                        // 基础速度（会被方向向量缩放）
                );
            }
        }
    }

    private void showTrack(World world, Vec3d startPos, Vec3d endPos) {
        final int PARTICLE_COUNT = 50;

        for (int i = 0; i <= PARTICLE_COUNT; i++) {
            double t = (double) i / PARTICLE_COUNT;

            // 线性插值计算位置
            double x = startPos.x + t * (endPos.x - startPos.x);
            double y = startPos.y + t * (endPos.y - startPos.y);
            double z = startPos.z + t * (endPos.z - startPos.z);

            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;

                // 根据位置在路径上的比例调整粒子特性
                int particleCount = t > 0.7 ? 5 : 3; // 末端更多粒子
                double speed = 0.03 + t * 0.02; // 逐渐加速

                // 主轨迹粒子
                serverWorld.spawnParticles(
                        ParticleTypes.FIREWORK,
                        x, y, z,
                        particleCount,
                        0.1, 0.1, 0.1,
                        speed
                );

                // 辅助粒子效果
                if (i % 3 == 0) {
                    serverWorld.spawnParticles(
                            ParticleTypes.TOTEM_OF_UNDYING,
                            x, y, z,
                            1,
                            0.05, 0.05, 0.05,
                            0.01
                    );
                }
                // 辅助粒子效果
                if (i % 4 == 0) {
                    serverWorld.spawnParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            x, y, z,
                            1,
                            0.05, 0.05, 0.05,
                            0.01
                    );
                }

                // 在起点和终点添加特殊效果
                if (i == 0 || i == PARTICLE_COUNT) {
                    serverWorld.spawnParticles(
                            ParticleTypes.ELECTRIC_SPARK,
                            x, y, z,
                            10,
                            0.3, 0.3, 0.3,
                            0.1
                    );
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.tooltip.random-enchant.pearl_spear.detail_description_1"));
            tooltip.add(Text.translatable("item.tooltip.random-enchant.pearl_spear.detail_description_2"));
            tooltip.add(Text.translatable("item.tooltip.random-enchant.pearl_spear.detail_description_3"));
        } else {
            tooltip.add(Text.translatable("item.tooltip.random-enchant.for_shift_tooltip"));
        }
    }


    // 在物品使用时动态添加附魔
    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();

        // 使用传统方式添加附魔
        stack.addEnchantment(getRegistryEntry(null, Enchantments.SHARPNESS), 3);

        return stack;
    }

    private static int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        RegistryEntry<Enchantment> enchantmentEntry =
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                        .getEntry(enchantment).orElse(null);
        return enchantmentEntry != null ?
                EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
    }
    private static RegistryEntry<Enchantment> getRegistryEntry(World world, RegistryKey<Enchantment> enchantment){
        return world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                .getEntry(enchantment).orElse(null);
    }

    private static void spawnBee(World world, Entity target, int count, LivingEntity livingEntity) {
        if (target == null || world.isClient) return;

        for (int i = 0; i < Math.min(count, 20); i++) {
            BeeEntity bee = new BeeEntity(EntityType.BEE, world) {
                @Override
                public boolean tryAttack(Entity target) {
                    boolean result = super.tryAttack(target);
                    // 攻击后立即消失
                    if (result && !this.getWorld().isClient) {
                        this.discard(); // 移除实体
                    }

                    return result;
                }
            };

            bee.setPosition(target.getX(), target.getY() + 1, target.getZ());
            if (target instanceof LivingEntity) {
                bee.setTarget((LivingEntity) target);
            }

            world.spawnEntity(bee);
            bee.setCustomName(Text.translatable("entity.minecraft.bee.spawn_name"));
            bee.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST,11451419,count*2));
            bee.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,11451419,count*2));
            bee.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,11451419,(int)(count*0.2)));
            bee.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,11451419,count*2));
            bee.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,11451419,count*2));
        }
    }
}