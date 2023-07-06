package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StopButtonPacket extends AbstractServerboundPacket {
	protected final boolean shift;

	public StopButtonPacket(boolean shift) {
		this.shift = shift;
	}

	public StopButtonPacket(FriendlyByteBuf buf) {
		this.shift = buf.readBoolean();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.shift);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		//FIXME
		if (this.shift) capability.setOrder(null);
		else capability.setPaused(!capability.isPaused());
	}
}
