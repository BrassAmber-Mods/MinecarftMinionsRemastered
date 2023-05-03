package io.github.jodlodi.minions;

import io.github.jodlodi.minions.event.UseStaffEvent;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;

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
            player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(capability -> {
                player.getCooldowns().addCooldown(this, 10);
                MinecraftForge.EVENT_BUS.post(new UseStaffEvent(player, this.contextToHitResult(onContext)));
            });
        }

        return InteractionResult.SUCCESS;
    }

    protected BlockHitResult contextToHitResult(UseOnContext context) {
        return new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), false);
    }
}
