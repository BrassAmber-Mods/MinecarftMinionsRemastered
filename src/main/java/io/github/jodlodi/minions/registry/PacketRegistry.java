package io.github.jodlodi.minions.registry;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.network.*;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistry {
    private static final String V = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(MinionsRemastered.locate("channel"), () -> V, V::equals, V::equals);

    public static void init() {
        int id = -1;
        CHANNEL.registerMessage(++id, MasterPacket.class, MasterPacket::encode, MasterPacket::new, MasterPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, BlinkPacket.class, BlinkPacket::encode, BlinkPacket::new, BlinkPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, MineDownButtonPacket.class, MineDownButtonPacket::encode, MineDownButtonPacket::new, MineDownButtonPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, ContainerBlockPacket.class, ContainerBlockPacket::encode, ContainerBlockPacket::new, ContainerBlockPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, ContainerEntityPacket.class, ContainerEntityPacket::encode, ContainerEntityPacket::new, ContainerEntityPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, SummonPacket.class, SummonPacket::encode, SummonPacket::new, SummonPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, BanishPacket.class, BanishPacket::encode, BanishPacket::new, BanishPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, StopButtonPacket.class, StopButtonPacket::encode, StopButtonPacket::new, StopButtonPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, PoofPacket.class, PoofPacket::encode, PoofPacket::new, PoofPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, ContainerClearPacket.class, ContainerClearPacket::encode, ContainerClearPacket::new, ContainerClearPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, EntityStaffScreenPacket.class, EntityStaffScreenPacket::encode, EntityStaffScreenPacket::new, EntityStaffScreenPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, BlockStaffScreenPacket.class, BlockStaffScreenPacket::encode, BlockStaffScreenPacket::new, BlockStaffScreenPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, SitPacket.class, SitPacket::encode, SitPacket::new, SitPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, CarryLivingButtonPacket.class, CarryLivingButtonPacket::encode, CarryLivingButtonPacket::new, CarryLivingButtonPacket.Handler::onMessage);
        CHANNEL.registerMessage(++id, MineAheadButtonPacket.class, MineAheadButtonPacket::encode, MineAheadButtonPacket::new, MineAheadButtonPacket.Handler::onMessage);
    }
}
