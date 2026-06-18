package kawun.new_treasure_maps.utils;

import kawun.new_treasure_maps.Constants;
import net.minecraft.resources.Identifier;

public class Utils {


    public static Identifier identifier(String id) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, id);
    }
}
