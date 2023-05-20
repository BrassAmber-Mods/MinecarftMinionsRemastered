package io.github.jodlodi.minions.registry;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.client.MinionEntityModel;
import io.github.jodlodi.minions.client.MinionEntityRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = MinionsRemastered.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {
    public static final ModelLayerLocation MINION_MODEL = new ModelLayerLocation(MinionsRemastered.locate("minion_model"), "main");
    public static final ModelLayerLocation MINION_ROBES = new ModelLayerLocation(MinionsRemastered.locate("minion_robes"), "main");

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MINION_MODEL, MinionEntityModel::createBodyLayer);
        event.registerLayerDefinition(MINION_ROBES, MinionEntityModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(CommonRegistry.MINION.get(), MinionEntityRenderer::new);
    }
}
