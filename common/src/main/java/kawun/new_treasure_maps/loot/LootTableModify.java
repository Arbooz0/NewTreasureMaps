package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.items.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public class LootTableModify {



    public static LootPool.Builder modify(ResourceLocation identifier) {
        if (!identifier.getPath().contains("chests")) {
            return null;
        }

        float chance = 0.1f;
        if (identifier.toString().equals("minecraft:chests/buried_treasure")) {
            chance = 0.4f;
        }
        return LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.TREASURE_MAP))
                .apply(TreasureMapFunction.builder(0))
                .when(TreasureCondition.build())
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }


}
