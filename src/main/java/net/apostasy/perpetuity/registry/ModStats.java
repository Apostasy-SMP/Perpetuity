package net.apostasy.perpetuity.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class ModStats {
    public static Identifier REMNANTS_REPAIRED_WITH_CAKE = register("remnants_repaired_with_cake", StatFormatter.DEFAULT);

    private static Identifier register(String id, StatFormatter formatter) {
        Identifier identifier = Identifier.ofVanilla(id);
        Registry.register(Registries.CUSTOM_STAT, id, identifier);
        Stats.CUSTOM.getOrCreateStat(identifier, formatter);
        return identifier;
    }

    public static void init() {

    }
}
