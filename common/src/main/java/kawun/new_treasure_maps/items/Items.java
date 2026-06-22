package kawun.new_treasure_maps.items;

import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Items {


    public static Item TREASURE_MAP;


    public static void register(BiConsumer<Identifier, Item> consumer) {
        TREASURE_MAP = register_item("treasure_map", Item::new, consumer);
    }


    private static Item register_item(String name, Function<Item.Properties, Item> func, BiConsumer<Identifier, Item> consumer) {
        Identifier id = Utils.identifier(name);
        Item item = func.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        consumer.accept(id, item);
        return item;
    }
}
