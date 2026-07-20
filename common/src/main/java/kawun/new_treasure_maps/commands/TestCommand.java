package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.item.ItemStack;

public class TestCommand {




    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("test")
                                .then(Commands.argument("type", IntegerArgumentType.integer())
                                    .executes(TestCommand::execute));
    }


    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        ItemStack itemStack = player.getMainHandItem();
        int x = player.getBlockX();
        int z = player.getBlockZ();

        int type = IntegerArgumentType.getInteger(context, "type");

        TimePassed time = new TimePassed();
        time.start();



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
