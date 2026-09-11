package com.device.esp32_protocol_lab.client;

import com.device.esp32_protocol_lab.model.LedCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class Esp32LedClient {

    private final RestClient restClient;
    private final String ledPath;

    public Esp32LedClient(RestClient restClient,
                          @Value("${esp32.led-path:/led}") String ledPath) {
        this.restClient = restClient;
        this.ledPath = ledPath;
    }

    public void sendCommand(LedCommand command) {

        String json = """
            {
              "color": "%s",
              "on": %s
            }
            """.formatted(command.color(), command.on());

        System.err.println("json = " + json);

        restClient.post()
                .uri(ledPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toBodilessEntity();
    }
}
