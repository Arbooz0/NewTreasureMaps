package kawun.new_treasure_maps.enums;


import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum MapType {
    NONE(0),
    DOTTED_LINE(1),
    LANDMARKS(2),
    COLORED(3),
    PERSPECTIVE(4),
    SIDE_VIEW(5);

    private final byte id;
    private static final MapType[] BY_ID = values();

    public static final Codec<MapType> CODEC = Codec.BYTE.xmap(MapType::byId, MapType::getId);
    public static final StreamCodec<ByteBuf, MapType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, MapType::getId, MapType::byId
    );


    MapType(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }

    public static MapType byId(byte id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }
}
