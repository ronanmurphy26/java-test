package com.multibank.candle.api;

import com.multibank.candle.config.IntervalConfig;
import com.multibank.candle.storage.CandleStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryController {

    private final CandleStorage storage;

    public HistoryController(CandleStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> history(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam long from,
            @RequestParam long to) {

        if (!IntervalConfig.supportedIntervals().contains(interval) || from > to) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(HistoryResponse.from(storage.getRange(symbol, interval, from, to)));
    }
}
