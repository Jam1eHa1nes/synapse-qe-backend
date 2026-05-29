package com.synapseqe.synapse_qe.repository;

import com.synapseqe.synapse_qe.entity.TestRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestRunRepository extends JpaRepository<TestRunEntity, Long> {
    Optional<TestRunEntity> findByBuildNumber(String buildNumber);
    java.util.List<TestRunEntity> findByEnvironmentAndStatus(String environment, TestRunEntity.Status status);
}
