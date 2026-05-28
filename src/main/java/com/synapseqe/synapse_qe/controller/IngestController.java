package com.synapseqe.synapse_qe.controller;

import com.synapseqe.synapse_qe.dto.LiveUpdateDTO;
import com.synapseqe.synapse_qe.model.ExecutionBatch;
import com.synapseqe.synapse_qe.model.TestRun;
import com.synapseqe.synapse_qe.service.IngestionTimerService;
import com.synapseqe.synapse_qe.service.LiveBroadcasterService;
import com.synapseqe.synapse_qe.service.StateManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final StateManager stateManager;
    private final IngestionTimerService timerService;
    private final LiveBroadcasterService broadcasterService;

    public IngestController(StateManager stateManager, IngestionTimerService timerService, LiveBroadcasterService broadcasterService) {
        this.stateManager = stateManager;
        this.timerService = timerService;
        this.broadcasterService = broadcasterService;
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> ingestBatch(
            @RequestParam String buildNumber,
            @RequestParam String environment,
            @RequestBody ExecutionBatch batch) {
        
        TestRun run = stateManager.getOrCreateRun(buildNumber, environment);
        run.addBatch(batch);
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
}
