package com.device.esp32_protocol_lab.websocket;

import com.device.esp32_protocol_lab.client.MqttClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttWebSocketHandlerTest {

    @Mock
    private MqttClientService mqttClientService;

    @Mock
    private WebSocketSession sessionA;

    @Mock
    private WebSocketSession sessionB;

    @Captor
    private ArgumentCaptor<TextMessage> textMessageCaptor;

    @Captor
    private ArgumentCaptor<Consumer<String>> consumerCaptor;

    private MqttWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MqttWebSocketHandler(mqttClientService);
        // capture the consumer registered by the handler constructor
        verify(mqttClientService).setIncomingMessageHandler(consumerCaptor.capture());
    }


    @Test
    void validGreenOn_publishesAndNoError() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(mqttClientService.publishGreenLed(true)).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));

        //Using times(1) is optional here and below but I keep it for clarity.
        verify(mqttClientService, times(1)).publishGreenLed(true);
        verify(sessionA, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void validGreenOff_publishes() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(mqttClientService.publishGreenLed(false)).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":false}"));

        verify(mqttClientService, times(1)).publishGreenLed(false);
        verify(sessionA, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void reversedFieldOrder_publishes() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(mqttClientService.publishGreenLed(true)).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"on\":true,\"color\":\"GREEN\"}"));

        verify(mqttClientService, times(1)).publishGreenLed(true);
        verify(sessionA, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void invalidColor_sendsError_noPublish() throws Exception {
        when(sessionA.isOpen()).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"RED\",\"on\":true}"));

        verify(mqttClientService, never()).publishGreenLed(anyBoolean());
        verify(sessionA, times(1)).sendMessage(textMessageCaptor.capture());
        String sent = textMessageCaptor.getValue().getPayload();
        assertTrue(sent.contains("error"));
    }

    @Test
    void missingOn_sendsError_noPublish() throws Exception {
        when(sessionA.isOpen()).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\"}"));

        verify(mqttClientService, never()).publishGreenLed(anyBoolean());
        verify(sessionA, times(1)).sendMessage(textMessageCaptor.capture());
        assertTrue(textMessageCaptor.getValue().getPayload().contains("error"));
    }

    @Test
    void invalidOnType_sendsError_noPublish() throws Exception {
        when(sessionA.isOpen()).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":\"true\"}"));

        verify(mqttClientService, never()).publishGreenLed(anyBoolean());
        verify(sessionA, times(1)).sendMessage(textMessageCaptor.capture());
        assertTrue(textMessageCaptor.getValue().getPayload().contains("error"));
    }

    @Test
    void malformedJson_sendsError_noPublish() throws Exception {
        when(sessionA.isOpen()).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("this is not json"));

        verify(mqttClientService, never()).publishGreenLed(anyBoolean());
        verify(sessionA, times(1)).sendMessage(textMessageCaptor.capture());
        assertTrue(textMessageCaptor.getValue().getPayload().contains("error"));
    }

    @Test
    void mqttPublishFailure_sendsFailureAndClearsPending() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(sessionA.isOpen()).thenReturn(true);
        when(mqttClientService.publishGreenLed(true)).thenReturn(false);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));

        verify(mqttClientService, times(1)).publishGreenLed(true);
        verify(sessionA, times(1)).sendMessage(textMessageCaptor.capture());
        String sent = textMessageCaptor.getValue().getPayload();
        assertTrue(sent.contains("Failed to publish MQTT command"));

        // Now ensure another session can send because pending was cleared
        when(sessionB.getId()).thenReturn("B");
        when(mqttClientService.publishGreenLed(false)).thenReturn(true);
        handler.handleTextMessage(sessionB, new TextMessage("{\"color\":\"GREEN\",\"on\":false}"));
        verify(mqttClientService, times(1)).publishGreenLed(false);
    }

    @Test
    void successfulMqttResponse_forwardsToPendingSession_andIgnoresSubsequent() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(sessionA.isOpen()).thenReturn(true);
        when(mqttClientService.publishGreenLed(true)).thenReturn(true);

        // send command
        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));
        verify(mqttClientService, times(1)).publishGreenLed(true);

        // capture consumer and invoke with success payload
        Consumer<String> incoming = consumerCaptor.getValue();
        assertNotNull(incoming);

        incoming.accept("{\"success\":true}");

        verify(sessionA, times(1)).sendMessage(textMessageCaptor.capture());
        assertEquals("{\"success\":true}", textMessageCaptor.getValue().getPayload());

        // invoke again - should be ignored
        incoming.accept("{\"success\":true}");
        // still only one send
        verify(sessionA, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void onlyOneCommandPending_secondSessionGetsError() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(sessionB.isOpen()).thenReturn(true);
        when(mqttClientService.publishGreenLed(true)).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));
        verify(mqttClientService, times(1)).publishGreenLed(true);

        handler.handleTextMessage(sessionB, new TextMessage("{\"color\":\"GREEN\",\"on\":false}"));

        verify(mqttClientService, times(1)).publishGreenLed(anyBoolean());
        verify(sessionB, times(1)).sendMessage(textMessageCaptor.capture());
        assertTrue(textMessageCaptor.getValue().getPayload().contains("Another MQTT command is already pending"));
    }

    @Test
    void pendingClearedOnConnectionClosed_allowsNextCommand() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(sessionB.getId()).thenReturn("B");
        when(mqttClientService.publishGreenLed(true)).thenReturn(true);
        when(mqttClientService.publishGreenLed(false)).thenReturn(true);

        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));
        verify(mqttClientService, times(1)).publishGreenLed(true);

        handler.afterConnectionClosed(sessionA, null);

        handler.handleTextMessage(sessionB, new TextMessage("{\"color\":\"GREEN\",\"on\":false}"));
        verify(mqttClientService, times(1)).publishGreenLed(false);
    }

    @Test
    void closedSessionWhenForwarding_noSend() throws Exception {
        when(sessionA.getId()).thenReturn("A");
        when(sessionA.isOpen()).thenReturn(false);
        when(mqttClientService.publishGreenLed(true)).thenReturn(true);

        // send command - pending session will be sessionA (though isOpen false)
        handler.handleTextMessage(sessionA, new TextMessage("{\"color\":\"GREEN\",\"on\":true}"));
        verify(mqttClientService, times(1)).publishGreenLed(true);

        Consumer<String> incoming = consumerCaptor.getValue();
        assertNotNull(incoming);

        incoming.accept("{\"success\":true}");

        // session is closed, so sendMessage should not be called
        verify(sessionA, never()).sendMessage(any(TextMessage.class));
    }
}
