package kawun.new_treasure_maps.mixin;


import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerModel.class)
public class MixinPlayerModel {



    @Inject(at = @At("TAIL"), method = "setupAnim")
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        AbstractClientPlayer player = (AbstractClientPlayer) entity;
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.is(Items.TREASURE_MAP)) {
            PlayerModel model = (PlayerModel) (Object) this;
            MapRenderer.setupAnim(itemStack, model, player.getMainArm());
            return;
        }
        itemStack = player.getOffhandItem();
        if (itemStack.is(Items.TREASURE_MAP)) {
            PlayerModel model = (PlayerModel) (Object) this;
            MapRenderer.setupAnim(itemStack, model, player.getMainArm().getOpposite());
        }
    }

}
