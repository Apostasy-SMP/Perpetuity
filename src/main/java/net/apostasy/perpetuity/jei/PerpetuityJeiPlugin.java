package net.apostasy.perpetuity.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.component.ModDataComponents;
import net.apostasy.perpetuity.component.util.RemnantComponent;
import net.apostasy.perpetuity.registry.ModItems;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PerpetuityJeiPlugin implements IModPlugin {
    @Override
    public @NonNull Identifier getPluginUid() {
        return Perpetuity.id(Perpetuity.MOD_ID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<IJeiAnvilRecipe> recipes = new ArrayList<>();

        List<Item> renoviteRepairs = Registries.ITEM.stream().filter(item -> item.getDefaultStack().isDamageable() && !item.getDefaultStack().isIn(TagKey.of(RegistryKeys.ITEM, Perpetuity.id("unrepairable_with_renovite")))).toList();
        List<ItemStack> renoviteRepairInputs = new ArrayList<>();
        List<ItemStack> renoviteRepairOutputs = new ArrayList<>();

        for (Item item : renoviteRepairs) {
            ItemStack stack = item.getDefaultStack();
            stack.setDamage(stack.getMaxDamage()-1);
            ItemStack repaired = stack.copy();
            repaired.setDamage(stack.getDamage()/2-1);

            renoviteRepairInputs.add(stack);
            renoviteRepairOutputs.add(repaired);
        }

        recipes.add(registration.getVanillaRecipeFactory().createAnvilRecipe(renoviteRepairInputs, Collections.singletonList(ModItems.RENOVITE.getDefaultStack()), renoviteRepairOutputs, Perpetuity.id("recipe/renovite/repair_damageable")));

        RemnantDataCollector.remnantTypes.forEach((identifier, data) -> {
            List<ItemStack> outputs = new ArrayList<>();
            List<ItemStack> renoviteOutputs = new ArrayList<>();

            List<ItemStack> rightStacks = new ArrayList<>();
            data.resources().forEach(item -> rightStacks.add(item.getDefaultStack()));

            RemnantDataCollector.remnantMappings.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(identifier))
                    .map(Map.Entry::getKey)
                    .forEach(item -> {
                        ItemStack stack = item.getDefaultStack().copy();
                        stack.setDamage(stack.getMaxDamage()/10*9);

                        ItemStack renoviteStack = item.getDefaultStack().copy();
                        renoviteStack.setDamage(stack.getMaxDamage()/2);

                        ItemStack remnantDataStack = stack.copy();
                        remnantDataStack.setDamage(remnantDataStack.getMaxDamage());

                        ItemStack leftStack = ModItems.REMNANT.getDefaultStack().copy();
                        leftStack.set(ModDataComponents.REMNANT, new RemnantComponent(remnantDataStack, data));

                        Identifier itemId = Identifier.of(item.toString());
                        recipes.add(registration.getVanillaRecipeFactory().createAnvilRecipe(leftStack, rightStacks, List.of(stack), identifier.withSuffixedPath("/recipe/" + itemId.getNamespace() + "/" + itemId.getPath())));
                        recipes.add(registration.getVanillaRecipeFactory().createAnvilRecipe(leftStack, List.of(ModItems.RENOVITE.getDefaultStack()), List.of(renoviteStack), identifier.withSuffixedPath("/recipe/renovite/" + itemId.getNamespace() + "/" + itemId.getPath())));

                        outputs.add(stack);
                        renoviteOutputs.add(renoviteStack);
                    });

            ItemStack genericLeftStack = ModItems.REMNANT.getDefaultStack().copy();
            genericLeftStack.set(ModDataComponents.REMNANT, new RemnantComponent(ItemStack.EMPTY, data));

            if (!outputs.isEmpty()) recipes.add(registration.getVanillaRecipeFactory().createAnvilRecipe(genericLeftStack, rightStacks, outputs, identifier.withSuffixedPath("/recipe/generic"))); // Allows for componentless remnants to show EVERY recipe
            if (!renoviteOutputs.isEmpty()) recipes.add(registration.getVanillaRecipeFactory().createAnvilRecipe(genericLeftStack, rightStacks, renoviteOutputs, identifier.withSuffixedPath("/recipe/renovite/generic"))); // Allows for componentless remnants to show EVERY recipe
        });

        registration.addRecipes(RecipeTypes.ANVIL, recipes);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.REMNANT, (itemStack, context) -> {
            RemnantComponent component = itemStack.get(ModDataComponents.REMNANT);

            if (component == null || component.item().isEmpty()) return "";

            Identifier itemId = Identifier.of(component.item().getItem().toString());
            return component.data().id() + "/" + itemId.getNamespace() + "/" + itemId.getPath();
        });
    }
}
