package kawun.new_treasure_maps.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;

public class SetCountFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetCountFunction> CODEC = RecordCodecBuilder.mapCodec(
            i -> commonFields(i)
                    .and(NumberProviders.CODEC.fieldOf("count").forGetter(f -> f.count))
                    .apply(i, SetCountFunction::new)
    );

    public static final LootItemFunctionType<? extends LootItemConditionalFunction> TYPE = new LootItemFunctionType<>(CODEC);

    public static final TagKey<Item> tagSingleItems = TagKey.create(Registries.ITEM, Utils.identifier("single"));

    private final NumberProvider count;

    private SetCountFunction(List<LootItemCondition> predicates, NumberProvider count) {
        super(predicates);
        this.count = count;
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        if (itemStack.is(tagSingleItems)) {
            itemStack.setCount(1);
            return itemStack;
        }
        int count = this.count.getInt(context);
        itemStack.setCount(Math.min(count, itemStack.getMaxStackSize()));
        return itemStack;
    }
}
