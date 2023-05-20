package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.orders.CarryLivingOrder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class CarryLivingButtonPacket extends AbstractServerboundPacket {
	private final UUID id;

	public CarryLivingButtonPacket(UUID id) {
		this.id = id;
	}

	public CarryLivingButtonPacket(FriendlyByteBuf buf) {
		this.id = buf.readUUID();
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(this.id);
	}

	@Override
	void execute(ServerPlayer player, IMasterCapability capability) {
		Entity entity = player.getLevel().getEntity(this.id);
		if (entity == null) return;
		if (entity.getVehicle() instanceof Minion minion && capability.isMinion(minion.getUUID())) entity.stopRiding();
		else if (!entity.isPassenger()) capability.setOrder(new CarryLivingOrder(this.id));
	}
}
