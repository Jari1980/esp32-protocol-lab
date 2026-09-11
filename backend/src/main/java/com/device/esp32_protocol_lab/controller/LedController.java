package com.device.esp32_protocol_lab.controller;

import com.device.esp32_protocol_lab.client.Esp32LedClient;
import com.device.esp32_protocol_lab.model.LedCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/", "/api"})
public class LedController {

    private final Esp32LedClient esp32LedClient;

    public LedController(Esp32LedClient esp32LedClient) {
        this.esp32LedClient = esp32LedClient;
    }

    @PostMapping("/led")
    public ResponseEntity<Void> controlLed(@RequestBody LedCommand command) {
        esp32LedClient.sendCommand(command);
        return ResponseEntity.accepted().build();
    }
}
