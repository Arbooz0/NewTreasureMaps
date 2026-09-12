package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

public class NextChestPosCommand {


    public static BlockPos pos = null;


    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("next_chest_pos").then(
                Commands.argument("pos", BlockPosArgument.blockPos()).executes(NextChestPosCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        pos = BlockPosArgument.getBlockPos(context, "pos");
        return 1;
    }


    public static BlockPos getPos() {
        BlockPos p = pos;
        pos = null;
        return p;
    }
}
