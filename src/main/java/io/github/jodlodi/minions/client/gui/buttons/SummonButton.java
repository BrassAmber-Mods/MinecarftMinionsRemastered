package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.SummonPacket;
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
public class SummonButton extends AbstractMastersButton {
    protected final IMasterCapability capability;

    public SummonButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen, IMasterCapability capability) {
        super(x, y, 111, 0, screen);
        this.capability = capability;
    }

    @Override
    public void onPress() {
        PacketRegistry.CHANNEL.sendToServer(new SummonPacket(this.getScreen().getContext().getBlockPos()));
        this.screen.onClose();
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Summon Minion").withStyle(ChatFormatting.BLUE);
    }

    @Override
    protected List<MutableComponent> getTooltip() {
        return List.of(Component.literal("Current Count: " + this.capability.minionCount()).withStyle(ChatFormatting.GRAY));
    }
}
