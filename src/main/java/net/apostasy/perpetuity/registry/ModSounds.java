package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

/**
 * @author Chemthunder
 */
public class ModSounds {
    public static final SoundEvent DISTANT_BANG = create("event.distant_bang");

    private static SoundEvent create(String name) {
        return Registry.register(Registries.SOUND_EVENT, Perpetuity.id(name), SoundEvent.of(Perpetuity.id(name)));
    }

    public static void init() {}
}
