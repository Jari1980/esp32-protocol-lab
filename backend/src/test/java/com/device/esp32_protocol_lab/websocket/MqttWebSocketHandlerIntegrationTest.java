package com.device.esp32_protocol_lab.websocket;

import com.device.esp32_protocol_lab.client.MqttClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = MqttWebSocketHandlerIntegrationTest.TestConfig.class)
@ExtendWith(SpringExtension.class)
class MqttWebSocketHandlerIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        MqttClientService mqttClientService() {
            return mock(MqttClientService.class);
        }

        @Bean
        MqttWebSocketHandler mqttWebSocketHandler(MqttClientService svc) {
            return new MqttWebSocketHandler(svc);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private MqttClientService mockSvc;

    @org.springframework.beans.factory.annotation.Autowired
    private MqttWebSocketHandler handler;

    @Test
    void handlerPublishesAndForwardsResponse() throws Exception {
        // capture consumer registered by handler constructor
        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Consumer<String>> captor = (ArgumentCaptor) ArgumentCaptor.forClass(Consumer.class);
        verify(mockSvc).setIncomingMessageHandler(captor.capture());
        Consumer<String> incoming = captor.getValue();

        // prepare a mocked WebSocketSession
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("int-session");
        when(session.isOpen()).thenReturn(true);

        // stub publish to succeed
        when(mockSvc.publishGreenLed(true)).thenReturn(true);

        // send command
        handler.handleTextMessage(session, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));

        // verify publish called
        verify(mockSvc, times(1)).publishGreenLed(true);

        // simulate incoming MQTT response
        incoming.accept("{\"success\":true}");

        // verify websocket session got the exact payload
        ArgumentCaptor<TextMessage> msgCap = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, times(1)).sendMessage(msgCap.capture());
        assertEquals("{\"success\":true}", msgCap.getValue().getPayload());
    }
}
