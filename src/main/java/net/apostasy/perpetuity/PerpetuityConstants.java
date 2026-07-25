package net.apostasy.perpetuity;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public interface PerpetuityConstants {
    List<Identifier> GENERIC_REMNANT_PREVIEWS = new ArrayList<>(List.of(
            Perpetuity.id("copper_remnant"),
            Perpetuity.id("iron_remnant"),
            Perpetuity.id("gold_remnant"),
            Perpetuity.id("lapis_remnant"),
            Perpetuity.id("diamond_remnant"),
            Perpetuity.id("netherite_remnant"),
            Perpetuity.id("amethyst_remnant"),
            Perpetuity.id("experience_remnant")
    ));
}
