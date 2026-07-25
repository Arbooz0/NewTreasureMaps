package kawun.new_treasure_maps.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayer<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<S>> extends RenderLayer<S, M> {


    public MixinItemInHandLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Inject(at = @At("HEAD"), method = "submitArmWithItem", cancellable = true)
    protected void submitArmWithItem(
            S state,
            ItemStackRenderState item,
            ItemStack itemStack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            CallbackInfo ci
    ) {
        if (!itemStack.is(Items.TREASURE_MAP)) {
            return;
        }

        EntityModel<S> model = this.getParentModel();
        if (model instanceof HumanoidModel<?> humanoidModel) {
            MapRenderer.renderThirdPerson(itemStack, humanoidModel, arm, poseStack, submitNodeCollector, lightCoords);
        }

        ci.cancel();
    }
}

