package com.synapseqe.synapse_qe.controller;

import com.synapseqe.synapse_qe.model.TestRun;
import com.synapseqe.synapse_qe.service.IngestionTimerService;
import com.synapseqe.synapse_qe.service.LiveBroadcasterService;
import com.synapseqe.synapse_qe.service.StateManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestController.class)
@Import({StateManager.class, IngestionTimerService.class, LiveBroadcasterService.class})
class IngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StateManager stateManager;

    @Test
    void shouldAcceptBatchAndAppendToSameRun() throws Exception {
        String buildNumber = "build-123";
        String env = "QA";
        
        String json1 = """
            {
                "batchId": "batch-1",
                "durationMs": 100,
                "metadata": {},
                "testCases": []
            }
            """;
        
        String json2 = """
            {
                "batchId": "batch-2",
                "durationMs": 200,
                "metadata": {},
                "testCases": []
            }
            """;

        mockMvc.perform(post("/api/v1/ingest/batch")
                .param("buildNumber", buildNumber)
                .param("environment", env)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/v1/ingest/batch")
                .param("buildNumber", buildNumber)
                .param("environment", env)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
                .andExpect(status().isAccepted());

        TestRun run = stateManager.getRun(buildNumber);
        assertEquals(2, run.getBatches().size());
        assertEquals(env, run.getEnvironment());
    }
}
