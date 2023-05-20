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
	private final int startDir;
	private final boolean stairs;

	public MineDownButtonPacket(BlockPos minPos, BlockPos maxPos, int startDir, boolean stairs) {
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.startDir = startDir;
		this.stairs = stairs;
	}

	public MineDownButtonPacket(FriendlyByteBuf buf) {
		this.minPos = buf.readBlockPos();
		this.maxPos = buf.readBlockPos();
		this.startDir = buf.readInt();
		this.stairs = buf.readBoolean();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.minPos);
		buf.writeBlockPos(this.maxPos);
		buf.writeInt(this.startDir);
		buf.writeBoolean(this.stairs);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		capability.setOrder(new MineDownOrder(this.minPos, this.maxPos, this.startDir, this.stairs));
	}
}
