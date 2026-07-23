package net.apostasy.perpetuity.component.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.IntProvider;

public record RemnantComponent(ItemStack item, Identifier texture) {
    public static final Codec<RemnantComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(RemnantComponent::item),
            Identifier.CODEC.fieldOf("texture").forGetter(RemnantComponent::texture)
    ).apply(instance, RemnantComponent::new));
}
