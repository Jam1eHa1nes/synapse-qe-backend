package com.synapseqe.synapse_qe.controller;

import com.synapseqe.synapse_qe.service.LiveBroadcasterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream")
public class StreamController {

    private final LiveBroadcasterService broadcasterService;

    public StreamController(LiveBroadcasterService broadcasterService) {
        this.broadcasterService = broadcasterService;
    }

    @GetMapping("/live")
    public SseEmitter streamLive() {
        return broadcasterService.subscribe();
    }
}
