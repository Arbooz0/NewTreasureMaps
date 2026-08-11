package kawun.new_treasure_maps.enums;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import kawun.new_treasure_maps.client.animation.AccordionAnimation;
import kawun.new_treasure_maps.client.animation.Animation;
import kawun.new_treasure_maps.client.animation.ScrollAnimation;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum FoldType{

    ACCORDION(0,"square", "accordion", new AccordionAnimation()),
    SCROLL(1,"tall", "scroll", new ScrollAnimation());

    private final byte id;
    private final String texture;
    public String lastTexture;
    public final String model;
    public final Animation animation;
    private static final FoldType[] BY_ID = values();

    public static final Codec<FoldType> CODEC = Codec.BYTE.xmap(FoldType::byId, FoldType::getId);
    public static final StreamCodec<ByteBuf, FoldType> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, FoldType::getId, FoldType::byId
    );

    FoldType(int id, String texture, String model, Animation animation) {
        this.id = (byte) id;
        this.texture = texture;
        this.model = model;
        this.animation = animation;
    }

    public byte getId() {
        return id;
    }

    public static FoldType byId(byte id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : ACCORDION;
    }

    public static FoldType random() {
        return BY_ID[(byte) (Math.random() * BY_ID.length)];
    }

    public String getTexture() {
        lastTexture = texture + ((int) (Math.random() * 2) + 1);
        return lastTexture;
    }
}
