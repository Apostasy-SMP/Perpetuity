package net.apostasy.perpetuity.mixin.echoes;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.apostasy.perpetuity.util.EchoUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Chemthunder
 */
@Mixin(value = EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @WrapMethod(method = "getLevel")
    private static int perpetuity$denyEnchantments(RegistryEntry<Enchantment> enchantment, ItemStack stack, Operation<Integer> original) {
        Entity entity = stack.getHolder();
        if (EchoUtil.effectsApplicable(entity)) {
            return 0;
        }
        return original.call(enchantment, stack);
    }

    @WrapMethod(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V")
    private static void perpetuity$denyEnchantments(ItemStack stack, EnchantmentHelper.Consumer consumer, Operation<Void> original) {
        Entity entity = stack.getHolder();
        if (EchoUtil.effectsApplicable(entity)) {
            return;
        }
        original.call(stack, consumer);
    }
}
