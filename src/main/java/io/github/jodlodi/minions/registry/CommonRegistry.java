package io.github.jodlodi.minions.registry;

import io.github.jodlodi.minions.MastersStaff;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.capabilities.MasterCapabilityHandler;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = MinionsRemastered.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MinionsRemastered.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MinionsRemastered.MODID);

    public static final Capability<IMasterCapability> MASTER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final RegistryObject<EntityType<Minion>> MINION = ENTITY_TYPES.register("minion", () ->
            EntityType.Builder.<Minion>of((type, level) -> new Minion(level), MobCategory.CREATURE)
                    .sized(0.6F, 0.95F)
                    .clientTrackingRange(80)
                    .fireImmune()
                    .updateInterval(3).build("minion"));

    public static final RegistryObject<Item> MINION_EGG = ITEMS.register("minion_spawn_egg", () -> new ForgeSpawnEggItem(MINION, 0xA03333, 0x3B3836, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> MASTERS_STAFF = ITEMS.register("masters_staff", () -> new MastersStaff(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).rarity(MinionsRemastered.RARITY)));

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(MINION.get(), Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D).build());

    }

    public static void capabilitySetup(RegisterCapabilitiesEvent event) {
        event.register(IMasterCapability.class);
    }

    public static void addCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(IMasterCapability.ID, new ICapabilitySerializable<CompoundTag>() {
                final LazyOptional<IMasterCapability> capabilityLazyOptional = LazyOptional.of(() ->
                        Util.make(new MasterCapabilityHandler(), masterCapabilityHandler -> masterCapabilityHandler.setEntity(player)));

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
                    return MASTER_CAPABILITY.orEmpty(capability, this.capabilityLazyOptional.cast());
                }

                @Override
                public CompoundTag serializeNBT() {
                    return this.capabilityLazyOptional.orElseThrow(NullPointerException::new).serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag compoundTag) {
                    this.capabilityLazyOptional.orElseThrow(NullPointerException::new).deserializeNBT(compoundTag);
                }
            });
        }
    }
}
