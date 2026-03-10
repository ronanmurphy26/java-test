package com.multibank.candle.api;

import com.multibank.candle.model.Candle;
import com.multibank.candle.storage.CandleStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.task.scheduling.enabled=false")
class HistoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandleStorage storage;

    @BeforeEach
    void setUp() {
        storage.clear();
        storage.save(new Candle(1620000000L, 29500.5, 29510.0, 29490.0, 29505.0, 10), "BTC-USD", "1m");
        storage.save(new Candle(1620000060L, 29501.0, 29505.0, 29500.0, 29502.0, 8), "BTC-USD", "1m");
    }

    @Test
    void historyReturnsOkWithArrays() throws Exception {
        mockMvc.perform(get("/history")
                        .param("symbol", "BTC-USD")
                        .param("interval", "1m")
                        .param("from", "1620000000")
                        .param("to", "1620000600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s").value("ok"))
                .andExpect(jsonPath("$.t").isArray())
                .andExpect(jsonPath("$.o").isArray())
                .andExpect(jsonPath("$.h").isArray())
                .andExpect(jsonPath("$.l").isArray())
                .andExpect(jsonPath("$.c").isArray())
                .andExpect(jsonPath("$.v").isArray())
                .andExpect(jsonPath("$.t.length()").value(2))
                .andExpect(jsonPath("$.t[0]").value(1620000000))
                .andExpect(jsonPath("$.t[1]").value(1620000060))
                .andExpect(jsonPath("$.o[0]").value(29500.5))
                .andExpect(jsonPath("$.c[0]").value(29505.0))
                .andExpect(jsonPath("$.v[0]").value(10));
    }

    @Test
    void historyRejectsUnsupportedInterval() throws Exception {
        mockMvc.perform(get("/history")
                        .param("symbol", "BTC-USD")
                        .param("interval", "2m")
                        .param("from", "1620000000")
                        .param("to", "1620000600"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void historyRejectsFromAfterTo() throws Exception {
        mockMvc.perform(get("/history")
                        .param("symbol", "BTC-USD")
                        .param("interval", "1m")
                        .param("from", "1620000600")
                        .param("to", "1620000000"))
                .andExpect(status().isBadRequest());
    }
}
