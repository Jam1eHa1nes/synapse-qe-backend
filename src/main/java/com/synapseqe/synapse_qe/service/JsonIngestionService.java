package com.synapseqe.synapse_qe.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import com.synapseqe.synapse_qe.model.ExecutionBatch;
import org.springframework.stereotype.Service;

@Service
public class JsonIngestionService {

    private final JsonMapper jsonMapper;

    public JsonIngestionService() {
        this.jsonMapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();
    }

    public ExecutionBatch parseBatch(String json) throws JacksonException {
        return jsonMapper.readValue(json, ExecutionBatch.class);
    }
}
