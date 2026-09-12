package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/**
 * @author Chemthunder
 */
public class ModTags {
    public static final TagKey<Item> IGNORED_BY_PYLON = TagKey.of(RegistryKeys.ITEM, Perpetuity.id("ignored_by_pylon"));
    public static final TagKey<Item> UNREPAIRABLE_WITH_RENOVITE = TagKey.of(RegistryKeys.ITEM, Perpetuity.id("unrepairable_with_renovite"));
}
