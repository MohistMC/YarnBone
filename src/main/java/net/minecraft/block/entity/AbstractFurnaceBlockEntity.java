/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFurnaceBlockEntity
extends LockableContainerBlockEntity
implements SidedInventory,
RecipeUnlocker,
RecipeInputProvider {
    protected static final int INPUT_SLOT_INDEX = 0;
    protected static final int FUEL_SLOT_INDEX = 1;
    protected static final int OUTPUT_SLOT_INDEX = 2;
    public static final int BURN_TIME_PROPERTY_INDEX = 0;
    private static final int[] TOP_SLOTS = new int[]{0};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
    private static final int[] SIDE_SLOTS = new int[]{1};
    public static final int FUEL_TIME_PROPERTY_INDEX = 1;
    public static final int COOK_TIME_PROPERTY_INDEX = 2;
    public static final int COOK_TIME_TOTAL_PROPERTY_INDEX = 3;
    public static final int PROPERTY_COUNT = 4;
    public static final int DEFAULT_COOK_TIME = 200;
    public static final int field_31295 = 2;
    protected DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    int burnTime;
    int fuelTime;
    int cookTime;
    int cookTimeTotal;
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate(){

        @Override
        public int get(int index) {
            switch (index) {
                case 0: {
                    return AbstractFurnaceBlockEntity.this.burnTime;
                }
                case 1: {
                    return AbstractFurnaceBlockEntity.this.fuelTime;
                }
                case 2: {
                    return AbstractFurnaceBlockEntity.this.cookTime;
                }
                case 3: {
                    return AbstractFurnaceBlockEntity.this.cookTimeTotal;
                }
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: {
                    AbstractFurnaceBlockEntity.this.burnTime = value;
                    break;
                }
                case 1: {
                    AbstractFurnaceBlockEntity.this.fuelTime = value;
                    break;
                }
                case 2: {
                    AbstractFurnaceBlockEntity.this.cookTime = value;
                    break;
                }
                case 3: {
                    AbstractFurnaceBlockEntity.this.cookTimeTotal = value;
                    break;
                }
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };
    private final Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap();
    private final RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> matchGetter;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType, pos, state);
        this.matchGetter = RecipeManager.createCachedMatchGetter(recipeType);
    }

    public static Map<Item, Integer> createFuelTimeMap() {
        LinkedHashMap<Item, Integer> map = Maps.newLinkedHashMap();
        AbstractFurnaceBlockEntity.addFuel(map, Items.LAVA_BUCKET, 20000);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.COAL_BLOCK, 16000);
        AbstractFurnaceBlockEntity.addFuel(map, Items.BLAZE_ROD, 2400);
        AbstractFurnaceBlockEntity.addFuel(map, Items.COAL, 1600);
        AbstractFurnaceBlockEntity.addFuel(map, Items.CHARCOAL, 1600);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.LOGS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.BAMBOO_BLOCKS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.PLANKS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.BAMBOO_MOSAIC, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_STAIRS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_SLABS, 150);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.BAMBOO_MOSAIC_SLAB, 150);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_TRAPDOORS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_FENCES, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.FENCE_GATES, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.NOTE_BLOCK, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.BOOKSHELF, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.CHISELED_BOOKSHELF, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.LECTERN, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.JUKEBOX, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.CHEST, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.TRAPPED_CHEST, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.CRAFTING_TABLE, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.DAYLIGHT_DETECTOR, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.BANNERS, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Items.BOW, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Items.FISHING_ROD, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.LADDER, 300);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.SIGNS, 200);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.HANGING_SIGNS, 800);
        AbstractFurnaceBlockEntity.addFuel(map, Items.WOODEN_SHOVEL, 200);
        AbstractFurnaceBlockEntity.addFuel(map, Items.WOODEN_SWORD, 200);
        AbstractFurnaceBlockEntity.addFuel(map, Items.WOODEN_HOE, 200);
        AbstractFurnaceBlockEntity.addFuel(map, Items.WOODEN_AXE, 200);
        AbstractFurnaceBlockEntity.addFuel(map, Items.WOODEN_PICKAXE, 200);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_DOORS, 200);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.BOATS, 1200);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOOL, 100);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOODEN_BUTTONS, 100);
        AbstractFurnaceBlockEntity.addFuel(map, Items.STICK, 100);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.SAPLINGS, 100);
        AbstractFurnaceBlockEntity.addFuel(map, Items.BOWL, 100);
        AbstractFurnaceBlockEntity.addFuel(map, ItemTags.WOOL_CARPETS, 67);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.DRIED_KELP_BLOCK, 4001);
        AbstractFurnaceBlockEntity.addFuel(map, Items.CROSSBOW, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.BAMBOO, 50);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.DEAD_BUSH, 100);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.SCAFFOLDING, 50);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.LOOM, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.BARREL, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.CARTOGRAPHY_TABLE, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.FLETCHING_TABLE, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.SMITHING_TABLE, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.COMPOSTER, 300);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.AZALEA, 100);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.FLOWERING_AZALEA, 100);
        AbstractFurnaceBlockEntity.addFuel(map, Blocks.MANGROVE_ROOTS, 300);
        return map;
    }

    private static boolean isNonFlammableWood(Item item) {
        return item.getRegistryEntry().isIn(ItemTags.NON_FLAMMABLE_WOOD);
    }

    private static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime) {
        for (RegistryEntry<Item> lv : Registries.ITEM.iterateEntries(tag)) {
            if (AbstractFurnaceBlockEntity.isNonFlammableWood(lv.value())) continue;
            fuelTimes.put(lv.value(), fuelTime);
        }
    }

    private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime) {
        Item lv = item.asItem();
        if (AbstractFurnaceBlockEntity.isNonFlammableWood(lv)) {
            if (SharedConstants.isDevelopment) {
                throw Util.throwOrPause(new IllegalStateException("A developer tried to explicitly make fire resistant item " + lv.getName(null).getString() + " a furnace fuel. That will not work!"));
            }
            return;
        }
        fuelTimes.put(lv, fuelTime);
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.burnTime = nbt.getShort("BurnTime");
        this.cookTime = nbt.getShort("CookTime");
        this.cookTimeTotal = nbt.getShort("CookTimeTotal");
        this.fuelTime = this.getFuelTime(this.inventory.get(1));
        NbtCompound lv = nbt.getCompound("RecipesUsed");
        for (String string : lv.getKeys()) {
            this.recipesUsed.put(new Identifier(string), lv.getInt(string));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("BurnTime", (short)this.burnTime);
        nbt.putShort("CookTime", (short)this.cookTime);
        nbt.putShort("CookTimeTotal", (short)this.cookTimeTotal);
        Inventories.writeNbt(nbt, this.inventory);
        NbtCompound lv = new NbtCompound();
        this.recipesUsed.forEach((identifier, count) -> lv.putInt(identifier.toString(), (int)count));
        nbt.put("RecipesUsed", lv);
    }

    public static void tick(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
        boolean bl4;
        boolean bl = blockEntity.isBurning();
        boolean bl2 = false;
        if (blockEntity.isBurning()) {
            --blockEntity.burnTime;
        }
        ItemStack lv = blockEntity.inventory.get(1);
        boolean bl3 = !blockEntity.inventory.get(0).isEmpty();
        boolean bl5 = bl4 = !lv.isEmpty();
        if (blockEntity.isBurning() || bl4 && bl3) {
            Recipe lv2 = bl3 ? (Recipe)blockEntity.matchGetter.getFirstMatch(blockEntity, world).orElse(null) : null;
            int i = blockEntity.getMaxCountPerStack();
            if (!blockEntity.isBurning() && AbstractFurnaceBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), lv2, blockEntity.inventory, i)) {
                blockEntity.fuelTime = blockEntity.burnTime = blockEntity.getFuelTime(lv);
                if (blockEntity.isBurning()) {
                    bl2 = true;
                    if (bl4) {
                        Item lv3 = lv.getItem();
                        lv.decrement(1);
                        if (lv.isEmpty()) {
                            Item lv4 = lv3.getRecipeRemainder();
                            blockEntity.inventory.set(1, lv4 == null ? ItemStack.EMPTY : new ItemStack(lv4));
                        }
                    }
                }
            }
            if (blockEntity.isBurning() && AbstractFurnaceBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), lv2, blockEntity.inventory, i)) {
                ++blockEntity.cookTime;
                if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
                    blockEntity.cookTime = 0;
                    blockEntity.cookTimeTotal = AbstractFurnaceBlockEntity.getCookTime(world, blockEntity);
                    if (AbstractFurnaceBlockEntity.craftRecipe(world.getRegistryManager(), lv2, blockEntity.inventory, i)) {
                        blockEntity.setLastRecipe(lv2);
                    }
                    bl2 = true;
                }
            } else {
                blockEntity.cookTime = 0;
            }
        } else if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
            blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
        }
        if (bl != blockEntity.isBurning()) {
            bl2 = true;
            state = (BlockState)state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }
        if (bl2) {
            AbstractFurnaceBlockEntity.markDirty(world, pos, state);
        }
    }

    private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (slots.get(0).isEmpty() || recipe == null) {
            return false;
        }
        ItemStack lv = recipe.getOutput(registryManager);
        if (lv.isEmpty()) {
            return false;
        }
        ItemStack lv2 = slots.get(2);
        if (lv2.isEmpty()) {
            return true;
        }
        if (!lv2.isItemEqual(lv)) {
            return false;
        }
        if (lv2.getCount() < count && lv2.getCount() < lv2.getMaxCount()) {
            return true;
        }
        return lv2.getCount() < lv.getMaxCount();
    }

    private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (recipe == null || !AbstractFurnaceBlockEntity.canAcceptRecipeOutput(registryManager, recipe, slots, count)) {
            return false;
        }
        ItemStack lv = slots.get(0);
        ItemStack lv2 = recipe.getOutput(registryManager);
        ItemStack lv3 = slots.get(2);
        if (lv3.isEmpty()) {
            slots.set(2, lv2.copy());
        } else if (lv3.isOf(lv2.getItem())) {
            lv3.increment(1);
        }
        if (lv.isOf(Blocks.WET_SPONGE.asItem()) && !slots.get(1).isEmpty() && slots.get(1).isOf(Items.BUCKET)) {
            slots.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        lv.decrement(1);
        return true;
    }

    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        Item lv = fuel.getItem();
        return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(lv, 0);
    }

    private static int getCookTime(World world, AbstractFurnaceBlockEntity furnace) {
        return furnace.matchGetter.getFirstMatch(furnace, world).map(AbstractCookingRecipe::getCookTime).orElse(200);
    }

    public static boolean canUseAsFuel(ItemStack stack) {
        return AbstractFurnaceBlockEntity.createFuelTimeMap().containsKey(stack.getItem());
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        if (side == Direction.UP) {
            return TOP_SLOTS;
        }
        return SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == 1) {
            return stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET);
        }
        return true;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack lv : this.inventory) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack lv = this.inventory.get(slot);
        boolean bl = !stack.isEmpty() && stack.isItemEqual(lv) && ItemStack.areNbtEqual(stack, lv);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
        if (slot == 0 && !bl) {
            this.cookTimeTotal = AbstractFurnaceBlockEntity.getCookTime(this.world, this);
            this.cookTime = 0;
            this.markDirty();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 2) {
            return false;
        }
        if (slot == 1) {
            ItemStack lv = this.inventory.get(1);
            return AbstractFurnaceBlockEntity.canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !lv.isOf(Items.BUCKET);
        }
        return true;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public void setLastRecipe(@Nullable Recipe<?> recipe) {
        if (recipe != null) {
            Identifier lv = recipe.getId();
            this.recipesUsed.addTo(lv, 1);
        }
    }

    @Override
    @Nullable
    public Recipe<?> getLastRecipe() {
        return null;
    }

    @Override
    public void unlockLastRecipe(PlayerEntity player) {
    }

    public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
        List<Recipe<?>> list = this.getRecipesUsedAndDropExperience(player.getWorld(), player.getPos());
        player.unlockRecipes(list);
        this.recipesUsed.clear();
    }

    public List<Recipe<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        ArrayList<Recipe<?>> list = Lists.newArrayList();
        for (Object2IntMap.Entry entry : this.recipesUsed.object2IntEntrySet()) {
            world.getRecipeManager().get((Identifier)entry.getKey()).ifPresent(recipe -> {
                list.add((Recipe<?>)recipe);
                AbstractFurnaceBlockEntity.dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe).getExperience());
            });
        }
        return list;
    }

    private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int j = MathHelper.floor((float)multiplier * experience);
        float g = MathHelper.fractionalPart((float)multiplier * experience);
        if (g != 0.0f && Math.random() < (double)g) {
            ++j;
        }
        ExperienceOrbEntity.spawn(world, pos, j);
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack lv : this.inventory) {
            finder.addInput(lv);
        }
    }
}

