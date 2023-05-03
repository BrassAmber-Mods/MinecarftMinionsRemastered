package io.github.jodlodi.minions;

import com.mojang.logging.LogUtils;
import io.github.jodlodi.minions.event.ClientEventSubscriber;
import io.github.jodlodi.minions.registry.PacketRegistry;
import io.github.jodlodi.minions.registry.OrderRegistry;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@Mod(MinionsRemastered.MODID)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinionsRemastered {
    public static final String MODID = "minions_remastered";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Rarity RARITY = Rarity.create("MASTER", ChatFormatting.RED);

    public MinionsRemastered() {
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CommonRegistry::addCapabilities);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CommonRegistry.ENTITY_TYPES.register(eventBus);
        CommonRegistry.ITEMS.register(eventBus);

        eventBus.addListener(this::commonSetup);
        eventBus.addListener(CommonRegistry::capabilitySetup);
        Class clas = ClientEventSubscriber.class;

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketRegistry.init();
        OrderRegistry.init();
    }

    public static ResourceLocation locate(String id) {
        return new ResourceLocation(MODID, id);
    }

    //TODO: MOD compat (kobold skins with TF installed)
}
