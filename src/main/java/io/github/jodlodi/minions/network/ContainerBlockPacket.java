package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ContainerBlockPacket extends AbstractServerboundPacket {
	private final BlockPos pos;

	public ContainerBlockPacket(BlockPos pos) {
		this.pos = pos;
	}

	public ContainerBlockPacket(FriendlyByteBuf buf) {
		this.pos = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		capability.setContainerBlock(this.pos);
		capability.setContainerEntity(null);
	}
}
