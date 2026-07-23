package net.apostasy.perpetuity.data;

import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final ComponentType<ItemStack> REMNANT_ITEM = register("remnant_item", builder ->  builder.codec(ItemStack.CODEC));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Perpetuity.id(name), builder.apply(ComponentType.builder()).build());
    }

    public static void registerDataComponents() {

    }
}
