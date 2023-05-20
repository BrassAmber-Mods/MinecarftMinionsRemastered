package io.github.jodlodi.minions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.registry.ClientRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MinionEntityRenderer<T extends Minion> extends HumanoidMobRenderer<T, MinionEntityModel<T>> {
    private static final ResourceLocation RESOURCE_LOCATION = MinionsRemastered.locate("textures/entity/minion.png");

    public MinionEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new MinionEntityModel<>(context.bakeLayer(ClientRegistry.MINION_MODEL)), 0.3F);
        this.layers.removeIf(tMinionEntityModelRenderLayer -> tMinionEntityModelRenderLayer instanceof CustomHeadLayer<T, MinionEntityModel<T>>);
        this.addLayer(new MinionRobesLayer<>(this, context.getModelSet()));
        this.addLayer(new MinionHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(T minion) {
        return RESOURCE_LOCATION;
    }

    @Override
    public void render(T minion, float jaw, float partial, PoseStack stack, MultiBufferSource source, int light) {
        boolean sitting = minion.getSit();
        if (sitting) {
            stack.pushPose();
            stack.translate(0.0D, -0.2D, 0.0D);
        }
        super.render(minion, jaw, partial, stack, source, light);
        if (sitting) stack.popPose();
    }
}
