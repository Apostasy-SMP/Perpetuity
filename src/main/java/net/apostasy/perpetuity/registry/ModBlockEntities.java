package net.apostasy.perpetuity.registry;

import net.apostasy.perpetuity.Perpetuity;
import net.apostasy.perpetuity.block.entity.RenovitePylonBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static final BlockEntityType<RenovitePylonBlockEntity> RENOVITE_PYLON =
            register("renovite_pylon", RenovitePylonBlockEntity::new, ModBlocks.RENOVITE_PYLON);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Perpetuity.id(name), FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void init() {

    }
}
