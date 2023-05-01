/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.text;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.text.KeybindTranslations;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jetbrains.annotations.Nullable;

public class KeybindTextContent
implements TextContent {
    private final String key;
    @Nullable
    private Supplier<Text> translated;

    public KeybindTextContent(String key) {
        this.key = key;
    }

    private Text getTranslated() {
        if (this.translated == null) {
            this.translated = KeybindTranslations.factory.apply(this.key);
        }
        return this.translated.get();
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return this.getTranslated().visit(visitor);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return this.getTranslated().visit(visitor, style);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeybindTextContent)) return false;
        KeybindTextContent lv = (KeybindTextContent)o;
        if (!this.key.equals(lv.key)) return false;
        return true;
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public String toString() {
        return "keybind{" + this.key + "}";
    }

    public String getKey() {
        return this.key;
    }
}

