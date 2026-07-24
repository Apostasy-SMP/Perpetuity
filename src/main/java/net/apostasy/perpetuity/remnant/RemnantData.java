package net.apostasy.perpetuity.remnant;

import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public record RemnantData(List<Item> resource, Text name, Identifier texture) {
}
