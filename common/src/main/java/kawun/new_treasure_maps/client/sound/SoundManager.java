package kawun.new_treasure_maps.client.sound;

import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public class SoundManager {

    public static int soundIndex = 0;



    public static void playSound() {
        playSound(1);
    }


    public static void playSound(double x, double y, double z) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float d = (float) (Math.sqrt(player.distanceToSqr(x, y, z)) / 15.0);
        playSound(0.9f - d);
    }


    public static void playSound(float volume) {
        soundIndex++;
        if (soundIndex > 3) {
            soundIndex = 1;
        }
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                Utils.identifier("unroll" + soundIndex),
                SoundSource.PLAYERS,
                volume,
                1,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0, 0, 0,
                true
        ));
    }


}
