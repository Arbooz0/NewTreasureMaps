package kawun.new_treasure_maps.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.Constants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
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
    private final NumberProvider count;

    private SetCountFunction(List<LootItemCondition> predicates, NumberProvider count) {
        super(predicates);
        this.count = count;
    }

    @Override
    public MapCodec<SetCountFunction> codec() {
        return CODEC;
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validate(context, "count", this.count);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        int count = this.count.getInt(context);
        itemStack.setCount(Math.min(count, itemStack.getMaxStackSize()));
        return itemStack;
    }
}
