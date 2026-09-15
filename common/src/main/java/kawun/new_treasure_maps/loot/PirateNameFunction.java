package kawun.new_treasure_maps.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class PirateNameFunction extends LootItemConditionalFunction {
    public static final MapCodec<PirateNameFunction> CODEC = RecordCodecBuilder.mapCodec(
            i -> commonFields(i).apply(i, PirateNameFunction::new)
    );

    public static final LootItemFunctionType<? extends LootItemConditionalFunction> TYPE = new LootItemFunctionType<>(CODEC);


    private PirateNameFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }


    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return TYPE;
    }


    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        String first;
        String last;
        if (Math.random() < 0.4) {
            first = NameType.PREFIX.random();
            last = NameType.NAME.random();
        } else {
            first = NameType.NAME.random();
            last = NameType.NICKNAME.random();
        }
        itemStack.set(DataComponents.ITEM_NAME, Component.translatable(first).append(" ").append(Component.translatable(last)));
        return itemStack;
    }


    public enum NameType {
        NAME(16),
        NICKNAME(18),
        PREFIX(15);

        public final int count;

        NameType(int count) {
            this.count = count;
        }

        public String random() {
            return "new_treasure_maps." + name().toLowerCase() + String.valueOf((int) (Math.random() * count) + 1);
        }
    }
}
