package com.synapseqe.synapse_qe.model;

import java.util.List;
import java.util.Map;

public record ExecutionBatch(
    String batchId,
    long durationMs,
    Map<String, String> metadata,
    List<TestCase> testCases
) {
}
