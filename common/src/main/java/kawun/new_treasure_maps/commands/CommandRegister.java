package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kawun.new_treasure_maps.Constants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;
import java.util.function.Supplier;

public class CommandRegister {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        List<Supplier<LiteralArgumentBuilder<CommandSourceStack>>> commands = List.of(
                ConfigCommand::getCommand,
                CreateMapCommand::getCommand,
                NextChestPosCommand::getCommand,
                ChestLootCommand::getCommand
        );
        registerCommands(dispatcher, commands);
    }



    private static void registerCommands(
            CommandDispatcher<CommandSourceStack> dispatcher,
            List<Supplier<LiteralArgumentBuilder<CommandSourceStack>>> commands
    ) {
        LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal(Constants.MOD_ID)
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS));

        for (Supplier<LiteralArgumentBuilder<CommandSourceStack>> supplier : commands) {
            LiteralArgumentBuilder<CommandSourceStack> command = supplier.get();

            baseCommand.then(command);
        }

        dispatcher.register(baseCommand);
    }
}
