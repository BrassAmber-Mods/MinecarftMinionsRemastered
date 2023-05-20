package io.github.jodlodi.minions.minion;

import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.goals.*;
import io.github.jodlodi.minions.network.BlinkPacket;
import io.github.jodlodi.minions.network.PoofPacket;
import io.github.jodlodi.minions.registry.CommonRegistry;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Minion extends PathfinderMob implements OwnableEntity {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(Minion.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Integer> DATA_BLINKING = SynchedEntityData.defineId(Minion.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(Minion.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(Minion.class, EntityDataSerializers.BOOLEAN);
    public static final int BLINK_COOLDOWN = 20;
    public static final int DEFAULT_RED = 9185572;
    public static final float DEFAULT_WIDTH = 0.6F;
    public static final float DEFAULT_HEIGHT = 0.95F;

    public Minion(Level level) {
        super(CommonRegistry.MINION.get(), level);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_BLINKING, 0);
        this.entityData.define(DATA_COLOR, DEFAULT_RED);
        this.entityData.define(DATA_SITTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new EnactOrderGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new CustomFloatGoal(this));
        this.goalSelector.addGoal(2, new FollowMasterGoal(this, 1.2D, 9.0F, 2.0F, false));
        this.goalSelector.addGoal(3, new LookAtMasterGoal(this));
        this.goalSelector.addGoal(4, new MinionRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        this.dimensions = this.getDimensions(this.getPose());
        super.aiStep();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new MinionNavigation(this, level);
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            int blink = this.entityData.get(DATA_BLINKING);
            if (blink > -BLINK_COOLDOWN) {
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

    public int getMinionID(IMasterCapability masterCapability) {
        return masterCapability.getMinions().indexOf(this.getUUID());
    }

    public boolean isOwnerHoldingStaff() {
        LivingEntity living = this.getOwner();
        if (living != null) {
            return living.getMainHandItem().is(CommonRegistry.MASTERS_STAFF.get()) || living.getOffhandItem().is(CommonRegistry.MASTERS_STAFF.get());
        }
        return false;
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
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (item.getItem() instanceof DyeItem dyeItem) {
            IMasterCapability cap = this.getMastersCapability().orElse(null);
            if (cap != null && player == this.getOwner()) {
                CompoundTag tag = cap.getInventory(this.getMinionID(cap));
                int newColor = MinUtil.dye(dyeItem, MinUtil.getColor(tag));
                tag.putInt("Color", newColor);
                this.entityData.set(DATA_COLOR, newColor);
            }
            if (!player.getAbilities().instabuild) item.shrink(1);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand hand) {
        if (player != this.getOwner()) return InteractionResult.PASS;
        return super.checkAndHandleImportantInteractions(player, hand);
    }

    @Override
    protected void pickUpItem(ItemEntity item) {
        this.getMastersCapability().ifPresent(masterCapability -> {
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

    public boolean canBlink() {
        return this.entityData.get(DATA_BLINKING) <= -BLINK_COOLDOWN;
    }

    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setSit(boolean sit) {
        this.entityData.set(DATA_SITTING, sit);
    }

    public boolean getSit() {
        return this.entityData.get(DATA_SITTING);
    }

    public boolean sittingOrRiding() {
        return this.isPassenger() || this.getSit();
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
        return this.goalSelector.getRunningGoals().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof EnactOrderGoal);
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
        return 3.75F;//FIXME
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double)this.dimensions.height * 0.75D * (this.getSit() ? 0.9D : 1.2D);
    }

    @Override
    public boolean canRiderInteract() {
        return true;
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
        tag.putInt("Color", this.getColor());
        tag.putBoolean("Sit", this.getSit());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        UUID uuid = null;
        if (tag.hasUUID("Owner")) uuid = tag.getUUID("Owner");
        else if (this.getServer() != null) uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), tag.getString("Owner"));

        if (uuid != null) this.setOwnerUUID(uuid);

        this.setColor(MinUtil.getColor(tag));
        this.getMastersCapability().ifPresent(masterCapability -> {
            CompoundTag inventory = masterCapability.getInventory(this.getMinionID(masterCapability));
            this.setColor(MinUtil.getColor(inventory));
        });

        this.setSit(tag.contains("Sit") && tag.getBoolean("Sit"));
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

    @Override
    @SuppressWarnings("deprecation")
    public void travel(Vec3 vec3) {
        if (this.isAlive() && !this.sittingOrRiding()) {
            Entity entity = this.getControllingPassenger();
            if (this.isVehicle() && entity instanceof Player) {
                this.setYRot(entity.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(entity.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
                this.maxUpStep = 1.15F;
                this.flyingSpeed = this.getSpeed() * 0.1F;

                if (this.isControlledByLocalInstance()) {
                    float f = this.getSteeringSpeed();

                    this.setSpeed(f);
                    super.travel(new Vec3(0.0D, 0.0D, 1.0D));
                    this.lerpSteps = 0;
                } else {
                    this.calculateEntityAnimation(this, false);
                    this.setDeltaMovement(Vec3.ZERO);
                }

                this.tryCheckInsideBlocks();
                return;
            }
        }
        this.maxUpStep = 0.5F;
        this.flyingSpeed = 0.02F;
        super.travel(vec3);
    }


    @Override
    public Vec3 collide(Vec3 vec34) {
        AABB aabb = MinUtil.getRidingAABB(this);
        List<VoxelShape> list = this.level.getEntityCollisions(this, aabb.expandTowards(vec34));
        Vec3 vec3 = vec34.lengthSqr() == 0.0D ? vec34 : collideBoundingBox(this, vec34, aabb, this.level, list);
        boolean flag = vec34.x != vec3.x;
        boolean flag1 = vec34.y != vec3.y;
        boolean flag2 = vec34.z != vec3.z;
        boolean flag3 = this.onGround || flag1 && vec34.y < 0.0D;
        float stepHeight = getStepHeight();
        if (stepHeight > 0.0F && flag3 && (flag || flag2)) {
            Vec3 vec31 = collideBoundingBox(this, new Vec3(vec34.x, stepHeight, vec34.z), aabb, this.level, list);
            Vec3 vec32 = collideBoundingBox(this, new Vec3(0.0D, stepHeight, 0.0D), aabb.expandTowards(vec34.x, 0.0D, vec34.z), this.level, list);
            if (vec32.y < (double)stepHeight) {
                Vec3 vec33 = collideBoundingBox(this, new Vec3(vec34.x, 0.0D, vec34.z), aabb.move(vec32), this.level, list).add(vec32);
                if (vec33.horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
                    vec31 = vec33;
                }
            }

            if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr()) {
                return vec31.add(collideBoundingBox(this, new Vec3(0.0D, -vec31.y + vec34.y, 0.0D), aabb.move(vec31), this.level, list));
            }
        }

        return vec3;
    }

    @Nullable
    public Entity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        return entity != null && this.canBeControlledBy(entity) ? entity : null;
    }

    protected boolean canBeControlledBy(Entity entity) {
        if (entity instanceof Player player) {
            return player.getMainHandItem().is(CommonRegistry.MASTERS_STAFF.get()) || player.getOffhandItem().is(CommonRegistry.MASTERS_STAFF.get());
        } else {
            return false;
        }
    }

    public float getSteeringSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
    }
}
