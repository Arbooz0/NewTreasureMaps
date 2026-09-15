package kawun.new_treasure_maps.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerItemInHandLayer.class)
public abstract class MixinItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {


    public MixinItemInHandLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
    }

    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    protected void submitArmWithItem(
            LivingEntity entity,
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource submitNodeCollector,
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
            MapRenderer.renderThirdPerson(itemStack, playerModel, entity, arm, poseStack, submitNodeCollector, lightCoords);
        }

        ci.cancel();
    }
}

