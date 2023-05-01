/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

public class PackResourceMetadataReader
implements ResourceMetadataSerializer<PackResourceMetadata> {
    @Override
    public PackResourceMetadata fromJson(JsonObject jsonObject) {
        MutableText lv = Text.Serializer.fromJson(jsonObject.get("description"));
        if (lv == null) {
            throw new JsonParseException("Invalid/missing description!");
        }
        int i = JsonHelper.getInt(jsonObject, "pack_format");
        return new PackResourceMetadata(lv, i);
    }

    @Override
    public JsonObject toJson(PackResourceMetadata arg) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("description", Text.Serializer.toJsonTree(arg.getDescription()));
        jsonObject.addProperty("pack_format", arg.getPackFormat());
        return jsonObject;
    }

    @Override
    public String getKey() {
        return "pack";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject json) {
        return this.fromJson(json);
    }
}

