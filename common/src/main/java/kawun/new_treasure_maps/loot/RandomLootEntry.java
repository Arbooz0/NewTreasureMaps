package kawun.new_treasure_maps.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;
import java.util.function.Consumer;

public class RandomLootEntry extends LootPoolSingletonContainer {

    public static final MapCodec<RandomLootEntry> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    Codec.INT.optionalFieldOf("max_rarity", 3).forGetter(e -> e.maxRarity),
                    NumberProviders.CODEC.optionalFieldOf("count_items", new ConstantValue(1)).forGetter(e -> e.countItems),
                    Codec.FLOAT.optionalFieldOf("count_multiplier", 1.0f).forGetter(e -> e.countMultiplier),
                    Codec.INT.optionalFieldOf("luck", 0).forGetter(e -> e.luck)
                    )
                    .and(singletonFields(inst))
                    .apply(inst, RandomLootEntry::new)
    );


    private final int maxRarity;
    private final NumberProvider countItems;
    private final float countMultiplier;
    private final int luck;



    public RandomLootEntry(int maxRarity, NumberProvider countItems, float countMultiplier, int luck, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.maxRarity = Math.clamp(maxRarity, 0, 3);
        this.countItems = countItems;
        this.countMultiplier = countMultiplier;
        this.luck = luck;
    }


    @Override
    public MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        TimePassed time = new TimePassed();

        int countItems = Math.min(this.countItems.getInt(context), 27);
        for (RandomItems.Type type : RandomItems.Type.getRandomTypes(countItems)) {
            RandomItems.ItemEntry entry = RandomItems.getRandomItem(type, maxRarity, luck);
            if (entry == null) {
                continue;
            }
            ItemStack itemStack = new ItemStack(entry.item);
            int maxCount = itemStack.getMaxStackSize();
            if (maxCount > 1) {
                float max = maxCount / (entry.rarity / 2.0f + 1);
                if (!entry.vanilla) {
                    max /= 2.0f;
                }
                itemStack.setCount(Math.clamp((int) ((Math.random() * max + 1) * countMultiplier), 1, maxCount));
            }
            output.accept(itemStack);
        }

        time.end("Generate LOOT");
    }
}
