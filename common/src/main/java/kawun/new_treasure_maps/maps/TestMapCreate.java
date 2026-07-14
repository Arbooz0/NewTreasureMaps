package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.Pixels;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.MapColor;

import java.util.Optional;

public class TestMapCreate extends BaseMapCreate {


    public static boolean isTag = true;

    @Override
    public void start() {
        Pixels pixels = new Pixels(256);

        MapColor.Brightness b = MapColor.Brightness.NORMAL;
        byte green = MapColor.COLOR_GREEN.getPackedId(b);
        byte yellow = MapColor.COLOR_YELLOW.getPackedId(b);
        byte red = MapColor.COLOR_RED.getPackedId(b);

        ChunkMap map = level.getChunkSource().chunkMap;

        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                boolean has = false;

                if (isTag) {
                    Optional<CompoundTag> optional = map.read(new ChunkPos(x, y)).join();
                    if (optional.isPresent()) {
                        CompoundTag tag = optional.get().getCompoundOrEmpty("structures").getCompoundOrEmpty("starts");
                        has = !tag.isEmpty();
                    }
                } else {
                    ChunkAccess chunk = level.getChunk(x, y, ChunkStatus.STRUCTURE_STARTS, true);
                    if (chunk != null) {
                        has = !chunk.getAllStarts().isEmpty();
                    }
                }

                pixels.fillSquare(x * 4, y * 4, 4, has ? green : red);
            }
        }

        save(MapType.TEST, pixels.pixels);
    }




    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = MapColor::getColorFromPackedId;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }

}
