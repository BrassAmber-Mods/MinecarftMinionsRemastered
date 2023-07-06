package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.CarryLivingButtonPacket;
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
public class CarryLivingOrderButton extends AbstractMastersButton {

    public CarryLivingOrderButton(int x, int y, MastersStaffScreen.EntityStaffScreen screen) {
        super(x, y, 73, 57, screen);
    }

    @Override
    public void onPress() {
        if (this.getScreen().target != null) {
            PacketRegistry.CHANNEL.sendToServer(new CarryLivingButtonPacket(this.getScreen().target.getUUID()));
            this.screen.onClose();
        }
    }

    @Override
    public MastersStaffScreen.EntityStaffScreen getScreen() {
        return (MastersStaffScreen.EntityStaffScreen)this.screen;
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Pick Up ").withStyle(ChatFormatting.BLUE).append(this.getScreen().target.getType().getDescription());
    }
}
