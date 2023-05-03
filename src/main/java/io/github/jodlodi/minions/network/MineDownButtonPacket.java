package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.orders.MineDownOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MineDownButtonPacket extends AbstractServerboundPacket {
	private final BlockPos minPos;
	private final BlockPos maxPos;

	public MineDownButtonPacket(BlockPos minPos, BlockPos maxPos) {
		this.minPos = minPos;
		this.maxPos = maxPos;
	}

	public MineDownButtonPacket(FriendlyByteBuf buf) {
		this.minPos = buf.readBlockPos();
		this.maxPos = buf.readBlockPos();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.minPos);
		buf.writeBlockPos(this.maxPos);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		capability.setOrder(new MineDownOrder(this.minPos, this.maxPos));
	}
}
