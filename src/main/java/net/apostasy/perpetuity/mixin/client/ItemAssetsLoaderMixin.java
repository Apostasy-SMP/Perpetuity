package net.apostasy.perpetuity.mixin.client;

import com.mojang.serialization.MapCodec;
import net.apostasy.perpetuity.remnant.RemnantData;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ItemAssetsLoader.class)
public class ItemAssetsLoaderMixin {
    @Inject(
            method = "load",
            at = @At("TAIL"),
            cancellable = true
    )
    private static void perpetuity$injectRemnantAssets(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<ItemAssetsLoader.Result>> cir) {
        CompletableFuture<ItemAssetsLoader.Result> originalFuture = cir.getReturnValue();

        CompletableFuture<ItemAssetsLoader.Result> modifiedFuture = originalFuture.thenApply(originalResult -> {
            Map<Identifier, ItemAsset> mutableContents = new HashMap<>(originalResult.contents());

            for (Map.Entry<Identifier, RemnantData> entry : RemnantDataCollector.remnantTypes.entrySet()) {
                BasicItemModel.Unbaked model = new BasicItemModel.Unbaked(
                        entry.getKey(),
                        List.of()
                );

                ItemAsset asset = new ItemAsset(model, new ItemAsset.Properties(true, false, 1.0F));
                mutableContents.put(entry.getKey(), asset);
            }

            return new ItemAssetsLoader.Result(Map.copyOf(mutableContents));
        });

        cir.setReturnValue(modifiedFuture);
    }
}
