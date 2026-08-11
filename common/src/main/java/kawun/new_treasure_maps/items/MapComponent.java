package kawun.new_treasure_maps.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.enums.MapType;

public record MapComponent(int id, FoldType foldType, MapType mapType) {

    public static final Codec<MapComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("id").forGetter(MapComponent::id),
            FoldType.CODEC.optionalFieldOf("fold_type", FoldType.ACCORDION).forGetter(MapComponent::foldType),
            MapType.CODEC.optionalFieldOf("map_type", MapType.NONE).forGetter(MapComponent::mapType)
    ).apply(builder, MapComponent::new));

}
