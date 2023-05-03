package io.github.jodlodi.minions.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.registry.ClientRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
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
        this.addLayer(new MinionHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(T minion) {
        return RESOURCE_LOCATION;
    }

    public static class MinionHeadLayer<T extends Minion, M extends MinionEntityModel<T>> extends CustomHeadLayer<T, M> {
        public MinionHeadLayer(RenderLayerParent<T, M> parent, EntityModelSet set, ItemInHandRenderer itemInHandRenderer) {
            super(parent, set, itemInHandRenderer);
        }

        @Override
        public void render(PoseStack stack, MultiBufferSource source, int light, T minion, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack itemstack = minion.getItemBySlot(EquipmentSlot.HEAD);
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                stack.pushPose();
                stack.scale(this.scaleX, this.scaleY, this.scaleZ);
                if (minion.isBaby()) {
                    stack.translate(0.0D, 0.03125D, 0.0D);
                    stack.scale(0.7F, 0.7F, 0.7F);
                    stack.translate(0.0D, 1.0D, 0.0D);
                }

                this.getParentModel().translateToHead(stack);
                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock) {
                    stack.scale(1.1875F, -1.1875F, -1.1875F);

                    GameProfile gameprofile = null;
                    if (itemstack.hasTag()) {
                        CompoundTag compoundtag = itemstack.getTag();
                        if (compoundtag != null && compoundtag.contains("SkullOwner", 10)) {
                            gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                        }
                    }

                    stack.translate(-0.5D, 0.0D, -0.5D);
                    SkullBlock.Type skullblock$type = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
                    SkullModelBase skullmodelbase = this.skullModels.get(skullblock$type);
                    RenderType rendertype = SkullBlockRenderer.getRenderType(skullblock$type, gameprofile);
                    SkullBlockRenderer.renderSkull(null, 180.0F, limbSwing, stack, source, light, skullmodelbase, rendertype);
                } else if (!(item instanceof ArmorItem armorItem) || armorItem.getSlot() != EquipmentSlot.HEAD) {
                    translateToHead(stack, false);
                    this.itemInHandRenderer.renderItem(minion, itemstack, ItemTransforms.TransformType.HEAD, false, stack, source, light);
                }

                stack.popPose();
            }
        }
    }
}
