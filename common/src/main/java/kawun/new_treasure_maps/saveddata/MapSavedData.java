package kawun.new_treasure_maps.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;


public class MapSavedData extends SavedData {

    public static final Codec<MapSavedData> CODEC = RecordCodecBuilder.create(
        i -> i.group(
                    MapType.CODEC.fieldOf("type").forGetter(m -> m.mapType),
                    Codec.BYTE_BUFFER.fieldOf("bytes").forGetter(m -> ByteBuffer.wrap(m.bytes))
                )
                .apply(i, MapSavedData::new)
    );


    public int id;
    public MapType mapType = MapType.NONE;
    public byte[] bytes;


    public MapSavedData() {}

    public MapSavedData(int size) {bytes = new byte[size * size];}

    public MapSavedData(MapType mapType, ByteBuffer bytes) {
        this.mapType = mapType;
        this.bytes = bytes.array();
    }

    public MapSavedData(byte[] bytes) {this.bytes = bytes;}


    public static SavedDataType<MapSavedData> getSavedDataType(int id) {
        return new SavedDataType<>(
                Utils.identifier("map" + id),
                MapSavedData::new,
                CODEC,
                null
        );
    }


    public static @Nullable MapSavedData load(int id) {
        if (NewTreasureMaps.server != null) {
            MapSavedData data = NewTreasureMaps.server.getDataStorage().get(getSavedDataType(id));
            if (data != null) {
                data.id = id;
            }
            return data;
        }
        return null;
    }


    public void save() {
        if (NewTreasureMaps.server != null) {
            NewTreasureMaps.server.getDataStorage().set(getSavedDataType(id), this);
        }
    }


}
