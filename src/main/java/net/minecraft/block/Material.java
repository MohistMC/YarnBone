/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;

public final class Material {
    public static final Material AIR = new Builder(MapColor.CLEAR).allowsMovement().lightPassesThrough().notSolid().replaceable().build();
    public static final Material STRUCTURE_VOID = new Builder(MapColor.CLEAR).allowsMovement().lightPassesThrough().notSolid().replaceable().build();
    public static final Material PORTAL = new Builder(MapColor.CLEAR).allowsMovement().lightPassesThrough().notSolid().blocksPistons().build();
    public static final Material CARPET = new Builder(MapColor.WHITE_GRAY).allowsMovement().lightPassesThrough().notSolid().burnable().build();
    public static final Material PLANT = new Builder(MapColor.DARK_GREEN).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
    public static final Material UNDERWATER_PLANT = new Builder(MapColor.WATER_BLUE).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
    public static final Material REPLACEABLE_PLANT = new Builder(MapColor.DARK_GREEN).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().burnable().build();
    public static final Material NETHER_SHOOTS = new Builder(MapColor.DARK_GREEN).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
    public static final Material REPLACEABLE_UNDERWATER_PLANT = new Builder(MapColor.WATER_BLUE).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
    public static final Material WATER = new Builder(MapColor.WATER_BLUE).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().liquid().build();
    public static final Material BUBBLE_COLUMN = new Builder(MapColor.WATER_BLUE).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().liquid().build();
    public static final Material LAVA = new Builder(MapColor.BRIGHT_RED).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().liquid().build();
    public static final Material SNOW_LAYER = new Builder(MapColor.WHITE).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
    public static final Material FIRE = new Builder(MapColor.CLEAR).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().replaceable().build();
    public static final Material DECORATION = new Builder(MapColor.CLEAR).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
    public static final Material COBWEB = new Builder(MapColor.WHITE_GRAY).allowsMovement().lightPassesThrough().destroyedByPiston().build();
    public static final Material SCULK = new Builder(MapColor.BLACK).build();
    public static final Material REDSTONE_LAMP = new Builder(MapColor.CLEAR).build();
    public static final Material ORGANIC_PRODUCT = new Builder(MapColor.LIGHT_BLUE_GRAY).build();
    public static final Material SOIL = new Builder(MapColor.DIRT_BROWN).build();
    public static final Material SOLID_ORGANIC = new Builder(MapColor.PALE_GREEN).build();
    public static final Material DENSE_ICE = new Builder(MapColor.PALE_PURPLE).build();
    public static final Material AGGREGATE = new Builder(MapColor.PALE_YELLOW).build();
    public static final Material SPONGE = new Builder(MapColor.YELLOW).build();
    public static final Material SHULKER_BOX = new Builder(MapColor.PURPLE).build();
    public static final Material WOOD = new Builder(MapColor.OAK_TAN).burnable().build();
    public static final Material NETHER_WOOD = new Builder(MapColor.OAK_TAN).build();
    public static final Material BAMBOO_SAPLING = new Builder(MapColor.OAK_TAN).burnable().destroyedByPiston().allowsMovement().build();
    public static final Material BAMBOO = new Builder(MapColor.OAK_TAN).burnable().destroyedByPiston().build();
    public static final Material WOOL = new Builder(MapColor.WHITE_GRAY).burnable().build();
    public static final Material TNT = new Builder(MapColor.BRIGHT_RED).burnable().lightPassesThrough().build();
    public static final Material LEAVES = new Builder(MapColor.DARK_GREEN).burnable().lightPassesThrough().destroyedByPiston().build();
    public static final Material GLASS = new Builder(MapColor.CLEAR).lightPassesThrough().build();
    public static final Material ICE = new Builder(MapColor.PALE_PURPLE).lightPassesThrough().build();
    public static final Material CACTUS = new Builder(MapColor.DARK_GREEN).lightPassesThrough().destroyedByPiston().build();
    public static final Material STONE = new Builder(MapColor.STONE_GRAY).build();
    public static final Material METAL = new Builder(MapColor.IRON_GRAY).build();
    public static final Material SNOW_BLOCK = new Builder(MapColor.WHITE).build();
    public static final Material REPAIR_STATION = new Builder(MapColor.IRON_GRAY).blocksPistons().build();
    public static final Material BARRIER = new Builder(MapColor.CLEAR).blocksPistons().build();
    public static final Material PISTON = new Builder(MapColor.STONE_GRAY).blocksPistons().build();
    public static final Material MOSS_BLOCK = new Builder(MapColor.DARK_GREEN).destroyedByPiston().build();
    public static final Material GOURD = new Builder(MapColor.DARK_GREEN).destroyedByPiston().build();
    public static final Material EGG = new Builder(MapColor.DARK_GREEN).destroyedByPiston().build();
    public static final Material CAKE = new Builder(MapColor.CLEAR).destroyedByPiston().build();
    public static final Material AMETHYST = new Builder(MapColor.PURPLE).build();
    public static final Material POWDER_SNOW = new Builder(MapColor.WHITE).notSolid().allowsMovement().build();
    public static final Material FROGSPAWN = new Builder(MapColor.WATER_BLUE).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build();
    public static final Material FROGLIGHT = new Builder(MapColor.CLEAR).build();
    public static final Material DECORATED_POT = new Builder(MapColor.TERRACOTTA_RED).destroyedByPiston().build();
    private final MapColor color;
    private final PistonBehavior pistonBehavior;
    private final boolean blocksMovement;
    private final boolean burnable;
    private final boolean liquid;
    private final boolean blocksLight;
    private final boolean replaceable;
    private final boolean solid;

    public Material(MapColor color, boolean liquid, boolean solid, boolean blocksMovement, boolean blocksLight, boolean burnable, boolean replaceable, PistonBehavior pistonBehavior) {
        this.color = color;
        this.liquid = liquid;
        this.solid = solid;
        this.blocksMovement = blocksMovement;
        this.blocksLight = blocksLight;
        this.burnable = burnable;
        this.replaceable = replaceable;
        this.pistonBehavior = pistonBehavior;
    }

    public boolean isLiquid() {
        return this.liquid;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean blocksMovement() {
        return this.blocksMovement;
    }

    public boolean isBurnable() {
        return this.burnable;
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean blocksLight() {
        return this.blocksLight;
    }

    public PistonBehavior getPistonBehavior() {
        return this.pistonBehavior;
    }

    public MapColor getColor() {
        return this.color;
    }

    public static class Builder {
        private PistonBehavior pistonBehavior = PistonBehavior.NORMAL;
        private boolean blocksMovement = true;
        private boolean burnable;
        private boolean liquid;
        private boolean replaceable;
        private boolean solid = true;
        private final MapColor color;
        private boolean blocksLight = true;

        public Builder(MapColor color) {
            this.color = color;
        }

        public Builder liquid() {
            this.liquid = true;
            return this;
        }

        public Builder notSolid() {
            this.solid = false;
            return this;
        }

        public Builder allowsMovement() {
            this.blocksMovement = false;
            return this;
        }

        Builder lightPassesThrough() {
            this.blocksLight = false;
            return this;
        }

        protected Builder burnable() {
            this.burnable = true;
            return this;
        }

        public Builder replaceable() {
            this.replaceable = true;
            return this;
        }

        protected Builder destroyedByPiston() {
            this.pistonBehavior = PistonBehavior.DESTROY;
            return this;
        }

        protected Builder blocksPistons() {
            this.pistonBehavior = PistonBehavior.BLOCK;
            return this;
        }

        public Material build() {
            return new Material(this.color, this.liquid, this.solid, this.blocksMovement, this.blocksLight, this.burnable, this.replaceable, this.pistonBehavior);
        }
    }
}

