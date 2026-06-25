package kawun.new_treasure_maps.mixin;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.commands.pose.PoseCommand;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.client.model.MapModelGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {


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
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!itemStack.is(Items.TREASURE_MAP)) {
          return;
        }

        MapRenderer.render(player, xRot, hand, attack, itemStack, inverseArmHeight, poseStack, submitNodeCollector, lightCoords);

        ci.cancel();
    }
}