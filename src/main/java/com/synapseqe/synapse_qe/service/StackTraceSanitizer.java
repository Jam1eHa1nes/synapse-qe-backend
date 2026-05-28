package com.synapseqe.synapse_qe.service;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

@Component
public class StackTraceSanitizer {

    private static final Pattern HEX_ADDRESS_PATTERN = Pattern.compile("0x[0-9a-fA-F]+");
    private static final Pattern LINE_NUMBER_PATTERN = Pattern.compile(":\\d+");
    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("(/[^\\s:]+)+");

    public String calculateFingerprint(String rawStackTrace) {
        if (rawStackTrace == null || rawStackTrace.isBlank()) {
            return null;
        }

        String sanitized = sanitize(rawStackTrace);
        return hash(sanitized);
    }

    private String sanitize(String input) {
        String sanitized = input;
        
        // Remove hex addresses
        sanitized = HEX_ADDRESS_PATTERN.matcher(sanitized).replaceAll("[HEX]");
        
        // Remove line numbers
        sanitized = LINE_NUMBER_PATTERN.matcher(sanitized).replaceAll(":[LINE]");
        
        // Remove file paths (heuristic)
        sanitized = FILE_PATH_PATTERN.matcher(sanitized).replaceAll("[PATH]");

        // Remove extra whitespace
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        return sanitized;
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
