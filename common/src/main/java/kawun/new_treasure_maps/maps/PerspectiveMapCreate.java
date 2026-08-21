package kawun.new_treasure_maps.maps;

import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.pixels.PixelsLoader;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashSet;


public class PerspectiveMapCreate extends BaseMapCreate {


    public static final byte[] TEXTURE_LOGS;
    public static final byte[] TEXTURE_LEAVES;
    public static final byte[] TEXTURE_GRASS_TOP;
    public static final byte[] TEXTURE_GRASS_SIDE;




    @Override
    public void start() {
        canChestUnderWater = false;

        Pixels pixels = findMapLocation(fromPosition);
        if (pixels == null) {
            errorGenerate();
            return;
        }

        save(MapType.PERSPECTIVE, pixels.pixels);
    }




    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = BaseDrawnMapCreate::convertColor;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }




    public Pixels findMapLocation(Vector2i from) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        ArrayList<Vector2i> chunkCheked = new ArrayList<>();
        Vector2i chunkStart = new Vector2i(from.x >> 4, from.y >> 4);

        for (int j = 0; j < 10; j++) {
            Vector2i chunkPos = Utils.getRandomPoint(2, 4).add(chunkStart);
            if (chunkCheked.contains(chunkPos)) {
                continue;
            }
            chunkCheked.add(chunkPos);

            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.y, ChunkStatus.FULL, false);
            if (chunk == null) {
                continue;
            }

            Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
            HashSet<Integer> heights = new HashSet<Integer>();
            int[] heightSection = new int[4];

            for (int x = 1; x < 15; x++) {
                for (int z = 1; z < 15; z++) {
                    int y = heightmap.getFirstAvailable(x, z);
                    heights.add(y);
                    int section = (x > 7 ? 1 : 0) + (z > 7 ? 2 : 0);
                    heightSection[section] += y;
                }
            }

            if (heights.size() < 5) {
                continue;
            }

            Vector2i[] checkPos = new Vector2i[2];

            if (heightSection[1] > heightSection[2]) {
                checkPos[0] = new Vector2i(2, 13);
            } else {
                checkPos[0] = new Vector2i(13, 2);
            }
            if (heightSection[0] > heightSection[3]) {
                checkPos[1] = new Vector2i(13, 13);
            } else {
                checkPos[1] = new Vector2i(2, 2);
            }

            int chunkGlobalPosX = chunkPos.x << 4;
            int chunkGlobalPosZ = chunkPos.y << 4;

            for (Vector2i localPos : checkPos) {
                Vector2i dir = new Vector2i(localPos.x == 2 ? 1 : -1, localPos.y == 2 ? 1 : -1);
                for (int i = 0; i <= 4; i+=2) {
                    int x = chunkGlobalPosX + localPos.x + (dir.x * i);
                    int z = chunkGlobalPosZ + localPos.y + (dir.y * i);
                    int y = getFloor(x, z);
                    if (y == INVALID_HEIGHT) {
                        continue;
                    }
                    y -= 3;

                    mPos.set(x, y, z);
                    if (!canPlaceChest(mPos)) {
                        continue;
                    }

                    Vec3 rayFrom = new Vec3(x + 0.5, y + 4.5, z + 0.5);
                    Vec3 rayTo = rayFrom.add(dir.x * -2, 5, dir.y * -2);
                    if (rayCast(rayFrom, rayTo)) {
                        continue;
                    }

                    createChest(mPos);
                    mPos.set(x, y + 3, z);
                    return renderMap(rayTo, new Vec3(dir.x, -1, dir.y), mPos);
                }
            }
            
        }

        return null;
    }



    // Возвращает true, если луч столкнулся с блоком
    public boolean rayCast(Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(
                from, to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
        );
        BlockHitResult result = level.clip(context);
        return result.getType() != HitResult.Type.MISS;
    }




    public Pixels renderMap(Vec3 from, Vec3 dir, BlockPos cross) {
        Pixels pixels = new Pixels(256);

        dir = dir.normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = dir.cross(worldUp).normalize();
        Vec3 up = right.cross(dir).normalize();
        int baseColor = 0b1100_0000;

        double nearestDist = 999;
        Vector2i crossPos = new Vector2i();

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                Vec3 rayDir = dir.add(right.scale((x - 128) / 200.0)).add(up.scale((y - 128) / -200.0)).normalize();
                Vec3 to = from.add(rayDir.scale(18));

                ClipContext context = new ClipContext(
                        from, to,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                );
                BlockHitResult result = level.clip(context);

                if (result.getType() != HitResult.Type.BLOCK) {
                    continue;
                }

                BlockPos blockPos = result.getBlockPos();
                double dist = from.distanceTo(blockPos.getCenter());
                if (dist > 17) {
                    continue;
                }

                Vec3 pos = result.getLocation();
                if (cross.equals(blockPos)) {
                    double distToCenter = pos.distanceToSqr(blockPos.getCenter());
                    if (distToCenter < nearestDist) {
                        nearestDist = distToCenter;
                        crossPos.x = x;
                        crossPos.y = y;
                    }
                }

                int blackout = 3;
                double v1 = 0;
                double v2 = 0;

                switch (result.getDirection()) {
                    case DOWN:
                        blackout = 0;
                    case UP:
                        v1 = pos.x;
                        v2 = pos.z;
                        break;
                    case NORTH, SOUTH:
                        blackout = 1;
                        v1 = pos.x;
                        v2 = pos.y;
                        break;
                    case WEST, EAST:
                        blackout = 2;
                        v1 = pos.z;
                        v2 = pos.y;
                        break;
                };

                v1 = Math.abs(v1 % 1);
                v2 = Math.abs(v2 % 1);

                int alpha = 12 - (int) (Math.max(dist - 6, 0));

                if (dist < 16) {
                    if ((v1 < 0.05) || (v1 > 0.95) || (v2 < 0.05) || (v2 > 0.95)) {
                        alpha += 3;
                    } else {
                        BlockState state = level.getBlockState(blockPos);
                        byte[] texture = null;
                        if (state.is(BlockTags.LOGS)) {
                            texture = TEXTURE_LOGS;
                        } else if (state.is(BlockTags.LEAVES)) {
                            texture = TEXTURE_LEAVES;
                        } else if (state.is(BlockTags.GRASS_BLOCKS)) {
                            if (result.getDirection() == Direction.UP) {
                                texture = TEXTURE_GRASS_TOP;
                            } else {
                                texture = TEXTURE_GRASS_SIDE;
                            }
                        }
                        if (texture != null) {
                            int tX = (int) (v1 * 8);
                            int tY = (int) (v2 * 8);
                            byte a = texture[tX + tY * 8];
                            alpha = Math.clamp(alpha + a, 0, 15);
                        }
                    }
                    if (dist < 12) {
                        v1 -= 0.5;
                        v2 -= 0.5;
                        if ((v1 * v1 + v2 * v2) > 0.15) {
                            if (((x + y) & 1) == 0) {
                                blackout -= 1;
                            }
                        }
                    }
                }

                byte color = (byte) (baseColor | (blackout << 4) | alpha);
                pixels.setPixel(x, y, color);
            }
        }

        Pixels crossTexture = PixelsLoader.loadTexture("cross");
        if (crossTexture != null) {
            pixels.drawImage(crossPos, crossTexture);
        }

        return pixels;
    }



    static {
        TEXTURE_LOGS = new byte[]{
             0,  2,  2,  0, -1,  2,  0,  2,
            -1,  2, -1,  0, -1,  2, -1,  0,
            -1,  2, -1,  2,  0,  2, -1,  0,
             2,  0, -1,  2,  0, -1, -1,  2,
             2,  0,  2,  2,  2, -1,  2,  2,
             2,  2,  2, -1,  2,  0,  2,  0,
             0,  2,  0, -1,  0,  0, -1,  0,
             0, -1,  0, -1,  0,  2, -1,  0
        };
        TEXTURE_LEAVES = new byte[]{
             0 , 0 ,-20, 0 , 0 , 0 , 0 , 0 ,
            -20,-20, 0 , 0 ,-20,-20, 0 , 0 ,
             0 , 0 , 0 , 0 , 0 ,-20,-20, 0 ,
             0 ,-20,-20, 0 , 0 , 0 , 0 , 0 ,
             0 , 0 , 0 ,-20, 0 , 0 , 0 ,-20,
             0 , 0 , 0 , 0 , 0 ,-20, 0 ,-20,
             0 , 0 , 0 , 0 , 0 ,-20,-20, 0 ,
             0 , 0 ,-20,-20, 0 , 0 , 0 , 0
        };
        TEXTURE_GRASS_TOP = new byte[]{
            -1, 0, -1, 0, -1, -1, 0, 0,
            0, -1, -1, 0, -1, 0, -1, -1,
            -1, 0, 0, -1, 0, 0, -1, 0,
            0, -1, 0, 0, -1, -1, 0, 0,
            -1, 0, -1, -1, 0, 0, -1, -1,
            -1, 0, -1, 0, -1, -1, 0, 0,
            0, -1, 0, -1, 0, -1, 0, -1,
            0, -1, 0, 0, -1, 0, -1, 0
        };
        TEXTURE_GRASS_SIDE = new byte[]{
            1, 2, 1, 2, 1, 1, 2, 2,
            2, 1, 1, 2, 1, 2, 1, 1,
            1, 2, 2, 1, 2, 2, 1, 2,
            2, 1, 2, 2, 1, 1, 2, 2,
            1, 2, 1, 1, 2, 2, 1, 1,
            -1, 2, -1, 2, -1, -1, 2, 2,
            0, -1, 0, -1, 0, -1, 0, -1,
            0, -1, 0, 0, -1, 0, -1, 0
        };


    }




}
