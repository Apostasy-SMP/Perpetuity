package net.apostasy.perpetuity.datagen;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.registry.ModSounds;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * @author Chemthunder
 */
public class ModSoundsProvider extends FabricSoundsProvider {
    public ModSoundsProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    protected void configure(RegistryWrapper.WrapperLookup registryLookup, SoundExporter exporter) {
        exporter.add(ModSounds.DISTANT_BANG, SoundTypeBuilder.of(ModSounds.DISTANT_BANG)
                .sound(
                        SoundTypeBuilder.EntryBuilder.create(
                                SoundTypeBuilder.RegistrationType.FILE,
                                Perpetuity.id("event/distant_bang")
                        )
                )
        );
    }

    public String getName() {
        return "Sounds";
    }
}
