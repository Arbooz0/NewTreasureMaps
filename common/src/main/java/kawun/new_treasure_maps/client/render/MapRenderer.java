package kawun.new_treasure_maps.client.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.model.MapModelGenerator;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.commands.pose.PoseCommand;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.MapComponent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.joml.Matrix4f;

public class MapRenderer {

    private static final RenderPipeline.Snippet snippet = RenderPipeline
            .builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
            .withVertexShader("core/entity")
            .withFragmentShader("core/entity")
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();
    private static final RenderPipeline pipeline = RenderPipeline.builder(snippet)
            .withLocation("pipeline/treasure_map")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("PER_FACE_LIGHTING")
            .withSampler("Sampler1")
            .withCull(false)
            .build();

    public static Int2ObjectMap<RenderType> renders = new Int2ObjectOpenHashMap<>();
    private static final MapModelGenerator mapGenerator = new MapModelGenerator();



    public static void render(
            AbstractClientPlayer player,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack itemStack,
            float inverseArmHeight,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords
    ) {
        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();

        poseStack.mulPose(PoseCommand.pose.pose());

        poseStack.mulPose(new Matrix4f().rotateLocalX(xRot * ((float) Math.PI / 180F)));

        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }

        RenderType renderType;
        if (renders.containsKey(data.id())) {
            renderType = renders.get(data.id());
        } else {
            renderType = newRenderType(data.id(), data.foldType());
        }

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            mapGenerator.update(pose, vertexConsumer, lightCoords, inverseArmHeight);
        });
    }



    public static RenderType newRenderType(int id, FoldType type) {
        RenderSetup render_state = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", MapTextureManager.createNewTexture(id, type))
                .useLightmap().useOverlay().affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();

        RenderType render_type = RenderType.create("treasure_map", render_state);
        renders.put(id, render_type);
        return render_type;
    }
}
