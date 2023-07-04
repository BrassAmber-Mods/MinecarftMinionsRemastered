package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 130, 0, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 130, 19, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 130, 38, this.width, this.height);
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
