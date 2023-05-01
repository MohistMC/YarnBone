/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.data.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class BlockListProvider
implements DataProvider {
    private final DataOutput output;

    public BlockListProvider(DataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        JsonObject jsonObject = new JsonObject();
        for (Block lv : Registries.BLOCK) {
            Identifier lv2 = Registries.BLOCK.getId(lv);
            JsonObject jsonObject2 = new JsonObject();
            StateManager<Block, BlockState> lv3 = lv.getStateManager();
            if (!lv3.getProperties().isEmpty()) {
                JsonObject jsonObject3 = new JsonObject();
                for (Property property : lv3.getProperties()) {
                    JsonArray jsonArray = new JsonArray();
                    for (Comparable comparable : property.getValues()) {
                        jsonArray.add(Util.getValueAsString(property, comparable));
                    }
                    jsonObject3.add(property.getName(), jsonArray);
                }
                jsonObject2.add("properties", jsonObject3);
            }
            JsonArray jsonArray2 = new JsonArray();
            for (BlockState blockState : lv3.getStates()) {
                JsonObject jsonObject4 = new JsonObject();
                JsonObject jsonObject5 = new JsonObject();
                for (Property<?> lv6 : lv3.getProperties()) {
                    jsonObject5.addProperty(lv6.getName(), Util.getValueAsString(lv6, blockState.get(lv6)));
                }
                if (jsonObject5.size() > 0) {
                    jsonObject4.add("properties", jsonObject5);
                }
                jsonObject4.addProperty("id", Block.getRawIdFromState(blockState));
                if (blockState == lv.getDefaultState()) {
                    jsonObject4.addProperty("default", true);
                }
                jsonArray2.add(jsonObject4);
            }
            jsonObject2.add("states", jsonArray2);
            jsonObject.add(lv2.toString(), jsonObject2);
        }
        Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("blocks.json");
        return DataProvider.writeToPath(writer, jsonObject, path);
    }

    @Override
    public final String getName() {
        return "Block List";
    }
}

