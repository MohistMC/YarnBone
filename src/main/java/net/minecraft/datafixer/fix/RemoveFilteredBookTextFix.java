/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemNbtFix;

public class RemoveFilteredBookTextFix
extends ItemNbtFix {
    public RemoveFilteredBookTextFix(Schema schema) {
        super(schema, "Remove filtered text from books", itemId -> itemId.equals("minecraft:writable_book") || itemId.equals("minecraft:written_book"));
    }

    @Override
    protected <T> Dynamic<T> fixNbt(Dynamic<T> dynamic) {
        return dynamic.remove("filtered_title").remove("filtered_pages");
    }
}

