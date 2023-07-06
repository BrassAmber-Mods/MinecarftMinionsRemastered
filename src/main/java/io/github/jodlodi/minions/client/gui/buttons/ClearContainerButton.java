package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.ContainerClearPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClearContainerButton extends AbstractMastersButton {
    public ClearContainerButton(int x, int y, MastersStaffScreen screen) {
        super(x, y, 187, 0, screen);
    }

    @Override
    public void onPress() {
        PacketRegistry.CHANNEL.sendToServer(new ContainerClearPacket());
        this.screen.onClose();
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Remove Container").withStyle(ChatFormatting.BLUE);
    }
}
