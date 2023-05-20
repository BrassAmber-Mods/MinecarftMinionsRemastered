package io.github.jodlodi.minions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.registry.ClientRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinionRobesLayer<T extends Minion, M extends MinionEntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation RESOURCE_LOCATION = MinionsRemastered.locate("textures/entity/minion_robes.png");
    private final MinionEntityModel<T> minionModel;

    public MinionRobesLayer(RenderLayerParent<T, M> parent, EntityModelSet set) {
        super(parent);
        this.minionModel = new MinionEntityModel<>(set.bakeLayer(ClientRegistry.MINION_ROBES));
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource source, int light, T minion, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        int i = minion.getColor();
        float r = (float)(i >> 16 & 255) / 255.0F;
        float g = (float)(i >> 8 & 255) / 255.0F;
        float b = (float)(i & 255) / 255.0F;
        RenderLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), this.minionModel, RESOURCE_LOCATION, stack, source, light, minion, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, r, g, b);
    }

}
