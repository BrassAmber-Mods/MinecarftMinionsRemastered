package io.github.jodlodi.minions.registry;

import io.github.jodlodi.minions.orders.AbstractOrder;
import io.github.jodlodi.minions.orders.CarryLivingOrder;
import io.github.jodlodi.minions.orders.FollowMasterOrder;
import io.github.jodlodi.minions.orders.MineDownOrder;
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
    }
}
