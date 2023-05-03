package io.github.jodlodi.minions.event;

import io.github.jodlodi.minions.MastersStaff;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = MinionsRemastered.MODID, value = Dist.CLIENT)
public class ClientEventSubscriber {
    @SubscribeEvent
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof MastersStaffScreen mastersScreen) {
            Optional.ofNullable(mastersScreen.getSelectedButton()).ifPresent(mastersButton -> mastersButton.onSelectedTick(event));
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof LocalPlayer localPlayer && localPlayer.getItemInHand(event.getHand()).getItem() instanceof MastersStaff) {
            Minecraft.getInstance().setScreen(MastersStaffScreen.make(localPlayer, new EntityHitResult(event.getTarget())));
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseStaffEvent(UseStaffEvent event) {
        if (event.getEntity() instanceof LocalPlayer localPlayer) {
            Minecraft.getInstance().setScreen(MastersStaffScreen.make(localPlayer, event.getBlockHitResult()));
        }
    }

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent event) {
        ItemStack item = event.getItemStack();
        if (item.is(CommonRegistry.MASTERS_STAFF.get())) {
            event.getToolTip().add(Component.literal("THE CRAFTING RECIPE AND THE MOD ARE STILL A WORK IN PROGRESS"));
        }
    }
}
