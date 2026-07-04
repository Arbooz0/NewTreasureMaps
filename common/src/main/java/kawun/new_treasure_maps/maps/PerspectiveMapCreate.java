package kawun.new_treasure_maps.maps;

import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.saveddata.FreeID;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import kawun.new_treasure_maps.utils.Pixels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector2i;


public class PerspectiveMapCreate {



    public static ItemStack create(Vector2i from, Level level) {

        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, from.x, from.y) + 4;
        Pixels pixels = renderMap(new Vec3(from.x, y, from.y), new Vec3(1, -1, 1), level);

        int id = FreeID.getFreeID();
        MapSavedData data = new MapSavedData(id, MapType.PERSPECTIVE, pixels.pixels);
        data.save();

        return Items.newTreasureMap(id);
    }




    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = LandmarksMapCreate::convertColor;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }




    public static Pixels renderMap(Vec3 from, Vec3 dir, Level level) {
        Pixels pixels = new Pixels(256);

        dir = dir.normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = dir.cross(worldUp).normalize();
        Vec3 up = right.cross(dir).normalize();
        int baseColor = 0b1100_0000;

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                Vec3 rayDir = dir.add(right.scale((x - 128) / 200.0)).add(up.scale((y - 128) / -200.0)).normalize();
                //Vec3 to = from.add(rayDir.scale(15));

                Vec3 from2 = from.add(right.scale((x - 128) / 20.0)).add(up.scale((y - 128) / -20.0));
                Vec3 to = from2.add(rayDir.scale(15));

                ClipContext context = new ClipContext(
                        from2, to,
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
                if (dist > 13) {
                    continue;
                }

                Vec3 pos = result.getLocation();

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

                int alpha = 12 - (int) (Math.max(dist - 2, 0));

                if (dist < 12) {
                    if ((v1 < 0.05) || (v1 > 0.95) || (v2 < 0.05) || (v2 > 0.95)) {
                        alpha += 3;
                    }
                    if (dist < 8) {
                        v1 -= 0.5;
                        v2 -= 0.5;
                        if ((v1 * v1 + v2 * v2) > 0.1) {
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

        return pixels;
    }




}
