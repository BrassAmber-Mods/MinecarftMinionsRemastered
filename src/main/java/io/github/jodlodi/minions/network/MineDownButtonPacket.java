package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.orders.MineDownOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MineDownButtonPacket extends AbstractServerboundPacket {
	protected final boolean paused;
	protected final BlockPos minPos;
	protected final BlockPos maxPos;
	protected final int startDir;
	protected final int stairs;

	public MineDownButtonPacket(boolean paused, BlockPos minPos, BlockPos maxPos, int startDir, int stairs) {
		this.paused = paused;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.startDir = startDir;
		this.stairs = stairs;
	}

	public MineDownButtonPacket(FriendlyByteBuf buf) {
		this.paused = buf.readBoolean();
		this.minPos = buf.readBlockPos();
		this.maxPos = buf.readBlockPos();
		this.startDir = buf.readInt();
		this.stairs = buf.readInt();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.paused);
		buf.writeBlockPos(this.minPos);
		buf.writeBlockPos(this.maxPos);
		buf.writeInt(this.startDir);
		buf.writeInt(this.stairs);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		capability.setOrder(new MineDownOrder(this.minPos, this.maxPos, this.startDir, this.stairs));
		capability.setPaused(this.paused);
	}
}
