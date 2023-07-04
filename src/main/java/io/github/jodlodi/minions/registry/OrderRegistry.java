package io.github.jodlodi.minions.registry;

import io.github.jodlodi.minions.orders.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class OrderRegistry {
    public static final Map<ResourceLocation, Function<CompoundTag, AbstractOrder>> ORDERS = new HashMap<>();

    public static void init() {
        ORDERS.put(MineDownOrder.ID, MineDownOrder::new);
        ORDERS.put(FollowMasterOrder.ID, FollowMasterOrder::new);
        ORDERS.put(CarryLivingOrder.ID, CarryLivingOrder::new);
        ORDERS.put(MineAheadOrder.ID, MineAheadOrder::new);
        ORDERS.put(ChopOrder.ID, ChopOrder::new);
        ORDERS.put(HoeWartOrder.ID, HoeWartOrder::new);
    }
}
