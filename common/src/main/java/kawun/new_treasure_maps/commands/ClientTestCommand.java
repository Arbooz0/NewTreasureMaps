package kawun.new_treasure_maps.commands;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.client.NewTreasureMapsClient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Objects;

public class ClientTestCommand {




    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("client")
                                .then(Commands.argument("text", StringArgumentType.string())
                                    .executes(ClientTestCommand::execute));
    }


    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        String text = StringArgumentType.getString(context, "text");

        return 1;
    }







}
