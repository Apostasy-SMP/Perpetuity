package net.apostasy.perpetuity.component.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.intprovider.IntProvider;

public record RemnantComponent(RegistryKey<Item> remnantItem, IntProvider remnantCount) {
    public static final Codec<RemnantComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(Registries.ITEM.getKey()).fieldOf("item").forGetter(RemnantComponent::remnantItem),
            IntProvider.VALUE_CODEC.fieldOf("count").forGetter(RemnantComponent::remnantCount)
    ).apply(instance, RemnantComponent::new));
}
