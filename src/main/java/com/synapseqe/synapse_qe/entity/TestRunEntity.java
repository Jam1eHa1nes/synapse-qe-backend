package com.synapseqe.synapse_qe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String buildNumber;

    private String environment;

    @Enumerated(EnumType.STRING)
    private Status status;

    private long totalPass;
    private long totalFail;

    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExecutionBatchEntity> batches = new ArrayList<>();

    public enum Status {
        IN_PROGRESS, COMPLETED
    }
}
