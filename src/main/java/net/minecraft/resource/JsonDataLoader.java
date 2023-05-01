/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

public abstract class JsonDataLoader
extends SinglePreparationResourceReloader<Map<Identifier, JsonElement>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson gson;
    private final String dataType;

    public JsonDataLoader(Gson gson, String dataType) {
        this.gson = gson;
        this.dataType = dataType;
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager arg, Profiler arg2) {
        HashMap<Identifier, JsonElement> map = Maps.newHashMap();
        ResourceFinder lv = ResourceFinder.json(this.dataType);
        for (Map.Entry<Identifier, Resource> entry : lv.findResources(arg).entrySet()) {
            Identifier lv2 = entry.getKey();
            Identifier lv3 = lv.toResourceId(lv2);
            try {
                BufferedReader reader = entry.getValue().getReader();
                try {
                    JsonElement jsonElement = JsonHelper.deserialize(this.gson, (Reader)reader, JsonElement.class);
                    JsonElement jsonElement2 = map.put(lv3, jsonElement);
                    if (jsonElement2 == null) continue;
                    throw new IllegalStateException("Duplicate data file ignored with ID " + lv3);
                }
                finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            }
            catch (JsonParseException | IOException | IllegalArgumentException exception) {
                LOGGER.error("Couldn't parse data file {} from {}", lv3, lv2, exception);
            }
        }
        return map;
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }
}

