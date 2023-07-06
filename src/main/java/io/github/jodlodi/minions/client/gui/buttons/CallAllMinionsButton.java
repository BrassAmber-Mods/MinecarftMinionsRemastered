package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.BanishPacket;
import io.github.jodlodi.minions.network.SummonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CallAllMinionsButton extends AbstractMastersButton {
    protected final IMasterCapability capability;

    public CallAllMinionsButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen, IMasterCapability capability) {
        super(x, y, 149, 0, screen);
        this.capability = capability;
    }

    @Override
    public void onPress() {
        BlockPos pos = this.getScreen().getContext().getBlockPos();
        for (UUID uuid : this.capability.getMinions()) {
            if (uuid != null) {
                int index = this.capability.getMinions().indexOf(uuid);
                PacketRegistry.CHANNEL.sendToServer(new BanishPacket(uuid));
                PacketRegistry.CHANNEL.sendToServer(new SummonPacket(pos, index));
            }
        }
        this.screen.onClose();
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Call Minions").withStyle(ChatFormatting.BLUE);
    }
}
