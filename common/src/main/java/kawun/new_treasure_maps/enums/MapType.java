package kawun.new_treasure_maps.enums;


import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum MapType {
    NONE,
    TEST, // После удаление изменить random()
    PERSPECTIVE,
    DOTTED_LINE,
    LANDMARKS,
    COLORED,
    SIDE_VIEW;

    private final byte id;
    private static final MapType[] BY_ID = values();

    public static final Codec<MapType> CODEC = Codec.BYTE.xmap(MapType::byId, MapType::getId);
    public static final StreamCodec<ByteBuf, MapType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, MapType::getId, MapType::byId
    );


    MapType() {
        this.id = (byte) ordinal();
    }

    public byte getId() {
        return id;
    }

    public static MapType byId(byte id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }

    public static MapType random(int level) {
        int skip = (level == 0) ? 3 : 2; // После удаления TEST убавить на 1
        return BY_ID[(int) (Math.random() * (BY_ID.length - skip)) + skip];
    }

}
