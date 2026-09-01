package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.world.LabyrinthChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModWorldGen {
    private ModWorldGen() {}

    public static void init() {
        Registry.register(Registries.CHUNK_GENERATOR, Perpetuity.id("labyrinth"), LabyrinthChunkGenerator.CODEC);
    }
}