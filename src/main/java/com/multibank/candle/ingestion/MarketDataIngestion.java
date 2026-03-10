package com.multibank.candle.ingestion;

import com.multibank.candle.aggregation.CandleAggregator;
import com.multibank.candle.config.IntervalConfig;
import com.multibank.candle.model.BidAskEvent;
import com.multibank.candle.model.Candle;
import com.multibank.candle.storage.CandleStorage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class MarketDataIngestion {

    private static final String[] SYMBOLS = {"BTC-USD", "ETH-USD"};

    private final CandleAggregator aggregator;
    private final CandleStorage storage;

    public MarketDataIngestion(CandleAggregator aggregator, CandleStorage storage) {
        this.aggregator = aggregator;
        this.storage = storage;
    }

    @Scheduled(fixedRate = 200)
    public void emitEvent() {
        String symbol = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
        long ts = System.currentTimeMillis() / 1000;
        double base = "BTC-USD".equals(symbol) ? 95_000 : 3_500;
        double spread = 10 + ThreadLocalRandom.current().nextDouble() * 20;
        double bid = base + (ThreadLocalRandom.current().nextDouble() - 0.5) * 100;
        BidAskEvent event = new BidAskEvent(symbol, bid, bid + spread, ts);

        for (String interval : IntervalConfig.supportedIntervals()) {
            Candle completed = aggregator.process(event, interval);
            if (completed != null) storage.save(completed, event.symbol(), interval);
        }
    }
}
