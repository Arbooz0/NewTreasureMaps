package kawun.new_treasure_maps.network;

import io.netty.buffer.ByteBuf;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.maps.*;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record OpenMapPacket(int id, boolean isOpen) implements CustomPacketPayload {

    public static final Type<OpenMapPacket> TYPE = new Type<>(Utils.identifier("open_map"));
    public static final StreamCodec<ByteBuf, OpenMapPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, OpenMapPacket::id,
            ByteBufCodecs.BOOL, OpenMapPacket::isOpen,
            OpenMapPacket::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(OpenMapPacket packet) {
        MapRenderer.setOpenMap(packet.id, packet.isOpen);
    }
}
