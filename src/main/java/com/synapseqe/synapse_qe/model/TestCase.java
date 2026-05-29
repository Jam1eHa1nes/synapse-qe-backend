package com.synapseqe.synapse_qe.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record TestCase(
    String suiteName,
    String caseName,
    Status status,
    String errorMessage,
    String rawStackTrace,
    String errorFingerprint
) {
    public enum Status {
        PASS, FAIL
    }
}
