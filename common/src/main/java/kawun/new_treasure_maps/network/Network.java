package kawun.new_treasure_maps.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Network {



    public static void registerPacket(PacketConsumer consumer) {
        consumer.accept(MapPacket.TYPE, MapPacket.STREAM_CODEC, obj -> MapPacket.handle((MapPacket) obj));
        consumer.accept(OpenMapPacket.TYPE, OpenMapPacket.STREAM_CODEC, obj -> OpenMapPacket.handle((OpenMapPacket) obj));
    }


    public static void registerPacket(BiConsumer<CustomPacketPayload.Type, StreamCodec> consumer) {
        consumer.accept(MapPacket.TYPE, MapPacket.STREAM_CODEC);
        consumer.accept(OpenMapPacket.TYPE, OpenMapPacket.STREAM_CODEC);
    }


    public static void registerHandler(BiConsumer<CustomPacketPayload.Type, Consumer> consumer) {
        consumer.accept(MapPacket.TYPE, obj -> MapPacket.handle((MapPacket) obj));
        consumer.accept(OpenMapPacket.TYPE, obj -> OpenMapPacket.handle((OpenMapPacket) obj));
    }



    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(makePacket(payload));
    }



    public static Packet<ClientCommonPacketListener> makePacket(CustomPacketPayload payload) {
        return new ClientboundCustomPayloadPacket(payload);
    }



    public interface PacketConsumer {
        void accept(CustomPacketPayload.Type type, StreamCodec codec, Consumer handler);
    }

}
