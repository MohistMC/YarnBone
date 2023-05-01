/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayerEntity
extends PlayerEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29769 = 32;
    private static final int field_29770 = 10;
    public ServerPlayNetworkHandler networkHandler;
    public final MinecraftServer server;
    public final ServerPlayerInteractionManager interactionManager;
    private final PlayerAdvancementTracker advancementTracker;
    private final ServerStatHandler statHandler;
    private float lastHealthScore = Float.MIN_VALUE;
    private int lastFoodScore = Integer.MIN_VALUE;
    private int lastAirScore = Integer.MIN_VALUE;
    private int lastArmorScore = Integer.MIN_VALUE;
    private int lastLevelScore = Integer.MIN_VALUE;
    private int lastExperienceScore = Integer.MIN_VALUE;
    private float syncedHealth = -1.0E8f;
    private int syncedFoodLevel = -99999999;
    private boolean syncedSaturationIsZero = true;
    private int syncedExperience = -99999999;
    private int joinInvulnerabilityTicks = 60;
    private ChatVisibility clientChatVisibility = ChatVisibility.FULL;
    private boolean clientChatColorsEnabled = true;
    private long lastActionTime = Util.getMeasuringTimeMs();
    @Nullable
    private Entity cameraEntity;
    private boolean inTeleportationState;
    private boolean seenCredits;
    private final ServerRecipeBook recipeBook = new ServerRecipeBook();
    @Nullable
    private Vec3d levitationStartPos;
    private int levitationStartTick;
    private boolean disconnected;
    @Nullable
    private Vec3d fallStartPos;
    @Nullable
    private Vec3d enteredNetherPos;
    @Nullable
    private Vec3d vehicleInLavaRidingPos;
    private ChunkSectionPos watchedSection = ChunkSectionPos.from(0, 0, 0);
    private RegistryKey<World> spawnPointDimension = World.OVERWORLD;
    @Nullable
    private BlockPos spawnPointPosition;
    private boolean spawnForced;
    private float spawnAngle;
    private final TextStream textStream;
    private boolean filterText;
    private boolean allowServerListing = true;
    private SculkShriekerWarningManager sculkShriekerWarningManager = new SculkShriekerWarningManager(0, 0, 0);
    private final ScreenHandlerSyncHandler screenHandlerSyncHandler = new ScreenHandlerSyncHandler(){

        @Override
        public void updateState(ScreenHandler handler, DefaultedList<ItemStack> stacks, ItemStack cursorStack, int[] properties) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new InventoryS2CPacket(handler.syncId, handler.nextRevision(), stacks, cursorStack));
            for (int i = 0; i < properties.length; ++i) {
                this.sendPropertyUpdate(handler, i, properties[i]);
            }
        }

        @Override
        public void updateSlot(ScreenHandler handler, int slot, ItemStack stack) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), slot, stack));
        }

        @Override
        public void updateCursorStack(ScreenHandler handler, ItemStack stack) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, handler.nextRevision(), -1, stack));
        }

        @Override
        public void updateProperty(ScreenHandler handler, int property, int value) {
            this.sendPropertyUpdate(handler, property, value);
        }

        private void sendPropertyUpdate(ScreenHandler handler, int property, int value) {
            ServerPlayerEntity.this.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(handler.syncId, property, value));
        }
    };
    private final ScreenHandlerListener screenHandlerListener = new ScreenHandlerListener(){

        @Override
        public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
            Slot lv = handler.getSlot(slotId);
            if (lv instanceof CraftingResultSlot) {
                return;
            }
            if (lv.inventory == ServerPlayerEntity.this.getInventory()) {
                Criteria.INVENTORY_CHANGED.trigger(ServerPlayerEntity.this, ServerPlayerEntity.this.getInventory(), stack);
            }
        }

        @Override
        public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
        }
    };
    @Nullable
    private PublicPlayerSession session;
    private int screenHandlerSyncId;
    public int pingMilliseconds;
    public boolean notInAnyWorld;

    public ServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
        this.textStream = server.createFilterer(this);
        this.interactionManager = server.getPlayerInteractionManager(this);
        this.server = server;
        this.statHandler = server.getPlayerManager().createStatHandler(this);
        this.advancementTracker = server.getPlayerManager().getAdvancementTracker(this);
        this.setStepHeight(1.0f);
        this.moveToSpawn(world);
    }

    private void moveToSpawn(ServerWorld world) {
        BlockPos lv = world.getSpawnPos();
        if (world.getDimension().hasSkyLight() && world.getServer().getSaveProperties().getGameMode() != GameMode.ADVENTURE) {
            long l;
            long m;
            int i = Math.max(0, this.server.getSpawnRadius(world));
            int j = MathHelper.floor(world.getWorldBorder().getDistanceInsideBorder(lv.getX(), lv.getZ()));
            if (j < i) {
                i = j;
            }
            if (j <= 1) {
                i = 1;
            }
            int k = (m = (l = (long)(i * 2 + 1)) * l) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)m;
            int n = this.calculateSpawnOffsetMultiplier(k);
            int o = Random.create().nextInt(k);
            for (int p = 0; p < k; ++p) {
                int q = (o + n * p) % k;
                int r = q % (i * 2 + 1);
                int s = q / (i * 2 + 1);
                BlockPos lv2 = SpawnLocating.findOverworldSpawn(world, lv.getX() + r - i, lv.getZ() + s - i);
                if (lv2 == null) continue;
                this.refreshPositionAndAngles(lv2, 0.0f, 0.0f);
                if (!world.isSpaceEmpty(this)) {
                    continue;
                }
                break;
            }
        } else {
            this.refreshPositionAndAngles(lv, 0.0f, 0.0f);
            while (!world.isSpaceEmpty(this) && this.getY() < (double)(world.getTopY() - 1)) {
                this.setPosition(this.getX(), this.getY() + 1.0, this.getZ());
            }
        }
    }

    private int calculateSpawnOffsetMultiplier(int horizontalSpawnArea) {
        return horizontalSpawnArea <= 16 ? horizontalSpawnArea - 1 : 17;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("warden_spawn_tracker", NbtElement.COMPOUND_TYPE)) {
            SculkShriekerWarningManager.CODEC.parse(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("warden_spawn_tracker"))).resultOrPartial(LOGGER::error).ifPresent(sculkShriekerWarningManager -> {
                this.sculkShriekerWarningManager = sculkShriekerWarningManager;
            });
        }
        if (nbt.contains("enteredNetherPosition", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv = nbt.getCompound("enteredNetherPosition");
            this.enteredNetherPos = new Vec3d(lv.getDouble("x"), lv.getDouble("y"), lv.getDouble("z"));
        }
        this.seenCredits = nbt.getBoolean("seenCredits");
        if (nbt.contains("recipeBook", NbtElement.COMPOUND_TYPE)) {
            this.recipeBook.readNbt(nbt.getCompound("recipeBook"), this.server.getRecipeManager());
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        if (nbt.contains("SpawnX", NbtElement.NUMBER_TYPE) && nbt.contains("SpawnY", NbtElement.NUMBER_TYPE) && nbt.contains("SpawnZ", NbtElement.NUMBER_TYPE)) {
            this.spawnPointPosition = new BlockPos(nbt.getInt("SpawnX"), nbt.getInt("SpawnY"), nbt.getInt("SpawnZ"));
            this.spawnForced = nbt.getBoolean("SpawnForced");
            this.spawnAngle = nbt.getFloat("SpawnAngle");
            if (nbt.contains("SpawnDimension")) {
                this.spawnPointDimension = World.CODEC.parse(NbtOps.INSTANCE, nbt.get("SpawnDimension")).resultOrPartial(LOGGER::error).orElse(World.OVERWORLD);
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        SculkShriekerWarningManager.CODEC.encodeStart(NbtOps.INSTANCE, this.sculkShriekerWarningManager).resultOrPartial(LOGGER::error).ifPresent(encoded -> nbt.put("warden_spawn_tracker", (NbtElement)encoded));
        this.writeGameModeNbt(nbt);
        nbt.putBoolean("seenCredits", this.seenCredits);
        if (this.enteredNetherPos != null) {
            NbtCompound lv = new NbtCompound();
            lv.putDouble("x", this.enteredNetherPos.x);
            lv.putDouble("y", this.enteredNetherPos.y);
            lv.putDouble("z", this.enteredNetherPos.z);
            nbt.put("enteredNetherPosition", lv);
        }
        Entity lv2 = this.getRootVehicle();
        Entity lv3 = this.getVehicle();
        if (lv3 != null && lv2 != this && lv2.hasPlayerRider()) {
            NbtCompound lv4 = new NbtCompound();
            NbtCompound lv5 = new NbtCompound();
            lv2.saveNbt(lv5);
            lv4.putUuid("Attach", lv3.getUuid());
            lv4.put("Entity", lv5);
            nbt.put("RootVehicle", lv4);
        }
        nbt.put("recipeBook", this.recipeBook.toNbt());
        nbt.putString("Dimension", this.world.getRegistryKey().getValue().toString());
        if (this.spawnPointPosition != null) {
            nbt.putInt("SpawnX", this.spawnPointPosition.getX());
            nbt.putInt("SpawnY", this.spawnPointPosition.getY());
            nbt.putInt("SpawnZ", this.spawnPointPosition.getZ());
            nbt.putBoolean("SpawnForced", this.spawnForced);
            nbt.putFloat("SpawnAngle", this.spawnAngle);
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPointDimension.getValue()).resultOrPartial(LOGGER::error).ifPresent(encoded -> nbt.put("SpawnDimension", (NbtElement)encoded));
        }
    }

    public void setExperiencePoints(int points) {
        float f = this.getNextLevelExperience();
        float g = (f - 1.0f) / f;
        this.experienceProgress = MathHelper.clamp((float)points / f, 0.0f, g);
        this.syncedExperience = -1;
    }

    public void setExperienceLevel(int level) {
        this.experienceLevel = level;
        this.syncedExperience = -1;
    }

    @Override
    public void addExperienceLevels(int levels) {
        super.addExperienceLevels(levels);
        this.syncedExperience = -1;
    }

    @Override
    public void applyEnchantmentCosts(ItemStack enchantedItem, int experienceLevels) {
        super.applyEnchantmentCosts(enchantedItem, experienceLevels);
        this.syncedExperience = -1;
    }

    private void onScreenHandlerOpened(ScreenHandler screenHandler) {
        screenHandler.addListener(this.screenHandlerListener);
        screenHandler.updateSyncHandler(this.screenHandlerSyncHandler);
    }

    public void onSpawn() {
        this.onScreenHandlerOpened(this.playerScreenHandler);
    }

    @Override
    public void enterCombat() {
        super.enterCombat();
        this.networkHandler.sendPacket(new EnterCombatS2CPacket());
    }

    @Override
    public void endCombat() {
        super.endCombat();
        this.networkHandler.sendPacket(new EndCombatS2CPacket(this.getDamageTracker()));
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        Criteria.ENTER_BLOCK.trigger(this, state);
    }

    @Override
    protected ItemCooldownManager createCooldownManager() {
        return new ServerItemCooldownManager(this);
    }

    @Override
    public void tick() {
        Entity lv;
        this.interactionManager.update();
        this.sculkShriekerWarningManager.tick();
        --this.joinInvulnerabilityTicks;
        if (this.timeUntilRegen > 0) {
            --this.timeUntilRegen;
        }
        this.currentScreenHandler.sendContentUpdates();
        if (!this.world.isClient && !this.currentScreenHandler.canUse(this)) {
            this.closeHandledScreen();
            this.currentScreenHandler = this.playerScreenHandler;
        }
        if ((lv = this.getCameraEntity()) != this) {
            if (lv.isAlive()) {
                this.updatePositionAndAngles(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch());
                this.getWorld().getChunkManager().updatePosition(this);
                if (this.shouldDismount()) {
                    this.setCameraEntity(this);
                }
            } else {
                this.setCameraEntity(this);
            }
        }
        Criteria.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            Criteria.LEVITATION.trigger(this, this.levitationStartPos, this.age - this.levitationStartTick);
        }
        this.tickFallStartPos();
        this.tickVehicleInLavaRiding();
        this.advancementTracker.sendUpdate(this);
    }

    public void playerTick() {
        try {
            if (!this.isSpectator() || !this.isRegionUnloaded()) {
                super.tick();
            }
            for (int i = 0; i < this.getInventory().size(); ++i) {
                Packet<?> lv2;
                ItemStack lv = this.getInventory().getStack(i);
                if (!lv.getItem().isNetworkSynced() || (lv2 = ((NetworkSyncedItem)lv.getItem()).createSyncPacket(lv, this.world, this)) == null) continue;
                this.networkHandler.sendPacket(lv2);
            }
            if (this.getHealth() != this.syncedHealth || this.syncedFoodLevel != this.hungerManager.getFoodLevel() || this.hungerManager.getSaturationLevel() == 0.0f != this.syncedSaturationIsZero) {
                this.networkHandler.sendPacket(new HealthUpdateS2CPacket(this.getHealth(), this.hungerManager.getFoodLevel(), this.hungerManager.getSaturationLevel()));
                this.syncedHealth = this.getHealth();
                this.syncedFoodLevel = this.hungerManager.getFoodLevel();
                boolean bl = this.syncedSaturationIsZero = this.hungerManager.getSaturationLevel() == 0.0f;
            }
            if (this.getHealth() + this.getAbsorptionAmount() != this.lastHealthScore) {
                this.lastHealthScore = this.getHealth() + this.getAbsorptionAmount();
                this.updateScores(ScoreboardCriterion.HEALTH, MathHelper.ceil(this.lastHealthScore));
            }
            if (this.hungerManager.getFoodLevel() != this.lastFoodScore) {
                this.lastFoodScore = this.hungerManager.getFoodLevel();
                this.updateScores(ScoreboardCriterion.FOOD, MathHelper.ceil(this.lastFoodScore));
            }
            if (this.getAir() != this.lastAirScore) {
                this.lastAirScore = this.getAir();
                this.updateScores(ScoreboardCriterion.AIR, MathHelper.ceil(this.lastAirScore));
            }
            if (this.getArmor() != this.lastArmorScore) {
                this.lastArmorScore = this.getArmor();
                this.updateScores(ScoreboardCriterion.ARMOR, MathHelper.ceil(this.lastArmorScore));
            }
            if (this.totalExperience != this.lastExperienceScore) {
                this.lastExperienceScore = this.totalExperience;
                this.updateScores(ScoreboardCriterion.XP, MathHelper.ceil(this.lastExperienceScore));
            }
            if (this.experienceLevel != this.lastLevelScore) {
                this.lastLevelScore = this.experienceLevel;
                this.updateScores(ScoreboardCriterion.LEVEL, MathHelper.ceil(this.lastLevelScore));
            }
            if (this.totalExperience != this.syncedExperience) {
                this.syncedExperience = this.totalExperience;
                this.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }
            if (this.age % 20 == 0) {
                Criteria.LOCATION.trigger(this);
            }
        }
        catch (Throwable throwable) {
            CrashReport lv3 = CrashReport.create(throwable, "Ticking player");
            CrashReportSection lv4 = lv3.addElement("Player being ticked");
            this.populateCrashReport(lv4);
            throw new CrashException(lv3);
        }
    }

    @Override
    public void onLanding() {
        if (this.getHealth() > 0.0f && this.fallStartPos != null) {
            Criteria.FALL_FROM_HEIGHT.trigger(this, this.fallStartPos);
        }
        this.fallStartPos = null;
        super.onLanding();
    }

    public void tickFallStartPos() {
        if (this.fallDistance > 0.0f && this.fallStartPos == null) {
            this.fallStartPos = this.getPos();
        }
    }

    public void tickVehicleInLavaRiding() {
        if (this.getVehicle() != null && this.getVehicle().isInLava()) {
            if (this.vehicleInLavaRidingPos == null) {
                this.vehicleInLavaRidingPos = this.getPos();
            } else {
                Criteria.RIDE_ENTITY_IN_LAVA.trigger(this, this.vehicleInLavaRidingPos);
            }
        }
        if (!(this.vehicleInLavaRidingPos == null || this.getVehicle() != null && this.getVehicle().isInLava())) {
            this.vehicleInLavaRidingPos = null;
        }
    }

    private void updateScores(ScoreboardCriterion criterion, int score2) {
        this.getScoreboard().forEachScore(criterion, this.getEntityName(), score -> score.setScore(score2));
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        this.emitGameEvent(GameEvent.ENTITY_DIE);
        boolean bl = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        if (bl) {
            Text lv = this.getDamageTracker().getDeathMessage();
            this.networkHandler.sendPacket(new DeathMessageS2CPacket(this.getDamageTracker(), lv), PacketCallbacks.of(() -> {
                int i = 256;
                String string = lv.asTruncatedString(256);
                MutableText lv = Text.translatable("death.attack.message_too_long", Text.literal(string).formatted(Formatting.YELLOW));
                MutableText lv2 = Text.translatable("death.attack.even_more_magic", this.getDisplayName()).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lv)));
                return new DeathMessageS2CPacket(this.getDamageTracker(), lv2);
            }));
            AbstractTeam lv2 = this.getScoreboardTeam();
            if (lv2 == null || lv2.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.ALWAYS) {
                this.server.getPlayerManager().broadcast(lv, false);
            } else if (lv2.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerManager().sendToTeam(this, lv);
            } else if (lv2.getDeathMessageVisibilityRule() == AbstractTeam.VisibilityRule.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerManager().sendToOtherTeams(this, lv);
            }
        } else {
            this.networkHandler.sendPacket(new DeathMessageS2CPacket(this.getDamageTracker(), ScreenTexts.EMPTY));
        }
        this.dropShoulderEntities();
        if (this.world.getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.forgiveMobAnger();
        }
        if (!this.isSpectator()) {
            this.drop(damageSource);
        }
        this.getScoreboard().forEachScore(ScoreboardCriterion.DEATH_COUNT, this.getEntityName(), ScoreboardPlayerScore::incrementScore);
        LivingEntity lv3 = this.getPrimeAdversary();
        if (lv3 != null) {
            this.incrementStat(Stats.KILLED_BY.getOrCreateStat(lv3.getType()));
            lv3.updateKilledAdvancementCriterion(this, this.scoreAmount, damageSource);
            this.onKilledBy(lv3);
        }
        this.world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
        this.incrementStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        this.extinguish();
        this.setFrozenTicks(0);
        this.setOnFire(false);
        this.getDamageTracker().update();
        this.setLastDeathPos(Optional.of(GlobalPos.create(this.world.getRegistryKey(), this.getBlockPos())));
    }

    private void forgiveMobAnger() {
        Box lv = new Box(this.getBlockPos()).expand(32.0, 10.0, 32.0);
        this.world.getEntitiesByClass(MobEntity.class, lv, EntityPredicates.EXCEPT_SPECTATOR).stream().filter(entity -> entity instanceof Angerable).forEach(entity -> ((Angerable)((Object)entity)).forgive(this));
    }

    @Override
    public void updateKilledAdvancementCriterion(Entity entityKilled, int score, DamageSource damageSource) {
        if (entityKilled == this) {
            return;
        }
        super.updateKilledAdvancementCriterion(entityKilled, score, damageSource);
        this.addScore(score);
        String string = this.getEntityName();
        String string2 = entityKilled.getEntityName();
        this.getScoreboard().forEachScore(ScoreboardCriterion.TOTAL_KILL_COUNT, string, ScoreboardPlayerScore::incrementScore);
        if (entityKilled instanceof PlayerEntity) {
            this.incrementStat(Stats.PLAYER_KILLS);
            this.getScoreboard().forEachScore(ScoreboardCriterion.PLAYER_KILL_COUNT, string, ScoreboardPlayerScore::incrementScore);
        } else {
            this.incrementStat(Stats.MOB_KILLS);
        }
        this.updateScoreboardScore(string, string2, ScoreboardCriterion.TEAM_KILLS);
        this.updateScoreboardScore(string2, string, ScoreboardCriterion.KILLED_BY_TEAMS);
        Criteria.PLAYER_KILLED_ENTITY.trigger(this, entityKilled, damageSource);
    }

    private void updateScoreboardScore(String playerName, String team, ScoreboardCriterion[] criterions) {
        int i;
        Team lv = this.getScoreboard().getPlayerTeam(team);
        if (lv != null && (i = lv.getColor().getColorIndex()) >= 0 && i < criterions.length) {
            this.getScoreboard().forEachScore(criterions[i], playerName, ScoreboardPlayerScore::incrementScore);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        PlayerEntity lv5;
        PersistentProjectileEntity lv3;
        Entity lv4;
        PlayerEntity lv2;
        boolean bl;
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        boolean bl2 = bl = this.server.isDedicated() && this.isPvpEnabled() && source.isIn(DamageTypeTags.IS_FALL);
        if (!bl && this.joinInvulnerabilityTicks > 0 && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        Entity lv = source.getAttacker();
        if (lv instanceof PlayerEntity && !this.shouldDamagePlayer(lv2 = (PlayerEntity)lv)) {
            return false;
        }
        if (lv instanceof PersistentProjectileEntity && (lv4 = (lv3 = (PersistentProjectileEntity)lv).getOwner()) instanceof PlayerEntity && !this.shouldDamagePlayer(lv5 = (PlayerEntity)lv4)) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public boolean shouldDamagePlayer(PlayerEntity player) {
        if (!this.isPvpEnabled()) {
            return false;
        }
        return super.shouldDamagePlayer(player);
    }

    private boolean isPvpEnabled() {
        return this.server.isPvpEnabled();
    }

    @Override
    @Nullable
    protected TeleportTarget getTeleportTarget(ServerWorld destination) {
        TeleportTarget lv = super.getTeleportTarget(destination);
        if (lv != null && this.world.getRegistryKey() == World.OVERWORLD && destination.getRegistryKey() == World.END) {
            Vec3d lv2 = lv.position.add(0.0, -1.0, 0.0);
            return new TeleportTarget(lv2, Vec3d.ZERO, 90.0f, 0.0f);
        }
        return lv;
    }

    @Override
    @Nullable
    public Entity moveToWorld(ServerWorld destination) {
        this.inTeleportationState = true;
        ServerWorld lv = this.getWorld();
        RegistryKey<World> lv2 = lv.getRegistryKey();
        if (lv2 == World.END && destination.getRegistryKey() == World.OVERWORLD) {
            this.detach();
            this.getWorld().removePlayer(this, Entity.RemovalReason.CHANGED_DIMENSION);
            if (!this.notInAnyWorld) {
                this.notInAnyWorld = true;
                this.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, this.seenCredits ? GameStateChangeS2CPacket.field_33328 : 1.0f));
                this.seenCredits = true;
            }
            return this;
        }
        WorldProperties lv3 = destination.getLevelProperties();
        this.networkHandler.sendPacket(new PlayerRespawnS2CPacket(destination.getDimensionKey(), destination.getRegistryKey(), BiomeAccess.hashSeed(destination.getSeed()), this.interactionManager.getGameMode(), this.interactionManager.getPreviousGameMode(), destination.isDebugWorld(), destination.isFlat(), 3, this.getLastDeathPos()));
        this.networkHandler.sendPacket(new DifficultyS2CPacket(lv3.getDifficulty(), lv3.isDifficultyLocked()));
        PlayerManager lv4 = this.server.getPlayerManager();
        lv4.sendCommandTree(this);
        lv.removePlayer(this, Entity.RemovalReason.CHANGED_DIMENSION);
        this.unsetRemoved();
        TeleportTarget lv5 = this.getTeleportTarget(destination);
        if (lv5 != null) {
            lv.getProfiler().push("moving");
            if (lv2 == World.OVERWORLD && destination.getRegistryKey() == World.NETHER) {
                this.enteredNetherPos = this.getPos();
            } else if (destination.getRegistryKey() == World.END) {
                this.createEndSpawnPlatform(destination, BlockPos.ofFloored(lv5.position));
            }
            lv.getProfiler().pop();
            lv.getProfiler().push("placing");
            this.setWorld(destination);
            this.networkHandler.requestTeleport(lv5.position.x, lv5.position.y, lv5.position.z, lv5.yaw, lv5.pitch);
            this.networkHandler.syncWithPlayerPosition();
            destination.onPlayerChangeDimension(this);
            lv.getProfiler().pop();
            this.worldChanged(lv);
            this.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(this.getAbilities()));
            lv4.sendWorldInfo(this, destination);
            lv4.sendPlayerStatus(this);
            for (StatusEffectInstance lv6 : this.getStatusEffects()) {
                this.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), lv6));
            }
            this.networkHandler.sendPacket(new WorldEventS2CPacket(WorldEvents.TRAVEL_THROUGH_PORTAL, BlockPos.ORIGIN, 0, false));
            this.syncedExperience = -1;
            this.syncedHealth = -1.0f;
            this.syncedFoodLevel = -1;
        }
        return this;
    }

    private void createEndSpawnPlatform(ServerWorld world, BlockPos centerPos) {
        BlockPos.Mutable lv = centerPos.mutableCopy();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -1; k < 3; ++k) {
                    BlockState lv2 = k == -1 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState();
                    world.setBlockState(lv.set(centerPos).move(j, k, i), lv2);
                }
            }
        }
    }

    @Override
    protected Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder) {
        Optional<BlockLocating.Rectangle> optional = super.getPortalRect(destWorld, destPos, destIsNether, worldBorder);
        if (optional.isPresent()) {
            return optional;
        }
        Direction.Axis lv = this.world.getBlockState(this.lastNetherPortalPosition).getOrEmpty(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
        Optional<BlockLocating.Rectangle> optional2 = destWorld.getPortalForcer().createPortal(destPos, lv);
        if (!optional2.isPresent()) {
            LOGGER.error("Unable to create a portal, likely target out of worldborder");
        }
        return optional2;
    }

    private void worldChanged(ServerWorld origin) {
        RegistryKey<World> lv = origin.getRegistryKey();
        RegistryKey<World> lv2 = this.world.getRegistryKey();
        Criteria.CHANGED_DIMENSION.trigger(this, lv, lv2);
        if (lv == World.NETHER && lv2 == World.OVERWORLD && this.enteredNetherPos != null) {
            Criteria.NETHER_TRAVEL.trigger(this, this.enteredNetherPos);
        }
        if (lv2 != World.NETHER) {
            this.enteredNetherPos = null;
        }
    }

    @Override
    public boolean canBeSpectated(ServerPlayerEntity spectator) {
        if (spectator.isSpectator()) {
            return this.getCameraEntity() == this;
        }
        if (this.isSpectator()) {
            return false;
        }
        return super.canBeSpectated(spectator);
    }

    @Override
    public void sendPickup(Entity item, int count) {
        super.sendPickup(item, count);
        this.currentScreenHandler.sendContentUpdates();
    }

    @Override
    public Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos) {
        Direction lv = this.world.getBlockState(pos).get(HorizontalFacingBlock.FACING);
        if (this.isSleeping() || !this.isAlive()) {
            return Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM);
        }
        if (!this.world.getDimension().natural()) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE);
        }
        if (!this.isBedTooFarAway(pos, lv)) {
            return Either.left(PlayerEntity.SleepFailureReason.TOO_FAR_AWAY);
        }
        if (this.isBedObstructed(pos, lv)) {
            return Either.left(PlayerEntity.SleepFailureReason.OBSTRUCTED);
        }
        this.setSpawnPoint(this.world.getRegistryKey(), pos, this.getYaw(), false, true);
        if (this.world.isDay()) {
            return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW);
        }
        if (!this.isCreative()) {
            double d = 8.0;
            double e = 5.0;
            Vec3d lv2 = Vec3d.ofBottomCenter(pos);
            List<HostileEntity> list = this.world.getEntitiesByClass(HostileEntity.class, new Box(lv2.getX() - 8.0, lv2.getY() - 5.0, lv2.getZ() - 8.0, lv2.getX() + 8.0, lv2.getY() + 5.0, lv2.getZ() + 8.0), entity -> entity.isAngryAt(this));
            if (!list.isEmpty()) {
                return Either.left(PlayerEntity.SleepFailureReason.NOT_SAFE);
            }
        }
        Either<PlayerEntity.SleepFailureReason, Unit> either = super.trySleep(pos).ifRight(unit -> {
            this.incrementStat(Stats.SLEEP_IN_BED);
            Criteria.SLEPT_IN_BED.trigger(this);
        });
        if (!this.getWorld().isSleepingEnabled()) {
            this.sendMessage(Text.translatable("sleep.not_possible"), true);
        }
        ((ServerWorld)this.world).updateSleepingPlayers();
        return either;
    }

    @Override
    public void sleep(BlockPos pos) {
        this.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        super.sleep(pos);
    }

    private boolean isBedTooFarAway(BlockPos pos, Direction direction) {
        return this.isBedTooFarAway(pos) || this.isBedTooFarAway(pos.offset(direction.getOpposite()));
    }

    private boolean isBedTooFarAway(BlockPos pos) {
        Vec3d lv = Vec3d.ofBottomCenter(pos);
        return Math.abs(this.getX() - lv.getX()) <= 3.0 && Math.abs(this.getY() - lv.getY()) <= 2.0 && Math.abs(this.getZ() - lv.getZ()) <= 3.0;
    }

    private boolean isBedObstructed(BlockPos pos, Direction direction) {
        BlockPos lv = pos.up();
        return !this.doesNotSuffocate(lv) || !this.doesNotSuffocate(lv.offset(direction.getOpposite()));
    }

    @Override
    public void wakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers) {
        if (this.isSleeping()) {
            this.getWorld().getChunkManager().sendToNearbyPlayers(this, new EntityAnimationS2CPacket(this, EntityAnimationS2CPacket.WAKE_UP));
        }
        super.wakeUp(skipSleepTimer, updateSleepingPlayers);
        if (this.networkHandler != null) {
            this.networkHandler.requestTeleport(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        }
    }

    @Override
    public void requestTeleportAndDismount(double destX, double destY, double destZ) {
        this.dismountVehicle();
        this.setPosition(destX, destY, destZ);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource) || this.isInTeleportationState();
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected void applyMovementEffects(BlockPos pos) {
        if (!this.isSpectator()) {
            super.applyMovementEffects(pos);
        }
    }

    public void handleFall(double heightDifference, boolean onGround) {
        if (this.isRegionUnloaded()) {
            return;
        }
        BlockPos lv = this.getLandingPos();
        super.fall(heightDifference, onGround, this.world.getBlockState(lv), lv);
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign) {
        sign.setEditor(this.getUuid());
        this.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, sign.getPos()));
        this.networkHandler.sendPacket(new SignEditorOpenS2CPacket(sign.getPos()));
    }

    private void incrementScreenHandlerSyncId() {
        this.screenHandlerSyncId = this.screenHandlerSyncId % 100 + 1;
    }

    @Override
    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
        if (factory == null) {
            return OptionalInt.empty();
        }
        if (this.currentScreenHandler != this.playerScreenHandler) {
            this.closeHandledScreen();
        }
        this.incrementScreenHandlerSyncId();
        ScreenHandler lv = factory.createMenu(this.screenHandlerSyncId, this.getInventory(), this);
        if (lv == null) {
            if (this.isSpectator()) {
                this.sendMessage(Text.translatable("container.spectatorCantOpen").formatted(Formatting.RED), true);
            }
            return OptionalInt.empty();
        }
        this.networkHandler.sendPacket(new OpenScreenS2CPacket(lv.syncId, lv.getType(), factory.getDisplayName()));
        this.onScreenHandlerOpened(lv);
        this.currentScreenHandler = lv;
        return OptionalInt.of(this.screenHandlerSyncId);
    }

    @Override
    public void sendTradeOffers(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
        this.networkHandler.sendPacket(new SetTradeOffersS2CPacket(syncId, offers, levelProgress, experience, leveled, refreshable));
    }

    @Override
    public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
        if (this.currentScreenHandler != this.playerScreenHandler) {
            this.closeHandledScreen();
        }
        this.incrementScreenHandlerSyncId();
        this.networkHandler.sendPacket(new OpenHorseScreenS2CPacket(this.screenHandlerSyncId, inventory.size(), horse.getId()));
        this.currentScreenHandler = new HorseScreenHandler(this.screenHandlerSyncId, this.getInventory(), inventory, horse);
        this.onScreenHandlerOpened(this.currentScreenHandler);
    }

    @Override
    public void useBook(ItemStack book, Hand hand) {
        if (book.isOf(Items.WRITTEN_BOOK)) {
            if (WrittenBookItem.resolve(book, this.getCommandSource(), this)) {
                this.currentScreenHandler.sendContentUpdates();
            }
            this.networkHandler.sendPacket(new OpenWrittenBookS2CPacket(hand));
        }
    }

    @Override
    public void openCommandBlockScreen(CommandBlockBlockEntity commandBlock) {
        this.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(commandBlock, BlockEntity::createNbt));
    }

    @Override
    public void closeHandledScreen() {
        this.networkHandler.sendPacket(new CloseScreenS2CPacket(this.currentScreenHandler.syncId));
        this.onHandledScreenClosed();
    }

    @Override
    public void onHandledScreenClosed() {
        this.currentScreenHandler.onClosed(this);
        this.playerScreenHandler.copySharedSlots(this.currentScreenHandler);
        this.currentScreenHandler = this.playerScreenHandler;
    }

    public void updateInput(float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking) {
        if (this.hasVehicle()) {
            if (sidewaysSpeed >= -1.0f && sidewaysSpeed <= 1.0f) {
                this.sidewaysSpeed = sidewaysSpeed;
            }
            if (forwardSpeed >= -1.0f && forwardSpeed <= 1.0f) {
                this.forwardSpeed = forwardSpeed;
            }
            this.jumping = jumping;
            this.setSneaking(sneaking);
        }
    }

    @Override
    public void increaseStat(Stat<?> stat, int amount) {
        this.statHandler.increaseStat(this, stat, amount);
        this.getScoreboard().forEachScore(stat, this.getEntityName(), score -> score.incrementScore(amount));
    }

    @Override
    public void resetStat(Stat<?> stat) {
        this.statHandler.setStat(this, stat, 0);
        this.getScoreboard().forEachScore(stat, this.getEntityName(), ScoreboardPlayerScore::clearScore);
    }

    @Override
    public int unlockRecipes(Collection<Recipe<?>> recipes) {
        return this.recipeBook.unlockRecipes(recipes, this);
    }

    @Override
    public void unlockRecipes(Identifier[] ids) {
        ArrayList<Recipe<?>> list = Lists.newArrayList();
        for (Identifier lv : ids) {
            this.server.getRecipeManager().get(lv).ifPresent(list::add);
        }
        this.unlockRecipes(list);
    }

    @Override
    public int lockRecipes(Collection<Recipe<?>> recipes) {
        return this.recipeBook.lockRecipes(recipes, this);
    }

    @Override
    public void addExperience(int experience) {
        super.addExperience(experience);
        this.syncedExperience = -1;
    }

    public void onDisconnect() {
        this.disconnected = true;
        this.removeAllPassengers();
        if (this.isSleeping()) {
            this.wakeUp(true, false);
        }
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    public void markHealthDirty() {
        this.syncedHealth = -1.0E8f;
    }

    @Override
    public void sendMessage(Text message, boolean overlay) {
        this.sendMessageToClient(message, overlay);
    }

    @Override
    protected void consumeItem() {
        if (!this.activeItemStack.isEmpty() && this.isUsingItem()) {
            this.networkHandler.sendPacket(new EntityStatusS2CPacket(this, EntityStatuses.CONSUME_ITEM));
            super.consumeItem();
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        super.lookAt(anchorPoint, target);
        this.networkHandler.sendPacket(new LookAtS2CPacket(anchorPoint, target.x, target.y, target.z));
    }

    public void lookAtEntity(EntityAnchorArgumentType.EntityAnchor anchorPoint, Entity targetEntity, EntityAnchorArgumentType.EntityAnchor targetAnchor) {
        Vec3d lv = targetAnchor.positionAt(targetEntity);
        super.lookAt(anchorPoint, lv);
        this.networkHandler.sendPacket(new LookAtS2CPacket(anchorPoint, targetEntity, targetAnchor));
    }

    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive) {
        this.sculkShriekerWarningManager = oldPlayer.sculkShriekerWarningManager;
        this.filterText = oldPlayer.filterText;
        this.session = oldPlayer.session;
        this.interactionManager.setGameMode(oldPlayer.interactionManager.getGameMode(), oldPlayer.interactionManager.getPreviousGameMode());
        this.sendAbilitiesUpdate();
        if (alive) {
            this.getInventory().clone(oldPlayer.getInventory());
            this.setHealth(oldPlayer.getHealth());
            this.hungerManager = oldPlayer.hungerManager;
            this.experienceLevel = oldPlayer.experienceLevel;
            this.totalExperience = oldPlayer.totalExperience;
            this.experienceProgress = oldPlayer.experienceProgress;
            this.setScore(oldPlayer.getScore());
            this.lastNetherPortalPosition = oldPlayer.lastNetherPortalPosition;
        } else if (this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator()) {
            this.getInventory().clone(oldPlayer.getInventory());
            this.experienceLevel = oldPlayer.experienceLevel;
            this.totalExperience = oldPlayer.totalExperience;
            this.experienceProgress = oldPlayer.experienceProgress;
            this.setScore(oldPlayer.getScore());
        }
        this.enchantmentTableSeed = oldPlayer.enchantmentTableSeed;
        this.enderChestInventory = oldPlayer.enderChestInventory;
        this.getDataTracker().set(PLAYER_MODEL_PARTS, (Byte)oldPlayer.getDataTracker().get(PLAYER_MODEL_PARTS));
        this.syncedExperience = -1;
        this.syncedHealth = -1.0f;
        this.syncedFoodLevel = -1;
        this.recipeBook.copyFrom(oldPlayer.recipeBook);
        this.seenCredits = oldPlayer.seenCredits;
        this.enteredNetherPos = oldPlayer.enteredNetherPos;
        this.setShoulderEntityLeft(oldPlayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(oldPlayer.getShoulderEntityRight());
        this.setLastDeathPos(oldPlayer.getLastDeathPos());
    }

    @Override
    protected void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source) {
        super.onStatusEffectApplied(effect, source);
        this.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect));
        if (effect.getEffectType() == StatusEffects.LEVITATION) {
            this.levitationStartTick = this.age;
            this.levitationStartPos = this.getPos();
        }
        Criteria.EFFECTS_CHANGED.trigger(this, source);
    }

    @Override
    protected void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source) {
        super.onStatusEffectUpgraded(effect, reapplyEffect, source);
        this.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect));
        Criteria.EFFECTS_CHANGED.trigger(this, source);
    }

    @Override
    protected void onStatusEffectRemoved(StatusEffectInstance effect) {
        super.onStatusEffectRemoved(effect);
        this.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(this.getId(), effect.getEffectType()));
        if (effect.getEffectType() == StatusEffects.LEVITATION) {
            this.levitationStartPos = null;
        }
        Criteria.EFFECTS_CHANGED.trigger(this, (Entity)null);
    }

    @Override
    public void requestTeleport(double destX, double destY, double destZ) {
        this.networkHandler.requestTeleport(destX, destY, destZ, this.getYaw(), this.getPitch(), PositionFlag.ROT);
    }

    @Override
    public void requestTeleportOffset(double offsetX, double offsetY, double offsetZ) {
        this.networkHandler.requestTeleport(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, this.getYaw(), this.getPitch(), PositionFlag.VALUES);
    }

    @Override
    public boolean teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch) {
        ChunkPos lv = new ChunkPos(BlockPos.ofFloored(destX, destY, destZ));
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, lv, 1, this.getId());
        this.stopRiding();
        if (this.isSleeping()) {
            this.wakeUp(true, true);
        }
        if (world == this.world) {
            this.networkHandler.requestTeleport(destX, destY, destZ, yaw, pitch, flags);
        } else {
            this.teleport(world, destX, destY, destZ, yaw, pitch);
        }
        this.setHeadYaw(yaw);
        return true;
    }

    @Override
    public void refreshPositionAfterTeleport(double x, double y, double z) {
        this.requestTeleport(x, y, z);
        this.networkHandler.syncWithPlayerPosition();
    }

    @Override
    public void addCritParticles(Entity target) {
        this.getWorld().getChunkManager().sendToNearbyPlayers(this, new EntityAnimationS2CPacket(target, EntityAnimationS2CPacket.CRIT));
    }

    @Override
    public void addEnchantedHitParticles(Entity target) {
        this.getWorld().getChunkManager().sendToNearbyPlayers(this, new EntityAnimationS2CPacket(target, EntityAnimationS2CPacket.ENCHANTED_HIT));
    }

    @Override
    public void sendAbilitiesUpdate() {
        if (this.networkHandler == null) {
            return;
        }
        this.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(this.getAbilities()));
        this.updatePotionVisibility();
    }

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld)this.world;
    }

    public boolean changeGameMode(GameMode gameMode) {
        if (!this.interactionManager.changeGameMode(gameMode)) {
            return false;
        }
        this.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, gameMode.getId()));
        if (gameMode == GameMode.SPECTATOR) {
            this.dropShoulderEntities();
            this.stopRiding();
        } else {
            this.setCameraEntity(this);
        }
        this.sendAbilitiesUpdate();
        this.markEffectsDirty();
        return true;
    }

    @Override
    public boolean isSpectator() {
        return this.interactionManager.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return this.interactionManager.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public void sendMessage(Text message) {
        this.sendMessageToClient(message, false);
    }

    public void sendMessageToClient(Text message, boolean overlay) {
        if (!this.acceptsMessage(overlay)) {
            return;
        }
        this.networkHandler.sendPacket(new GameMessageS2CPacket(message, overlay), PacketCallbacks.of(() -> {
            if (this.acceptsMessage(false)) {
                int i = 256;
                String string = message.asTruncatedString(256);
                MutableText lv = Text.literal(string).formatted(Formatting.YELLOW);
                return new GameMessageS2CPacket(Text.translatable("multiplayer.message_not_delivered", lv).formatted(Formatting.RED), false);
            }
            return null;
        }));
    }

    public void sendChatMessage(SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params) {
        if (this.acceptsChatMessage()) {
            message.send(this, filterMaskEnabled, params);
        }
    }

    public String getIp() {
        SocketAddress socketAddress = this.networkHandler.getConnectionAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            return InetAddresses.toAddrString(inetSocketAddress.getAddress());
        }
        return "<unknown>";
    }

    public void setClientSettings(ClientSettingsC2SPacket packet) {
        this.clientChatVisibility = packet.chatVisibility();
        this.clientChatColorsEnabled = packet.chatColors();
        this.filterText = packet.filterText();
        this.allowServerListing = packet.allowsListing();
        this.getDataTracker().set(PLAYER_MODEL_PARTS, (byte)packet.playerModelBitMask());
        this.getDataTracker().set(MAIN_ARM, (byte)(packet.mainArm() != Arm.LEFT ? 1 : 0));
    }

    public boolean areClientChatColorsEnabled() {
        return this.clientChatColorsEnabled;
    }

    public ChatVisibility getClientChatVisibility() {
        return this.clientChatVisibility;
    }

    private boolean acceptsMessage(boolean overlay) {
        if (this.clientChatVisibility == ChatVisibility.HIDDEN) {
            return overlay;
        }
        return true;
    }

    private boolean acceptsChatMessage() {
        return this.clientChatVisibility == ChatVisibility.FULL;
    }

    public void sendResourcePackUrl(String url, String hash, boolean required, @Nullable Text resourcePackPrompt) {
        this.networkHandler.sendPacket(new ResourcePackSendS2CPacket(url, hash, required, resourcePackPrompt));
    }

    public void sendServerMetadata(ServerMetadata metadata) {
        this.networkHandler.sendPacket(new ServerMetadataS2CPacket(metadata.description(), metadata.favicon().map(ServerMetadata.Favicon::iconBytes), metadata.secureChatEnforced()));
    }

    @Override
    protected int getPermissionLevel() {
        return this.server.getPermissionLevel(this.getGameProfile());
    }

    public void updateLastActionTime() {
        this.lastActionTime = Util.getMeasuringTimeMs();
    }

    public ServerStatHandler getStatHandler() {
        return this.statHandler;
    }

    public ServerRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected void updatePotionVisibility() {
        if (this.isSpectator()) {
            this.clearPotionSwirls();
            this.setInvisible(true);
        } else {
            super.updatePotionVisibility();
        }
    }

    public Entity getCameraEntity() {
        return this.cameraEntity == null ? this : this.cameraEntity;
    }

    public void setCameraEntity(@Nullable Entity entity) {
        Entity lv = this.getCameraEntity();
        Entity entity2 = this.cameraEntity = entity == null ? this : entity;
        if (lv != this.cameraEntity) {
            World world = this.cameraEntity.getWorld();
            if (world instanceof ServerWorld) {
                ServerWorld lv2 = (ServerWorld)world;
                this.teleport(lv2, this.cameraEntity.getX(), this.cameraEntity.getY(), this.cameraEntity.getZ(), Set.of(), this.getYaw(), this.getPitch());
            }
            if (entity != null) {
                this.getWorld().getChunkManager().updatePosition(this);
            }
            this.networkHandler.sendPacket(new SetCameraEntityS2CPacket(this.cameraEntity));
            this.networkHandler.syncWithPlayerPosition();
        }
    }

    @Override
    protected void tickPortalCooldown() {
        if (!this.inTeleportationState) {
            super.tickPortalCooldown();
        }
    }

    @Override
    public void attack(Entity target) {
        if (this.interactionManager.getGameMode() == GameMode.SPECTATOR) {
            this.setCameraEntity(target);
        } else {
            super.attack(target);
        }
    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    @Nullable
    public Text getPlayerListName() {
        return null;
    }

    @Override
    public void swingHand(Hand hand) {
        super.swingHand(hand);
        this.resetLastAttackedTicks();
    }

    public boolean isInTeleportationState() {
        return this.inTeleportationState;
    }

    public void onTeleportationDone() {
        this.inTeleportationState = false;
    }

    public PlayerAdvancementTracker getAdvancementTracker() {
        return this.advancementTracker;
    }

    public void teleport(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch) {
        this.setCameraEntity(this);
        this.stopRiding();
        if (targetWorld == this.world) {
            this.networkHandler.requestTeleport(x, y, z, yaw, pitch);
        } else {
            ServerWorld lv = this.getWorld();
            WorldProperties lv2 = targetWorld.getLevelProperties();
            this.networkHandler.sendPacket(new PlayerRespawnS2CPacket(targetWorld.getDimensionKey(), targetWorld.getRegistryKey(), BiomeAccess.hashSeed(targetWorld.getSeed()), this.interactionManager.getGameMode(), this.interactionManager.getPreviousGameMode(), targetWorld.isDebugWorld(), targetWorld.isFlat(), 3, this.getLastDeathPos()));
            this.networkHandler.sendPacket(new DifficultyS2CPacket(lv2.getDifficulty(), lv2.isDifficultyLocked()));
            this.server.getPlayerManager().sendCommandTree(this);
            lv.removePlayer(this, Entity.RemovalReason.CHANGED_DIMENSION);
            this.unsetRemoved();
            this.refreshPositionAndAngles(x, y, z, yaw, pitch);
            this.setWorld(targetWorld);
            targetWorld.onPlayerTeleport(this);
            this.worldChanged(lv);
            this.networkHandler.requestTeleport(x, y, z, yaw, pitch);
            this.server.getPlayerManager().sendWorldInfo(this, targetWorld);
            this.server.getPlayerManager().sendPlayerStatus(this);
        }
    }

    @Nullable
    public BlockPos getSpawnPointPosition() {
        return this.spawnPointPosition;
    }

    public float getSpawnAngle() {
        return this.spawnAngle;
    }

    public RegistryKey<World> getSpawnPointDimension() {
        return this.spawnPointDimension;
    }

    public boolean isSpawnForced() {
        return this.spawnForced;
    }

    public void setSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage) {
        if (pos != null) {
            boolean bl3;
            boolean bl = bl3 = pos.equals(this.spawnPointPosition) && dimension.equals(this.spawnPointDimension);
            if (sendMessage && !bl3) {
                this.sendMessage(Text.translatable("block.minecraft.set_spawn"));
            }
            this.spawnPointPosition = pos;
            this.spawnPointDimension = dimension;
            this.spawnAngle = angle;
            this.spawnForced = forced;
        } else {
            this.spawnPointPosition = null;
            this.spawnPointDimension = World.OVERWORLD;
            this.spawnAngle = 0.0f;
            this.spawnForced = false;
        }
    }

    public void sendChunkPacket(ChunkPos chunkPos, Packet<?> chunkDataPacket) {
        this.networkHandler.sendPacket(chunkDataPacket);
    }

    public void sendUnloadChunkPacket(ChunkPos chunkPos) {
        if (this.isAlive()) {
            this.networkHandler.sendPacket(new UnloadChunkS2CPacket(chunkPos.x, chunkPos.z));
        }
    }

    public ChunkSectionPos getWatchedSection() {
        return this.watchedSection;
    }

    public void setWatchedSection(ChunkSectionPos section) {
        this.watchedSection = section;
    }

    @Override
    public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch) {
        this.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(event), category, this.getX(), this.getY(), this.getZ(), volume, pitch, this.random.nextLong()));
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new PlayerSpawnS2CPacket(this);
    }

    @Override
    public ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        ItemEntity lv = super.dropItem(stack, throwRandomly, retainOwnership);
        if (lv == null) {
            return null;
        }
        this.world.spawnEntity(lv);
        ItemStack lv2 = lv.getStack();
        if (retainOwnership) {
            if (!lv2.isEmpty()) {
                this.increaseStat(Stats.DROPPED.getOrCreateStat(lv2.getItem()), stack.getCount());
            }
            this.incrementStat(Stats.DROP);
        }
        return lv;
    }

    public TextStream getTextStream() {
        return this.textStream;
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
        this.interactionManager.setWorld(world);
    }

    @Nullable
    private static GameMode gameModeFromNbt(@Nullable NbtCompound nbt, String key) {
        return nbt != null && nbt.contains(key, NbtElement.NUMBER_TYPE) ? GameMode.byId(nbt.getInt(key)) : null;
    }

    private GameMode getServerGameMode(@Nullable GameMode backupGameMode) {
        GameMode lv = this.server.getForcedGameMode();
        if (lv != null) {
            return lv;
        }
        return backupGameMode != null ? backupGameMode : this.server.getDefaultGameMode();
    }

    public void setGameMode(@Nullable NbtCompound nbt) {
        this.interactionManager.setGameMode(this.getServerGameMode(ServerPlayerEntity.gameModeFromNbt(nbt, "playerGameType")), ServerPlayerEntity.gameModeFromNbt(nbt, "previousPlayerGameType"));
    }

    private void writeGameModeNbt(NbtCompound nbt) {
        nbt.putInt("playerGameType", this.interactionManager.getGameMode().getId());
        GameMode lv = this.interactionManager.getPreviousGameMode();
        if (lv != null) {
            nbt.putInt("previousPlayerGameType", lv.getId());
        }
    }

    @Override
    public boolean shouldFilterText() {
        return this.filterText;
    }

    public boolean shouldFilterMessagesSentTo(ServerPlayerEntity player) {
        if (player == this) {
            return false;
        }
        return this.filterText || player.filterText;
    }

    @Override
    public boolean canModifyAt(World world, BlockPos pos) {
        return super.canModifyAt(world, pos) && world.canPlayerModifyAt(this, pos);
    }

    @Override
    protected void tickItemStackUsage(ItemStack stack) {
        Criteria.USING_ITEM.trigger(this, stack);
        super.tickItemStackUsage(stack);
    }

    public boolean dropSelectedItem(boolean entireStack) {
        PlayerInventory lv = this.getInventory();
        ItemStack lv2 = lv.dropSelectedItem(entireStack);
        this.currentScreenHandler.getSlotIndex(lv, lv.selectedSlot).ifPresent(index -> this.currentScreenHandler.setPreviousTrackedSlot(index, lv.getMainHandStack()));
        return this.dropItem(lv2, false, true) != null;
    }

    public boolean allowsServerListing() {
        return this.allowServerListing;
    }

    @Override
    public Optional<SculkShriekerWarningManager> getSculkShriekerWarningManager() {
        return Optional.of(this.sculkShriekerWarningManager);
    }

    @Override
    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        super.triggerItemPickedUpByEntityCriteria(item);
        Entity lv = item.getOwner();
        if (lv != null) {
            Criteria.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, item.getStack(), lv);
        }
    }

    public void setSession(PublicPlayerSession session) {
        this.session = session;
    }

    @Nullable
    public PublicPlayerSession getSession() {
        return this.session;
    }

    @Override
    public void tiltScreen(double deltaX, double deltaZ) {
        this.damageTiltYaw = (float)(MathHelper.atan2(deltaZ, deltaX) * 57.2957763671875 - (double)this.getYaw());
        this.networkHandler.sendPacket(new DamageTiltS2CPacket(this));
    }

    @Override
    public /* synthetic */ World getWorld() {
        return this.getWorld();
    }
}

