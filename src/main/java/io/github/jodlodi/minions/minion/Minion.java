package io.github.jodlodi.minions.minion;

import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.network.BlinkPacket;
import io.github.jodlodi.minions.network.PoofPacket;
import io.github.jodlodi.minions.registry.CommonRegistry;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Minion extends PathfinderMob implements OwnableEntity {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(Minion.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Integer> DATA_BLINKING = SynchedEntityData.defineId(Minion.class, EntityDataSerializers.INT);
    public static final int INVENTORY_SIZE = 256;
    public Vec3 lastPos = Vec3.ZERO;

    public Minion(Level level) {
        super(CommonRegistry.MINION.get(), level);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_BLINKING, 0);
    }

    @Override
    protected void registerGoals() {//TODO dump full inventory goal
        this.goalSelector.addGoal(0, new FollowOrderGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowMasterGoal(this, 1.2D, 9.0F, 2.0F, false));
        this.goalSelector.addGoal(3, new LookAtMasterGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !((Minion)this.mob).isOwnerHoldingStaff() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !((Minion)this.mob).isOwnerHoldingStaff() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        super.aiStep();
        if (this.isEnactingOrder()) {
            Optional.ofNullable(this.navigation.getPath()).ifPresent(path -> {
                if (!path.canReach() || (this.position().equals(this.lastPos) && this.random.nextInt(10) == 1)) {//TODO: see if theres a better way
                    BlockPos nearbyPos = MinUtil.randomOpenNearby(path.getTarget(), level);
                    if (nearbyPos == null) nearbyPos = path.getTarget().above();
                    this.blink(nearbyPos);
                    this.navigation.stop();
                }
                this.lastPos = this.position();
            });
        }
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            int blink = this.entityData.get(DATA_BLINKING);
            if (blink > 0) {
                this.entityData.set(DATA_BLINKING, blink - 1);
                this.getLookControl().tick();
                if (blink == 1) {
                    PacketRegistry.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this), new BlinkPacket(this.getId(), this.position()));
                }
            }

            LivingEntity owner = this.getOwner();
            if (owner != null) {
                owner.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(this::accept);
            }
        }

        super.tick();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        PacketRegistry.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this), new PoofPacket(this.getId()));
        super.remove(removalReason);
    }

    protected int getMinionID(IMasterCapability masterCapability) {
        return masterCapability.getMinions().indexOf(this.getUUID());
    }

    public boolean isOwnerHoldingStaff() {
        return this.getOwner() != null && this.getOwner().getMainHandItem().is(CommonRegistry.MASTERS_STAFF.get());
    }

    @Override
    public void setCustomName(@Nullable Component component) {
        this.getMastersCapability().ifPresent(masterCapability -> {
            CompoundTag tag = masterCapability.getInventory(this.getMinionID(masterCapability));
            tag.putString("CustomName", Component.Serializer.toJson(component));
        });
        super.setCustomName(component);
    }

    @Override
    public InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand hand) {
        if (player != this.getOwner()) return InteractionResult.PASS;
        return super.checkAndHandleImportantInteractions(player, hand);
    }

    @Override
    protected void pickUpItem(ItemEntity item) {
        this.getMastersCapability().ifPresent(masterCapability -> {
            /*TODO: inventories
            ItemStack stack = item.getItem().copy();
            int stored = storeItem(masterCapability.getInventory(this.getMinionID(masterCapability)), stack);
            if (stored > 0) {
                stack.shrink(stored);
                if (stack.isEmpty()) {
                    this.onItemPickup(item);
                    this.take(item, item.getItem().getCount());
                    item.discard();
                } else item.setItem(stack);
            }

            if (true) return;*/


            if (masterCapability.getContainerBlock() != null) {
                BlockEntity entity = this.level.getBlockEntity(masterCapability.getContainerBlock());
                if (entity != null && entity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                    entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> this.storeItem(itemHandler, item));
                    return;
                } else masterCapability.setContainerBlock(null);
            }
            if (this.level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(masterCapability.getContainerEntity());
                if (entity != null && (!(entity instanceof LivingEntity living) || !living.isDeadOrDying())) {
                    entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> this.storeItem(itemHandler, item));
                    if (entity instanceof LivingEntity livingEntity) livingEntity.onItemPickup(item);
                } else {
                    masterCapability.setContainerEntity(null);
                    entity = serverLevel.getEntity(masterCapability.getContainerEntity());
                    if (entity != null) {
                        entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> this.storeItem(itemHandler, item));
                        if (entity instanceof LivingEntity livingEntity) livingEntity.onItemPickup(item);
                    }
                }
            }
        });
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }

    protected static int storeItem(CompoundTag inventory, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem().canFitInsideContainerItems()) {
            if (!inventory.contains("Items")) inventory.put("Items", new ListTag());

            int contentWeight = getContentWeight(inventory);
            int stackWeight = getWeight(stack);
            int min = Math.min(stack.getCount(), (INVENTORY_SIZE - contentWeight) / stackWeight);
            if (min == 0) return 0;
            else {
                ListTag items = inventory.getList("Items", 10);
                Optional<CompoundTag> optionalMatchingItem = getMatchingItem(stack, items);
                if (optionalMatchingItem.isPresent()) {
                    CompoundTag matchingItem = optionalMatchingItem.get();
                    ItemStack itemStack = ItemStack.of(matchingItem);
                    itemStack.grow(min);
                    itemStack.save(matchingItem);
                    items.remove(matchingItem);
                    items.add(0, matchingItem);
                    MinionsRemastered.LOGGER.warn("Added {} {} into the inventory!", itemStack.getCount(), itemStack.getItem());
                } else {
                    ItemStack itemStack = stack.copy();
                    itemStack.setCount(min);
                    CompoundTag compoundTag = new CompoundTag();
                    itemStack.save(compoundTag);
                    items.add(0, compoundTag);
                    MinionsRemastered.LOGGER.warn("Added {} {} into the inventory!", itemStack.getCount(), itemStack.getItem());
                }

                return min;
            }
        } else return 0;
    }

    private static int getContentWeight(CompoundTag inventory) {
        return getContents(inventory).mapToInt((stack) -> getWeight(stack) * stack.getCount()).sum();
    }

    private static Stream<ItemStack> getContents(CompoundTag inventory) {
        return inventory.getList("Items", 10).stream().map(CompoundTag.class::cast).map(ItemStack::of);
    }

    private static Optional<CompoundTag> getMatchingItem(ItemStack p_150757_, ListTag p_150758_) {
        return p_150757_.is(Items.BUNDLE) ? Optional.empty() : p_150758_.stream().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).filter((tag) ->
                ItemStack.isSameItemSameTags(ItemStack.of(tag), p_150757_)).findFirst();
    }

    private static int getWeight(ItemStack stack) {
        if (stack.is(Items.BUNDLE)) return 4 + getContentWeight(stack.getOrCreateTag());
        else {
            if ((stack.is(Items.BEEHIVE) || stack.is(Items.BEE_NEST)) && stack.hasTag()) {
                CompoundTag compoundtag = BlockItem.getBlockEntityData(stack);
                if (compoundtag != null && !compoundtag.getList("Bees", 10).isEmpty()) return 64;
            }
            return 64 / stack.getMaxStackSize();
        }
    }

    protected void storeItem(IItemHandler outputIItemHandler, ItemEntity item) {
        ItemStack inputStack = item.getItem();

        if (!inputStack.isEmpty()) {
            int firstProperStack = -1;
            for (int j = 0; j < outputIItemHandler.getSlots(); j++) {
                if (outputIItemHandler.isItemValid(j, inputStack)) {
                    ItemStack outputStack = outputIItemHandler.getStackInSlot(j);

                    if (firstProperStack == -1 && outputStack.isEmpty()) {
                        firstProperStack = j; //We reference the index of the first empty slot, in case there is no stacks that aren't at max size
                    } else if (ItemStack.isSameItemSameTags(inputStack, outputStack)
                            && outputStack.getCount() < outputStack.getMaxStackSize()
                            && outputStack.getCount() < outputIItemHandler.getSlotLimit(j)) {
                        firstProperStack = j;
                        break;
                    }
                }
            }

            if (firstProperStack != -1) { //If there weren't any non-full stacks, we transfer to an empty space instead
                ItemStack sim = outputIItemHandler.insertItem(firstProperStack, inputStack, true);
                if (sim.getCount() < inputStack.getCount()) {
                    outputIItemHandler.insertItem(firstProperStack, inputStack, false);
                    if (sim.isEmpty()) {
                        item.discard();
                        this.take(item, inputStack.getCount());
                    } else item.setItem(sim);
                }
            }

        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        Optional<IMasterCapability> masterCapabilityOptional = this.getMastersCapability();
        if (masterCapabilityOptional.isPresent()) {
            IMasterCapability masterCapability = masterCapabilityOptional.get();
            CompoundTag inventory = masterCapability.getInventory(this.getMinionID(masterCapability));
            if (dropContents(inventory, player, this)) return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private static boolean dropContents(CompoundTag inventory, Player player, Minion minion) {
        if (!inventory.contains("Items")) return false;
        else {
            if (player instanceof ServerPlayer) {
                ListTag listtag = inventory.getList("Items", 10);
                for(int i = 0; i < listtag.size(); ++i) {
                    CompoundTag tag = listtag.getCompound(i);
                    ItemStack itemstack = ItemStack.of(tag);
                    ItemEntity item = new ItemEntity(minion.level, minion.getX(), minion.getEyeY(), minion.getZ(), itemstack);
                    item.setPickUpDelay(40);
                    item.setThrower(minion.getUUID());

                    float f7 = 0.3F;
                    float f8 = Mth.sin(minion.getXRot() * Mth.DEG_TO_RAD);
                    float f2 = Mth.cos(minion.getXRot() * Mth.DEG_TO_RAD);
                    float f3 = Mth.sin(minion.getYHeadRot() * Mth.DEG_TO_RAD);
                    float f4 = Mth.cos(minion.getYHeadRot() * Mth.DEG_TO_RAD);
                    float f5 = minion.random.nextFloat() * ((float)Math.PI * 2F);
                    float f6 = 0.02F * minion.random.nextFloat();
                    item.setDeltaMovement((double)(-f3 * f2 * 0.3F) + Math.cos(f5) * (double)f6, -f8 * 0.3F + 0.1F + (minion.random.nextFloat() - minion.random.nextFloat()) * 0.1F, (double)(f4 * f2 * 0.3F) + Math.sin(f5) * (double)f6);
                    minion.level.addFreshEntity(item);
                }
            }

            inventory.remove("Items");
            return true;
        }
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return !this.isEnactingOrder();
    }

    @Override
    protected boolean isAffectedByFluids() {
        return !this.isEnactingOrder();
    }

    public boolean getBlinking() {
        return this.entityData.get(DATA_BLINKING) > 0;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float v) {
        return false;
    }

    public boolean isEnactingOrder() {
        return this.goalSelector.getRunningGoals().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof FollowOrderGoal);
    }

    public void blink(BlockPos pos) {
        this.blink(Vec3.atBottomCenterOf(pos));
    }

    public void blink(Vec3 vec3) {
        this.blink(vec3.x, vec3.y, vec3.z, this.getXRot(), this.getYRot());
    }

    public void blink(double x, double y, double z, float f, float g) {
        PacketRegistry.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this), new BlinkPacket(this.getId(), this.position()));
        this.moveTo(x, y, z, f, g);
        this.entityData.set(DATA_BLINKING, 15);
    }

    @Override
    public ItemStack getMainHandItem() {
        return this.getBlinking() ? ItemStack.EMPTY : super.getMainHandItem();
    }

    @Override
    public boolean isInvisible() {
        return this.getBlinking() || super.isInvisible();
    }

    public float getMineSpeed() {
        return 3.75F;//TODO
    }

    public void release() {
        if (this.getOwner() instanceof Player player) {
            player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(iMasterCapability -> iMasterCapability.removeMinion(this.uuid));
        }
        this.discard();
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public Optional<IMasterCapability> getMastersCapability() {
        LivingEntity owner = this.getOwner();
        if (owner == null) return Optional.empty();
        else return owner.getCapability(CommonRegistry.MASTER_CAPABILITY).resolve();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) tag.putUUID("Owner", this.getOwnerUUID());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        UUID uuid = null;
        if (tag.hasUUID("Owner")) uuid = tag.getUUID("Owner");
        else if (this.getServer() != null) uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), tag.getString("Owner"));

        if (uuid != null) this.setOwnerUUID(uuid);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void accept(@NotNull IMasterCapability iMasterCapability) {
        if (!iMasterCapability.isMinion(this.getUUID())) {
            this.discard();
        }
    }
}
