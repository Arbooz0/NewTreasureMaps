package kawun.new_treasure_maps.saveddata;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.network.Network;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;


public class MapSavedData extends SavedData {

    public int id;
    public MapType mapType = MapType.NONE;
    public byte[] bytes;


    public MapSavedData() {}

    public MapSavedData(int id, MapType mapType, byte[] bytes) {
        this.id = id;
        this.mapType = mapType;
        this.bytes = bytes;
    }

    public MapSavedData(MapType mapType, byte[] bytes) {
        this.mapType = mapType;
        this.bytes = bytes;
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putByte("type", mapType.getId());
        tag.putByteArray("bytes", bytes);
        return tag;
    }

    public static MapSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return new MapSavedData(MapType.byId(tag.getByte("type")), tag.getByteArray("bytes"));
    }


    public static SavedData.Factory<MapSavedData> factory() {
        return new Factory<>(MapSavedData::new, MapSavedData::load, null);
    }


    public static MapSavedData load(int id) {
        if (NewTreasureMaps.server != null) {
            MapSavedData data = NewTreasureMaps.server.overworld().getDataStorage().get(factory(), Constants.MOD_ID + "_map" + id);
            if (data != null) {
                data.id = id;
            }
            return data;
        }
        return null;
    }


    public void save() {
        if (NewTreasureMaps.server != null) {
            setDirty();
            NewTreasureMaps.server.overworld().getDataStorage().set(Constants.MOD_ID + "_map" + id, this);
        }
    }


    public void sendToPlayer(ServerPlayer player) {
        Network.sendToPlayer(player, new MapPacket(id, mapType, bytes));
    }


}
