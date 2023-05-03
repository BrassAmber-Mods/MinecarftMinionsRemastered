package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class PoofPacket {
    private final int entityID;

    public PoofPacket(int id) {
        this.entityID = id;
    }

    public PoofPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
    }

    public static class Handler {
        public static void onMessage(PoofPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().cameraEntity).ifPresent(entity -> {
                if (entity.level.getEntity(message.entityID) instanceof Minion minion) {
                    for (int i = 0; i < 16; i++) {
                        minion.level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, minion.getRandomX(0.5D), minion.getRandomY(), minion.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
                        minion.level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, minion.getRandomX(1.0D), minion.getRandomY(), minion.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
                        minion.level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, minion.getRandomX(1.5D), minion.getRandomY(), minion.getRandomZ(1.5D), 0.0D, 0.0D, 0.0D);
                    }
                }
            }));

            ctx.get().setPacketHandled(true);
        }
    }
}
