package net.apostasy.perpetuity.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.nexusrealms.nebulon.api.render.client.DynamicUniformBlock;
import de.nexusrealms.nebulon.api.render.client.RenderStage;
import de.nexusrealms.nebulon.api.render.client.ShaderMaterial;
import de.nexusrealms.nebulon.api.render.client.composite.CompositorContext;
import de.nexusrealms.nebulon.api.render.client.composite.CompositorPass;
import de.nexusrealms.nebulon.api.render.client.composite.CompositorPassHandle;
import de.nexusrealms.nebulon.api.render.client.composite.NebulonCompositor;
import net.apostasy.perpetuity.Perpetuity;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.OptionalInt;

/**
 * The sky and rim mist of The Echoes, drawn as one fullscreen compositor pass.
 *
 * <p>1.21.11 has no public sky-renderer hook, so the sky is composited rather than rendered: the
 * pass runs at {@link RenderStage#END_MAIN} and the shader keeps only pixels still at the far plane,
 * fogging the rest. Sky and fog share the one depth read.
 *
 * <p>The pass is registered only while the player is in The Echoes. The compositor clears each
 * pass's target before calling it and then blits the last target over the main framebuffer, so a
 * registered pass that declines to draw blacks out the frame - skipping means not being registered.
 *
 * <p>Hand-rolled rather than using {@code FullscreenCompositorPass} because that helper uploads
 * {@code IDENTITY} as the modelview - fine for stretching a triangle, but it leaves a shader with no
 * idea where the camera looks, welding any view-dependent effect to the screen. This uploads the
 * real camera-to-world rotation and position; the projection still comes from Minecraft's own
 * {@code Projection} block via {@link RenderSystem#bindDefaultUniforms}.
 */
public final class EchoesSkyPass implements CompositorPass {
    public static final RegistryKey<World> THE_ECHOES =
            RegistryKey.of(RegistryKeys.WORLD, Perpetuity.id("the_echoes"));

    private static final Identifier PASS_ID = Perpetuity.id("echoes_sky");
    private static final Identifier MATERIAL_ID = Perpetuity.id("echoes_sky_pipeline");
    /** Fragment stage; resolves to assets/perpetuity/shaders/core/echoes_sky.fsh. */
    private static final Identifier SHADER_ID = Perpetuity.id("core/echoes_sky");
    /** Needs no vertex buffer, just gl_VertexID. */
    private static final Identifier FULLSCREEN_VSH = Identifier.of("nebulon", "core/fullscreen_triangle");

    private static final String UNIFORM_BLOCK = "EchoesSky";
    private static final int PRIORITY = 100;

    private static final int BLOCK_BYTES = new Std140SizeCalculator().putMat4f().putVec4().get();

    /**
     * @param cameraToWorld rotation only; turns camera-space rays into world-space ones
     * @param seconds       continuous clock - {@code GameTime} wraps once per day and would pop
     */
    private record SkyUniforms(Matrix4f cameraToWorld, float camX, float camY, float camZ, float seconds) {}

    private static final EchoesSkyPass INSTANCE = new EchoesSkyPass();
    private static CompositorPassHandle handle;

    private final ShaderMaterial material;
    private final DynamicUniformBlock<SkyUniforms> uniforms;
    private final long start = System.nanoTime();
    private boolean prepared;

    private EchoesSkyPass() {
        this.material = ShaderMaterial.builder(MATERIAL_ID)
                .shaders(FULLSCREEN_VSH, SHADER_ID)
                .geometry(VertexFormats.POSITION, VertexFormat.DrawMode.TRIANGLES)
                .transparency(ShaderMaterial.Transparency.OPAQUE)
                .depth(DepthTestFunction.NO_DEPTH_TEST, false)
                .cull(false)
                .sampler("InputColor")
                .sampler("SceneColor")
                .sampler("SceneDepth")
                .uniformBlock(UNIFORM_BLOCK)
                .build();

        this.uniforms = new DynamicUniformBlock<>(UNIFORM_BLOCK, BLOCK_BYTES, 1, EchoesSkyPass::encode);
    }

    public static void init() {
        // Synced per frame rather than per tick so the registration matches the dimension the very
        // frame it changes; the compositor reads its pass list later in this same frame.
        WorldRenderEvents.START_MAIN.register(context -> sync(MinecraftClient.getInstance().world));
    }

    private static void sync(ClientWorld world) {
        boolean wanted = world != null && world.getRegistryKey().equals(THE_ECHOES);
        if (wanted == (handle != null)) return;

        if (wanted) {
            handle = NebulonCompositor.register(PASS_ID, RenderStage.END_MAIN, PRIORITY, INSTANCE);
        } else {
            handle.close();
            handle = null;
        }
    }

    private static void encode(Std140Builder builder, SkyUniforms value) {
        builder.putMat4f(value.cameraToWorld())
                .putVec4(value.camX(), value.camY(), value.camZ(), value.seconds());
    }

    @Override
    public void render(CompositorContext context) {
        // Never bail out: the compositor cleared this target and will blit it over the frame either
        // way, so a frame without a usable camera gets the sky from the origin, not a black screen.
        CameraRenderState camera = context.world().worldState().cameraRenderState;
        boolean usable = camera != null && camera.initialized && camera.pos != null;

        if (!prepared) {
            material.precompile();
            uniforms.prepare();
            prepared = true;
        }
        uniforms.beginFrame();

        // Orientation already maps camera space to world space; the view matrix is its inverse.
        Matrix4f cameraToWorld = usable ? new Matrix4f().rotation(camera.orientation) : new Matrix4f();
        float seconds = (System.nanoTime() - start) / 1_000_000_000.0f;

        GpuBufferSlice slice = uniforms.upload(usable
                ? new SkyUniforms(cameraToWorld,
                (float) camera.pos.x, (float) camera.pos.y, (float) camera.pos.z, seconds)
                : new SkyUniforms(cameraToWorld, 0.0f, 0.0f, 0.0f, seconds));

        GpuSampler linear = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
        GpuSampler nearest = RenderSystem.getSamplerCache().get(FilterMode.NEAREST);

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (RenderPass pass = encoder.createRenderPass(() -> "Perpetuity echoes sky",
                context.output().getColorAttachmentView(), OptionalInt.empty())) {
            pass.setPipeline(material.pipeline());
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform(UNIFORM_BLOCK, slice);
            pass.bindTexture("InputColor", context.inputColor(), linear);
            pass.bindTexture("SceneColor", context.scene().color(), linear);
            // Depth must not be filtered: interpolating near and far samples invents surfaces and
            // frays the sky mask along silhouettes.
            pass.bindTexture("SceneDepth", context.scene().depth(), nearest);
            pass.draw(0, 3);
        }
    }
}