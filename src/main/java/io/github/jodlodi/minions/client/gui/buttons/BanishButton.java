package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.network.BanishPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BanishButton extends AbstractMastersButton {
    protected final IMasterCapability capability;

    public BanishButton(int x, int y, MastersStaffScreen.EntityStaffScreen screen, IMasterCapability capability) {
        super(x, y, 130, 0, screen);
        this.capability = capability;
    }

    @Override
    public void onPress() {
        if (this.getScreen().target instanceof Minion minion && this.capability.isMinion(minion.getUUID())) {
            PacketRegistry.CHANNEL.sendToServer(new BanishPacket(minion.getUUID()));
            this.screen.onClose();
        }
    }

    @Override
    public MastersStaffScreen.EntityStaffScreen getScreen() {
        return (MastersStaffScreen.EntityStaffScreen)this.screen;
    }

    @Override
    protected MutableComponent getName() {
        Component name = Component.literal("Minion");
        if (this.getScreen().target instanceof Minion minion && this.capability.isMinion(minion.getUUID())) {
            name = minion.getName();
        }
        return Component.literal("Banish ").append(name).withStyle(ChatFormatting.DARK_RED);
    }
}
