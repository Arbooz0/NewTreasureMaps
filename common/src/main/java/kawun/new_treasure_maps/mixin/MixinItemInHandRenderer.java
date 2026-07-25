package kawun.new_treasure_maps.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

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
        if (!itemStack.is(Items.TREASURE_MAP)) {
            MapRenderer.setLastItem(hand, null);
            return;
        }

        poseStack.pushPose();
        MapRenderer.render(player, xRot, hand, attack, itemStack, inverseArmHeight, poseStack, submitNodeCollector, lightCoords);
        poseStack.popPose();

        ci.cancel();
    }

}