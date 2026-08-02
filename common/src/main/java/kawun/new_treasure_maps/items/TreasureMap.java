package kawun.new_treasure_maps.items;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.network.Network;
import kawun.new_treasure_maps.network.OpenMapPacket;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
import java.util.HashSet;
import java.util.List;

public class TreasureMap extends Item {


    public static Int2ObjectOpenHashMap<Tracker> trackers = new Int2ObjectOpenHashMap<>();
    private int tick = 0;


    public TreasureMap(Properties properties) {
        Constants.LOG.info("NEW TreasureMap: " + properties.toString());
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (level.isClientSide()) {
            return;
        }

        tick++;
        if (tick < 10) {
            return;
        }
        tick = 0;

        if (slot == null || slot.getType() != EquipmentSlot.Type.HAND) {
            return;
        }

        if (owner instanceof ServerPlayer ownerPlayer) {
            MapComponent data = itemStack.get(Items.MAP_COMPONENT);
            if (data == null) {
                return;
            }
            getTracker(data.id()).update(level, ownerPlayer);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        MapComponent data = player.getItemInHand(hand).get(Items.MAP_COMPONENT);
        if (data == null) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            MapRenderer.toggleOpenMap(data.id());
        } else {
            getTracker(data.id()).toggleOpen((ServerPlayer) player);
        }

        return InteractionResult.CONSUME;
    }


    public static Tracker getTracker(int id) {
        return trackers.computeIfAbsent(id, i -> new Tracker(id));
    }


    public static void clear() {
        trackers.clear();
    }

    public static void playerLeaved(ServerPlayer player) {
        for (Tracker tracker : trackers.values()) {
            tracker.playersReceived.removeBoolean(player);
        }
    }



    public static class Tracker {

        public final int id;
        public Object2BooleanOpenHashMap<ServerPlayer> playersReceived = new Object2BooleanOpenHashMap<>();
        public boolean isOpen = false;


        public Tracker(int id) {
            this.id = id;
        }


        public void update(ServerLevel level, ServerPlayer ownerPlayer) {
            for (ServerPlayer player : getPlayersNearPlayer(level, ownerPlayer, 10)) {

                if (playersReceived.containsKey(player)) {
                    if (playersReceived.getBoolean(player) != isOpen) {
                        playersReceived.put(player, isOpen);
                        sendOpen(player);
                    }
                } else {
                    MapSavedData savedData = MapSavedData.load(id);
                    if (savedData != null) {
                        savedData.sendToPlayer(player);
                        playersReceived.put(player, isOpen);
                        if (isOpen) {
                            sendOpen(player);
                        }
                        Constants.LOG.info("Send MapData " + id + " to " + player);
                    } else {
                        Constants.LOG.error("No contains MapData " + id);
                        break;
                    }
                }

            }
        }


        public List<ServerPlayer> getPlayersNearPlayer(ServerLevel level, ServerPlayer player, float radius) {
            float radiusSq = radius * radius;
            return level.getPlayers((p) -> p.distanceToSqr(player) <= radiusSq);
        }


        public void toggleOpen(ServerPlayer playerOpen) {
            isOpen = !isOpen;
            playersReceived.put(playerOpen, isOpen);

            for (Object2BooleanMap.Entry<ServerPlayer> entry : playersReceived.object2BooleanEntrySet()) {
                if (isOpen != entry.getBooleanValue()) {
                    if (entry.getKey().distanceToSqr(playerOpen) <= 100) {
                        playersReceived.put(entry.getKey(), isOpen);
                        sendOpen(entry.getKey());
                    }
                }
            }
        }



        public void sendOpen(ServerPlayer player) {
            Network.sendToPlayer(player, new OpenMapPacket(id, isOpen));
        }



    }


}
