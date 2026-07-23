package net.apostasy.perpetuity.component;

import com.mojang.serialization.Codec;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class PerpetuityDataComponents {
    public static final ComponentType<RemnantComponent> REMNANT = register(
            "remnant",
            builder -> builder.codec(RemnantComponent.CODEC)
    );

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Perpetuity.id(name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static void init() {

    }
}
