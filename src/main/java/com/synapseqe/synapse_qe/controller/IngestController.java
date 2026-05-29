package com.synapseqe.synapse_qe.controller;

import com.synapseqe.synapse_qe.dto.LiveUpdateDTO;
import com.synapseqe.synapse_qe.model.ExecutionBatch;
import com.synapseqe.synapse_qe.model.TestCase;
import com.synapseqe.synapse_qe.model.TestRun;
import com.synapseqe.synapse_qe.service.IngestionTimerService;
import com.synapseqe.synapse_qe.service.LiveBroadcasterService;
import com.synapseqe.synapse_qe.service.StackTraceSanitizer;
import com.synapseqe.synapse_qe.service.StateManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final StateManager stateManager;
    private final IngestionTimerService timerService;
    private final LiveBroadcasterService broadcasterService;
    private final StackTraceSanitizer sanitizer;

    public IngestController(StateManager stateManager, 
                            IngestionTimerService timerService, 
                            LiveBroadcasterService broadcasterService,
                            StackTraceSanitizer sanitizer) {
        this.stateManager = stateManager;
        this.timerService = timerService;
        this.broadcasterService = broadcasterService;
        this.sanitizer = sanitizer;
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> ingestBatch(
            @RequestParam String buildNumber,
            @RequestParam String environment,
            @RequestBody ExecutionBatch batch) {
        
        List<TestCase> sanitizedTestCases = batch.testCases().stream()
            .map(tc -> tc.toBuilder()
                .errorFingerprint(sanitizer.calculateFingerprint(tc.rawStackTrace()))
                .build())
            .collect(Collectors.toList());

        ExecutionBatch processedBatch = new ExecutionBatch(
            batch.batchId(),
            batch.durationMs(),
            batch.metadata(),
            sanitizedTestCases
        );

        TestRun run = stateManager.getOrCreateRun(buildNumber, environment);
        run.addBatch(processedBatch);
        stateManager.saveRun(run);
        timerService.resetTimer(buildNumber);
        
        broadcasterService.broadcast(new LiveUpdateDTO(
            run.getBuildNumber(),
            run.getEnvironment(),
            run.getStatus(),
            run.getTotalPass(),
            run.getTotalFail(),
            run.getBatches().size()
        ));
        
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeRun(@RequestParam String buildNumber) {
        TestRun run = stateManager.getRun(buildNumber);
        if (run != null) {
            run.setStatus(TestRun.Status.COMPLETED);
            stateManager.saveRun(run);
            
            broadcasterService.broadcast(new LiveUpdateDTO(
                run.getBuildNumber(),
                run.getEnvironment(),
                run.getStatus(),
                run.getTotalPass(),
                run.getTotalFail(),
                run.getBatches().size()
            ));
            
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
