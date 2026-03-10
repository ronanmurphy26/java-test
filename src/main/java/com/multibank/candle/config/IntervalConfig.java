package com.multibank.candle.config;

import java.util.Map;
import java.util.Set;

public final class IntervalConfig {

    private static final Map<String, Integer> INTERVALS = Map.of(
            "1s", 1,
            "5s", 5,
            "1m", 60,
            "15m", 900,
            "1h", 3600
    );

    public static Set<String> supportedIntervals() {
        return INTERVALS.keySet();
    }

    public static int secondsFor(String interval) {
        Integer sec = INTERVALS.get(interval);
        if (sec == null) throw new IllegalArgumentException("Unsupported interval: " + interval);
        return sec;
    }

    public static long candleStartTime(long timestampSeconds, String interval) {
        return candleStartTime(timestampSeconds, secondsFor(interval));
    }

    public static long candleStartTime(long timestampSeconds, int intervalSeconds) {
        return (timestampSeconds / intervalSeconds) * intervalSeconds;
    }

    private IntervalConfig() {}
}
