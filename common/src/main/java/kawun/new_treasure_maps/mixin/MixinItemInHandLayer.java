package kawun.new_treasure_maps.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerItemInHandLayer.class)
public abstract class MixinItemInHandLayer<S extends AvatarRenderState, M extends EntityModel<S> & ArmedModel<S> & HeadedModel> extends ItemInHandLayer<S, M> {


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
            if (this.getParentModel() instanceof PlayerModel playerModel) {
                MapRenderer.removeLastMapPlayer(arm, playerModel);
            }
            return;
        }

        if (this.getParentModel() instanceof PlayerModel playerModel) {
            MapRenderer.renderThirdPerson(itemStack, playerModel, state, arm, poseStack, submitNodeCollector, lightCoords);
        }

        ci.cancel();
    }
}

