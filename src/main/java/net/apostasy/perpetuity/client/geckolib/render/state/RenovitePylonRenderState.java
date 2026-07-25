package net.apostasy.perpetuity.client.geckolib.render.state;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import org.jspecify.annotations.NonNull;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class RenovitePylonRenderState extends BlockEntityRenderState implements GeoRenderState {
    @Override
    public @NonNull Map<DataTicket<?>, Object> getDataMap() {
        return Map.of();
    }
}
