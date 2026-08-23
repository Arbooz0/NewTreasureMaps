package kawun.new_treasure_maps.mixin;


import kawun.new_treasure_maps.client.model_property.MapTypeModelProperty;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SelectItemModelProperties.class)
public class MixinSelectItemModelProperties {


    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, SelectItemModelProperty.Type<?, ?>> ID_MAPPER;

    @Inject(at = @At("TAIL"), method = "bootstrap")
    private static void onBootstrap(CallbackInfo ci) {
        ID_MAPPER.put(Utils.identifier("map_type"), MapTypeModelProperty.TYPE);
    }
}
