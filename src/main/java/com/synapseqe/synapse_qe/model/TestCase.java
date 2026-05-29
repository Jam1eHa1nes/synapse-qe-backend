package com.synapseqe.synapse_qe.model;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record TestCase(
    String suiteName,
    String caseName,
    Status status,
    String errorMessage,
    String rawStackTrace,
    String errorFingerprint,
    List<TestStep> steps
) {
    public enum Status {
        PASS, FAIL
    }
}
