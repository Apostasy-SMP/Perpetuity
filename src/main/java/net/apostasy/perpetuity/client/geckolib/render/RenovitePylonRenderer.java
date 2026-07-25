package net.apostasy.perpetuity.client.geckolib.render;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.block.entity.RenovitePylonBlockEntity;
import net.apostasy.perpetuity.client.geckolib.render.state.RenovitePylonRenderState;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;

public class RenovitePylonRenderer extends GeoBlockRenderer<RenovitePylonBlockEntity, RenovitePylonRenderState> {
    public RenovitePylonRenderer() {
        super(new DefaultedBlockGeoModel<>(Perpetuity.id("renovite_pylon")));
        this.withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
