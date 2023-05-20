package io.github.jodlodi.minions.event;

import io.github.jodlodi.minions.MastersStaff;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.network.EntityStaffScreenPacket;
import io.github.jodlodi.minions.registry.CommonRegistry;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

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
        if (player != null && player.getItemInHand(event.getHand()).getItem() instanceof MastersStaff) {
            if (!player.level.isClientSide) {
                player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(IMasterCapability::sendUpdatePacket);
                PacketRegistry.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new EntityStaffScreenPacket(event.getTarget(), player));
            }
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource() == DamageSource.IN_WALL && event.getEntity().getVehicle() instanceof Minion) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerEventClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        original.reviveCaps();

        original.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(oldCap ->
                player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(newCap ->
                        newCap.deserializeNBT(oldCap.serializeNBT())));

        original.invalidateCaps();
    }
}
