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
                TestCommand::get_command,
                CreateMapCommand::get_command,
                PoseCommand::get_command,
                ChestLootCommand::get_command
        );
        registerCommands(dispatcher, commands);
    }


    public static void registerClient(CommandDispatcher<CommandSourceStack> dispatcher) {
        List<Supplier<LiteralArgumentBuilder<CommandSourceStack>>> commands = List.of(
                ClientTestCommand::get_command
        );
        registerCommands(dispatcher, commands);
    }



    private static void registerCommands(
            CommandDispatcher<CommandSourceStack> dispatcher,
            List<Supplier<LiteralArgumentBuilder<CommandSourceStack>>> commands
    ) {
        LiteralArgumentBuilder<CommandSourceStack> base_command = Commands.literal(Constants.MOD_ID);

        for (Supplier<LiteralArgumentBuilder<CommandSourceStack>> supplier : commands) {
            LiteralArgumentBuilder<CommandSourceStack> command = supplier.get();

            base_command.then(command);
        }

        dispatcher.register(base_command);
    }
}
