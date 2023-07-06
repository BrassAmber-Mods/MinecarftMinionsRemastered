package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractAdjustableOrderButton extends AbstractMastersButton {
    protected final boolean leftAndRight;
    protected final boolean toggle;
    protected final boolean pausable;

    public AbstractAdjustableOrderButton(int x, int y, int textureX, int textureY, MastersStaffScreen screen, boolean leftAndRight, boolean toggle, boolean pausable) {
        super(x, y, textureX, textureY, screen);
        this.leftAndRight = leftAndRight;
        this.toggle = toggle;
        this.pausable = pausable;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.leftAndRight && this.isMouseOverLeftMini((int)mouseX, (int)mouseY)) {
            this.onLeftPress();
            return;
        }

        if (this.toggle && this.isMouseOverToggleMini((int)mouseX, (int)mouseY)) {
            this.onTogglePress();
            return;
        }

        if (this.leftAndRight && this.isMouseOverRightMini((int)mouseX, (int)mouseY)) {
            this.onRightPress();
            return;
        }


        super.onClick(mouseX, mouseY);
    }

    abstract void onLeftPress();

    abstract void onTogglePress();

    abstract void onRightPress();

    @Override
    protected void renderExtra(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        super.renderExtra(stack, mouseX, mouseY, partialTick);
        RenderSystem.setShaderTexture(0, this.getShaderTexture(mouseX, mouseY));

        if (this.leftAndRight) {
            boolean left = this.isMouseOverLeftMini(mouseX, mouseY);
            this.blit(stack, this.x + 1, this.y + 11, 225, left ? 5 : 0, 5, 5);

            boolean right = this.isMouseOverRightMini(mouseX, mouseY);
            this.blit(stack, this.x + 13, this.y + 11, 230, right ? 5 : 0, 5, 5);
        }

        if (this.toggle) {
            boolean toggle = this.isMouseOverToggleMini(mouseX, mouseY);
            this.blit(stack, this.x + 7, this.y + 13, 235, toggle ? 5 : 0, 5, 5);
        }
    }

    public boolean isMouseOverLeftMini(int mouseX, int mouseY) {
        return this.leftAndRight && this.active && this.visible && this.isMouseOver(mouseX, mouseY) && mouseX <= this.x + 6 && mouseY >= this.y + 11;
    }

    public boolean isMouseOverToggleMini(int mouseX, int mouseY) {
        return this.toggle && this.active && this.visible && this.isMouseOver(mouseX, mouseY) && mouseX >= this.x + 7 && mouseX <= this.x + 12 && mouseY >= this.y + 13;
    }

    public boolean isMouseOverRightMini(int mouseX, int mouseY) {
        return this.leftAndRight && this.active && this.visible && this.isMouseOver(mouseX, mouseY) && mouseX >= this.x + 13 && mouseY >= this.y + 11;
    }

    @Override
    protected final List<MutableComponent> getTooltip() {
        List<MutableComponent> list = new ArrayList<>(this.getAdjustableTooltip());
        if (this.pausable) list.add(Component.literal("[SHIFT]").withStyle(this.screen.isShiftKeyDown() ? ChatFormatting.AQUA : ChatFormatting.DARK_RED).append(Component.literal(" Preview mode").withStyle(ChatFormatting.DARK_GRAY)));
        return list;
    }

    abstract protected List<MutableComponent> getAdjustableTooltip();
}
