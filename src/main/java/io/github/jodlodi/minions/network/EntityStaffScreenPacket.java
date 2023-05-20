package io.github.jodlodi.minions.network;

import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class EntityStaffScreenPacket {
    private final int entityID;
    private final boolean hostile;

    public EntityStaffScreenPacket(Entity entity, Player player) {
        this.entityID = entity.getId();
        this.hostile = !player.getAbilities().instabuild && ((entity instanceof NeutralMob neutralMob && neutralMob.isAngryAt(player)) || entity instanceof Enemy);
    }

    public EntityStaffScreenPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readInt();
        this.hostile = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeBoolean(this.hostile);
    }

    public static class Handler {
        @SuppressWarnings("Convert2Lambda")
        public static void onMessage(EntityStaffScreenPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    Minecraft minecraft = Minecraft.getInstance();
                    LocalPlayer player = minecraft.player;
                    if (player != null) {
                        Entity target = player.level.getEntity(message.entityID);
                        if (target != null) minecraft.setScreen(MastersStaffScreen.make(player, new EntityHitResult(target), message.hostile));
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
