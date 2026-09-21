package com.device.esp32_protocol_lab.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class Esp32LedClientTest {

    @Test
    void redOn_buildsAndSendsHttpRequest() {
        RestClient restClient = mock(RestClient.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        String ledPath = "/led";
        Esp32LedClient client = new Esp32LedClient(restClient, ledPath);

        com.device.esp32_protocol_lab.model.LedCommand cmd = new com.device.esp32_protocol_lab.model.LedCommand(com.device.esp32_protocol_lab.model.LedColor.RED, true);

        client.sendCommand(cmd);

        // verify URI and contentType were used
        verify(restClient.post()).uri(ledPath);
        verify(restClient.post().uri(ledPath)).contentType(MediaType.APPLICATION_JSON);

        // capture body value
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(restClient.post().uri(ledPath).contentType(MediaType.APPLICATION_JSON)).body(bodyCaptor.capture());

        String body = bodyCaptor.getValue();
        assertEquals("{\"color\":\"RED\",\"on\":true}", body.replaceAll("\\s+", ""));

        // verify retrieve called
        verify(restClient.post().uri(ledPath).contentType(MediaType.APPLICATION_JSON).body(body)).retrieve();
    }
}
