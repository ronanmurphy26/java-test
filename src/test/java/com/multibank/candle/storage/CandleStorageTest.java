package com.multibank.candle.storage;

import com.multibank.candle.model.Candle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandleStorageTest {

    private CandleStorage storage;

    @BeforeEach
    void setUp() {
        storage = new CandleStorage();
    }

    @Test
    void saveAndGetRange() {
        storage.save(new Candle(1620000000L, 100, 105, 99, 102, 10), "BTC-USD", "1m");
        storage.save(new Candle(1620000060L, 102, 106, 101, 104, 8), "BTC-USD", "1m");

        List<Candle> result = storage.getRange("BTC-USD", "1m", 1620000000L, 1620000060L);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).time()).isEqualTo(1620000000L);
        assertThat(result.get(1).time()).isEqualTo(1620000060L);
    }

    @Test
    void getRangeEmptyWhenNoData() {
        List<Candle> result = storage.getRange("BTC-USD", "1m", 1620000000L, 1620000060L);
        assertThat(result).isEmpty();
    }

    @Test
    void getRangeFiltersByTime() {
        storage.save(new Candle(1620000000L, 100, 105, 99, 102, 10), "BTC-USD", "1m");
        storage.save(new Candle(1620000060L, 102, 106, 101, 104, 8), "BTC-USD", "1m");
        storage.save(new Candle(1620000120L, 104, 108, 103, 106, 5), "BTC-USD", "1m");

        List<Candle> result = storage.getRange("BTC-USD", "1m", 1620000060L, 1620000060L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).time()).isEqualTo(1620000060L);
    }
}
