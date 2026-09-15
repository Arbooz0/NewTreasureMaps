package kawun.new_treasure_maps.saveddata;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class FreeID extends SavedData {

    private int lastID;


    public FreeID() {}

    public FreeID(int lastID) {
        this.lastID = lastID;
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("id", lastID);
        return tag;
    }


    public static FreeID load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return new FreeID(tag.getInt("id"));
    }


    public static SavedData.Factory<FreeID> factory() {
        return new Factory<>(FreeID::new, FreeID::load, null);
    }


    public static int getFreeID() {
        if (NewTreasureMaps.server != null) {
            return NewTreasureMaps.server.overworld().getDataStorage().computeIfAbsent(factory(), Constants.MOD_ID + "_last_id").nextID();
        }
        return 0;
    }


    public int nextID() {
        lastID++;
        this.setDirty();
        return lastID;
    }
}
