package kawun.new_treasure_maps.maps;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.TreasureMap;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


public abstract class BaseMapCreate {

    public static final int INVALID_HEIGHT = -999;

    public int mapId;
    public ServerLevel level;
    public Vector2i fromPosition;
    public int lootLevel = 0;

    public boolean canChestUnderWater = true;
    public boolean checkAirOverChest = false;
    public boolean checkPlaceCross = false;
    public boolean isBigCross = false;


    public void setContext(Vector2i fromPosition, ServerLevel level, int id, int lootLevel) {
        this.fromPosition = fromPosition;
        this.level = level;
        this.mapId = id;
        this.lootLevel = Math.clamp(lootLevel, 0, 2);
        Constants.LOG.info("New map " + getMapType() + " " + id + ", level: " + lootLevel + ", pos: " + fromPosition.toString(new DecimalFormat()));
        NewTreasureMaps.addTask(this::waitTick);
    }


    private boolean waitTick() {
        start();
        return true;
    }


    public abstract void start();


    public abstract MapType getMapType();




    public void save(byte[] bytes) {
        new MapSavedData(mapId, getMapType(), bytes).save();
    }


    public void errorGenerate() {
        if (getMapType() != MapType.COLORED && getMapType() != MapType.SIDE_VIEW) {
            BaseMapCreate map;
            if (Math.random() < 0.5) {
                map = new ColoredMapCreate();
            } else {
                map = new SideViewMapCreate();
            }
            TreasureMap.changeType.put(mapId, map.getMapType());
            map.setContext(fromPosition, level, mapId, lootLevel);
        } else {
            TreasureMap.changeType.put(mapId, MapType.NONE);
        }
        Utils.sendErrorCreateMap();
    }



    public boolean canPlaceChest(BlockPos center) {
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


    public boolean checkAir(BlockPos center) {
        BlockPos.MutableBlockPos pos = center.mutable();
        int count = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 5; y++) {
                for (int z = -2; z <= 2; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(BlockTags.AIR)) {
                        count++;
                        if (count > 10) {
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

            if (checkAirOverChest) {
                pos.setY(y + 1);
                if (!checkAir(pos)) {
                    continue;
                }
            }

            if (checkPlaceCross) {
                if (!placeCross(pos, true));
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
        if (state.is(BlockTags.ICE)) {
            return INVALID_HEIGHT;
        }
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
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity instanceof RandomizableContainerBlockEntity container) {

                            String path;
                            if (getMapType() == MapType.PERSPECTIVE) {
                                path = "treasure/2";
                            } else {
                                if ((lootLevel < 2) && (Math.random() < 0.4)) {
                                    path = "treasure_map/" + (lootLevel + 1);
                                } else {
                                    path = "treasure/" + lootLevel;
                                }
                            }

                            ResourceKey<LootTable> lootTableKey = ResourceKey.create(
                                    Registries.LOOT_TABLE,
                                    Utils.identifier(path)
                            );
                            container.setLootTable(lootTableKey);
                        }
                    } else if (y == -1) {
                        level.setBlock(pos, Blocks.RED_SANDSTONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(pos, Blocks.RED_SAND.defaultBlockState(), 3);
                    }
                }
            }
        }
        Constants.LOG.info("Chest pos: " + center);
    }




    public String getStructure(int chunkX, int chunkZ, boolean onlyOnGround) {
        if (level.hasChunk(chunkX, chunkZ)) {
            return getStructureFromChunk(chunkX, chunkZ, onlyOnGround);
        } else {
            return getStructureFromFile(chunkX, chunkZ, onlyOnGround);
        }
    }



    public String getStructureFromFile(int chunkX, int chunkZ, boolean onlyOnGround) {
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
                int y = aabb.get(4).value();
                if (y < 60) {
                    continue;
                }
            }
            String[] name = entry.getKey().split(":", 2);
            if (!name[0].equals("minecraft")) {
                continue;
            }
            return name[1];
        }

        return "";
    }


    public String getStructureFromChunk(int chunkX, int chunkZ, boolean onlyOnGround) {
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        for (Map.Entry<Structure, StructureStart> entry : chunk.getAllStarts().entrySet()) {
            if (onlyOnGround) {
                BoundingBox box = entry.getValue().getBoundingBox();
                if (box.maxY() < 60) {
                    continue;
                }
            }

            Identifier identifier = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(entry.getKey());
            if (identifier == null) {
                continue;
            }
            if (!identifier.getNamespace().equals("minecraft")) {
                continue;
            }
            return identifier.getPath();
        }
        return "";
    }



    public void addStructure(BlockPos pos, String id) {
        Optional<StructureTemplate> optional = level.getStructureManager().get(Utils.identifier(id));
        if (optional.isEmpty()) {
            return;
        }

        StructureTemplate structureTemplate = optional.get();
        Vec3i size = structureTemplate.getSize();
        pos = pos.offset(size.getX() / -2, 0, size.getZ() / -2);

        structureTemplate.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.getRandom(), 2);
    }



    public boolean placeCross(BlockPos center, boolean isCheckOnly) {
        BlockPos.MutableBlockPos pos = center.mutable();
        int l = isBigCross ? 2 : 1;
        int count = 0;
        for (int x = -l; x <= l; x++) {
            for (int z = -l; z <= l; z++) {
                if (Math.abs(x) != Math.abs(z)) {
                    continue;
                }
                pos.setX(center.getX() + x);
                pos.setZ(center.getZ() + z);
                pos.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos) - 1);
                BlockState blockState = level.getBlockState(pos);
                if (blockState.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) {
                    if (!isCheckOnly) {
                        level.setBlock(pos, Blocks.RED_SAND.defaultBlockState(), 2);
                    }
                } else {
                    if (isCheckOnly) {
                        count++;
                        if (isBigCross) {
                            if (count > 2) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }




}
