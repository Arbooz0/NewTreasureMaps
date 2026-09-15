package kawun.new_treasure_maps.client.model_property;

import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.MapComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class MapTypeModelProperty implements ClampedItemPropertyFunction {


    @Override
    public float unclampedCall(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        return data == null ? 0 : (data.mapType().ordinal() / 10.0F);
    }
}
