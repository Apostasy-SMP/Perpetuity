package net.apostasy.perpetuity.component;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class PerpetuityDataComponents {
    /// To modders depending on Perpetuity: NEVER add this component to your items.
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
