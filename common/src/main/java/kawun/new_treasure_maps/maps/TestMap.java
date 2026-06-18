package kawun.new_treasure_maps.maps;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;

public class TestMap {



    public static ItemStack create_map(Vector2i pos, ServerLevel level) {
        ItemStack item = MapItem.create(level, pos.x, pos.y, (byte) 0, false, false);

        MapItemSavedData data = MapItem.getSavedData(item, level).locked();

        TimePassed time = new TimePassed();
        coloring_map(level, data);
        time.end("Coloring map");

        level.setMapData(item.get(DataComponents.MAP_ID), data);


        return item;
    }


    public static void coloring_map(ServerLevel level, MapItemSavedData data) {
        int start_x = data.centerX - 64;
        int start_z = data.centerZ - 64;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int z = 0; z < 128; ++z) {
            for(int x = 0; x < 128; ++x) {
                pos.set(start_x + x, 0, start_z + z);
                pos.setY(level.getHeight(Heightmap.Types.WORLD_SURFACE, pos) - 1);
                MapColor color = level.getBlockState(pos).getMapColor(level, pos);

                data.setColor(x, z, color.getPackedId(MapColor.Brightness.NORMAL));
            }
        }

    }



    public static boolean is_water_block(ServerLevel level, BlockPos.MutableBlockPos pos) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos) - 1;
        pos.setY(y);
        Block block = level.getBlockState(pos).getBlock();
        return block == Blocks.WATER;
    }

    public static boolean is_water_biome(ServerLevel level, BlockPos.MutableBlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER);
    }


    public static void renderBiomePreviewMap(ServerLevel level, MapItemSavedData data, boolean is_block) {
        if (data != null && level.dimension() == data.dimension) {
            int scale = 1 << data.scale;
            int centerX = data.centerX;
            int centerZ = data.centerZ;
            boolean[] isBiomeWatery = new boolean[16384];
            int unscaledStartX = centerX / scale - 64;
            int unscaledStartZ = centerZ / scale - 64;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for(int row = 0; row < 128; ++row) {
                for(int column = 0; column < 128; ++column) {

                    pos.set((unscaledStartX + column) * scale, 0, (unscaledStartZ + row) * scale);

                    boolean is_water;
                    if (is_block) {
                        is_water = is_water_block(level, pos);
                    } else {
                        is_water = is_water_biome(level, pos);
                    }
                    isBiomeWatery[row * 128 + column] = is_water;
                }
            }

            for(int mx = 1; mx < 127; ++mx) {
                for(int mz = 1; mz < 127; ++mz) {
                    int waterCount = 0;

                    for(int dx = -1; dx < 2; ++dx) {
                        for(int dz = -1; dz < 2; ++dz) {
                            if ((dx != 0 || dz != 0) && isBiomeWatery(isBiomeWatery, mx + dx, mz + dz)) {
                                ++waterCount;
                            }
                        }
                    }

                    MapColor.Brightness brightness = MapColor.Brightness.LOWEST;
                    MapColor newColor = MapColor.NONE;
                    if (isBiomeWatery(isBiomeWatery, mx, mz)) {
                        newColor = MapColor.COLOR_ORANGE;
                        if (waterCount > 7 && mz % 2 == 0) {
                            switch ((mx + (int)(Mth.sin((double)((float)mz + 0.0F)) * 7.0F)) / 8 % 5) {
                                case 0:
                                case 4:
                                    brightness = MapColor.Brightness.LOW;
                                    break;
                                case 1:
                                case 3:
                                    brightness = MapColor.Brightness.NORMAL;
                                    break;
                                case 2:
                                    brightness = MapColor.Brightness.HIGH;
                            }
                        } else if (waterCount > 7) {
                            newColor = MapColor.NONE;
                        } else if (waterCount > 5) {
                            brightness = MapColor.Brightness.NORMAL;
                        } else if (waterCount > 3) {
                            brightness = MapColor.Brightness.LOW;
                        } else if (waterCount > 1) {
                            brightness = MapColor.Brightness.LOW;
                        }
                    } else if (waterCount > 0) {
                        newColor = MapColor.COLOR_BROWN;
                        if (waterCount > 3) {
                            brightness = MapColor.Brightness.NORMAL;
                        } else {
                            brightness = MapColor.Brightness.LOWEST;
                        }
                    }

                    if (newColor != MapColor.NONE) {
                        data.setColor(mx, mz, newColor.getPackedId(brightness));
                    }
                }
            }
        }

    }

    private static boolean isBiomeWatery(boolean[] isBiomeWatery, int x, int z) {
        return isBiomeWatery[z * 128 + x];
    }

}
