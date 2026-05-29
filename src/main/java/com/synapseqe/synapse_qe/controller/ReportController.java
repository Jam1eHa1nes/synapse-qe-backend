package com.synapseqe.synapse_qe.controller;

import com.synapseqe.synapse_qe.model.TestRun;
import com.synapseqe.synapse_qe.service.StateManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final StateManager stateManager;

    public ReportController(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @GetMapping("/active")
    public ResponseEntity<java.util.List<TestRun>> getActiveReports() {
        return ResponseEntity.ok(stateManager.getLatestRuns());
    }

    @GetMapping("/history")
    public ResponseEntity<java.util.List<TestRun>> getHistoricalReports(@RequestParam(required = false) String environment) {
        if (environment != null) {
            return ResponseEntity.ok(stateManager.getHistoricalRunsForEnvironment(environment));
        }
        return ResponseEntity.ok(stateManager.getHistoricalRuns());
    }

    @GetMapping("/testcase-history")
    public ResponseEntity<java.util.List<com.synapseqe.synapse_qe.dto.TestCaseHistoryDTO>> getTestCaseHistory(
            @RequestParam String suiteName,
            @RequestParam String caseName,
            @RequestParam String environment) {
        return ResponseEntity.ok(stateManager.getTestCaseHistory(suiteName, caseName, environment));
    }

    @GetMapping("/{buildNumber}")
    public ResponseEntity<TestRun> getReport(@PathVariable String buildNumber) {
        TestRun run = stateManager.getRun(buildNumber);
        if (run == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(run);
    }
}
