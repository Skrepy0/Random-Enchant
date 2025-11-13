package com.random_enchant.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable {
    @Unique
    float Times = 0F;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    @Shadow
    public abstract boolean clearStatusEffects();

    @Shadow
    public final boolean addStatusEffect(StatusEffectInstance effect) {
        return this.addStatusEffect(effect, (Entity)null);
    }

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source);

    @Shadow @Final
    private static TrackedData<Float> HEALTH;

    @Shadow public abstract boolean damage(DamageSource source, float amount);

    @Shadow @Nullable protected PlayerEntity attackingPlayer;


    // 添加原版方法的返回值存储
    @Unique
    private boolean totemUsed = false;

    @Unique
    private int getEnchantmentLevel(ItemStack stack, World world, RegistryKey<Enchantment> enchantment) {
        RegistryEntry<Enchantment> enchantmentEntry = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(enchantment)
                .orElse(null);

        int level = enchantmentEntry != null ? EnchantmentHelper.getLevel(enchantmentEntry, stack) : 0;
        return level;
    }

    @Unique
    private void explode() {
        float f = 4.0F;
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), f, World.ExplosionSourceType.TNT);

    }

    @Unique
    private void normal(){
        this.setHealth(1.0F);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
    }

    /**
     * @author
     * Mafuyu33 & Skrepy
     * @reason
     * Change the totem of undying code
     */
    @Overwrite
    private boolean tryUseTotem(DamageSource source) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        Entity attacker = source.getAttacker();
        Entity user= source.getSource();
        World world = null;
        Vec3d position=source.getPosition();
        if (livingEntity != null){
            world = livingEntity.getWorld();
        }

        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        } else {
            ItemStack itemStack = null;
            Hand[] var4 = Hand.values();

            for (Hand hand : var4) {
                ItemStack itemStack2 = this.getStackInHand(hand);
                if (itemStack2.isOf(Items.TOTEM_OF_UNDYING)) {
                    itemStack = itemStack2.copy();
                    int k = 0;//无限
                    if (livingEntity != null) {
                        k = getEnchantmentLevel(itemStack, world, Enchantments.INFINITY);
                    }
                    if (k == 0) {
                        itemStack2.decrement(1);
                    }

                    break;
                }
            }

            if (itemStack != null) { // 仅当成功获取图腾（使用图腾）时执行以下逻辑
                this.clearStatusEffects();
                normal();
                int i = getEnchantmentLevel(itemStack,world,Enchantments.PROTECTION);//保护
                if (i > 0) {
                    this.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST,900,i));
                    this.setHealth(1);
                }

                int j = getEnchantmentLevel(itemStack,world,Enchantments.BLAST_PROTECTION);//爆炸保护
                if (j > 0) {
                    if (attacker != null && livingEntity != null) {
                        explode();
                    }
                }

                int k = getEnchantmentLevel(itemStack,world,Enchantments.CHANNELING);//引雷
                if (k > 0) {
                    if (this.getWorld() instanceof ServerWorld) {
                        if (user != null && attacker instanceof LivingEntity) {
                            BlockPos blockPos = attacker.getBlockPos();
                            LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(this.getWorld());
                            if (lightningEntity != null) {
                                lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(blockPos));
                                lightningEntity.setChanneler(user instanceof ServerPlayerEntity ? (ServerPlayerEntity) user : null);
                                this.getWorld().spawnEntity(lightningEntity);
                                this.playSound(SoundEvents.ITEM_FIRECHARGE_USE, 5, 1.0F);
                            }
                        }
                    }
                }

                int m = getEnchantmentLevel(itemStack,world,Enchantments.POWER);
                if (m>0){
                    if (this.getWorld() instanceof ServerWorld) {
                        if (livingEntity != null) {
                            this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,100,m));
                        }
                    }
                }

                this.getWorld().sendEntityStatus(this, (byte)35); // 原有状态码（可保留，不影响粒子）
                Times++;

                // 标记图腾已被使用
                totemUsed = true;

                // 如果是玩家，触发逃脱死亡进度
                if (livingEntity instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.getAdvancementTracker().grantCriterion(
                            serverPlayer.getServer().getAdvancementLoader().get(
                                    Identifier.ofVanilla("adventure/totem_of_undying")
                            ),
                            "used_totem"
                    );
                }

                // 【修复核心】将图腾粒子触发逻辑移到此处：仅使用图腾时发送
                this.getWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);
            }

            // 【删除/注释】原错误位置的这行代码（移到上面的 if 块内了）
            // this.getWorld().sendEntityStatus(this, EntityStatuses.USE_TOTEM_OF_UNDYING);

            return itemStack != null;
        }
    }

    // 添加一个注入点，确保进度被正确触发
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // 如果图腾被使用，确保触发相应逻辑
        if (totemUsed && (Object)this instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)(Object)this;
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                // 触发进度
                serverPlayer.getAdvancementTracker().grantCriterion(
                        serverPlayer.getServer().getAdvancementLoader().get(
                                Identifier.ofVanilla("adventure/totem_of_undying")
                        ),
                        "used_totem"
                );
            }
            totemUsed = false; // 重置标志
        }
    }
}