package com.synapseqe.synapse_qe.service;

import com.synapseqe.synapse_qe.dto.TestCaseHistoryDTO;
import com.synapseqe.synapse_qe.entity.ExecutionBatchEntity;
import com.synapseqe.synapse_qe.entity.TestCaseEntity;
import com.synapseqe.synapse_qe.entity.TestRunEntity;
import com.synapseqe.synapse_qe.model.ExecutionBatch;
import com.synapseqe.synapse_qe.model.TestCase;
import com.synapseqe.synapse_qe.model.TestRun;
import com.synapseqe.synapse_qe.repository.TestCaseRepository;
import com.synapseqe.synapse_qe.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateManager {

    private final TestRunRepository testRunRepository;
    private final TestCaseRepository testCaseRepository;

    @Transactional
    public TestRun getOrCreateRun(String buildNumber, String environment) {
        Optional<TestRunEntity> existing = testRunRepository.findByBuildNumber(buildNumber);
        if (existing.isPresent()) {
            return mapToModel(existing.get());
        }

        // It's a new run. Mark all existing IN_PROGRESS runs for this environment as COMPLETED
        List<TestRunEntity> activeRuns = testRunRepository.findByEnvironmentAndStatus(environment, TestRunEntity.Status.IN_PROGRESS);
        for (TestRunEntity run : activeRuns) {
            run.setStatus(TestRunEntity.Status.COMPLETED);
            testRunRepository.save(run);
        }

        TestRunEntity newRun = TestRunEntity.builder()
                .buildNumber(buildNumber)
                .environment(environment)
                .status(TestRunEntity.Status.IN_PROGRESS)
                .totalPass(0)
                .totalFail(0)
                .build();
        
        return mapToModel(testRunRepository.save(newRun));
    }

    @Transactional(readOnly = true)
    public TestRun getRun(String buildNumber) {
        return testRunRepository.findByBuildNumber(buildNumber)
                .map(this::mapToModel)
                .orElse(null);
    }

    @Transactional
    public void saveRun(TestRun run) {
        TestRunEntity entity = testRunRepository.findByBuildNumber(run.getBuildNumber())
                .orElseThrow(() -> new RuntimeException("Run not found: " + run.getBuildNumber()));

        entity.setStatus(TestRunEntity.Status.valueOf(run.getStatus().name()));
        entity.setTotalPass(run.getTotalPass());
        entity.setTotalFail(run.getTotalFail());

        // Map batches
        List<ExecutionBatchEntity> batchEntities = run.getBatches().stream()
                .map(b -> mapToBatchEntity(b, entity))
                .collect(Collectors.toList());

        
        entity.getBatches().clear();
        entity.getBatches().addAll(batchEntities);
        
        testRunRepository.save(entity);
    }

    @Transactional
    public void removeRun(String buildNumber) {
        testRunRepository.findByBuildNumber(buildNumber)
                .ifPresent(testRunRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<TestRun> getLatestRuns() {
        List<TestRunEntity> all = testRunRepository.findAll();
        // Group by environment and take the one with the highest ID (latest)
        return all.stream()
                .collect(Collectors.groupingBy(TestRunEntity::getEnvironment,
                        Collectors.maxBy(java.util.Comparator.comparing(TestRunEntity::getId))))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::mapToModel)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestRun> getHistoricalRuns() {
        List<TestRun> latest = getLatestRuns();
        java.util.Set<String> latestBuilds = latest.stream()
                .map(TestRun::getBuildNumber)
                .collect(Collectors.toSet());

        return testRunRepository.findAll().stream()
                .filter(r -> !latestBuilds.contains(r.getBuildNumber()))
                .map(this::mapToModel)
                .sorted(java.util.Comparator.comparing(TestRun::getBuildNumber).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestRun> getHistoricalRunsForEnvironment(String environment) {
        List<TestRun> latest = getLatestRuns();
        java.util.Set<String> latestBuilds = latest.stream()
                .map(TestRun::getBuildNumber)
                .collect(Collectors.toSet());

        return testRunRepository.findAll().stream()
                .filter(r -> r.getEnvironment().equals(environment))
                .filter(r -> !latestBuilds.contains(r.getBuildNumber()))
                .map(this::mapToModel)
                .sorted(java.util.Comparator.comparing(TestRun::getBuildNumber).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestCaseHistoryDTO> getTestCaseHistory(String suiteName, String caseName, String environment) {
        return testCaseRepository.findHistory(suiteName, caseName, environment).stream()
                .map(t -> new TestCaseHistoryDTO(
                        t.getBatch().getTestRun().getBuildNumber(),
                        t.getBatch().getTestRun().getEnvironment(),
                        t.getStatus()
                ))
                .collect(Collectors.toList());
    }

    private TestRun mapToModel(TestRunEntity entity) {
        return new TestRun(
                entity.getBuildNumber(),
                entity.getEnvironment(),
                TestRun.Status.valueOf(entity.getStatus().name()),
                entity.getBatches().stream().map(this::mapToBatchModel).collect(Collectors.toList()),
                entity.getTotalPass(),
                entity.getTotalFail()
        );
    }

    private ExecutionBatch mapToBatchModel(ExecutionBatchEntity entity) {
        return new ExecutionBatch(
                entity.getBatchId(),
                entity.getDurationMs(),
                entity.getMetadata(),
                entity.getTestCases().stream().map(this::mapToCaseModel).collect(Collectors.toList())
        );
    }

    private TestCase mapToCaseModel(TestCaseEntity entity) {
        return new TestCase(
                entity.getSuiteName(),
                entity.getCaseName(),
                TestCase.Status.valueOf(entity.getStatus().name()),
                entity.getErrorMessage(),
                entity.getRawStackTrace(),
                entity.getErrorFingerprint()
        );
    }

    private ExecutionBatchEntity mapToBatchEntity(ExecutionBatch model, TestRunEntity runEntity) {
        ExecutionBatchEntity entity = ExecutionBatchEntity.builder()
                .batchId(model.batchId())
                .durationMs(model.durationMs())
                .metadata(model.metadata())
                .testRun(runEntity)
                .build();
        
        List<TestCaseEntity> cases = model.testCases().stream()
                .map(c -> mapToCaseEntity(c, entity))
                .collect(Collectors.toList());
        
        entity.setTestCases(cases);
        return entity;
    }

    private TestCaseEntity mapToCaseEntity(TestCase model, ExecutionBatchEntity batchEntity) {
        return TestCaseEntity.builder()
                .suiteName(model.suiteName())
                .caseName(model.caseName())
                .status(TestCaseEntity.Status.valueOf(model.status().name()))
                .errorMessage(model.errorMessage())
                .rawStackTrace(model.rawStackTrace())
                .errorFingerprint(model.errorFingerprint())
                .batch(batchEntity)
                .build();
    }
}
