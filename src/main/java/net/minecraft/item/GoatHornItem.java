/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Instrument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class GoatHornItem
extends Item {
    private static final String INSTRUMENT_KEY = "instrument";
    private final TagKey<Instrument> instrumentTag;

    public GoatHornItem(Item.Settings settings, TagKey<Instrument> instrumentTag) {
        super(settings);
        this.instrumentTag = instrumentTag;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        Optional optional = this.getInstrument(stack).flatMap(RegistryEntry::getKey);
        if (optional.isPresent()) {
            MutableText lv = Text.translatable(Util.createTranslationKey(INSTRUMENT_KEY, ((RegistryKey)optional.get()).getValue()));
            tooltip.add(lv.formatted(Formatting.GRAY));
        }
    }

    public static ItemStack getStackForInstrument(Item item, RegistryEntry<Instrument> instrument) {
        ItemStack lv = new ItemStack(item);
        GoatHornItem.setInstrument(lv, instrument);
        return lv;
    }

    public static void setRandomInstrumentFromTag(ItemStack stack, TagKey<Instrument> instrumentTag, Random random) {
        Optional optional = Registries.INSTRUMENT.getEntryList(instrumentTag).flatMap(entryList -> entryList.getRandom(random));
        optional.ifPresent(instrument -> GoatHornItem.setInstrument(stack, instrument));
    }

    private static void setInstrument(ItemStack stack, RegistryEntry<Instrument> instrument) {
        NbtCompound lv = stack.getOrCreateNbt();
        lv.putString(INSTRUMENT_KEY, instrument.getKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).getValue().toString());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        Optional<? extends RegistryEntry<Instrument>> optional = this.getInstrument(lv);
        if (optional.isPresent()) {
            Instrument lv2 = optional.get().value();
            user.setCurrentHand(hand);
            GoatHornItem.playSound(world, user, lv2);
            user.getItemCooldownManager().set(this, lv2.useDuration());
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.consume(lv);
        }
        return TypedActionResult.fail(lv);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        Optional<? extends RegistryEntry<Instrument>> optional = this.getInstrument(stack);
        return optional.map(instrument -> ((Instrument)instrument.value()).useDuration()).orElse(0);
    }

    private Optional<? extends RegistryEntry<Instrument>> getInstrument(ItemStack stack) {
        Identifier lv2;
        NbtCompound lv = stack.getNbt();
        if (lv != null && lv.contains(INSTRUMENT_KEY, NbtElement.STRING_TYPE) && (lv2 = Identifier.tryParse(lv.getString(INSTRUMENT_KEY))) != null) {
            return Registries.INSTRUMENT.getEntry(RegistryKey.of(RegistryKeys.INSTRUMENT, lv2));
        }
        Iterator<RegistryEntry<Instrument>> iterator = Registries.INSTRUMENT.iterateEntries(this.instrumentTag).iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    private static void playSound(World world, PlayerEntity player, Instrument instrument) {
        SoundEvent lv = instrument.soundEvent().value();
        float f = instrument.range() / 16.0f;
        world.playSoundFromEntity(player, player, lv, SoundCategory.RECORDS, f, 1.0f);
        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }
}

