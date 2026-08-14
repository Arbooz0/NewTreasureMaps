package kawun.new_treasure_maps.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TagRandomEntry extends LootPoolSingletonContainer {

    public static final MapCodec<TagRandomEntry> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(TagKey.codec(Registries.ITEM).fieldOf("name").forGetter(e -> e.tag))
                    .and(singletonFields(i))
                    .apply(i, TagRandomEntry::new)
    );


    private final TagKey<Item> tag;


    public TagRandomEntry(TagKey<Item> tag, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.tag = tag;
    }


    @Override
    public MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        Optional<Holder<Item>> optional = BuiltInRegistries.ITEM.getRandomElementOf(this.tag, RandomSource.create());
        if (optional.isPresent()) {
            output.accept(new ItemStack(optional.get()));
        }
    }
}
