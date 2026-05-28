package com.synapseqe.synapse_qe.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StackTraceSanitizerTest {

    private final StackTraceSanitizer sanitizer = new StackTraceSanitizer();

    @Test
    void shouldCalculateSameFingerprintForVaryingLogs() {
        String logMachineA = """
            java.lang.NullPointerException: Cannot invoke "String.length()" because "s" is null
                at com.example.MyService.process(/home/userA/app/src/MyService.java:45)
                at com.example.MyService.execute(0x7f8c9d0e1f2a)
            """;

        String logMachineB = """
            java.lang.NullPointerException: Cannot invoke "String.length()" because "s" is null
                at com.example.MyService.process(/Users/userB/workspace/app/src/MyService.java:52)
                at com.example.MyService.execute(0x1a2b3c4d5e6f)
            """;

        String fingerprintA = sanitizer.calculateFingerprint(logMachineA);
        String fingerprintB = sanitizer.calculateFingerprint(logMachineB);

        assertNotNull(fingerprintA);
        assertEquals(fingerprintA, fingerprintB, "Fingerprints should be identical despite different paths and addresses");
    }

    @Test
    void shouldHandleEmptyOrNullLogs() {
        assertNull(sanitizer.calculateFingerprint(null));
        assertNull(sanitizer.calculateFingerprint(""));
        assertNull(sanitizer.calculateFingerprint("   "));
    }
}
