/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource.fs;

import java.nio.file.Path;
import java.util.Map;
import net.minecraft.resource.fs.ResourcePath;

interface ResourceFile {
    public static final ResourceFile EMPTY = new ResourceFile(){

        public String toString() {
            return "empty";
        }
    };
    public static final ResourceFile RELATIVE = new ResourceFile(){

        public String toString() {
            return "relative";
        }
    };

    public record Directory(Map<String, ResourcePath> children) implements ResourceFile
    {
    }

    public record File(Path contents) implements ResourceFile
    {
    }
}

