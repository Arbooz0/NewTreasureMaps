package kawun.new_treasure_maps.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.Constants;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

//    @ModifyConstant(
//            // Указываем имя синтетического метода лямбды и его полный дескриптор
//            method = "lambda$renderMap$0(ILcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
//            constant = @Constant(floatValue = 135.0F, ordinal = 0),
//            remap = false // Перекомпиляция имени лямбды обычно не мапится стандартным образом
//    )
//    private static float modifyMapVertexY(float original) {
//        // Возвращаем ваше новое значение вместо 135.0F
//        return (float) Math.random() * 100.0F + 50.0F;
//    }
//
//      @Inject(at = @At("HEAD"), method = "renderMap")
//      private void renderMap(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ItemStack itemStack, CallbackInfo ci) {
//          poseStack.rotateAround(new Quaternionf(0.1, 0.0, 0.0, 0.0), 1.0F, 0.0F, 0.0F);
//      }

      @Overwrite
      private float calculateMapTilt(float xRot) {
          return 0.0F;
      }
}