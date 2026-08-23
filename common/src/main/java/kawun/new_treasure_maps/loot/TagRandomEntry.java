package kawun.new_treasure_maps.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.config.ConfigManager;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TagRandomEntry extends LootPoolSingletonContainer {

    public static final MapCodec<TagRandomEntry> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.STRING.fieldOf("name").forGetter(e -> e.name))
                    .and(singletonFields(i))
                    .apply(i, TagRandomEntry::new)
    );


    private final String name;


    public TagRandomEntry(String name, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.name = name;
    }


    @Override
    public MapCodec<? extends LootPoolSingletonContainer> codec() {
        return CODEC;
    }


    @Override
    protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        if (ConfigManager.config.include_other_mods_items) {
            Item item = RandomItems.getRandomItem(name.substring(0, name.length() - 1));
            if (item != null) {
                output.accept(new ItemStack(item));
                return;
            }
        }

        Optional<Holder<Item>> optional = BuiltInRegistries.ITEM.getRandomElementOf(getTag(), RandomSource.create());
        if (optional.isPresent()) {
            output.accept(new ItemStack(optional.get()));
        } else {
            Constants.LOG.error("No has tag " + name);
        }
    }


    public TagKey<Item> getTag() {
        String folder = ConfigManager.config.imbalanced_loot ? "imbalanced/" : "balanced/";
        return TagKey.create(Registries.ITEM, Utils.identifier(folder + name));
    }
}
