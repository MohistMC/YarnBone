/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ZipResourcePack
extends AbstractFileResourcePack {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Splitter TYPE_NAMESPACE_SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
    private final File backingZipFile;
    @Nullable
    private ZipFile file;
    private boolean failedToOpen;

    public ZipResourcePack(String name, File backingZipFile, boolean alwaysStable) {
        super(name, alwaysStable);
        this.backingZipFile = backingZipFile;
    }

    @Nullable
    private ZipFile getZipFile() {
        if (this.failedToOpen) {
            return null;
        }
        if (this.file == null) {
            try {
                this.file = new ZipFile(this.backingZipFile);
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to open pack {}", (Object)this.backingZipFile, (Object)iOException);
                this.failedToOpen = true;
                return null;
            }
        }
        return this.file;
    }

    private static String toPath(ResourceType type, Identifier id) {
        return String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath());
    }

    @Override
    @Nullable
    public InputSupplier<InputStream> openRoot(String ... segments) {
        return this.openFile(String.join((CharSequence)"/", segments));
    }

    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        return this.openFile(ZipResourcePack.toPath(type, id));
    }

    @Nullable
    private InputSupplier<InputStream> openFile(String path) {
        ZipFile zipFile = this.getZipFile();
        if (zipFile == null) {
            return null;
        }
        ZipEntry zipEntry = zipFile.getEntry(path);
        if (zipEntry == null) {
            return null;
        }
        return InputSupplier.create(zipFile, zipEntry);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        ZipFile zipFile = this.getZipFile();
        if (zipFile == null) {
            return Set.of();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        HashSet<String> set = Sets.newHashSet();
        while (enumeration.hasMoreElements()) {
            ArrayList<String> list;
            ZipEntry zipEntry = enumeration.nextElement();
            String string = zipEntry.getName();
            if (!string.startsWith(type.getDirectory() + "/") || (list = Lists.newArrayList(TYPE_NAMESPACE_SPLITTER.split(string))).size() <= 1) continue;
            String string2 = (String)list.get(1);
            if (string2.equals(string2.toLowerCase(Locale.ROOT))) {
                set.add(string2);
                continue;
            }
            LOGGER.warn("Ignored non-lowercase namespace: {} in {}", (Object)string2, (Object)this.backingZipFile);
        }
        return set;
    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public void close() {
        if (this.file != null) {
            IOUtils.closeQuietly((Closeable)this.file);
            this.file = null;
        }
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResourcePack.ResultConsumer consumer) {
        ZipFile zipFile = this.getZipFile();
        if (zipFile == null) {
            return;
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        String string3 = type.getDirectory() + "/" + namespace + "/";
        String string4 = string3 + prefix + "/";
        while (enumeration.hasMoreElements()) {
            String string5;
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory() || !(string5 = zipEntry.getName()).startsWith(string4)) continue;
            String string6 = string5.substring(string3.length());
            Identifier lv = Identifier.of(namespace, string6);
            if (lv != null) {
                consumer.accept(lv, InputSupplier.create(zipFile, zipEntry));
                continue;
            }
            LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", (Object)namespace, (Object)string6);
        }
    }
}

