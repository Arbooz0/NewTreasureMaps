package kawun.new_treasure_maps.items;

import kawun.new_treasure_maps.Constants;
import net.minecraft.world.item.Item;

public class TreasureMap extends Item {


    public TreasureMap(Properties properties) {
        Constants.LOG.info("NEW TreasureMap: " + properties.toString());
        super(properties);
    }


}
