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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

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

        switch (type) {
            case 0 -> {

                Optional<CompoundTag> optional = level.getChunkSource().chunkMap.read(new ChunkPos(x, z)).join();
                if (optional.isPresent()) {
                    CompoundTag tag = optional.get();
                    Constants.LOG.info("Loaded tag, size: " + tag.size() + ", in bytes: " + tag.sizeInBytes());

                    CompoundTag structures = tag.getCompoundOrEmpty("structures");
                    CompoundTag references = structures.getCompoundOrEmpty("References");
                    CompoundTag starts = structures.getCompoundOrEmpty("starts");

                    CompoundTag heightmaps = tag.getCompoundOrEmpty("Heightmaps");
                    LongArrayTag surface = (LongArrayTag) heightmaps.get("WORLD_SURFACE");

                    for (Map.Entry<String, Tag> entry : references.entrySet()) {
                        Constants.LOG.info(entry.getKey() + ": " + entry.getValue().getType());
                    }
                    Constants.LOG.info("---");
                    for (Map.Entry<String, Tag> entry : starts.entrySet()) {
                        Constants.LOG.info(entry.getKey() + ": " + entry.getValue().getType());
                        CompoundTag startsTag = (CompoundTag) entry.getValue();
                        for (Map.Entry<String, Tag> entry2 : startsTag.entrySet()) {
                            Constants.LOG.info("   " + entry2.getKey() + ": " + entry2.getValue().getType());
                        }
                        ListTag listTag = startsTag.getList("Children").get();
                        for (Tag t : listTag) {
                            Constants.LOG.info("C: " + ((CompoundTag) t).get("BB").getType() + ", " + t);
                        }
                    }
                    Constants.LOG.info("---");

                    if (surface != null) {
                        Constants.LOG.info("H: " + getHeight(surface.getAsLongArray(), 0, 0, level));
                    }

                } else {
                    Constants.LOG.info("No load");
                }
                break;
            }
            case 1 -> {
                ChunkAccess chunkAccess = level.getChunk(x, z, ChunkStatus.FULL, true);
                Constants.LOG.info("H: " + chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0));
                break;
            }
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
