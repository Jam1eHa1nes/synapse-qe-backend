package com.synapseqe.synapse_qe.model;

import lombok.Data;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class TestRun {
    private final String buildNumber;
    private final String environment;
    private Status status = Status.IN_PROGRESS;
    private final List<ExecutionBatch> batches = new CopyOnWriteArrayList<>();

    public void addBatch(ExecutionBatch batch) {
        batches.add(batch);
    }

    public long getTotalPass() {
        return batches.stream()
            .flatMap(b -> b.testCases().stream())
            .filter(t -> t.status() == TestCase.Status.PASS)
            .count();
    }

    public long getTotalFail() {
        return batches.stream()
            .flatMap(b -> b.testCases().stream())
            .filter(t -> t.status() == TestCase.Status.FAIL)
            .count();
    }

    public enum Status {
        IN_PROGRESS, COMPLETED
    }
}
