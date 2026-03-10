package com.multibank.candle.api;

import com.multibank.candle.model.Candle;

import java.util.List;

public record HistoryResponse(String s, long[] t, double[] o, double[] h, double[] l, double[] c, long[] v) {

    public static HistoryResponse from(List<Candle> candles) {
        int n = candles.size();
        long[] t = new long[n];
        double[] o = new double[n];
        double[] h = new double[n];
        double[] l = new double[n];
        double[] c = new double[n];
        long[] v = new long[n];
        for (int i = 0; i < n; i++) {
            Candle x = candles.get(i);
            t[i] = x.time();
            o[i] = x.open();
            h[i] = x.high();
            l[i] = x.low();
            c[i] = x.close();
            v[i] = x.volume();
        }
        return new HistoryResponse("ok", t, o, h, l, c, v);
    }
}
