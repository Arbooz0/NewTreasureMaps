package kawun.new_treasure_maps.enums;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum FoldType{

    ACCORDION(0,"square2"),
    SCROLL(1,"tall");

    private final byte id;
    public final String texture;
    private static final FoldType[] BY_ID = values();

    public static final Codec<FoldType> CODEC = Codec.BYTE.xmap(FoldType::byId, FoldType::getId);
    public static final StreamCodec<ByteBuf, FoldType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, FoldType::getId, FoldType::byId
    );

    FoldType(int id, String texture) {
        this.id = (byte) id;
        this.texture = texture;
    }

    public byte getId() {
        return id;
    }

    public static FoldType byId(byte id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : ACCORDION;
    }

    public static FoldType random() {
        return ACCORDION;
        //return BY_ID[(byte) (Math.random() * BY_ID.length)];
    }
}
