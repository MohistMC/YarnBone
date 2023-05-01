/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.resource;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.PackFeatureSetMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ResourcePackProfile {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final PackFactory packFactory;
    private final Text displayName;
    private final Text description;
    private final ResourcePackCompatibility compatibility;
    private final FeatureSet requestedFeatures;
    private final InsertionPosition position;
    private final boolean alwaysEnabled;
    private final boolean pinned;
    private final ResourcePackSource source;

    @Nullable
    public static ResourcePackProfile create(String name, Text displayName, boolean alwaysEnabled, PackFactory packFactory, ResourceType type, InsertionPosition position, ResourcePackSource source) {
        Metadata lv = ResourcePackProfile.loadMetadata(name, packFactory);
        return lv != null ? ResourcePackProfile.of(name, displayName, alwaysEnabled, packFactory, lv, type, position, false, source) : null;
    }

    public static ResourcePackProfile of(String name, Text displayName, boolean alwaysEnabled, PackFactory packFactory, Metadata metadata, ResourceType type, InsertionPosition position, boolean pinned, ResourcePackSource source) {
        return new ResourcePackProfile(name, alwaysEnabled, packFactory, displayName, metadata, metadata.getCompatibility(type), position, pinned, source);
    }

    private ResourcePackProfile(String name, boolean alwaysEnabled, PackFactory packFactory, Text displayName, Metadata metadata, ResourcePackCompatibility compatibility, InsertionPosition position, boolean pinned, ResourcePackSource source) {
        this.name = name;
        this.packFactory = packFactory;
        this.displayName = displayName;
        this.description = metadata.description();
        this.compatibility = compatibility;
        this.requestedFeatures = metadata.requestedFeatures();
        this.alwaysEnabled = alwaysEnabled;
        this.position = position;
        this.pinned = pinned;
        this.source = source;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static Metadata loadMetadata(String name, PackFactory packFactory) {
        try (ResourcePack lv = packFactory.open(name);){
            PackResourceMetadata lv2 = lv.parseMetadata(PackResourceMetadata.SERIALIZER);
            if (lv2 == null) {
                LOGGER.warn("Missing metadata in pack {}", (Object)name);
                Metadata metadata = null;
                return metadata;
            }
            PackFeatureSetMetadata lv3 = lv.parseMetadata(PackFeatureSetMetadata.SERIALIZER);
            FeatureSet lv4 = lv3 != null ? lv3.flags() : FeatureSet.empty();
            Metadata metadata = new Metadata(lv2.getDescription(), lv2.getPackFormat(), lv4);
            return metadata;
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to read pack metadata", exception);
            return null;
        }
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public Text getDescription() {
        return this.description;
    }

    public Text getInformationText(boolean enabled) {
        return Texts.bracketed(this.source.decorate(Text.literal(this.name))).styled(style -> style.withColor(enabled ? Formatting.GREEN : Formatting.RED).withInsertion(StringArgumentType.escapeIfRequired(this.name)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.empty().append(this.displayName).append("\n").append(this.description))));
    }

    public ResourcePackCompatibility getCompatibility() {
        return this.compatibility;
    }

    public FeatureSet getRequestedFeatures() {
        return this.requestedFeatures;
    }

    public ResourcePack createResourcePack() {
        return this.packFactory.open(this.name);
    }

    public String getName() {
        return this.name;
    }

    public boolean isAlwaysEnabled() {
        return this.alwaysEnabled;
    }

    public boolean isPinned() {
        return this.pinned;
    }

    public InsertionPosition getInitialPosition() {
        return this.position;
    }

    public ResourcePackSource getSource() {
        return this.source;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourcePackProfile)) {
            return false;
        }
        ResourcePackProfile lv = (ResourcePackProfile)o;
        return this.name.equals(lv.name);
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    @FunctionalInterface
    public static interface PackFactory {
        public ResourcePack open(String var1);
    }

    public record Metadata(Text description, int format, FeatureSet requestedFeatures) {
        public ResourcePackCompatibility getCompatibility(ResourceType type) {
            return ResourcePackCompatibility.from(this.format, type);
        }
    }

    public static enum InsertionPosition {
        TOP,
        BOTTOM;


        public <T> int insert(List<T> items, T item, Function<T, ResourcePackProfile> profileGetter, boolean listInverted) {
            ResourcePackProfile lv2;
            int i;
            InsertionPosition lv;
            InsertionPosition insertionPosition = lv = listInverted ? this.inverse() : this;
            if (lv == BOTTOM) {
                ResourcePackProfile lv22;
                int i2;
                for (i2 = 0; i2 < items.size() && (lv22 = profileGetter.apply(items.get(i2))).isPinned() && lv22.getInitialPosition() == this; ++i2) {
                }
                items.add(i2, item);
                return i2;
            }
            for (i = items.size() - 1; i >= 0 && (lv2 = profileGetter.apply(items.get(i))).isPinned() && lv2.getInitialPosition() == this; --i) {
            }
            items.add(i + 1, item);
            return i + 1;
        }

        public InsertionPosition inverse() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}

