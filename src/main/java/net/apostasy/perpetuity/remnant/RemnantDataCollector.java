package net.apostasy.perpetuity.remnant;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.apostasy.perpetuity.Perpetuity;
import net.fabricmc.fabric.impl.resource.FabricResourceReloader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RemnantDataCollector implements FabricResourceReloader {
    public static Object2ObjectOpenHashMap<Identifier, RemnantData> remnantTypes = new Object2ObjectOpenHashMap<>();
    public static Object2ObjectOpenHashMap<Item, Identifier> remnantMappings = new Object2ObjectOpenHashMap<>();

    @Override
    public @NonNull Identifier fabric$getId() {
        return Perpetuity.id("remnant_data");
    }

    @Override
    public CompletableFuture<Void> reload(Store store, Executor prepareExecutor, Synchronizer reloadSynchronizer, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            ResourceManager manager = store.getResourceManager();
            for (Map.Entry<Identifier, Resource> entry : manager.findResources("remnant", path -> path.getPath().endsWith(".json")).entrySet()) {
                List<String> path = Arrays.stream(entry.getKey().toString().split("/")).toList();
                Identifier id = Identifier.of(entry.getKey().getNamespace(), path.getLast().replace(".json", ""));

                try {
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();

                    if (jsonObject.get("texture") == null) {
                        Perpetuity.LOGGER.warn("Remnant data at {} does not contain a texture reference, ignoring", entry.getKey().toString());
                        continue;
                    }
                    if (jsonObject.get("repair_items") == null || !jsonObject.get("repair_items").isJsonArray()) {
                        Perpetuity.LOGGER.warn("Remnant data at {} does not contain repair items list, ignoring", entry.getKey().toString());
                        continue;
                    }
                    List<Item> repairItems = new ArrayList<>();
                    jsonObject.get("repair_items").getAsJsonArray().asList().forEach(element -> {
                        Identifier itemId = Identifier.of(element.getAsString());
                        Item item = Registries.ITEM.get(itemId);
                        repairItems.add(item);
                    });
                    remnantTypes.put(id, new RemnantData(repairItems, Identifier.of(jsonObject.get("texture").getAsString())));

                    if (jsonObject.get("items") == null || !jsonObject.get("items").isJsonArray()) {
                        Perpetuity.LOGGER.warn("Remnant data at {} does not contain items list, ignoring", entry.getKey().toString());
                        continue;
                    }
                    jsonObject.get("items").getAsJsonArray().asList().forEach(element -> {
                        Identifier itemId = Identifier.of(element.getAsString());
                        Item item = Registries.ITEM.get(itemId);
                        remnantMappings.put(item, id);
                    });
                } catch (Exception e) {
                    Perpetuity.LOGGER.error("Failed to register remnant data at path {}: {}", entry.getKey().toString(), e.toString());
                }
            }
        });
    }
}
