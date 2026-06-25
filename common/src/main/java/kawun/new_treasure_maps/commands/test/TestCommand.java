package kawun.new_treasure_maps.commands.test;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.commands.Command;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.network.Network;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TestCommand extends Command {


    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("test").executes(TestCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        Constants.LOG.info("TEST!");
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();

        ItemStack item = player.getMainHandItem();

        byte[] bytes = new byte[16384];
        for (int i = 0; i < 16384; i++) {
            bytes[i] = (byte) (Math.sin(i) * 100.0);
        }

        Network.sendToPlayer(player, new MapPacket(33, MapType.DOTTED_LINE, bytes ));

        return 1;
    }



}
