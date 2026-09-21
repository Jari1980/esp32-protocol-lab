package com.device.esp32_protocol_lab.controller;

import com.device.esp32_protocol_lab.client.Esp32LedClient;
import com.device.esp32_protocol_lab.model.LedCommand;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LedControllerTest {

    @Test
    void redOn_callsEsp32Client_andReturnsAccepted() {
        Esp32LedClient mockClient = mock(Esp32LedClient.class);
        LedController controller = new LedController(mockClient);

        LedCommand cmd = new LedCommand(com.device.esp32_protocol_lab.model.LedColor.RED, true);

        ResponseEntity<Void> resp = controller.controlLed(cmd);

        verify(mockClient, times(1)).sendCommand(cmd);
        assertEquals(202, resp.getStatusCode().value());
    }
}
