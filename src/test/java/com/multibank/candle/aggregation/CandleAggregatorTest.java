package com.multibank.candle.aggregation;

import com.multibank.candle.model.BidAskEvent;
import com.multibank.candle.model.Candle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CandleAggregatorTest {

    private CandleAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new CandleAggregator();
    }

    @Test
    void singleEventProducesNoCompletedCandle() {
        Candle completed = aggregator.process(
                new BidAskEvent("BTC-USD", 100.0, 101.0, 1620000000L), "1m");
        assertThat(completed).isNull();
    }

    @Test
    void eventsInSameMinuteNoCompletion() {
        aggregator.process(new BidAskEvent("BTC-USD", 100.0, 101.0, 1620000000L), "1m");
        Candle completed = aggregator.process(new BidAskEvent("BTC-USD", 102.0, 103.0, 1620000030L), "1m");
        assertThat(completed).isNull();
    }

    @Test
    void eventInNextMinuteCompletesPreviousCandle() {
        aggregator.process(new BidAskEvent("BTC-USD", 100.0, 101.0, 1620000000L), "1m");
        aggregator.process(new BidAskEvent("BTC-USD", 102.0, 103.0, 1620000030L), "1m");
        Candle completed = aggregator.process(new BidAskEvent("BTC-USD", 99.0, 100.0, 1620000060L), "1m");
        assertThat(completed).isNotNull();
        assertThat(completed.time()).isEqualTo(1620000000L);
        assertThat(completed.open()).isEqualTo(100.5);
        assertThat(completed.high()).isEqualTo(102.5);
        assertThat(completed.low()).isEqualTo(99.5);
        assertThat(completed.close()).isEqualTo(99.5);
        assertThat(completed.volume()).isEqualTo(3);
    }

    @Test
    void multipleSymbolsAndIntervalsIndependent() {
        aggregator.process(new BidAskEvent("BTC-USD", 100.0, 101.0, 1620000000L), "1m");
        aggregator.process(new BidAskEvent("ETH-USD", 50.0, 51.0, 1620000000L), "1m");
        Candle completed = aggregator.process(new BidAskEvent("BTC-USD", 102.0, 103.0, 1620000060L), "1m");
        assertThat(completed).isNotNull();
        assertThat(completed.time()).isEqualTo(1620000000L);
        assertThat(completed.close()).isEqualTo(102.5);
    }
}
