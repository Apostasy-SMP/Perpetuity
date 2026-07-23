package net.apostasy.perpetuity.remnant;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;

public record RemnantData(List<Item> resource, Identifier texture) {
}
