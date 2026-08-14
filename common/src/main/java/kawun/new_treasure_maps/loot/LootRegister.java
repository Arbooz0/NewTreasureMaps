package kawun.new_treasure_maps.loot;

import com.mojang.serialization.MapCodec;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.function.BiConsumer;

public class LootRegister {



    public static void registerLootEntry(BiConsumer<Identifier, MapCodec<? extends LootPoolEntryContainer>> consumer) {
        consumer.accept(Utils.identifier("random_loot"), RandomLootEntry.CODEC);
        consumer.accept(Utils.identifier("tag"), TagRandomEntry.CODEC);
    }


    public static void registerLootFunction(BiConsumer<Identifier, MapCodec<? extends LootItemFunction>> consumer) {
        consumer.accept(Utils.identifier("treasure_map"), TreasureMapFunction.CODEC);
        consumer.accept(Utils.identifier("set_count"), SetCountFunction.CODEC);
    }







}
