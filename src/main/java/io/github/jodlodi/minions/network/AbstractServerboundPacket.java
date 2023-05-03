package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public abstract class AbstractServerboundPacket {
    abstract public void encode(FriendlyByteBuf buf);

    abstract void execute(ServerPlayer player, IMasterCapability capability);

    public static class Handler {
        public static void onMessage(AbstractServerboundPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                    Optional.ofNullable(ctx.get().getSender()).ifPresent(player ->
                            player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(iMasterCapability ->
                                    message.execute(player, iMasterCapability))));

            ctx.get().setPacketHandled(true);
        }
    }
}
