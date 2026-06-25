package kawun.new_treasure_maps.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class FreeID extends SavedData {

    public static final Codec<FreeID> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    Codec.INT.optionalFieldOf("id", -1).forGetter(m -> m.lastID))
                    .apply(i, FreeID::new)
    );

    public static final SavedDataType<FreeID> TYPE = new SavedDataType<>(
            Utils.identifier("last_id"), FreeID::new, CODEC, null
    );


    private int lastID;

    public FreeID() {}

    public FreeID(int lastID) {
        this.lastID = lastID;
    }



    public static int getFreeID() {
        if (NewTreasureMaps.server != null) {
            return NewTreasureMaps.server.getDataStorage().computeIfAbsent(TYPE).nextID();
        }
        return 0;
    }


    public int nextID() {
        lastID++;
        this.setDirty();
        return lastID;
    }
}
