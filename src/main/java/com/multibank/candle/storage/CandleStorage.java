package com.multibank.candle.storage;

import com.multibank.candle.model.Candle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleStorage {

    private final ConcurrentHashMap<String, List<Candle>> data = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    private static String key(String symbol, String interval) {
        return symbol + "|" + interval;
    }

    public void save(Candle candle, String symbol, String interval) {
        String k = key(symbol, interval);
        synchronized (locks.computeIfAbsent(k, x -> new Object())) {
            data.compute(k, (kk, list) -> {
                List<Candle> copy = list != null ? new ArrayList<>(list) : new ArrayList<>();
                copy.add(candle);
                copy.sort((a, b) -> Long.compare(a.time(), b.time()));
                return copy;
            });
        }
    }

    public List<Candle> getRange(String symbol, String interval, long from, long to) {
        List<Candle> list = data.get(key(symbol, interval));
        if (list == null) return List.of();
        return list.stream()
                .filter(c -> c.time() >= from && c.time() <= to)
                .toList();
    }

    public void clear() {
        data.clear();
        locks.clear();
    }
}
