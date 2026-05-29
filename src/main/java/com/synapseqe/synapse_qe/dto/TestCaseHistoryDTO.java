package com.synapseqe.synapse_qe.dto;

import com.synapseqe.synapse_qe.entity.TestCaseEntity;

public record TestCaseHistoryDTO(
    String buildNumber,
    String environment,
    TestCaseEntity.Status status
) {
}
