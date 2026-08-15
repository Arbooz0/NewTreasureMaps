package kawun.new_treasure_maps.client.model_property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.MapComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MapTypeModelProperty implements SelectItemModelProperty<String> {

    public static final Type<MapTypeModelProperty, String> TYPE = Type.create(MapCodec.unit(new MapTypeModelProperty()), Codec.STRING);

    @Override
    public @Nullable String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        return data == null ? "" : data.mapType().name();
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public Type<? extends SelectItemModelProperty<String>, String> type() {
        return TYPE;
    }
}
