package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.orders.ChopOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class ChopButtonPacket extends AbstractServerboundPacket {
	protected final boolean paused;
	protected final BlockPos minPos;
	protected final BlockPos maxPos;
	protected final Block predicate;

	public ChopButtonPacket(boolean paused, BlockPos minPos, BlockPos maxPos, @Nullable Block predicate) {
		this.paused = paused;
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.predicate = predicate;
	}

	public ChopButtonPacket(FriendlyByteBuf buf) {
		this.paused = buf.readBoolean();
		this.minPos = buf.readBlockPos();
		this.maxPos = buf.readBlockPos();
		this.predicate = buf.readBoolean() ? ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()) : null;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.paused);
		buf.writeBlockPos(this.minPos);
		buf.writeBlockPos(this.maxPos);

		boolean more = this.predicate != null;
		buf.writeBoolean(more);
		if (more) buf.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(this.predicate)));
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		capability.setOrder(new ChopOrder(this.minPos, this.maxPos, this.predicate));
		capability.setPaused(this.paused);
	}
}
