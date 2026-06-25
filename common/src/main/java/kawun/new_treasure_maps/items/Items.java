package kawun.new_treasure_maps.items;

import com.mojang.serialization.Codec;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;
import java.util.function.Function;


public class Items {


    public static Item TREASURE_MAP;
    public static DataComponentType<FoldType> FOLD_TYPE = DataComponentType.<FoldType>builder().persistent(FoldType.CODEC).build();


    public static void register(BiConsumer<Identifier, Item> consumer) {
        TREASURE_MAP = register_item("treasure_map", TreasureMap::new, consumer,
                new Item.Properties().stacksTo(1));
    }


    public static void registerComponents(BiConsumer<Identifier, DataComponentType<?>> consumer) {
        consumer.accept(Utils.identifier("fold_type"), FOLD_TYPE);
    }



    private static Item register_item(String name, Function<Item.Properties, Item> func, BiConsumer<Identifier, Item> consumer) {
        return register_item(name, func, consumer, new Item.Properties());
    }


    private static Item register_item(String name, Function<Item.Properties, Item> func, BiConsumer<Identifier, Item> consumer, Item.Properties properties) {
        Identifier id = Utils.identifier(name);
        Item item = func.apply(properties.setId(ResourceKey.create(Registries.ITEM, id)));
        consumer.accept(id, item);
        return item;
    }
}
