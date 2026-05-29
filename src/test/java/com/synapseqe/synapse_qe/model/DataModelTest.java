package com.synapseqe.synapse_qe.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DataModelTest {

    @Test
    void executionBatchShouldSupportDynamicMetadata() {
        Map<String, String> metadata = Map.of(
            "browser", "chrome",
            "worker", "worker-1"
        );
        TestCase testCase = new TestCase("Suite A", "Test 1", TestCase.Status.PASS, null, null, null);
        ExecutionBatch batch = new ExecutionBatch("batch-123", 1500L, metadata, List.of(testCase));

        assertEquals("chrome", batch.metadata().get("browser"));
        assertEquals("worker-1", batch.metadata().get("worker"));
        assertEquals(1, batch.testCases().size());
    }

    @Test
    void testRunShouldHandleConcurrentBatchAppends() throws InterruptedException {
        TestRun run = new TestRun("build-1", "production");
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                ExecutionBatch batch = new ExecutionBatch("batch-" + index, 100L, Map.of(), List.of());
                run.addBatch(batch);
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Threads did not complete in time");
        assertEquals(threadCount, run.getBatches().size(), "Not all batches were added");
        executor.shutdown();
    }
}
