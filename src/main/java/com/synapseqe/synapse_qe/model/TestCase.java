package com.synapseqe.synapse_qe.model;

public record TestCase(
    String suiteName,
    String caseName,
    Status status,
    String errorMessage,
    String rawStackTrace
) {
    public enum Status {
        PASS, FAIL
    }
}
