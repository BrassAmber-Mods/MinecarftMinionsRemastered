package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StopButtonPacket extends AbstractServerboundPacket {

	public StopButtonPacket() {

	}

	public StopButtonPacket(FriendlyByteBuf buf) {

	}

	@Override
	public void encode(FriendlyByteBuf buf) {

	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		capability.setOrder(null);
	}
}
