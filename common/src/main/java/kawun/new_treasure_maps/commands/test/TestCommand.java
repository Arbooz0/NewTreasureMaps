package kawun.new_treasure_maps.commands.test;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.commands.Command;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.maps.ColoredMapCreate;
import kawun.new_treasure_maps.maps.TestMap;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.network.Network;
import kawun.new_treasure_maps.utils.Pixels;
import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;

public class TestCommand extends Command {


    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("test").then(Commands.argument("index", IntegerArgumentType.integer()).executes(TestCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index");

        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        ItemStack itemStack = player.getMainHandItem();
        int x = player.getBlockX();
        int z = player.getBlockZ();

        Heightmap.Types type = Heightmap.Types.values()[index];
        int y = level.getHeight(type, x, z) - 1;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState block = level.getBlockState(pos);

        String text = type + ":\n" + x + " " + y + " " + z + "\n" + block;
        Constants.LOG.info(text);
        player.sendSystemMessage(Component.literal(text));

        return 1;
    }



}
