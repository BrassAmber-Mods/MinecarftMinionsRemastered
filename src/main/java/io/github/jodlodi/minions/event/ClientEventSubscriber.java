package io.github.jodlodi.minions.event;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack item = event.getItemStack();
        if (item.is(CommonRegistry.MASTERS_STAFF.get())) {
            event.getToolTip().add(Component.literal("THE CRAFTING RECIPE AND THE MOD ARE STILL A WORK IN PROGRESS"));//FIXME
        }
    }

    @SubscribeEvent
    public static <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving(RenderLivingEvent.Pre<T, M> event) {
        if (event.getEntity().getVehicle() instanceof Minion minion && minion.getBlinking()) {
            event.setCanceled(true);
        }
    }
}
