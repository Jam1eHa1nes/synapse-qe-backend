package com.synapseqe.synapse_qe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {
    private String buildNumber;
    private String environment;
    private Status status = Status.IN_PROGRESS;
    private List<ExecutionBatch> batches = new ArrayList<>();
    private long totalPass;
    private long totalFail;

    public TestRun(String buildNumber, String environment) {
        this.buildNumber = buildNumber;
        this.environment = environment;
        this.batches = new ArrayList<>();
        this.status = Status.IN_PROGRESS;
    }

    public void addBatch(ExecutionBatch batch) {
        batches.add(batch);
        updateTotals();
    }

    public void updateTotals() {
        this.totalPass = batches.stream()
            .flatMap(b -> b.testCases().stream())
            .filter(t -> t.status() == TestCase.Status.PASS)
            .count();

        this.totalFail = batches.stream()
            .flatMap(b -> b.testCases().stream())
            .filter(t -> t.status() == TestCase.Status.FAIL)
            .count();
    }

    public enum Status {
        IN_PROGRESS, COMPLETED
    }
}
