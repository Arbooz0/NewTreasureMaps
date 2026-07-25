package kawun.new_treasure_maps.items;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.network.Network;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class TreasureMap extends Item {


    public static HashMap<ServerPlayer, ArrayList<Integer>> playerGettedMap = new HashMap<>();


    public TreasureMap(Properties properties) {
        Constants.LOG.info("NEW TreasureMap: " + properties.toString());
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (level.isClientSide()) {
            return;
        }
        if (slot == null || slot.getType() != EquipmentSlot.Type.HAND) {
            return;
        }

        if (owner instanceof ServerPlayer player) {
            MapComponent data = itemStack.get(Items.MAP_COMPONENT);
            if (data == null) {
                return;
            }
            if (!playerGettedMap.containsKey(player) || !playerGettedMap.get(player).contains(data.id())) {
                if (!playerGettedMap.containsKey(player)) {
                    playerGettedMap.put(player, new ArrayList<>());
                }
                MapSavedData savedData = MapSavedData.load(data.id());
                if (savedData != null) {
                    savedData.sendToPlayer(player);
                    playerGettedMap.get(player).add(data.id());
                    Constants.LOG.info("Send MapData " + data.id() + " to " + player);
                } else {
                    Constants.LOG.error("No contains MapData " + data.id());
                }
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
       if (!level.isClientSide()) {
           return InteractionResult.PASS;
       }

        MapComponent data = player.getItemInHand(hand).get(Items.MAP_COMPONENT);
        if (data == null) {
            return InteractionResult.FAIL;
        }

        MapRenderer.startAnimation(data.id());

        return InteractionResult.CONSUME;
    }
}
