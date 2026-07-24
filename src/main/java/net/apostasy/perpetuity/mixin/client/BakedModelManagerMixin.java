package net.apostasy.perpetuity.mixin.client;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.remnant.RemnantData;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Atlases;
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

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @Inject(method = "reloadModels", at = @At("TAIL"), cancellable = true)
    private static void perpetuity$addRemnantModels(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<Map<Identifier, UnbakedModel>>> cir) {
        CompletableFuture<Map<Identifier, UnbakedModel>> original = cir.getReturnValue();
        CompletableFuture<Map<Identifier, UnbakedModel>> modified = original.thenApply(immutableMap -> {
            Map<Identifier, UnbakedModel> map = new HashMap<>(immutableMap);

            for (Map.Entry<Identifier, RemnantData> data : RemnantDataCollector.remnantTypes.entrySet()) {
                Identifier id = data.getKey();
                Identifier texture = data.getValue().texture();

                ModelTextures.Textures.Builder texturesBuilder = new ModelTextures.Textures.Builder();
                texturesBuilder.addSprite("particle", new SpriteIdentifier(BakedModelManager.BLOCK_OR_ITEM, texture));
                texturesBuilder.addSprite("layer0", new SpriteIdentifier(BakedModelManager.BLOCK_OR_ITEM, texture));
                ModelTextures.Textures textures = texturesBuilder.build();

                JsonUnbakedModel model = new JsonUnbakedModel(
                        null,
                        JsonUnbakedModel.GuiLight.ITEM,
                        true,
                        null,
                        textures,
                        Identifier.ofVanilla("item/generated")
                );

                map.put(id, model);
            }

            return Map.copyOf(map);
        });

        cir.setReturnValue(modified);
    }
}
