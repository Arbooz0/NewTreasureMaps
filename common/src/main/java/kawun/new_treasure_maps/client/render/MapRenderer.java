package kawun.new_treasure_maps.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import kawun.new_treasure_maps.client.animation.Animation;
import kawun.new_treasure_maps.client.model.Model;
import kawun.new_treasure_maps.client.sound.SoundManager;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.client.utils.AnimationTime;
import kawun.new_treasure_maps.client.utils.HandHelper;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.MapComponent;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;

import java.util.HashMap;

public class MapRenderer {

    public static Int2ObjectMap<RenderType> renders = new Int2ObjectOpenHashMap<>();
    public static Int2ObjectMap<RenderType> backRenders = new Int2ObjectOpenHashMap<>();
    public static IntOpenHashSet openMaps = new IntOpenHashSet();
    public static Int2ObjectMap<AnimationTime> animationsTime = new Int2ObjectOpenHashMap<>();
    public static ItemStack lastMainItem = null;
    public static ItemStack lastOffItem = null;
    public static HashMap<HumanoidArm, Object2IntOpenHashMap<PlayerModel>> lastPlayersMap = new HashMap<>();
    public static InteractionHand hideHand = null;
    public static float hideTime = 0;
    public static InteractionHand handStartAnimation = null;



    public static void render(
            AbstractClientPlayer player,
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
            if (hand == getOppositeHand(hideHand)) {
                hideHand = null;
            }
            handStartAnimation = hand;
        }

        Animation animation = data.foldType().animation;
        AnimationTime time = getAnimationTime(data.id());

        if (handStartAnimation == hand) {
            if (inverseArmHeight == 0) {
                handStartAnimation = null;
                if (time != null) {
                    time = new AnimationTime(1);
                    animationsTime.put(data.id(), time);
                }
            } else {
                time = null;
            }
        }

        boolean isMain = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMain ? player.getMainArm() : player.getMainArm().getOpposite();

        float anim = 1;
        if (time == null) {
            HandHelper.defaultArmPose(poseStack, inverseArmHeight, attack, arm);
            HandHelper.setHand(poseStack, arm, submitNodeCollector, lightCoords);
            poseStack.mulPose(animation.getMapOffsetInHand(arm));
        } else {
            time.update();

            if (time.needPlaySound()) {
                SoundManager.playSound();
            }

            hideHand = getOppositeHand(hand);
            if (player.getItemInHand(hand) != itemStack) {
                poseStack.translate(0, inverseArmHeight * -1, 0);
                hideTime = Math.max(1 - inverseArmHeight - 0.2f, 0);
            } else {
                hideTime = Math.min(time.part1 * 2, 1);
            }

            boolean mainHandEmpty = !isMain && player.getMainHandItem().isEmpty();
            if (mainHandEmpty) {
                hideTime = 1;
            }

            animation.animate(poseStack, submitNodeCollector, arm, time, lightCoords, mainHandEmpty);
            if (time.isEnd && time.isReverse) {
                animationsTime.remove(data.id());
                hideHand = null;
            }
            PoseStack old = poseStack;
            poseStack = new PoseStack();
            poseStack.mulPose(old.last().pose());

            anim = 1 - time.part2;
        }

        renderMap(poseStack, data, submitNodeCollector, lightCoords, anim);

    }



    public static void renderClosedMap(
            ItemStack itemStack,
            PoseStack poseStack,
            HumanoidArm arm,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords
    ) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }
        Animation animation = data.foldType().animation;
        HandHelper.defaultArmPose(poseStack, 0, 0, arm);
        HandHelper.setHand(poseStack, arm, submitNodeCollector, lightCoords);
        poseStack.mulPose(animation.getMapOffsetInHand(arm));
        renderMap(poseStack, data, submitNodeCollector, lightCoords, 1);
    }



    public static void renderThirdPerson(
        ItemStack itemStack,
        PlayerModel model,
        AvatarRenderState state,
        HumanoidArm arm,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords
    ) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }

        Object2IntOpenHashMap<PlayerModel> ids = lastPlayersMap.get(arm);
        if (ids.containsKey(model)) {
            if (ids.getInt(model) != data.id()) {
                animationsTime.remove(data.id());
                animationsTime.remove(ids.getInt(model));
                ids.put(model, data.id());
            }
        } else {
            ids.put(model, data.id());
        }

        Animation animation = data.foldType().animation;
        AnimationTime time = getAnimationTime(data.id());

        poseStack.pushPose();

        float anim = 1;
        if (time == null) {
            model.translateToHand(state, arm, poseStack);
            poseStack.mulPose(animation.getMapOffsetThirdPerson(arm));
        } else {
            time.update();
            if (time.needPlaySound()) {
                SoundManager.playSound(state.x, state.y, state.z);
            }
            anim = 1 - time.part2;
            animation.animateThirdPerson(poseStack, submitNodeCollector, arm, time, model, state, lightCoords);
            if (time.isEnd && time.isReverse) {
                animationsTime.remove(data.id());
            }
        }

        renderMap(poseStack, data, submitNodeCollector, lightCoords, anim);
        poseStack.popPose();
    }


    public static void setupAnim(ItemStack itemStack, PlayerModel model, HumanoidArm arm) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }

        AnimationTime time = getAnimationTime(data.id());

        if (time == null) {
            return;
        }

        time.update();
        Animation animation = data.foldType().animation;
        animation.animateArm(model, time, arm);
    }



    private static void renderMap(PoseStack poseStack, MapComponent data, SubmitNodeCollector submitNodeCollector, int light, float anim) {
        Model model = Model.getModel(data.foldType());
        if (model == null) {
            return;
        }

        RenderType renderType = getRenderType(data.id(), data.foldType());
        RenderType backRenderType = getBackRenderType(data.id(), data.foldType());

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            renderModel(model, pose, vertexConsumer, light, anim, true);
        });

        submitNodeCollector.submitCustomGeometry(poseStack, backRenderType, (pose, vertexConsumer) -> {
            renderModel(model, pose, vertexConsumer, light, anim, false);
        });
    }


    public static void renderModel(
            Model model, PoseStack.Pose pose, VertexConsumer vertexConsumer, int lightCoords, float t, boolean front
    ) {
        if (t > 0.3f) {
            float w = 1 - (t - 0.3f) / 5;
            int block = (int) (LightCoordsUtil.block(lightCoords) * w);
            int sky = (int) (LightCoordsUtil.sky(lightCoords) * w);
            lightCoords = LightCoordsUtil.pack(block, sky);
        }

        for (int j = 0; j < model.countVertex; j++) {
            int i = front ? model.countVertex - j - 1 : j;
            Vector2f uv = model.getUV(i);
            vertexConsumer
                    .addVertex(pose, model.getVertex(i, t))
                    .setColor(-1)
                    .setUv(uv.x, uv.y)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightCoords)
                    .setNormal(pose, model.getNormal(i, t).mul(front ? 1 : -1));
        }
    }



    public static ItemStack getLastItem(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? lastMainItem : lastOffItem;
    }

    public static void setLastItem(InteractionHand hand, ItemStack item) {
        if (hideHand != null && item == null) {
            if (hand == getOppositeHand(hideHand)) {
                hideHand = null;
            }
        }
        if (hand == InteractionHand.MAIN_HAND) {
            lastMainItem = item;
        } else {
            lastOffItem = item;
        }
    }


    public static void removeLastMapPlayer(HumanoidArm arm, PlayerModel model) {
        Object2IntOpenHashMap<PlayerModel> ids = lastPlayersMap.get(arm);
        if (ids.containsKey(model)) {
            int id = ids.removeInt(model);
            animationsTime.remove(id);
        }
    }


    public static InteractionHand getOppositeHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }


    public static void toggleOpenMap(int id) {
        setOpenMap(id, !openMaps.contains(id));
    }


    public static void setOpenMap(int id, boolean isOpen) {
        if (isOpen) {
            openMaps.add(id);
        } else {
            openMaps.remove(id);
        }
    }


    public static AnimationTime getAnimationTime(int id) {
        boolean isOpen = openMaps.contains(id);
        AnimationTime time = animationsTime.get(id);
        if (time != null) {
            if (time.isReverse == isOpen) {
                time = time.reverse();
                animationsTime.put(id, time);
            }
        } else {
            if (isOpen) {
                time = new AnimationTime(1);
                animationsTime.put(id, time);
            }
        }
        return time;
    }


    public static RenderType getRenderType(int id, FoldType type) {
        if (renders.containsKey(id)) {
            return renders.get(id);
        }
        RenderType renderType = newRenderType(MapTextureManager.createNewTexture(id, type));
        renders.put(id, renderType);
        return renderType;
    }


    public static RenderType getBackRenderType(int id, FoldType type) {
        if (backRenders.containsKey(id)) {
            return backRenders.get(id);
        }
        RenderType renderType = newRenderType(MapTextureManager.getBackTexture(type));
        backRenders.put(id, renderType);
        return renderType;
    }


    public static RenderType newRenderType(Identifier texture) {
        return RenderTypes.entityCutoutCull(texture);
    }


    public static void clear() {
        renders.clear();
        backRenders.clear();
        openMaps.clear();
        animationsTime.clear();
        lastPlayersMap.get(HumanoidArm.RIGHT).clear();
        lastPlayersMap.get(HumanoidArm.LEFT).clear();
        lastMainItem = null;
        lastOffItem = null;
        hideHand = null;
        handStartAnimation = null;
    }


    static {
        lastPlayersMap.put(HumanoidArm.RIGHT, new Object2IntOpenHashMap<>());
        lastPlayersMap.put(HumanoidArm.LEFT, new Object2IntOpenHashMap<>());
    }

}
