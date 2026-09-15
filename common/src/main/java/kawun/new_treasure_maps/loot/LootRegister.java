package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.function.BiConsumer;

public class LootRegister {



    public static void registerLootEntry(BiConsumer<ResourceLocation, LootPoolEntryType> consumer) {
        consumer.accept(Utils.identifier("tag"), TagRandomEntry.TYPE);
    }


    public static void registerLootFunction(BiConsumer<ResourceLocation, LootItemFunctionType<? extends LootItemConditionalFunction>> consumer) {
        consumer.accept(Utils.identifier("treasure_map"), TreasureMapFunction.TYPE);
        consumer.accept(Utils.identifier("set_count"), SetCountFunction.TYPE);
        consumer.accept(Utils.identifier("pirate_name"), PirateNameFunction.TYPE);
        consumer.accept(Utils.identifier("random_potion"), RandomPotionFunction.TYPE);
    }







}
