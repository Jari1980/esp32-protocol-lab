package com.device.esp32_protocol_lab.controller;

import com.device.esp32_protocol_lab.client.Esp32LedClient;
import com.device.esp32_protocol_lab.model.LedCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = LedControllerIntegrationTest.TestConfig.class)
class LedControllerIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        Esp32LedClient esp32LedClient() {
            return mock(Esp32LedClient.class);
        }

        @Bean
        LedController ledController(Esp32LedClient client) {
            return new LedController(client);
        }
    }

    @Autowired
    private LedController controller;

    @Autowired
    private Esp32LedClient mockClient;

    @Test
    void redOn_isForwardedAndReturnsAccepted() {
        LedCommand cmd = new LedCommand(com.device.esp32_protocol_lab.model.LedColor.RED, true);

        var resp = controller.controlLed(cmd);

        verify(mockClient, times(1)).sendCommand(cmd);
        assertEquals(202, resp.getStatusCode().value());
    }
}
