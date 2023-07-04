package io.github.jodlodi.minions.capabilities;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.orders.AbstractOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IMasterCapability extends INBTSerializable<CompoundTag> {

    ResourceLocation ID = MinionsRemastered.locate("effect_capability");

    void setEntity(Player player);

    Player getPlayer();

    void tick();

    List<UUID> getMinions();

    boolean isMinion(UUID uuid);

    void addMinion(UUID uuid);

    void addSpecificMinion(UUID uuid, int specificID);

    void removeMinion(UUID uuid);

    void releaseMinions(int count);

    void setInventory(int id, CompoundTag tag);

    CompoundTag getInventory(int id);

    int minionCount();

    void setOrder(@Nullable AbstractOrder order);

    @Nullable
    AbstractOrder getOrder();

    void sendUpdatePacket();

    void setContainerBlock(@Nullable BlockPos pos);

    BlockPos getContainerBlock();

    void setContainerEntity(@Nullable UUID uuid);

    @NotNull
    UUID getContainerEntity();

    void finalizeMinion(Minion minion);

    void setPaused(boolean paused);

    boolean isPaused();

}
