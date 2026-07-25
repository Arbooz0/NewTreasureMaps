package kawun.new_treasure_maps.client.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.animation.AccordionAnimation;
import kawun.new_treasure_maps.client.animation.Animation;
import kawun.new_treasure_maps.client.model.MapModelGenerator;
import kawun.new_treasure_maps.client.model.Model;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.client.utils.AnimationTime;
import kawun.new_treasure_maps.client.utils.HandHelper;
import kawun.new_treasure_maps.commands.PoseCommand;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.MapComponent;
import kawun.new_treasure_maps.mixin.MixinItemInHandRenderer;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.HashMap;

public class MapRenderer {

    private static final RenderPipeline.Snippet snippet = RenderPipeline
            .builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
            .withVertexShader("core/entity")
            .withFragmentShader("core/entity")
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();
    private static final RenderPipeline pipeline = RenderPipeline.builder(snippet)
            .withLocation("pipeline/treasure_map")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("PER_FACE_LIGHTING")
            .withSampler("Sampler1")
            .withCull(true)
            .build();

    public static Int2ObjectMap<RenderType> renders = new Int2ObjectOpenHashMap<>();
    public static HashMap<FoldType, RenderType> backRenders = new HashMap<>();
    public static Int2ObjectMap<AnimationTime> animationsTime = new Int2ObjectOpenHashMap<>();
    public static ItemStack lastMainItem = null;
    public static ItemStack lastOffItem = null;
    public static boolean isStartAnimation = false;


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
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }

        if (!itemStack.equals(getLastItem(hand))) {
            setLastItem(hand, itemStack);
            isStartAnimation = true;
        }

        Animation animation = data.foldType().animation;
        AnimationTime time = animationsTime.get(data.id());

        if (isStartAnimation) {
            if (inverseArmHeight == 0) {
                isStartAnimation = false;
                if (time != null) {
                    time = new AnimationTime(1);
                    animationsTime.put(data.id(), time);
                }
            } else {
                time = null;
            }
        }

        if (time != null) {
            if (player.getItemInHand(hand) != itemStack) {
                poseStack.translate(0, inverseArmHeight * -1, 0);
            }
        }


        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();

        float anim;
        if (time == null) {
            HandHelper.defaultArmPose(poseStack, inverseArmHeight, attack, arm);
            HandHelper.setHand(poseStack, arm, submitNodeCollector, lightCoords);
            poseStack.mulPose(animation.getMapOffsetInHand(arm));
            anim = 1;
        } else {
            time.update();
            inverseArmHeight *= 1 - time.part1;
            attack *= 1 - time.part1;
            anim = 1 - time.part2;
            animation.animate(poseStack, submitNodeCollector, arm, time, lightCoords, inverseArmHeight, attack);
            if (time.isEnd && time.isReverse) {
                animationsTime.remove(data.id());
            }
            PoseStack old = poseStack;
            poseStack = new PoseStack();
            poseStack.mulPose(old.last().pose());
        }

        //poseStack.mulPose(PoseCommand.pose.pose());

        renderMap(poseStack, data, submitNodeCollector, lightCoords, anim);

    }


    public static void renderThirdPerson(
        ItemStack itemStack,
        HumanoidModel<?> model,
        HumanoidArm arm,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords
    ) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }

        float t = (float) Math.sin(System.currentTimeMillis() / 1000.0);
        model.leftArm.xRot = t * 45;
        model.rightArm.yRot = t * 90;


        renderMap(poseStack, data, submitNodeCollector, lightCoords, t);
    }



    private static void renderMap(PoseStack poseStack, MapComponent data, SubmitNodeCollector submitNodeCollector, int light, float anim) {
        Model model = Model.getModel(data.foldType());
        if (model == null) {
            return;
        }

        RenderType renderType = getRenderType(data.id(), data.foldType());
        RenderType backRenderType = getBackRenderType(data.foldType());

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            MapModelGenerator.update(model, pose, vertexConsumer, light, anim, true);
        });

        submitNodeCollector.submitCustomGeometry(poseStack, backRenderType, (pose, vertexConsumer) -> {
            MapModelGenerator.update(model, pose, vertexConsumer, light, anim, false);
        });
    }



    public static ItemStack getLastItem(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? lastMainItem : lastOffItem;
    }

    public static void setLastItem(InteractionHand hand, ItemStack item) {
        if (hand == InteractionHand.MAIN_HAND) {
            lastMainItem = item;
        } else {
            lastOffItem = item;
        }
    }


    public static void startAnimation(int id) {
        if (animationsTime.containsKey(id)) {
            animationsTime.put(id, animationsTime.get(id).reverse());
        } else {
            animationsTime.put(id, new AnimationTime(1));
        }
    }


    public static RenderType getRenderType(int id, FoldType type) {
        if (renders.containsKey(id)) {
            return renders.get(id);
        }
        RenderType renderType = newRenderType(MapTextureManager.createNewTexture(id, type));
        renders.put(id, renderType);
        return renderType;
    }


    public static RenderType getBackRenderType(FoldType type) {
        if (backRenders.containsKey(type)) {
            return backRenders.get(type);
        }
        RenderType renderType = newRenderType(MapTextureManager.createNewBackTexture(type));
        backRenders.put(type, renderType);
        return renderType;
    }


    public static RenderType newRenderType(Identifier texture) {
        RenderSetup render_state = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", texture)
                .useLightmap().useOverlay().affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.IS_OUTLINE).createRenderSetup();

        return RenderType.create("treasure_map", render_state);
    }

}
