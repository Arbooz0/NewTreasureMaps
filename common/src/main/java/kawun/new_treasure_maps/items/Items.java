package kawun.new_treasure_maps.items;

import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.function.BiConsumer;
import java.util.function.Function;


public class Items {


    public static Item TREASURE_MAP;
    public static Item MUSIC_DISC_PIRATE_COVE;

    public static DataComponentType<MapComponent> MAP_COMPONENT =
            DataComponentType.<MapComponent>builder().persistent(MapComponent.CODEC).build();



    public static void register(BiConsumer<Identifier, Item> consumer) {
        TREASURE_MAP = registerItem("treasure_map", TreasureMap::new, consumer,
                new Item.Properties().stacksTo(1));

        MUSIC_DISC_PIRATE_COVE = registerItem("music_disc_pirate_cove", Item::new, consumer,
                new Item.Properties()
                        .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, Utils.identifier("pirate_cove")))
                        .stacksTo(1).rarity(Rarity.UNCOMMON).overrideDescription("item.minecraft.music_disc_5"));
    }


    public static void registerComponents(BiConsumer<Identifier, DataComponentType<?>> consumer) {
        consumer.accept(Utils.identifier("map"), MAP_COMPONENT);
    }


    private static Item registerItem(String name, Function<Item.Properties, Item> func, BiConsumer<Identifier, Item> consumer, Item.Properties properties) {
        Identifier id = Utils.identifier(name);
        Item item = func.apply(properties.setId(ResourceKey.create(Registries.ITEM, id)));
        consumer.accept(id, item);
        return item;
    }


    public static ItemStack newTreasureMap(int id, MapType type) {
        ItemStack itemStack = new ItemStack(TREASURE_MAP);
        itemStack.set(MAP_COMPONENT, new MapComponent(id, FoldType.random(), type));
        return itemStack;
    }

}
