package kawun.new_treasure_maps.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.maps.Maps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.text.DecimalFormat;
import java.util.List;


public class TreasureMapFunction extends LootItemConditionalFunction {


    public static final MapCodec<TreasureMapFunction> CODEC = RecordCodecBuilder.mapCodec(
            i -> commonFields(i)
                    .and(Codec.INT.optionalFieldOf("loot_level", 0).forGetter(f -> f.lootLevel))
                    .apply(i, TreasureMapFunction::new));


    private final int lootLevel;


    public TreasureMapFunction(List<LootItemCondition> conditions, int lootLevel) {
        super(conditions);
        this.lootLevel = lootLevel;
    }


    public static LootItemConditionalFunction.Builder<?> builder(int lootLevel) {
        return simpleBuilder(conditions -> new TreasureMapFunction(conditions, lootLevel));
    }


    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }


    @Override
    protected ItemStack run(ItemStack itemStack, LootContext context) {
        Vec3 lootPos = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (lootPos == null) {
            Constants.LOG.error("Loot origin is NULL");
            return itemStack;
        }
        Vector2i pos = new Vector2i((int) lootPos.x, (int) lootPos.z);
        Constants.LOG.info("Loot pos: " + lootPos + " " + pos.toString(new DecimalFormat()));
        itemStack = Maps.createMap(MapType.random(lootLevel), pos, context.getLevel(), lootLevel);
        return itemStack;
    }
}
