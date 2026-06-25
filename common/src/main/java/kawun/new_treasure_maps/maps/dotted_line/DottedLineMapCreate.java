package kawun.new_treasure_maps.maps.dotted_line;


import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.saveddata.FreeID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;

public class DottedLineMapCreate {


    public static ItemStack create() {



        int id = FreeID.getFreeID();
        ItemStack itemStack = new ItemStack(Items.TREASURE_MAP);
        itemStack.set(DataComponents.MAP_ID, new MapId(id));
        itemStack.set(Items.FOLD_TYPE, FoldType.random());



        return itemStack;
    }


}
