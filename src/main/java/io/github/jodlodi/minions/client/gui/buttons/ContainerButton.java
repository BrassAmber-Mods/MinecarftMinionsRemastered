package io.github.jodlodi.minions.client.gui.buttons;

import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.ContainerBlockPacket;
import io.github.jodlodi.minions.network.ContainerEntityPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerButton extends AbstractMastersButton {
    public ContainerButton(int x, int y, MastersStaffScreen screen) {
        super(x, y, 168, 0, screen);
    }

    @Override
    public void onPress() {
        if (this.getScreen().getContext() instanceof BlockHitResult blockHitResult) {
            PacketRegistry.CHANNEL.sendToServer(new ContainerBlockPacket(blockHitResult.getBlockPos()));
        } else if (this.getScreen().getContext() instanceof EntityHitResult entityHitResult) {
            PacketRegistry.CHANNEL.sendToServer(new ContainerEntityPacket(entityHitResult.getEntity().getUUID()));
        }
        this.screen.onClose();
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Set Container").withStyle(ChatFormatting.BLUE);
    }
}
