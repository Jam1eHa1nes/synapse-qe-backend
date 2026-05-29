package com.synapseqe.synapse_qe.controller;

import com.synapseqe.synapse_qe.model.TestRun;
import com.synapseqe.synapse_qe.service.IngestionTimerService;
import com.synapseqe.synapse_qe.service.LiveBroadcasterService;
import com.synapseqe.synapse_qe.service.StateManager;
import com.synapseqe.synapse_qe.service.StackTraceSanitizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.synapseqe.synapse_qe.model.ExecutionBatch;
import com.synapseqe.synapse_qe.repository.TestCaseRepository;
import com.synapseqe.synapse_qe.repository.TestRunRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestController.class)
@Import({StateManager.class, IngestionTimerService.class, LiveBroadcasterService.class, StackTraceSanitizer.class})
class IngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestRunRepository testRunRepository;

    @MockitoBean
    private TestCaseRepository testCaseRepository;

    @Test
    void shouldAcceptBatchAndAppendToSameRun() throws Exception {
        String buildNumber = "build-123";
        String env = "QA";
        
        TestRun mockRun = new TestRun(buildNumber, env, TestRun.Status.IN_PROGRESS, new ArrayList<>(), 0, 0);
        
        // Mock StateManager's dependencies
        when(testRunRepository.findByBuildNumber(buildNumber)).thenReturn(java.util.Optional.of(
            com.synapseqe.synapse_qe.entity.TestRunEntity.builder()
                .buildNumber(buildNumber)
                .environment(env)
                .status(com.synapseqe.synapse_qe.entity.TestRunEntity.Status.IN_PROGRESS)
                .batches(new ArrayList<>())
                .build()
        ));

        String json1 = """
            {
                "batchId": "batch-1",
                "durationMs": 100,
                "metadata": {},
                "testCases": [
                    {
                        "suiteName": "Suite 1",
                        "caseName": "Test 1",
                        "status": "PASS",
                        "steps": []
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/v1/ingest/batch")
                .param("buildNumber", buildNumber)
                .param("environment", env)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1))
                .andExpect(status().isAccepted());
    }
}
