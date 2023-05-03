package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SummonPacket extends AbstractServerboundPacket {
	private final BlockPos pos;
	private final int specificID;

	public SummonPacket(BlockPos pos) {
		this(pos, -1);
	}

	public SummonPacket(BlockPos pos, int specificID) {
		this.pos = pos;
		this.specificID = specificID;
	}

	public SummonPacket(FriendlyByteBuf buf) {
		this.pos = buf.readBlockPos();
		this.specificID = buf.readInt();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeInt(this.specificID);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		if (capability.minionCount() >= 4) return; //Just in case
		BlockPos bPos = this.pos;
		if (!player.level.getBlockState(bPos).getMaterial().isReplaceable()) bPos = bPos.above();
		if (player.level.getBlockState(bPos).getMaterial().isReplaceable()) {
			Minion minion = new Minion(player.level);
			minion.setOwnerUUID(player.getUUID());
			Vec3 pos = Vec3.atLowerCornerOf(bPos).add(player.level.random.nextDouble(), 0.0D, player.level.random.nextDouble());
			minion.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
			minion.blink(pos);

			if (this.specificID == -1) capability.addMinion(minion.getUUID());
			else capability.addSpecificMinion(minion.getUUID(), this.specificID);

			player.level.addFreshEntity(minion);
			capability.finalizeMinion(minion);
		}
	}
}
