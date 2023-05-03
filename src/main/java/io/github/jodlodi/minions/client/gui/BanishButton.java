package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.network.BanishPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BanishButton extends MastersButton {
    protected final IMasterCapability capability;

    public BanishButton(int x, int y, MastersStaffScreen.EntityStaffScreen screen, IMasterCapability capability) {
        super(x, y, screen);
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

    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 22, 180, this.width, this.height);
    }

    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 43, 180, this.width, this.height);
        super.renderIcon(stack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onSelectedTick(TickEvent.ClientTickEvent event) {

    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(Component.literal("Banish Minion").withStyle(ChatFormatting.DARK_RED).getVisualOrderText());
    }
}
