package com.synapseqe.synapse_qe.model;

import lombok.Builder;

@Builder
public record TestStep(
    String name,
    Status status,
    long durationMs,
    String errorMessage,
    long timestamp
) {
    public enum Status {
        PASS, FAIL
    }
}
