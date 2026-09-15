package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;

public class TreasureCondition implements LootItemCondition {


    public static LootItemCondition.Builder build() {
        return TreasureCondition::new;
    }


    @Override
    public boolean test(LootContext context) {
        Vec3 lootPos = context.getParamOrNull(LootContextParams.ORIGIN);
        if (lootPos == null) {
            Constants.LOG.error("Loot origin is NULL");
            return false;
        }

        ServerLevel level = context.getLevel();
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        pos.setY(64);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                pos.setX((int) (lootPos.x + (x * 50)));
                pos.setZ((int) (lootPos.z + (z * 50)));
                if (!level.getBiome(pos).is(BiomeTags.IS_OCEAN)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public LootItemConditionType getType() {
        return null;
    }
}
