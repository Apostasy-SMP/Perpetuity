package net.apostasy.perpetuity.remnant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.List;

public record RemnantData(List<Item> resources, Text name, Identifier texture, Identifier id) {
    public static final Codec<RemnantData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Registries.ITEM.getCodec()).fieldOf("resources").forGetter(RemnantData::resources),
            TextCodecs.CODEC.fieldOf("name").forGetter(RemnantData::name),
            Identifier.CODEC.fieldOf("texture").forGetter(RemnantData::texture),
            Identifier.CODEC.fieldOf("id").forGetter(RemnantData::id)
    ).apply(instance, RemnantData::new));
}
