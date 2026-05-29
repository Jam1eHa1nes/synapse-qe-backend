package com.synapseqe.synapse_qe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String suiteName;
    private String caseName;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String rawStackTrace;

    private String errorFingerprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ExecutionBatchEntity batch;

    public enum Status {
        PASS, FAIL
    }
}
