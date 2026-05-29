package com.synapseqe.synapse_qe.service;

import com.synapseqe.synapse_qe.model.TestRun;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = "synapseqe.ingestion.debounce-minutes=0") // Immediate timeout for testing
class DebounceTimerTest {

    @Autowired
    private IngestionTimerService timerService;

    @Autowired
    private StateManager stateManager;

    @Test
    void shouldMarkRunAsCompletedAfterTimeout() {
        String buildNumber = "timeout-build";
        TestRun run = stateManager.getOrCreateRun(buildNumber, "test");
        
        timerService.resetTimer(buildNumber);
        
        // The checkIdleRuns is scheduled every 10 seconds. 
        // With debounce-minutes=0, any check will trigger it.
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            TestRun currentRun = stateManager.getRun(buildNumber);
            assertEquals(TestRun.Status.COMPLETED, currentRun.getStatus());
        });
    }
}
