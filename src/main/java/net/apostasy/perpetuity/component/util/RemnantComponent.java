package net.apostasy.perpetuity.component.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.client.PerpetuityClient;
import net.apostasy.perpetuity.remnant.RemnantData;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.IntProvider;

import java.util.function.Consumer;

public record RemnantComponent(ItemStack item, RemnantData data) implements TooltipAppender {
    public static final Codec<RemnantComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(RemnantComponent::item),
            RemnantData.CODEC.fieldOf("data").forGetter(RemnantComponent::data)
    ).apply(instance, RemnantComponent::new));

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> list, TooltipType type, ComponentsAccess components) {
        list.accept(Text.translatable("tooltip.remnant.repairs_into").formatted(Formatting.GRAY).append(item.getFormattedName()));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Text bindKeyName = PerpetuityClient.getSneakKeyName();
            list.accept(Text.translatable("tooltip.remnant.preview", bindKeyName).formatted(Formatting.GRAY));
        }
    }
}
