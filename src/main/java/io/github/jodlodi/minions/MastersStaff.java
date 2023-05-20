package io.github.jodlodi.minions;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.network.BlockStaffScreenPacket;
import io.github.jodlodi.minions.registry.CommonRegistry;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MastersStaff extends Item {
    public MastersStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext onContext) {
        Player player = onContext.getPlayer();
        if (player != null) {
            if (!player.level.isClientSide) {
                player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(IMasterCapability::sendUpdatePacket);
                PacketRegistry.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new BlockStaffScreenPacket(onContext));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
