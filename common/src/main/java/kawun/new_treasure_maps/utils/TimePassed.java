package kawun.new_treasure_maps.utils;

import kawun.new_treasure_maps.Constants;

public class TimePassed {

    private long _start_t;


    public TimePassed() {
        start();
    }


    public void start() {
        _start_t = System.nanoTime();
    }


    public void end(String message) {
        long _end_t = System.nanoTime();
        double passed = Math.round((_end_t - _start_t) / 100_000.0) / 10.0;
        Constants.LOG.info(message + ": " + passed + " ms");
    }
}
