package io.github.jodlodi.minions.event;

import io.github.jodlodi.minions.MastersStaff;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = MinionsRemastered.MODID)
public class CommonEventSubscriber {
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        event.getEntity().getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(IMasterCapability::tick);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player != null && !player.level.isClientSide && player.getItemInHand(event.getHand()).getItem() instanceof MastersStaff) {
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }
}
