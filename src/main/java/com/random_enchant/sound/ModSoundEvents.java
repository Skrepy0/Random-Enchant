package com.random_enchant.sound;

import com.google.common.collect.ImmutableList;
import com.random_enchant.RandomEnchant;
import com.random_enchant.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.stream.IntStream;

public class ModSoundEvents {
    public static final SoundEvent ENTITY_SLIMEBALL_THROW = register("entity.slimeball.throw");
    private static RegistryEntry<SoundEvent> register(Identifier id, Identifier soundId, float distanceToTravel) {
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(soundId, distanceToTravel));
    }

    private static SoundEvent register(String id) {
        return register(Identifier.ofVanilla(id));
    }

    private static SoundEvent register(Identifier id) {
        return register(id, id);
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(String id) {
        return registerReference(Identifier.of(RandomEnchant.MOD_ID,id));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id) {
        return registerReference(id, id);
    }

    private static SoundEvent register(Identifier id, Identifier soundId) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(soundId));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id, Identifier soundId) {
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(soundId));
    }

    private static ImmutableList<RegistryEntry.Reference<SoundEvent>> registerGoatHornSounds() {
        return (ImmutableList<RegistryEntry.Reference<SoundEvent>>) IntStream.range(0, 8)
                .mapToObj(variant -> registerReference("item.goat_horn.sound." + variant))
                .collect(ImmutableList.toImmutableList());
    }
    public static void registerModSoundEvents() {
        RandomEnchant.LOGGER.info("Register Mod Sound Event");
    }
}
