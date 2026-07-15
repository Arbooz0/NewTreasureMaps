package kawun.new_treasure_maps.commands.test;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.commands.Command;
import kawun.new_treasure_maps.maps.TestMapCreate;
import kawun.new_treasure_maps.utils.TimePassed;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.*;

public class TestCommand extends Command {




    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("test")
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                .then(Commands.argument("type", IntegerArgumentType.integer(0, 5))
                                    .executes(TestCommand::execute))));
    }


    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        ItemStack itemStack = player.getMainHandItem();
        int x = player.getBlockX();
        int z = player.getBlockZ();

        x = IntegerArgumentType.getInteger(context, "x");
        z = IntegerArgumentType.getInteger(context, "z");
        int type = IntegerArgumentType.getInteger(context, "type");

        TimePassed time = new TimePassed();
        time.start();

        ChunkStatus status = null;
        switch (type) {
            case 0 -> {
                Optional<CompoundTag> optional = level.getChunkSource().chunkMap.read(new ChunkPos(x, z)).join();
                if (optional.isPresent()) {
                    CompoundTag tag = optional.get();
                    Constants.LOG.info("Status:" + tag.getStringOr("Status", "-"));
                } else {
                    Constants.LOG.info("No load");
                }
                break;
            }
            case 1 -> {
                status = ChunkStatus.EMPTY;
                break;
            }
            case 2 -> {
                status = ChunkStatus.STRUCTURE_STARTS;
                break;
            }
            case 3 -> {
                status = ChunkStatus.SURFACE;
                break;
            }
            case 4 -> {
                status = ChunkStatus.FEATURES;
                break;
            }
            case 5 -> {
                status = ChunkStatus.FULL;
                break;
            }
        }

        if (status != null) {
            ChunkAccess chunk = level.getChunk(x, z, status, true);
            time.end("Load");
            time.start();

            int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0);
            Constants.LOG.info("Y: " + y);
            BlockState blockState = chunk.getBlockState(new BlockPos(0, y - 1, 0));
            Constants.LOG.info("Block: " + blockState);
        }

        time.end("Time");

        return 1;
    }



    public static int getHeight(long[] rawData, int x, int z, ServerLevel level) {
        int heightBits = Mth.ceillog2(level.getHeight() + 1);
        BitStorage data = new SimpleBitStorage(heightBits, 256);

        long[] rawData2 = data.getRaw();
        if (rawData.length == rawData2.length) {
            System.arraycopy(rawData, 0, rawData2, 0, rawData.length);
        } else {
            Constants.LOG.info("Length !=");
        }

        return data.get(x + z * 16) + level.getMinY();
    }






}
