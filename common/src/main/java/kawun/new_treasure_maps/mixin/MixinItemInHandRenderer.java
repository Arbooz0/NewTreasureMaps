package kawun.new_treasure_maps.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {


    private boolean popPose = false;

    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    private void renderArmWithItem(
          AbstractClientPlayer player,
          float frameInterp,
          float xRot,
          InteractionHand hand,
          float attack,
          ItemStack itemStack,
          float inverseArmHeight,
          PoseStack poseStack,
          SubmitNodeCollector submitNodeCollector,
          int lightCoords,
          CallbackInfo ci
    ) {
        if (popPose) {
            poseStack.popPose();
            popPose = false;
        }

        if (hand == MapRenderer.hideHand) {
            if (MapRenderer.hideTime == 1) {
                ci.cancel();
            } else {
                if (hand == InteractionHand.MAIN_HAND) {
                    popPose = true;
                    poseStack.pushPose();
                }
                poseStack.translate(0, MapRenderer.hideTime * -0.5f, 0);
                if (itemStack.is(Items.TREASURE_MAP)) {
                    HumanoidArm arm = (hand == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
                    MapRenderer.renderClosedMap(itemStack, poseStack, arm, submitNodeCollector, lightCoords);
                    ci.cancel();
                }
            }
            return;
        }

        if (!itemStack.is(Items.TREASURE_MAP)) {
            MapRenderer.setLastItem(hand, null);
            return;
        }

        poseStack.pushPose();
        MapRenderer.render(player, hand, attack, itemStack, inverseArmHeight, poseStack, submitNodeCollector, lightCoords);
        poseStack.popPose();

        ci.cancel();
    }

}