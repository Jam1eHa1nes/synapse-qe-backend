package com.synapseqe.synapse_qe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/verify")
    public ResponseEntity<String> verify() {
        // If the request reaches here, it has successfully passed the JWT check 
        // AND the domain-based authorization filter.
        return ResponseEntity.ok("Authorized");
    }
}
