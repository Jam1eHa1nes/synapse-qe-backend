package com.synapseqe.synapse_qe.dto;

import com.synapseqe.synapse_qe.model.TestRun;

public record LiveUpdateDTO(
    String buildNumber,
    String environment,
    TestRun.Status status,
    long totalPass,
    long totalFail,
    int batchCount
) {
}
