package com.synapseqe.synapse_qe.service;

import com.synapseqe.synapse_qe.model.TestRun;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateManager {

    private final Map<String, TestRun> activeRuns = new ConcurrentHashMap<>();

    public TestRun getOrCreateRun(String buildNumber, String environment) {
        return activeRuns.computeIfAbsent(buildNumber, k -> new TestRun(buildNumber, environment));
    }

    public TestRun getRun(String buildNumber) {
        return activeRuns.get(buildNumber);
    }

    public void removeRun(String buildNumber) {
        activeRuns.remove(buildNumber);
    }

    public Map<String, TestRun> getAllActiveRuns() {
        return Map.copyOf(activeRuns);
    }
}
