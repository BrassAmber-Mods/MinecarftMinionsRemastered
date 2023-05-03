package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class BlinkPacket {
    private final int entityID;
    private final double x;
    private final double y;
    private final double z;

    public BlinkPacket(int id, double x, double y, double z) {
        this.entityID = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlinkPacket(int id, Vec3 vec3) {
        this(id, vec3.x, vec3.y, vec3.z);
    }

    public BlinkPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    public static class Handler {
        public static void onMessage(BlinkPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> Optional.ofNullable(Minecraft.getInstance().cameraEntity).ifPresent(entity -> {
                if (entity.level.getEntity(message.entityID) instanceof Minion minion) {
                    minion.setPosRaw(message.x, message.y, message.z);
                    minion.xo = message.x;
                    minion.xOld = message.x;
                    minion.yo = message.y;
                    minion.yOld = message.y;
                    minion.zo = message.z;
                    minion.zOld = message.z;

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
