package net.apostasy.perpetuity.component.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public record ToolInfoComponent(int timesBroken) implements TooltipAppender {
    public static final Codec<ToolInfoComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("times_broken").forGetter(ToolInfoComponent::timesBroken)
    ).apply(instance, ToolInfoComponent::new));

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> list, TooltipType type, ComponentsAccess components) {
        list.accept(Text.translatable("tooltip.tool.times_broken", timesBroken).formatted(Formatting.GRAY));
    }
}
