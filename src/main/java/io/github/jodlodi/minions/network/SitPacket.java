package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class SitPacket extends AbstractServerboundPacket {
    private final UUID entityID;

    public SitPacket(UUID id) {
        this.entityID = id;
    }

    public SitPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityID);
    }

    @Override
    void execute(ServerPlayer player, IMasterCapability capability) {
        if (player.level instanceof ServerLevel serverLevel && serverLevel.getEntity(this.entityID) instanceof Minion minion) {
            minion.setSit(!minion.getSit());
        }
    }
}
