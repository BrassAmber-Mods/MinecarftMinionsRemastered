package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class MasterPacket {
    private final int entityID;
    private final int count;
    private final List<UUID> minions;
    private final CompoundTag tag;

    private final int nullCount;
    private final List<Integer> nulls = new ArrayList<>();

    public MasterPacket(int id, IMasterCapability cap) {
        this.entityID = id;
        this.count = cap.minionCount();
        this.minions = cap.getMinions();
        this.tag = cap.serializeNBT();

        int j = 0;
        for (int i = 0; i < this.minions.size(); i++) {
            if (this.minions.get(i) == null) {
                this.nulls.add(i);
                j++;
            }
        }
        this.nullCount = j;
    }

    public MasterPacket(Entity entity, IMasterCapability cap) {
        this(entity.getId(), cap);
    }

    public MasterPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readInt();
        this.count = buf.readInt();
        this.tag = buf.readNbt();

        this.nullCount = buf.readInt();
        for (int i = 0; i < this.nullCount; i++) {
            this.nulls.add(buf.readInt());
        }

        this.minions = new ArrayList<>();
        for (int i = 0; i < this.count; i++) {
            if (this.nulls.contains(i)) {
                this.minions.add(null);
            } else this.minions.add(buf.readUUID());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeInt(this.count);
        buf.writeNbt(this.tag);

        buf.writeInt(this.nullCount);
        for (int i : this.nulls) buf.writeInt(i);
        for (UUID uuid : this.minions) {
            if (uuid != null) buf.writeUUID(uuid);
        }
    }

    public static class Handler {
        @SuppressWarnings("Convert2Lambda")
        public static void onMessage(MasterPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    Player player = Minecraft.getInstance().player;
                    if (player != null) player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(cap -> {
                        cap.getMinions().clear();
                        cap.deserializeNBT(message.tag);
                    });
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
