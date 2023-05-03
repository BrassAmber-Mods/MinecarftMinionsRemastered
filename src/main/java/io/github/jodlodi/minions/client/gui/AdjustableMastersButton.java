package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public abstract class AdjustableMastersButton extends MastersButton {
    public AdjustableMastersButton(int x, int y, MastersStaffScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.onMiniClick(mouseX, mouseY)) super.onClick(mouseX, mouseY);
    }

    abstract boolean onMiniClick(double mouseX, double mouseY);

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderMiniButtons(stack, mouseX, mouseY, partialTick);
    }

    protected void renderMiniButtons(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);

        boolean left = this.isMouseOverMini(mouseX, mouseY, this.x + 1, this.y + 13, 5, 5);
        this.blit(stack, this.x + 1, this.y + 13, 1, left ? 145 : 138, 5, 5);

        boolean right = this.isMouseOverMini(mouseX, mouseY, this.x + 13, this.y + 13, 5, 5);
        this.blit(stack, this.x + 13, this.y + 13, 8, right ? 145 : 138, 5, 5);
    }

    public boolean isMouseOverMini(int mouseX, int mouseY, int bX, int bY, int bW, int bH) {
        return this.active && this.visible && mouseX >= bX && mouseY >= bY && mouseX < bX + bW && mouseY < bY + bH;
    }
}
