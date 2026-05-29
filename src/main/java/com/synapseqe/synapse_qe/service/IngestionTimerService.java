package com.synapseqe.synapse_qe.service;

import com.synapseqe.synapse_qe.model.TestRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@EnableAsync
@Slf4j
public class IngestionTimerService {

    private final StateManager stateManager;
    private final Map<String, Instant> lastActivityMap = new ConcurrentHashMap<>();
    
    @org.springframework.beans.factory.annotation.Value("${synapseqe.ingestion.debounce-minutes:2}")
    private long debounceDurationMinutes;

    public IngestionTimerService(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void resetTimer(String buildNumber) {
        lastActivityMap.put(buildNumber, Instant.now());
        log.debug("Timer reset for build: {}", buildNumber);
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void checkIdleRuns() {
        Instant cutoff = Instant.now().minus(debounceDurationMinutes, java.time.temporal.ChronoUnit.MINUTES);
        
        lastActivityMap.forEach((buildNumber, lastActivity) -> {
            if (lastActivity.isBefore(cutoff)) {
                completeRun(buildNumber);
            }
        });
    }

    // For testing
    protected void setDebounceDurationMinutes(long minutes) {
        this.debounceDurationMinutes = minutes;
    }

    private void completeRun(String buildNumber) {
        TestRun run = stateManager.getRun(buildNumber);
        if (run != null && run.getStatus() == TestRun.Status.IN_PROGRESS) {
            run.setStatus(TestRun.Status.COMPLETED);
            stateManager.saveRun(run);
            lastActivityMap.remove(buildNumber);
            log.info("Build {} marked as COMPLETED due to inactivity.", buildNumber);
        }
    }
}
