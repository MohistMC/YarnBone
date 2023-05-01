/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

public interface TextContent {
    public static final TextContent EMPTY = new TextContent(){

        public String toString() {
            return "empty";
        }
    };

    default public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return Optional.empty();
    }

    default public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return Optional.empty();
    }

    default public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        return MutableText.of(this);
    }
}

