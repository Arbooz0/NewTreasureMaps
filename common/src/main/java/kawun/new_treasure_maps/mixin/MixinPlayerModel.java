package kawun.new_treasure_maps.mixin;


import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerModel.class)
public class MixinPlayerModel {



    @Inject(at = @At("TAIL"), method = "setupAnim")
    public void setupAnim(AvatarRenderState state, CallbackInfo ci) {
        ItemStack itemStack = state.getUseItemStackForArm(state.mainArm);
        if (itemStack.is(Items.TREASURE_MAP)) {
            PlayerModel model = (PlayerModel) (Object) this;
            MapRenderer.setupAnim(itemStack, model, state.mainArm);
            return;
        }
        itemStack = state.getUseItemStackForArm(state.mainArm.getOpposite());
        if (itemStack.is(Items.TREASURE_MAP)) {
            PlayerModel model = (PlayerModel) (Object) this;
            MapRenderer.setupAnim(itemStack, model, state.mainArm.getOpposite());
        }
    }

}
