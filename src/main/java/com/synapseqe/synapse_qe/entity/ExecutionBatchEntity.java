package com.synapseqe.synapse_qe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "execution_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionBatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String batchId;

    private long durationMs;

    @ElementCollection
    @CollectionTable(name = "batch_metadata", joinColumns = @JoinColumn(name = "batch_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id")
    private TestRunEntity testRun;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TestCaseEntity> testCases = new ArrayList<>();
}
