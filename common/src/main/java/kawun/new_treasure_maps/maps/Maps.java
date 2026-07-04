package kawun.new_treasure_maps.maps;

import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector2i;


public class Maps {


    public static ItemStack createMapFromCommand(MapType type, CommandContext<CommandSourceStack> context) {
        ItemStack itemStack = null;

        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        Vector2i player_pos = new Vector2i(player.getBlockX(), player.getBlockZ());

        TimePassed time = new TimePassed();
        itemStack = switch (type) {
            case DOTTED_LINE -> DottedLineMapCreate.create(player_pos, level);
            case COLORED -> ColoredMapCreate.create(player_pos, level);
            case LANDMARKS -> LandmarksMapCreate.create(player_pos, level);
            case PERSPECTIVE -> PerspectiveMapCreate.create(player_pos, level);
            default -> itemStack;
        };
        time.end("Generated map");

        if (itemStack != null) {
            player.getInventory().add(itemStack);
        }

        return itemStack;
    }


    public static void createChest(int x, int z, Level level) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.WATER) {
            pos.setY(level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 4);
        } else {
            pos.setY(y - 3);
        }
        createChest(pos, level);
    }


    public static void createChest(BlockPos.MutableBlockPos pos, Level level) {
        int center_x = pos.getX();
        int center_y = pos.getY();
        int center_z = pos.getZ();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    pos.set(center_x + x, center_y + y, center_z + z);
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
        Constants.LOG.info("Chest pos: " + center_x + " " + center_y + " " + center_z);
    }


    public static Vector2i getRandomPoint(int min, int max) {
        int v1 = (int) (Math.random() * (max - min)) + min;
        if (Math.random() > 0.5) {
            v1 *= -1;
        }
        int v2 = (int) (Math.random() * (max * 2 + 1)) - max;
        Vector2i pos;
        if (Math.random() > 0.5) {
            pos = new Vector2i(v1, v2);
        } else {
            pos = new Vector2i(v2, v1);
        }
        return pos;
    }


}
