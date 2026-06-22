package kawun.new_treasure_maps.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class MapRenderer implements SpecialModelRenderer<ItemStack> {

    public static final RenderType MAP_BACKGROUND = RenderTypes.text(Identifier.withDefaultNamespace("textures/map/map_background.png"));

    public static PoseStack.Pose pose = new PoseStack.Pose();
    public static PoseStack.Pose base_pose;

    public static void regiter(BiConsumer<Identifier, MapCodec<MapRenderer.Unbaked>> consumer) {
        Constants.LOG.info("+ register");
        consumer.accept(Utils.identifier("map"), Unbaked.MAP_CODEC);
    }



    @Override
    public void submit(@Nullable ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int i1, boolean b, int i2) {
        //Constants.LOG.info("+ submit: " + itemStack);

        base_pose = poseStack.last();

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);

        poseStack.last().mulPose(pose.pose());

        submitNodeCollector.submitCustomGeometry(poseStack, MAP_BACKGROUND, (pose, buffer) -> {
            buffer.addVertex(pose, -5.0F, 5.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(lightCoords);
            buffer.addVertex(pose, 5.0F, 5.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(lightCoords);
            buffer.addVertex(pose, 5.0F, -5.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(lightCoords);
            buffer.addVertex(pose, -5.0F, -5.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(lightCoords);
        });


    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        Constants.LOG.info("+ getExtents");
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack itemStack) {
        //Constants.LOG.info("+ extractArgument");
        return itemStack;
    }



    public static record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<MapRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new MapRenderer.Unbaked());

        public Unbaked() {
        }

        public MapCodec<MapRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        public MapRenderer bake(SpecialModelRenderer.BakingContext context) {
            Constants.LOG.info("+ bake");
            return new MapRenderer();
        }
    }


}
