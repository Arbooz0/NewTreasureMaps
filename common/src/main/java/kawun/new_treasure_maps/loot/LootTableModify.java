package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.items.Items;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jspecify.annotations.Nullable;

public class LootTableModify {



    public static LootPool.@Nullable Builder modify(Identifier identifier) {
        if (!identifier.getPath().contains("chests")) {
            return null;
        }
        return LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.TREASURE_MAP))
                .apply(TreasureMapFunction.builder(0))
                .when(LocationCheck.checkLocation(LocationPredicate.Builder.inDimension(Level.OVERWORLD)))
                .when(LootItemRandomChanceCondition.randomChance(0.1f));
    }


}
