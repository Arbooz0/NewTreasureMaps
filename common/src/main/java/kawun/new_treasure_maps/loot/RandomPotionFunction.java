package kawun.new_treasure_maps.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RandomPotionFunction extends LootItemConditionalFunction {

    public static final MapCodec<RandomPotionFunction> CODEC = RecordCodecBuilder.mapCodec(
            i -> commonFields(i).apply(i, RandomPotionFunction::new)
    );

    public static final LootItemFunctionType<? extends LootItemConditionalFunction> TYPE = new LootItemFunctionType<>(CODEC);


    private RandomPotionFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return TYPE;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext context) {
        List<Reference<Potion>> list = context.getLevel().registryAccess().lookupOrThrow(Registries.POTION).listElements().toList();
        Reference<Potion> potion = list.get((int) (Math.random() * list.size()));
        itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return itemStack;
    }
}
