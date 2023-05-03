package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class BanishPacket extends AbstractServerboundPacket {
	private final UUID id;

	public BanishPacket(UUID id) {
		this.id = id;
	}

	public BanishPacket(FriendlyByteBuf buf) {
		this.id = buf.readUUID();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(this.id);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		if (capability.minionCount() <= 0) return; //Just in case
		capability.removeMinion(this.id);
	}
}
