package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.network.SitPacket;
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
public class SitButton extends AbstractMastersButton {
    protected final IMasterCapability capability;

    public SitButton(int x, int y, MastersStaffScreen.EntityStaffScreen screen, IMasterCapability capability) {
        super(x, y, screen);
        this.capability = capability;
    }

    @Override
    public void onPress() {
        if (this.getScreen().target instanceof Minion minion && this.capability.isMinion(minion.getUUID())) {
            PacketRegistry.CHANNEL.sendToServer(new SitPacket(minion.getUUID()));
            this.screen.onClose();
        }
    }

    @Override
    public MastersStaffScreen.EntityStaffScreen getScreen() {
        return (MastersStaffScreen.EntityStaffScreen)this.screen;
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 54, 57, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 54, 76, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 54, 95, this.width, this.height);
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Sit").withStyle(ChatFormatting.BLUE);
    }
}
