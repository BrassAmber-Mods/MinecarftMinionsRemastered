package io.github.jodlodi.minions.capabilities;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.network.MasterPacket;
import io.github.jodlodi.minions.orders.AbstractOrder;
import io.github.jodlodi.minions.registry.OrderRegistry;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class MasterCapabilityHandler implements IMasterCapability {
    private final List<UUID> minions = new ArrayList<>();
    private final List<CompoundTag> inventories = new ArrayList<>();
    private Player player;
    private AbstractOrder order;
    private boolean synced = false;

    private BlockPos containerBlock = null;
    private UUID containerEntity = null;//TODO

    @Override
    public void setEntity(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void tick() {
        if (!this.player.level.isClientSide && !this.synced) {
            this.sendUpdatePacket();
            this.synced = true;
        }
        if (this.order != null) this.order.tick(this, player, this.player.level);
    }

    @Override
    public List<UUID> getMinions() {
        return minions;
    }

    @Override
    public boolean isMinion(UUID uuid) {
        return this.minions.contains(uuid);
    }

    @Override
    public int minionCount() {
        int count = 0;
        for (UUID uuid : this.minions) if (uuid != null) count++;
        return count;
    }

    @Override
    public void addMinion(UUID uuid) {
        int size = this.minions.size();
        for (int i = 0; i < size; i++) {
            if (this.minions.get(i) == null) {
                this.minions.set(i, uuid);
                if (!this.player.level.isClientSide) this.sendUpdatePacket();
                return;
            }
        }

        if (size < 4) this.minions.add(uuid);
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    public void addSpecificMinion(UUID uuid, int specificID) {
        this.minions.set(specificID, uuid);
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    public void removeMinion(UUID uuid) {
        this.minions.set(this.minions.indexOf(uuid), null);
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    public void releaseMinions(int count) {
        for (int i = 0; i < count; i++) {
            int size = this.minions.size();
            if (size > 0) this.minions.remove(size - 1);
        }
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    public void setInventory(int id, CompoundTag tag) {
        this.inventories.set(id, tag);
    }

    @Override
    public CompoundTag getInventory(int id) {
        return this.inventories.get(id);
    }

    @Override
    public void setOrder(@Nullable AbstractOrder order) {
        this.order = order;
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    @Nullable
    public AbstractOrder getOrder() {
        return this.order;
    }

    @Override
    public void setContainerBlock(@Nullable BlockPos pos) {
        this.containerBlock = pos;
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    @Nullable
    public BlockPos getContainerBlock() {
        return this.containerBlock;
    }

    @Override
    public void setContainerEntity(@Nullable UUID uuid) {
        this.containerEntity = uuid;
        if (!this.player.level.isClientSide) this.sendUpdatePacket();
    }

    @Override
    public @NotNull UUID getContainerEntity() {
        return this.containerEntity != null ? this.containerEntity : this.player.getUUID();
    }

    @Override
    public void finalizeMinion(Minion minion) {
        minion.setOwnerUUID(this.getPlayer().getUUID());

        int id = this.getMinions().indexOf(minion.getUUID());
        CompoundTag inventory = this.getInventory(id);

        if (inventory.contains("CustomName", 8)) {
            String s = inventory.getString("CustomName");

            try {
                minion.setCustomName(Component.Serializer.fromJson(s));
            } catch (Exception exception) {
                MinionsRemastered.LOGGER.warn("Failed to parse minion custom name {}", s, exception);
            }
        }
    }

    @Override
    public void sendUpdatePacket() {
        PacketRegistry.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.player), new MasterPacket(this.player, this));
    }

    @Override
    public CompoundTag serializeNBT() {
        return Util.make(new CompoundTag(), tag -> {
            for (int i = 0; i < this.minions.size(); i++) {
                if (this.minions.get(i) != null) {
                    tag.putUUID("MinionUUID" + i, this.minions.get(i));
                }
            }

            for (int i = 0; i < this.inventories.size(); i++) {
                tag.put("MinionInventory" + i, this.inventories.get(i));
            }

            if (this.order != null) {
                tag.putString("OrderID", this.order.getID().toString());
                tag.put("Order", this.order.serialize());
            }

            if (this.containerBlock != null) {
                tag.putLong("ChestPos", this.containerBlock.asLong());
            }

            if (this.containerEntity != null) {
                tag.putUUID("ContainerUUID", this.containerEntity);
            }
        });
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (int i = 0; i < 4; i++) {
            if (tag.contains("MinionUUID" + i)) {
                this.minions.add(tag.getUUID("MinionUUID" + i));
            } else this.minions.add(null);
        }

        for (int i = 0; i < 4; i++) {
            if (tag.contains("MinionInventory" + i)) {
                this.inventories.add(tag.getCompound("MinionInventory" + i));
            } else this.inventories.add(new CompoundTag());
        }

        this.order = null;

        ResourceLocation ID = ResourceLocation.tryParse(tag.getString("OrderID"));
        if (ID != null) {
            Function<CompoundTag, AbstractOrder> function = OrderRegistry.ORDERS.get(ID);
            if (function != null) this.order = function.apply(tag.getCompound("Order"));
        }

        this.containerBlock = tag.contains("ChestPos") ? BlockPos.of(tag.getLong("ChestPos")) : null;

        this.containerEntity = tag.contains("ContainerUUID") ? tag.getUUID("ContainerUUID") : null;
    }
}
