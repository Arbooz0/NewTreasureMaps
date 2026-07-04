package kawun.new_treasure_maps.network;

import io.netty.buffer.ByteBuf;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.maps.ColoredMapCreate;
import kawun.new_treasure_maps.maps.DottedLineMapCreate;
import kawun.new_treasure_maps.maps.LandmarksMapCreate;
import kawun.new_treasure_maps.maps.PerspectiveMapCreate;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record MapPacket(int id, MapType mapType, byte[] bytes) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MapPacket> TYPE = new CustomPacketPayload.Type<>(Utils.identifier("image"));
    public static final StreamCodec<ByteBuf, MapPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MapPacket::id,
            MapType.STREAM_CODEC, MapPacket::mapType,
            ByteBufCodecs.BYTE_ARRAY, MapPacket::bytes,
            MapPacket::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(MapPacket packet) {
        Constants.LOG.info("MapPacket received: id: " + packet.id + ", type: " + packet.mapType + ", size: " + packet.bytes.length);
        switch (packet.mapType) {
            case DOTTED_LINE -> DottedLineMapCreate.clientHandle(packet);
            case COLORED -> ColoredMapCreate.clientHandle(packet);
            case LANDMARKS -> LandmarksMapCreate.clientHandle(packet);
            case PERSPECTIVE -> PerspectiveMapCreate.clientHandle(packet);
        }
    }
}
