package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.StopButtonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StopOrderButton extends AbstractMastersButton {

    public StopOrderButton(int x, int y, MastersStaffScreen screen) {
        super(x, y, 206, 0, screen);
    }

    @Override
    public void onPress() {
        PacketRegistry.CHANNEL.sendToServer(new StopButtonPacket(this.screen.isShiftKeyDown()));
        this.screen.onClose();
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Pause / play order").withStyle(ChatFormatting.DARK_RED);
    }

    @Override
    protected List<MutableComponent> getTooltip() {
        return List.of(Component.literal("[SHIFT]").withStyle(this.screen.isShiftKeyDown() ? ChatFormatting.AQUA : ChatFormatting.DARK_RED).append(Component.literal(" Cancel Order").withStyle(ChatFormatting.DARK_GRAY)));
    }
}
