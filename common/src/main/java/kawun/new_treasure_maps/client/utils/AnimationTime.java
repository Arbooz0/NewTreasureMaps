package kawun.new_treasure_maps.client.utils;

import kawun.new_treasure_maps.Constants;

public class AnimationTime {


    private final long startTime = System.currentTimeMillis();
    private final float offset;
    public final float duration;
    public final boolean isReverse;

    public float t = 0;
    public float part1 = 0;
    public float part2 = 0;
    public float partTransition = 0;
    public boolean isEnd = false;


    public AnimationTime(float duration) {
        this(duration, false, 0);
    }

    public AnimationTime(float duration, boolean isReverse, float offset) {
        this.duration = duration;
        this.isReverse = isReverse;
        this.offset = offset;
    }


    public void update() {
        if (isEnd) {
            return;
        }
        float time = ((System.currentTimeMillis() - startTime) / 1000.0f) / duration + offset;
        if (time >= 1) {
            time = 1;
            isEnd = true;
        }

        if (isReverse) {
            time = 1 - time;
        }

        t = (float) Math.sin(time * Math.PI / 2.0f);
        part1 = (float) Math.sin(Math.min(t * 2, 1) * Math.PI / 2.0f);
        part2 = (float) Math.sin(Math.max((t - 0.5f) * 2, 0) * Math.PI / 2.0f);
        partTransition = (float) Math.sin(Math.max((part1 - 0.8f) * 5, 0) * Math.PI / 2.0f);

    }

    public AnimationTime reverse() {
        float time = Math.min(((System.currentTimeMillis() - startTime) / 1000.0f) / duration + offset, 1);
        return new AnimationTime(duration, !isReverse, 1 - time);
    }
}
