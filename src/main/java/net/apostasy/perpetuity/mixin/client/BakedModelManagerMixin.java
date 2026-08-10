package net.apostasy.perpetuity.mixin.client;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.remnant.RemnantData;
import net.apostasy.perpetuity.remnant.RemnantDataCollector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BlockRenderLayers;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.MissingItemModel;
import net.minecraft.client.render.item.tint.ConstantTintSource;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.GeneratedItemModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@Mixin(BakedModelManager.class)
public abstract class BakedModelManagerMixin {
    @Unique
    private static final Object2ObjectOpenHashMap<Identifier, BasicItemModel> remnantModels = new Object2ObjectOpenHashMap<>();

    @Inject(method = "reloadModels", at = @At("TAIL"))
    private static void perpetuity$resetRemnantModels(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<Map<Identifier, UnbakedModel>>> cir) {
        remnantModels.clear();
    }

//    @ModifyReturnValue(method = "getItemModel", at = @At("RETURN"))
//    private ItemModel perpetuity$getRemnantModel(ItemModel original, @Local(argsOnly = true) Identifier id) {
//        if (!RemnantDataCollector.remnantTypes.containsKey(id)) return original;
//
//        if (!remnantModels.containsKey(id)) {
//            RemnantData data = RemnantDataCollector.remnantTypes.get(id);
//            Identifier texture = data.texture();
//
//            ModelTextures.Textures.Builder texturesBuilder = new ModelTextures.Textures.Builder();
//            texturesBuilder.addSprite("particle", new SpriteIdentifier(BakedModelManager.BLOCK_OR_ITEM, texture));
//            texturesBuilder.addSprite("layer0", new SpriteIdentifier(BakedModelManager.BLOCK_OR_ITEM, texture));
//            ModelTextures.Textures textures = texturesBuilder.build();
//
//            GeneratedItemModel model = new GeneratedItemModel();
//            ReferencedModelsCollector collector = new ReferencedModelsCollector(
//                    Map.of(
//                            id, model
//                    ),
//                    MissingModel.create()
//            );
//            if (collector instanceof ReferencedModelsCollectorAccessor accessor) {
//                accessor.getModelCache().put(id, new ReferencedModelsCollector.Holder(id, model, true));
//            }
//            BakedSimpleModel baked = collector.collectModels().get(id);
//
//            Baker baker = new Baker() {
//                @Override
//                public BakedSimpleModel getModel(Identifier requestedId) {
//                    return collector.collectModels().get(requestedId);
//                }
//
//                @Override
//                public BlockModelPart getBlockPart() {
//                    throw new IllegalStateException();
//                }
//
//                @Override
//                public <T> T compute(Baker.ResolvableCacheKey<T> key) {
//                    return key.compute(this);
//                }
//
//                @Override
//                public ErrorCollectingSpriteGetter getSpriteGetter() {
//                    return new MixinFallbackSpriteGetter();
//                }
//
//                @Override
//                public Baker.Vec3fInterner getVec3fInterner() {
//                    return new Vec3fInternerImpl();
//                }
//            };
//
//            ModelTextures modelTextures = new ModelTextures.Builder().addFirst(textures).build(baked);
//            BakedGeometry geometry = baked.bakeGeometry(modelTextures, baker, ModelRotation.IDENTITY);
//
//            ModelTransformation transformations = new ModelTransformation(
//                    // thirdperson_lefthand
//                    new Transformation(new Vector3f(0, 0, 0), new Vector3f(0,  (float) 3 / 16, (float) 1 / 16), new Vector3f(0.55f, 0.55f, 0.55f)),
//                    // thirdperson_righthand
//                    new Transformation(new Vector3f(0, 0, 0), new Vector3f(0,  (float) 3 / 16, (float) 1 / 16), new Vector3f(0.55f, 0.55f, 0.55f)),
//                    // firstperson_lefthand
//                    new Transformation(new Vector3f(0, 90, -25), new Vector3f(1.13f / 16, 3.2f / 16, 1.13f / 16), new Vector3f(0.68f, 0.68f, 0.68f)),
//                    // firstperson_righthand
//                    new Transformation(new Vector3f(0, -90, 25), new Vector3f(1.13f / 16, 3.2f / 16, 1.13f / 16), new Vector3f(0.68f, 0.68f, 0.68f)),
//                    // head
//                    new Transformation(new Vector3f(0, 180, 0), new Vector3f(0, (float) 13 / 16, (float) 7 / 16), new Vector3f(1, 1, 1)),
//                    // gui
//                    Transformation.IDENTITY,
//                    // ground
//                    new Transformation(new Vector3f(0, 0, 0), new Vector3f(0, (float) 2 / 16, 0), new Vector3f(0.5f, 0.5f, 0.5f)),
//                    // fixed
//                    new Transformation(new Vector3f(0, 180, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)),
//                    // on_shelf
//                    Transformation.IDENTITY
//            );
//
//            remnantModels.put(id, BasicItemModelAccessor.create(
//                    new ArrayList<>(List.of(new ConstantTintSource(0xFFFFFFFF))),
//                    geometry.getAllQuads(),
//                    new ModelSettings(false, baked.getParticleTexture(modelTextures, baker), transformations),
//                    findRenderLayerGetter(geometry.getAllQuads())
//            ));
//        }
//
//        return remnantModels.get(id);
//    }
//
//    @Unique
//    private static final Function<ItemStack, RenderLayer> ITEMS_ATLAS_RENDER_LAYER_GETTER = stack -> TexturedRenderLayers.getItemTranslucentCull();
//    @Unique
//    private static final Function<ItemStack, RenderLayer> BLOCKS_ATLAS_RENDER_LAYER_GETTER = stack -> {
//        if (stack.getItem() instanceof BlockItem blockItem) {
//            BlockRenderLayer blockRenderLayer = BlockRenderLayers.getBlockLayer(blockItem.getBlock().getDefaultState());
//            if (blockRenderLayer != BlockRenderLayer.TRANSLUCENT) {
//                return TexturedRenderLayers.getEntityCutout();
//            }
//        }
//
//        return TexturedRenderLayers.getBlockTranslucentCull();
//    };
//
//    @Unique
//    private static Function<ItemStack, RenderLayer> findRenderLayerGetter(List<BakedQuad> quads) {
//        Iterator<BakedQuad> iterator = quads.iterator();
//        if (!iterator.hasNext()) {
//            return ITEMS_ATLAS_RENDER_LAYER_GETTER;
//        }
//
//        Identifier identifier = iterator.next().sprite().getAtlasId();
//
//        while (iterator.hasNext()) {
//            BakedQuad bakedQuad = iterator.next();
//            Identifier identifier2 = bakedQuad.sprite().getAtlasId();
//            if (!identifier2.equals(identifier)) {
//                throw new IllegalStateException("Multiple atlases used in model, expected " + identifier + ", but also got " + identifier2);
//            }
//        }
//
//        if (identifier.equals(SpriteAtlasTexture.ITEMS_ATLAS_TEXTURE)) {
//            return ITEMS_ATLAS_RENDER_LAYER_GETTER;
//        } else if (identifier.equals(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)) {
//            return BLOCKS_ATLAS_RENDER_LAYER_GETTER;
//        } else {
//            throw new IllegalArgumentException("Atlas " + identifier + " can't be used for item models");
//        }
//    }
//
//    static class Vec3fInternerImpl implements Baker.Vec3fInterner {
//        private final Interner<Vector3fc> INTERNER = Interners.newStrongInterner();
//
//        @Override
//        public Vector3fc intern(Vector3fc vec) {
//            return this.INTERNER.intern(vec);
//        }
//    }
//
//    private static class MixinFallbackSpriteGetter implements ErrorCollectingSpriteGetter {
//        @Override
//        public Sprite get(SpriteIdentifier id, SimpleModel model) {
//            return MinecraftClient.getInstance()
//                    .getAtlasManager()
//                    .getAtlasTexture(Atlases.ITEMS)
//                    .getSprite(id.getTextureId());
//        }
//
//        @Override
//        public Sprite getMissing(String name, SimpleModel model) {
//            return MinecraftClient.getInstance()
//                    .getAtlasManager()
//                    .getAtlasTexture(Atlases.BLOCKS)
//                    .getMissingSprite();
//        }
//    }
//
//    @Mixin(BasicItemModel.class)
//    private interface BasicItemModelAccessor {
//        @Invoker("<init>")
//        static BasicItemModel create(List<TintSource> tints, List<BakedQuad> quads, ModelSettings settings, Function<ItemStack, RenderLayer> renderLayerGetter) {
//            throw new AssertionError("Replaced by Perpetuity");
//        }
//    }
//
//    @Mixin(ReferencedModelsCollector.class)
//    private interface ReferencedModelsCollectorAccessor {
//        @Accessor("resolver")
//        ResolvableModel.Resolver getResolver();
//
//        @Accessor("modelCache")
//        Object2ObjectMap<Identifier, Object> getModelCache();
//    }
}
