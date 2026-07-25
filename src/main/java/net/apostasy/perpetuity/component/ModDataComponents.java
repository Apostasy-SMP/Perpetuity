package net.apostasy.perpetuity.component;

import com.mojang.serialization.Codec;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    /// To modders depending on Perpetuity: NEVER add this component to your items.<br>
    /// To other developers: If you want to override the texture of a remnant instance, just set its ITEM_MODEL component to the remnant's identifier
    public static final ComponentType<RemnantComponent> REMNANT = register(
            "remnant",
            builder -> builder.codec(RemnantComponent.CODEC)
    );

    public static final ComponentType<Integer> TIMES_BROKEN = register(
            "times_broken",
            builder -> builder.codec(Codec.INT)
    );

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Perpetuity.id(name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static void init() {
        ComponentTooltipAppenderRegistry.addAfter(DataComponentTypes.TRIM, REMNANT);
    }
}
