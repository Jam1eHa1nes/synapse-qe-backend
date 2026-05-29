package com.synapseqe.synapse_qe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    private long durationMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private long timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    private TestCaseEntity testCase;

    public enum Status {
        PASS, FAIL
    }
}
