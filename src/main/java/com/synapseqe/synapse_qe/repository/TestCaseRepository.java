package com.synapseqe.synapse_qe.repository;

import com.synapseqe.synapse_qe.entity.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCaseEntity, Long> {

    @Query("SELECT t FROM TestCaseEntity t " +
           "JOIN t.batch b " +
           "JOIN b.testRun r " +
           "WHERE t.suiteName = :suiteName " +
           "AND t.caseName = :caseName " +
           "AND r.environment = :environment " +
           "ORDER BY r.id DESC")
    List<TestCaseEntity> findHistory(String suiteName, String caseName, String environment);
}
