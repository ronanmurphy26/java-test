# Candle Aggregation Service

Backend Java service that ingests a stream of bid/ask market data, aggregates it into OHLC (candlestick) format per symbol and interval, and exposes a REST API for historical candle data.

## Features

- **Stream ingestion:** Simulated bid/ask events (configurable rate) for multiple symbols (e.g. BTC-USD, ETH-USD).
- **Candlestick aggregation:** OHLC per symbol and interval (1s, 5s, 1m, 15m, 1h). Mid price `(bid+ask)/2` for OHLC; volume = tick count.
- **Storage:** In-memory, thread-safe storage with range queries.
- **History API:** `GET /history?symbol=BTC-USD&interval=1m&from=1620000000&to=1620000600` returning `{ "s": "ok", "t": [...], "o": [...], "h": [...], "l": [...], "c": [...], "v": [...] }`.
- **Health:** Spring Boot Actuator `GET /actuator/health`.

## Assumptions and trade-offs

- **OHLC source:** Mid price `(bid+ask)/2` is used for open/high/low/close. High/low could alternatively use bid/ask extremes; mid was chosen for simplicity and consistency.
- **Volume:** Volume is the number of ticks (events) per candle. Event-derived or notional volume can be added later.
- **Storage:** In-memory only (no PostgreSQL/TimescaleDB) for minimal setup. Data is lost on restart. Suitable for demo and tests; production would use a time-series store.
- **Intervals:** Fixed set (1s, 5s, 1m, 15m, 1h). Adding new intervals requires code change in `IntervalConfig`.
- **Timestamps:** All timestamps are Unix seconds.

## Prerequisites

- **Java 17 or 21** (e.g. [Eclipse Temurin](https://adoptium.net/))
- **Maven 3.9+** ([Apache Maven](https://maven.apache.org/download.cgi))

Ensure `JAVA_HOME` and Maven `bin` are on your `PATH`.

## Build and run

```bash
# Compile
mvn clean compile

# Run all tests
mvn test

# Run the application
mvn spring-boot:run
```

The app listens on **http://localhost:8080**. A simulated stream emits bid/ask events every 200 ms. After a short time you can request history, e.g.:

```bash
# Use current time window (Unix seconds)
# Example: from=1720000000 to=1720003600
curl "http://localhost:8080/history?symbol=BTC-USD&interval=1m&from=1720000000&to=1720003600"
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Project layout

- `model/` – `BidAskEvent`, `Candle` records
- `config/` – `IntervalConfig` (supported intervals, candle time alignment)
- `aggregation/` – `CandleAggregator` (thread-safe OHLC from events)
- `storage/` – `CandleStorage` (in-memory, thread-safe)
- `ingestion/` – `MarketDataIngestion` (simulated stream, scheduled)
- `api/` – `HistoryController`, `HistoryResponse`

## Tests

- **Unit:** `CandleAggregatorTest` (OHLC and period rollover), `CandleStorageTest` (save and range query).
- **Integration:** `HistoryControllerIntegrationTest` (GET /history response shape and unsupported interval).

Run tests: `mvn test`.

## Bonus / future work

- Persistence with PostgreSQL or TimescaleDB.
- Real data sources (Kafka, WebSocket) behind an abstraction.
- Replay of missed data on startup.
- Configurable symbols and intervals via config file.
