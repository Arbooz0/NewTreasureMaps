package kawun.new_treasure_maps.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.enums.FoldType;

public record MapComponent(int id, FoldType foldType) {

    public static final Codec<MapComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("id").forGetter(MapComponent::id),
            FoldType.CODEC.optionalFieldOf("fold_type", FoldType.ACCORDION).forGetter(MapComponent::foldType)
    ).apply(builder, MapComponent::new));

}
