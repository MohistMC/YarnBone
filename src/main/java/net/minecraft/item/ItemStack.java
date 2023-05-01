/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class ItemStack {
    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registries.ITEM.getCodec().fieldOf("id")).forGetter(stack -> stack.item), ((MapCodec)Codec.INT.fieldOf("Count")).forGetter(stack -> stack.count), NbtCompound.CODEC.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.nbt))).apply((Applicative<ItemStack, ?>)instance, ItemStack::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((ItemConvertible)null);
    public static final DecimalFormat MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public static final String ENCHANTMENTS_KEY = "Enchantments";
    public static final String DISPLAY_KEY = "display";
    public static final String NAME_KEY = "Name";
    public static final String LORE_KEY = "Lore";
    public static final String DAMAGE_KEY = "Damage";
    public static final String COLOR_KEY = "color";
    private static final String UNBREAKABLE_KEY = "Unbreakable";
    private static final String REPAIR_COST_KEY = "RepairCost";
    private static final String CAN_DESTROY_KEY = "CanDestroy";
    private static final String CAN_PLACE_ON_KEY = "CanPlaceOn";
    private static final String HIDE_FLAGS_KEY = "HideFlags";
    private static final Text DISABLED_TEXT = Text.translatable("item.disabled").formatted(Formatting.RED);
    private static final int field_30903 = 0;
    private static final Style LORE_STYLE = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(true);
    private int count;
    private int bobbingAnimationTime;
    @Deprecated
    private final Item item;
    @Nullable
    private NbtCompound nbt;
    private boolean empty;
    @Nullable
    private Entity holder;
    @Nullable
    private BlockPredicatesChecker destroyChecker;
    @Nullable
    private BlockPredicatesChecker placeChecker;

    public Optional<TooltipData> getTooltipData() {
        return this.getItem().getTooltipData(this);
    }

    public ItemStack(ItemConvertible item) {
        this(item, 1);
    }

    public ItemStack(RegistryEntry<Item> entry) {
        this(entry.value(), 1);
    }

    private ItemStack(ItemConvertible item, int count, Optional<NbtCompound> nbt) {
        this(item, count);
        nbt.ifPresent(this::setNbt);
    }

    public ItemStack(RegistryEntry<Item> itemEntry, int count) {
        this(itemEntry.value(), count);
    }

    public ItemStack(ItemConvertible item, int count) {
        this.item = item == null ? null : item.asItem();
        this.count = count;
        if (this.item != null && this.item.isDamageable()) {
            this.setDamage(this.getDamage());
        }
        this.updateEmptyState();
    }

    private void updateEmptyState() {
        this.empty = false;
        this.empty = this.isEmpty();
    }

    private ItemStack(NbtCompound nbt) {
        this.item = Registries.ITEM.get(new Identifier(nbt.getString("id")));
        this.count = nbt.getByte("Count");
        if (nbt.contains("tag", NbtElement.COMPOUND_TYPE)) {
            this.nbt = nbt.getCompound("tag");
            this.getItem().postProcessNbt(this.nbt);
        }
        if (this.getItem().isDamageable()) {
            this.setDamage(this.getDamage());
        }
        this.updateEmptyState();
    }

    public static ItemStack fromNbt(NbtCompound nbt) {
        try {
            return new ItemStack(nbt);
        }
        catch (RuntimeException runtimeException) {
            LOGGER.debug("Tried to load invalid item: {}", (Object)nbt, (Object)runtimeException);
            return EMPTY;
        }
    }

    public boolean isEmpty() {
        if (this == EMPTY) {
            return true;
        }
        if (this.getItem() == null || this.isOf(Items.AIR)) {
            return true;
        }
        return this.count <= 0;
    }

    public boolean isItemEnabled(FeatureSet enabledFeatures) {
        return this.isEmpty() || this.getItem().isEnabled(enabledFeatures);
    }

    public ItemStack split(int amount) {
        int j = Math.min(amount, this.count);
        ItemStack lv = this.copy();
        lv.setCount(j);
        this.decrement(j);
        return lv;
    }

    public Item getItem() {
        return this.empty ? Items.AIR : this.item;
    }

    public RegistryEntry<Item> getRegistryEntry() {
        return this.getItem().getRegistryEntry();
    }

    public boolean isIn(TagKey<Item> tag) {
        return this.getItem().getRegistryEntry().isIn(tag);
    }

    public boolean isOf(Item item) {
        return this.getItem() == item;
    }

    public boolean itemMatches(Predicate<RegistryEntry<Item>> predicate) {
        return predicate.test(this.getItem().getRegistryEntry());
    }

    public boolean itemMatches(RegistryEntry<Item> itemEntry) {
        return this.getItem().getRegistryEntry() == itemEntry;
    }

    public Stream<TagKey<Item>> streamTags() {
        return this.getItem().getRegistryEntry().streamTags();
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity lv = context.getPlayer();
        BlockPos lv2 = context.getBlockPos();
        CachedBlockPosition lv3 = new CachedBlockPosition(context.getWorld(), lv2, false);
        if (lv != null && !lv.getAbilities().allowModifyWorld && !this.canPlaceOn(context.getWorld().getRegistryManager().get(RegistryKeys.BLOCK), lv3)) {
            return ActionResult.PASS;
        }
        Item lv4 = this.getItem();
        ActionResult lv5 = lv4.useOnBlock(context);
        if (lv != null && lv5.shouldIncrementStat()) {
            lv.incrementStat(Stats.USED.getOrCreateStat(lv4));
        }
        return lv5;
    }

    public float getMiningSpeedMultiplier(BlockState state) {
        return this.getItem().getMiningSpeedMultiplier(this, state);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return this.getItem().use(world, user, hand);
    }

    public ItemStack finishUsing(World world, LivingEntity user) {
        return this.getItem().finishUsing(this, world, user);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        Identifier lv = Registries.ITEM.getId(this.getItem());
        nbt.putString("id", lv == null ? "minecraft:air" : lv.toString());
        nbt.putByte("Count", (byte)this.count);
        if (this.nbt != null) {
            nbt.put("tag", this.nbt.copy());
        }
        return nbt;
    }

    public int getMaxCount() {
        return this.getItem().getMaxCount();
    }

    public boolean isStackable() {
        return this.getMaxCount() > 1 && (!this.isDamageable() || !this.isDamaged());
    }

    public boolean isDamageable() {
        if (this.empty || this.getItem().getMaxDamage() <= 0) {
            return false;
        }
        NbtCompound lv = this.getNbt();
        return lv == null || !lv.getBoolean(UNBREAKABLE_KEY);
    }

    public boolean isDamaged() {
        return this.isDamageable() && this.getDamage() > 0;
    }

    public int getDamage() {
        return this.nbt == null ? 0 : this.nbt.getInt(DAMAGE_KEY);
    }

    public void setDamage(int damage) {
        this.getOrCreateNbt().putInt(DAMAGE_KEY, Math.max(0, damage));
    }

    public int getMaxDamage() {
        return this.getItem().getMaxDamage();
    }

    public boolean damage(int amount, Random random, @Nullable ServerPlayerEntity player) {
        int j;
        if (!this.isDamageable()) {
            return false;
        }
        if (amount > 0) {
            j = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, this);
            int k = 0;
            for (int l = 0; j > 0 && l < amount; ++l) {
                if (!UnbreakingEnchantment.shouldPreventDamage(this, j, random)) continue;
                ++k;
            }
            if ((amount -= k) <= 0) {
                return false;
            }
        }
        if (player != null && amount != 0) {
            Criteria.ITEM_DURABILITY_CHANGED.trigger(player, this, this.getDamage() + amount);
        }
        j = this.getDamage() + amount;
        this.setDamage(j);
        return j >= this.getMaxDamage();
    }

    public <T extends LivingEntity> void damage(int amount, T entity, Consumer<T> breakCallback) {
        if (entity.world.isClient || entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode) {
            return;
        }
        if (!this.isDamageable()) {
            return;
        }
        if (this.damage(amount, entity.getRandom(), entity instanceof ServerPlayerEntity ? (ServerPlayerEntity)entity : null)) {
            breakCallback.accept(entity);
            Item lv = this.getItem();
            this.decrement(1);
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity)entity).incrementStat(Stats.BROKEN.getOrCreateStat(lv));
            }
            this.setDamage(0);
        }
    }

    public boolean isItemBarVisible() {
        return this.item.isItemBarVisible(this);
    }

    public int getItemBarStep() {
        return this.item.getItemBarStep(this);
    }

    public int getItemBarColor() {
        return this.item.getItemBarColor(this);
    }

    public boolean onStackClicked(Slot slot, ClickType clickType, PlayerEntity player) {
        return this.getItem().onStackClicked(this, slot, clickType, player);
    }

    public boolean onClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        return this.getItem().onClicked(this, stack, slot, clickType, player, cursorStackReference);
    }

    public void postHit(LivingEntity target, PlayerEntity attacker) {
        Item lv = this.getItem();
        if (lv.postHit(this, target, attacker)) {
            attacker.incrementStat(Stats.USED.getOrCreateStat(lv));
        }
    }

    public void postMine(World world, BlockState state, BlockPos pos, PlayerEntity miner) {
        Item lv = this.getItem();
        if (lv.postMine(this, world, state, pos, miner)) {
            miner.incrementStat(Stats.USED.getOrCreateStat(lv));
        }
    }

    public boolean isSuitableFor(BlockState state) {
        return this.getItem().isSuitableFor(state);
    }

    public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand) {
        return this.getItem().useOnEntity(this, user, entity, hand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack lv = new ItemStack(this.getItem(), this.count);
        lv.setBobbingAnimationTime(this.getBobbingAnimationTime());
        if (this.nbt != null) {
            lv.nbt = this.nbt.copy();
        }
        return lv;
    }

    public ItemStack copyWithCount(int count) {
        ItemStack lv = this.copy();
        lv.setCount(count);
        return lv;
    }

    public static boolean areNbtEqual(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (left.nbt == null && right.nbt != null) {
            return false;
        }
        return left.nbt == null || left.nbt.equals(right.nbt);
    }

    public static boolean areEqual(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        return left.isEqual(right);
    }

    private boolean isEqual(ItemStack stack) {
        if (this.count != stack.count) {
            return false;
        }
        if (!this.isOf(stack.getItem())) {
            return false;
        }
        if (this.nbt == null && stack.nbt != null) {
            return false;
        }
        return this.nbt == null || this.nbt.equals(stack.nbt);
    }

    public static boolean areItemsEqual(ItemStack left, ItemStack right) {
        if (left == right) {
            return true;
        }
        if (!left.isEmpty() && !right.isEmpty()) {
            return left.isItemEqual(right);
        }
        return false;
    }

    public boolean isItemEqual(ItemStack stack) {
        return !stack.isEmpty() && this.isOf(stack.getItem());
    }

    public static boolean canCombine(ItemStack stack, ItemStack otherStack) {
        return stack.isOf(otherStack.getItem()) && ItemStack.areNbtEqual(stack, otherStack);
    }

    public String getTranslationKey() {
        return this.getItem().getTranslationKey(this);
    }

    public String toString() {
        return this.count + " " + this.getItem();
    }

    public void inventoryTick(World world, Entity entity, int slot, boolean selected) {
        if (this.bobbingAnimationTime > 0) {
            --this.bobbingAnimationTime;
        }
        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, world, entity, slot, selected);
        }
    }

    public void onCraft(World world, PlayerEntity player, int amount) {
        player.increaseStat(Stats.CRAFTED.getOrCreateStat(this.getItem()), amount);
        this.getItem().onCraft(this, world, player);
    }

    public int getMaxUseTime() {
        return this.getItem().getMaxUseTime(this);
    }

    public UseAction getUseAction() {
        return this.getItem().getUseAction(this);
    }

    public void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks) {
        this.getItem().onStoppedUsing(this, world, user, remainingUseTicks);
    }

    public boolean isUsedOnRelease() {
        return this.getItem().isUsedOnRelease(this);
    }

    public boolean hasNbt() {
        return !this.empty && this.nbt != null && !this.nbt.isEmpty();
    }

    @Nullable
    public NbtCompound getNbt() {
        return this.nbt;
    }

    public NbtCompound getOrCreateNbt() {
        if (this.nbt == null) {
            this.setNbt(new NbtCompound());
        }
        return this.nbt;
    }

    public NbtCompound getOrCreateSubNbt(String key) {
        if (this.nbt == null || !this.nbt.contains(key, NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = new NbtCompound();
            this.setSubNbt(key, lv);
            return lv;
        }
        return this.nbt.getCompound(key);
    }

    @Nullable
    public NbtCompound getSubNbt(String key) {
        if (this.nbt == null || !this.nbt.contains(key, NbtElement.COMPOUND_TYPE)) {
            return null;
        }
        return this.nbt.getCompound(key);
    }

    public void removeSubNbt(String key) {
        if (this.nbt != null && this.nbt.contains(key)) {
            this.nbt.remove(key);
            if (this.nbt.isEmpty()) {
                this.nbt = null;
            }
        }
    }

    public NbtList getEnchantments() {
        if (this.nbt != null) {
            return this.nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        }
        return new NbtList();
    }

    public void setNbt(@Nullable NbtCompound nbt) {
        this.nbt = nbt;
        if (this.getItem().isDamageable()) {
            this.setDamage(this.getDamage());
        }
        if (nbt != null) {
            this.getItem().postProcessNbt(nbt);
        }
    }

    public Text getName() {
        NbtCompound lv = this.getSubNbt(DISPLAY_KEY);
        if (lv != null && lv.contains(NAME_KEY, NbtElement.STRING_TYPE)) {
            try {
                MutableText lv2 = Text.Serializer.fromJson(lv.getString(NAME_KEY));
                if (lv2 != null) {
                    return lv2;
                }
                lv.remove(NAME_KEY);
            }
            catch (Exception exception) {
                lv.remove(NAME_KEY);
            }
        }
        return this.getItem().getName(this);
    }

    public ItemStack setCustomName(@Nullable Text name) {
        NbtCompound lv = this.getOrCreateSubNbt(DISPLAY_KEY);
        if (name != null) {
            lv.putString(NAME_KEY, Text.Serializer.toJson(name));
        } else {
            lv.remove(NAME_KEY);
        }
        return this;
    }

    public void removeCustomName() {
        NbtCompound lv = this.getSubNbt(DISPLAY_KEY);
        if (lv != null) {
            lv.remove(NAME_KEY);
            if (lv.isEmpty()) {
                this.removeSubNbt(DISPLAY_KEY);
            }
        }
        if (this.nbt != null && this.nbt.isEmpty()) {
            this.nbt = null;
        }
    }

    public boolean hasCustomName() {
        NbtCompound lv = this.getSubNbt(DISPLAY_KEY);
        return lv != null && lv.contains(NAME_KEY, NbtElement.STRING_TYPE);
    }

    public List<Text> getTooltip(@Nullable PlayerEntity player, TooltipContext context) {
        int i;
        Integer integer;
        ArrayList<Text> list = Lists.newArrayList();
        MutableText lv = Text.empty().append(this.getName()).formatted(this.getRarity().formatting);
        if (this.hasCustomName()) {
            lv.formatted(Formatting.ITALIC);
        }
        list.add(lv);
        if (!context.isAdvanced() && !this.hasCustomName() && this.isOf(Items.FILLED_MAP) && (integer = FilledMapItem.getMapId(this)) != null) {
            list.add(Text.literal("#" + integer).formatted(Formatting.GRAY));
        }
        if (ItemStack.isSectionVisible(i = this.getHideFlags(), TooltipSection.ADDITIONAL)) {
            this.getItem().appendTooltip(this, player == null ? null : player.world, list, context);
        }
        if (this.hasNbt()) {
            if (ItemStack.isSectionVisible(i, TooltipSection.UPGRADES) && player != null) {
                ArmorTrim.appendTooltip(this, player.world.getRegistryManager(), list);
            }
            if (ItemStack.isSectionVisible(i, TooltipSection.ENCHANTMENTS)) {
                ItemStack.appendEnchantments(list, this.getEnchantments());
            }
            if (this.nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) {
                NbtCompound lv2 = this.nbt.getCompound(DISPLAY_KEY);
                if (ItemStack.isSectionVisible(i, TooltipSection.DYE) && lv2.contains(COLOR_KEY, NbtElement.NUMBER_TYPE)) {
                    if (context.isAdvanced()) {
                        list.add(Text.translatable("item.color", String.format(Locale.ROOT, "#%06X", lv2.getInt(COLOR_KEY))).formatted(Formatting.GRAY));
                    } else {
                        list.add(Text.translatable("item.dyed").formatted(Formatting.GRAY, Formatting.ITALIC));
                    }
                }
                if (lv2.getType(LORE_KEY) == NbtElement.LIST_TYPE) {
                    NbtList lv3 = lv2.getList(LORE_KEY, NbtElement.STRING_TYPE);
                    for (int j = 0; j < lv3.size(); ++j) {
                        String string = lv3.getString(j);
                        try {
                            MutableText lv4 = Text.Serializer.fromJson(string);
                            if (lv4 == null) continue;
                            list.add(Texts.setStyleIfAbsent(lv4, LORE_STYLE));
                            continue;
                        }
                        catch (Exception exception) {
                            lv2.remove(LORE_KEY);
                        }
                    }
                }
            }
        }
        if (ItemStack.isSectionVisible(i, TooltipSection.MODIFIERS)) {
            for (EquipmentSlot lv5 : EquipmentSlot.values()) {
                Multimap<EntityAttribute, EntityAttributeModifier> multimap = this.getAttributeModifiers(lv5);
                if (multimap.isEmpty()) continue;
                list.add(ScreenTexts.EMPTY);
                list.add(Text.translatable("item.modifiers." + lv5.getName()).formatted(Formatting.GRAY));
                for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
                    EntityAttributeModifier lv6 = entry.getValue();
                    double d = lv6.getValue();
                    boolean bl = false;
                    if (player != null) {
                        if (lv6.getId() == Item.ATTACK_DAMAGE_MODIFIER_ID) {
                            d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                            d += (double)EnchantmentHelper.getAttackDamage(this, EntityGroup.DEFAULT);
                            bl = true;
                        } else if (lv6.getId() == Item.ATTACK_SPEED_MODIFIER_ID) {
                            d += player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);
                            bl = true;
                        }
                    }
                    double e = lv6.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || lv6.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL ? d * 100.0 : (entry.getKey().equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? d * 10.0 : d);
                    if (bl) {
                        list.add(ScreenTexts.space().append(Text.translatable("attribute.modifier.equals." + lv6.getOperation().getId(), MODIFIER_FORMAT.format(e), Text.translatable(entry.getKey().getTranslationKey()))).formatted(Formatting.DARK_GREEN));
                        continue;
                    }
                    if (d > 0.0) {
                        list.add(Text.translatable("attribute.modifier.plus." + lv6.getOperation().getId(), MODIFIER_FORMAT.format(e), Text.translatable(entry.getKey().getTranslationKey())).formatted(Formatting.BLUE));
                        continue;
                    }
                    if (!(d < 0.0)) continue;
                    list.add(Text.translatable("attribute.modifier.take." + lv6.getOperation().getId(), MODIFIER_FORMAT.format(e *= -1.0), Text.translatable(entry.getKey().getTranslationKey())).formatted(Formatting.RED));
                }
            }
        }
        if (this.hasNbt()) {
            NbtList lv7;
            if (ItemStack.isSectionVisible(i, TooltipSection.UNBREAKABLE) && this.nbt.getBoolean(UNBREAKABLE_KEY)) {
                list.add(Text.translatable("item.unbreakable").formatted(Formatting.BLUE));
            }
            if (ItemStack.isSectionVisible(i, TooltipSection.CAN_DESTROY) && this.nbt.contains(CAN_DESTROY_KEY, NbtElement.LIST_TYPE) && !(lv7 = this.nbt.getList(CAN_DESTROY_KEY, NbtElement.STRING_TYPE)).isEmpty()) {
                list.add(ScreenTexts.EMPTY);
                list.add(Text.translatable("item.canBreak").formatted(Formatting.GRAY));
                for (int k = 0; k < lv7.size(); ++k) {
                    list.addAll(ItemStack.parseBlockTag(lv7.getString(k)));
                }
            }
            if (ItemStack.isSectionVisible(i, TooltipSection.CAN_PLACE) && this.nbt.contains(CAN_PLACE_ON_KEY, NbtElement.LIST_TYPE) && !(lv7 = this.nbt.getList(CAN_PLACE_ON_KEY, NbtElement.STRING_TYPE)).isEmpty()) {
                list.add(ScreenTexts.EMPTY);
                list.add(Text.translatable("item.canPlace").formatted(Formatting.GRAY));
                for (int k = 0; k < lv7.size(); ++k) {
                    list.addAll(ItemStack.parseBlockTag(lv7.getString(k)));
                }
            }
        }
        if (context.isAdvanced()) {
            if (this.isDamaged()) {
                list.add(Text.translatable("item.durability", this.getMaxDamage() - this.getDamage(), this.getMaxDamage()));
            }
            list.add(Text.literal(Registries.ITEM.getId(this.getItem()).toString()).formatted(Formatting.DARK_GRAY));
            if (this.hasNbt()) {
                list.add(Text.translatable("item.nbt_tags", this.nbt.getKeys().size()).formatted(Formatting.DARK_GRAY));
            }
        }
        if (player != null && !this.getItem().isEnabled(player.getWorld().getEnabledFeatures())) {
            list.add(DISABLED_TEXT);
        }
        return list;
    }

    private static boolean isSectionVisible(int flags, TooltipSection tooltipSection) {
        return (flags & tooltipSection.getFlag()) == 0;
    }

    private int getHideFlags() {
        if (this.hasNbt() && this.nbt.contains(HIDE_FLAGS_KEY, NbtElement.NUMBER_TYPE)) {
            return this.nbt.getInt(HIDE_FLAGS_KEY);
        }
        return 0;
    }

    public void addHideFlag(TooltipSection tooltipSection) {
        NbtCompound lv = this.getOrCreateNbt();
        lv.putInt(HIDE_FLAGS_KEY, lv.getInt(HIDE_FLAGS_KEY) | tooltipSection.getFlag());
    }

    public static void appendEnchantments(List<Text> tooltip, NbtList enchantments) {
        for (int i = 0; i < enchantments.size(); ++i) {
            NbtCompound lv = enchantments.getCompound(i);
            Registries.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(lv)).ifPresent(e -> tooltip.add(e.getName(EnchantmentHelper.getLevelFromNbt(lv))));
        }
    }

    private static Collection<Text> parseBlockTag(String tag) {
        try {
            return BlockArgumentParser.blockOrTag(Registries.BLOCK.getReadOnlyWrapper(), tag, true).map(arg -> Lists.newArrayList(arg.blockState().getBlock().getName().formatted(Formatting.DARK_GRAY)), arg2 -> arg2.tag().stream().map(arg -> ((Block)arg.value()).getName().formatted(Formatting.DARK_GRAY)).collect(Collectors.toList()));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return Lists.newArrayList(Text.literal("missingno").formatted(Formatting.DARK_GRAY));
        }
    }

    public boolean hasGlint() {
        return this.getItem().hasGlint(this);
    }

    public Rarity getRarity() {
        return this.getItem().getRarity(this);
    }

    public boolean isEnchantable() {
        if (!this.getItem().isEnchantable(this)) {
            return false;
        }
        return !this.hasEnchantments();
    }

    public void addEnchantment(Enchantment enchantment, int level) {
        this.getOrCreateNbt();
        if (!this.nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            this.nbt.put(ENCHANTMENTS_KEY, new NbtList());
        }
        NbtList lv = this.nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        lv.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchantment), (byte)level));
    }

    public boolean hasEnchantments() {
        if (this.nbt != null && this.nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            return !this.nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE).isEmpty();
        }
        return false;
    }

    public void setSubNbt(String key, NbtElement element) {
        this.getOrCreateNbt().put(key, element);
    }

    public boolean isInFrame() {
        return this.holder instanceof ItemFrameEntity;
    }

    public void setHolder(@Nullable Entity holder) {
        this.holder = holder;
    }

    @Nullable
    public ItemFrameEntity getFrame() {
        return this.holder instanceof ItemFrameEntity ? (ItemFrameEntity)this.getHolder() : null;
    }

    @Nullable
    public Entity getHolder() {
        return !this.empty ? this.holder : null;
    }

    public int getRepairCost() {
        if (this.hasNbt() && this.nbt.contains(REPAIR_COST_KEY, NbtElement.INT_TYPE)) {
            return this.nbt.getInt(REPAIR_COST_KEY);
        }
        return 0;
    }

    public void setRepairCost(int repairCost) {
        this.getOrCreateNbt().putInt(REPAIR_COST_KEY, repairCost);
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> multimap;
        if (this.hasNbt() && this.nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
            multimap = HashMultimap.create();
            NbtList lv = this.nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv.size(); ++i) {
                EntityAttributeModifier lv3;
                Optional<EntityAttribute> optional;
                NbtCompound lv2 = lv.getCompound(i);
                if (lv2.contains("Slot", NbtElement.STRING_TYPE) && !lv2.getString("Slot").equals(slot.getName()) || !(optional = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(lv2.getString("AttributeName")))).isPresent() || (lv3 = EntityAttributeModifier.fromNbt(lv2)) == null || lv3.getId().getLeastSignificantBits() == 0L || lv3.getId().getMostSignificantBits() == 0L) continue;
                multimap.put(optional.get(), lv3);
            }
        } else {
            multimap = this.getItem().getAttributeModifiers(slot);
        }
        return multimap;
    }

    public void addAttributeModifier(EntityAttribute attribute, EntityAttributeModifier modifier, @Nullable EquipmentSlot slot) {
        this.getOrCreateNbt();
        if (!this.nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
            this.nbt.put("AttributeModifiers", new NbtList());
        }
        NbtList lv = this.nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
        NbtCompound lv2 = modifier.toNbt();
        lv2.putString("AttributeName", Registries.ATTRIBUTE.getId(attribute).toString());
        if (slot != null) {
            lv2.putString("Slot", slot.getName());
        }
        lv.add(lv2);
    }

    public Text toHoverableText() {
        MutableText lv = Text.empty().append(this.getName());
        if (this.hasCustomName()) {
            lv.formatted(Formatting.ITALIC);
        }
        MutableText lv2 = Texts.bracketed(lv);
        if (!this.empty) {
            lv2.formatted(this.getRarity().formatting).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(this))));
        }
        return lv2;
    }

    public boolean canPlaceOn(Registry<Block> blockRegistry, CachedBlockPosition pos) {
        if (this.placeChecker == null) {
            this.placeChecker = new BlockPredicatesChecker(CAN_PLACE_ON_KEY);
        }
        return this.placeChecker.check(this, blockRegistry, pos);
    }

    public boolean canDestroy(Registry<Block> blockRegistry, CachedBlockPosition pos) {
        if (this.destroyChecker == null) {
            this.destroyChecker = new BlockPredicatesChecker(CAN_DESTROY_KEY);
        }
        return this.destroyChecker.check(this, blockRegistry, pos);
    }

    public int getBobbingAnimationTime() {
        return this.bobbingAnimationTime;
    }

    public void setBobbingAnimationTime(int bobbingAnimationTime) {
        this.bobbingAnimationTime = bobbingAnimationTime;
    }

    public int getCount() {
        return this.empty ? 0 : this.count;
    }

    public void setCount(int count) {
        this.count = count;
        this.updateEmptyState();
    }

    public void increment(int amount) {
        this.setCount(this.count + amount);
    }

    public void decrement(int amount) {
        this.increment(-amount);
    }

    public void usageTick(World world, LivingEntity user, int remainingUseTicks) {
        this.getItem().usageTick(world, user, this, remainingUseTicks);
    }

    public void onItemEntityDestroyed(ItemEntity entity) {
        this.getItem().onItemEntityDestroyed(entity);
    }

    public boolean isFood() {
        return this.getItem().isFood();
    }

    public SoundEvent getDrinkSound() {
        return this.getItem().getDrinkSound();
    }

    public SoundEvent getEatSound() {
        return this.getItem().getEatSound();
    }

    public static enum TooltipSection {
        ENCHANTMENTS,
        MODIFIERS,
        UNBREAKABLE,
        CAN_DESTROY,
        CAN_PLACE,
        ADDITIONAL,
        DYE,
        UPGRADES;

        private final int flag = 1 << this.ordinal();

        public int getFlag() {
            return this.flag;
        }
    }
}

