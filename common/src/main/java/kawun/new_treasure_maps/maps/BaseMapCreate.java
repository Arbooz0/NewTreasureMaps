package kawun.new_treasure_maps.maps;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


public abstract class BaseMapCreate {

    public static final int INVALID_HEIGHT = -999;

    public int mapId;
    public ServerLevel level;
    public Vector2i fromPosition;

    public boolean canChestUnderWater = true;
    private int attemptChestFind = 0;


    public void setContext(Vector2i fromPosition, ServerLevel level, int id) {
        this.fromPosition = fromPosition;
        this.level = level;
        this.mapId = id;
        NewTreasureMaps.addTask(this::waitTick);
    }


    private boolean waitTick() {
        start();
        return true;
    }


    public abstract void start();




    public void save(MapType type, byte[] bytes) {
        new MapSavedData(mapId, type, bytes).save();
    }



    public boolean canPlaceChest(BlockPos center) {
        attemptChestFind++;
        BlockPos.MutableBlockPos pos = center.mutable();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2) {
                        if (state.is(BlockTags.AIR)) {
                            return false;
                        }
                    } else {
                        if (!state.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }



    public @Nullable BlockPos findPlaceChest(int radiusChunkMin, int radiusChunkMax) {
        ArrayList<Vector2i> chunkCheked = new ArrayList<>();
        Vector2i chunkStart = new Vector2i(fromPosition.x >> 4, fromPosition.y >> 4);

        for (int j = 0; j < 10; j++) {
            Vector2i chunkPos = Utils.getRandomPoint(radiusChunkMin, radiusChunkMax).add(chunkStart);
            if (chunkCheked.contains(chunkPos)) {
                continue;
            }
            chunkCheked.add(chunkPos);

            BlockPos result = findPlaceChestInChunk(chunkPos.x, chunkPos.y);
            if (result != null) {
                return result;
            }
        }

        Constants.LOG.info("No find place chest, attempt: " + attemptChestFind);

        return null;
    }


    public @Nullable BlockPos findPlaceChestInChunk(int chunkX, int chunkZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int random = (int) (Math.random() * 4);

        for (int i = 0; i < 4; i++) {
            int localX = 3;
            int localZ = 3;

            switch ((i + random) % 4) {
                case 1:
                    localX = 12;
                    break;
                case 2:
                    localZ = 12;
                    break;
                case 3:
                    localX = 12;
                    localZ = 12;
                    break;
            }

            pos.setX((chunkX << 4) + localX);
            pos.setZ((chunkZ << 4) + localZ);

            int y = getFloor(pos.getX(), pos.getZ());
            if (y == INVALID_HEIGHT) {
                continue;
            }

            pos.setY(y - 3);
            if (canPlaceChest(pos)) {
                return pos;
            }
        }
        return null;
    }



    public int getFloor(int x, int z) {
        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
        if (chunk == null) {
            return INVALID_HEIGHT;
        }
        int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
        if (state.getBlock() == Blocks.WATER) {
            if (canChestUnderWater) {
                y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            } else {
                return INVALID_HEIGHT;
            }
        }
        return y;
    }



    public void createChest(BlockPos center) {
        BlockPos.MutableBlockPos pos = center.mutable();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (x == 0 && y == 0 && z == 0) {
                        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
                    } else if (y == -1) {
                        level.setBlock(pos, Blocks.RED_SANDSTONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(pos, Blocks.RED_SAND.defaultBlockState(), 3);
                    }
                }
            }
        }
        Constants.LOG.info("Chest pos: " + center);
        Utils.sendPos(center.above(3), "Chest pos (Attempt: " + attemptChestFind + ")");
    }



    public String getStructure(int chunkX, int chunkZ, boolean onlyOnGround) {
        Optional<CompoundTag> optional = level.getChunkSource().chunkMap.read(new ChunkPos(chunkX, chunkZ)).join();
        if (optional.isEmpty()) {
            return "";
        }

        CompoundTag tag = optional.get().getCompoundOrEmpty("structures").getCompoundOrEmpty("starts");
        if (tag.isEmpty()) {
            return "";
        }

        for (Map.Entry<String, Tag> entry : tag.entrySet()) {
            if (onlyOnGround) {
                CompoundTag data = ((CompoundTag) entry.getValue()).getListOrEmpty("Children").getCompoundOrEmpty(0);
                IntArrayTag aabb = (IntArrayTag) data.get("BB");
                int y = (aabb.get(1).value() + aabb.get(4).value()) / 2;
                if (y < 60) {
                    continue;
                }
            }
            return entry.getKey();
        }

        return "";
    }




}
