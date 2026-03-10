package com.multibank.candle.aggregation;

import com.multibank.candle.config.IntervalConfig;
import com.multibank.candle.model.BidAskEvent;
import com.multibank.candle.model.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CandleAggregator {

    private static final Logger log = LoggerFactory.getLogger(CandleAggregator.class);

    private final ConcurrentHashMap<CandleKey, CandleBuilder> current = new ConcurrentHashMap<>();

    public record CandleKey(String symbol, String interval, long candleTime) {}

    public Candle process(BidAskEvent event, String interval) {
        int intervalSec = IntervalConfig.secondsFor(interval);
        long candleTime = IntervalConfig.candleStartTime(event.timestamp(), interval);
        CandleKey key = new CandleKey(event.symbol(), interval, candleTime);
        CandleKey prevKey = new CandleKey(event.symbol(), interval, candleTime - intervalSec);

        CandleBuilder prev = current.remove(prevKey);
        Candle completed = null;
        if (prev != null) {
            prev.add(event);
            completed = prev.build();
            log.debug("Candle completed: {} {} @ {}", event.symbol(), interval, prevKey.candleTime());
        }

        current.compute(key, (k, existing) -> {
            if (existing != null) {
                existing.add(event);
                return existing;
            }
            return new CandleBuilder(event, intervalSec);
        });

        return completed;
    }

    private static final class CandleBuilder {
        private final long candleTime;
        private double open, high, low, close;
        private long volume;

        CandleBuilder(BidAskEvent first, int intervalSec) {
            this.candleTime = IntervalConfig.candleStartTime(first.timestamp(), intervalSec);
            double mid = mid(first);
            this.open = this.high = this.low = this.close = mid;
            this.volume = 1;
        }

        synchronized void add(BidAskEvent event) {
            double m = mid(event);
            high = Math.max(high, m);
            low = Math.min(low, m);
            close = m;
            volume++;
        }

        Candle build() {
            return new Candle(candleTime, open, high, low, close, volume);
        }

        private static double mid(BidAskEvent e) {
            return (e.bid() + e.ask()) / 2.0;
        }
    }
}
