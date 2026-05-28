package com.synapseqe.synapse_qe.service;

import tools.jackson.core.JacksonException;
import com.synapseqe.synapse_qe.model.ExecutionBatch;
import com.synapseqe.synapse_qe.model.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonIngestionServiceTest {

    private final JsonIngestionService service = new JsonIngestionService();

    @Test
    void shouldParseValidJson() throws JacksonException {
        String json = """
            {
                "batchId": "B-1",
                "durationMs": 500,
                "metadata": {"browser": "firefox"},
                "testCases": [
                    {"suiteName": "S1", "caseName": "C1", "status": "PASS"}
                ]
            }
            """;

        ExecutionBatch batch = service.parseBatch(json);

        assertEquals("B-1", batch.batchId());
        assertEquals(500, batch.durationMs());
        assertEquals("firefox", batch.metadata().get("browser"));
        assertEquals(1, batch.testCases().size());
        assertEquals(TestCase.Status.PASS, batch.testCases().get(0).status());
    }

    @Test
    void shouldIgnoreUnknownProperties() throws JacksonException {
        String json = """
            {
                "batchId": "B-2",
                "durationMs": 300,
                "metadata": {},
                "testCases": [],
                "unmappedField": "someValue",
                "nestedUnmapped": {"key": "value"}
            }
            """;

        ExecutionBatch batch = service.parseBatch(json);

        assertEquals("B-2", batch.batchId());
        assertNotNull(batch);
    }
}
