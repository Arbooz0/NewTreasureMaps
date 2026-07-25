package kawun.new_treasure_maps.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kawun.new_treasure_maps.Constants;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;


public class MapModelGenerator {


    public static float anim = 0;


    public static void update(
            Model model, PoseStack.Pose pose, VertexConsumer vertexConsumer, int lightCoords, float t, boolean front
    ) {
        anim = t;
        if (anim > 0.3f) {
            float w = 1 - (anim - 0.3f) / 5;
            int block = (int) (LightCoordsUtil.block(lightCoords) * w);
            int sky = (int) (LightCoordsUtil.sky(lightCoords) * w);
            lightCoords = LightCoordsUtil.pack(block, sky);
        }

        for (int j = 0; j < model.countVertex; j++) {
            int i = front ? model.countVertex - j - 1 : j;
            Vector2f uv = model.getUV(i);
            vertexConsumer
                    .addVertex(pose, model.getVertex(i, anim))
                    .setColor(-1)
                    .setUv(uv.x, uv.y)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightCoords)
                    .setNormal(pose, model.getNormal(i, anim).mul(front ? 1 : -1));
        }
    }

}
