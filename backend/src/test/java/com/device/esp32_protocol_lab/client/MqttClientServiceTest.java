package com.device.esp32_protocol_lab.client;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttClientServiceTest {

    // Values for constructor - not used because start() is not called
    private static final String SERVER = "localhost";
    private static final int PORT = 1883;
    private static final String USER = "";
    private static final String PASSWORD = "";

    private MqttClientService service;

    @Mock
    private IMqttClient mockClient;

    @Captor
    private ArgumentCaptor<MqttMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        service = new MqttClientService(SERVER, PORT, USER, PASSWORD);
    }

    private void injectClient(IMqttClient client) throws Exception {
        Field clientField = MqttClientService.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(service, client);
    }

    @Test
    void publishGreenLed_connected_true() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);
        doNothing().when(mockClient).publish(anyString(), any(MqttMessage.class));

        injectClient(mockClient);

        boolean result = service.publishGreenLed(true);

        assertTrue(result);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockClient, times(1)).publish(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("protocol-lab/esp32/green/set", topicCaptor.getValue());

        MqttMessage sent = messageCaptor.getValue();
        assertNotNull(sent);
        assertEquals(1, sent.getQos());
        assertFalse(sent.isRetained());
        assertArrayEquals("{\"color\":\"GREEN\",\"on\":true}".getBytes(), sent.getPayload());
    }

    @Test
    void publishGreenLed_connected_false() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);
        doNothing().when(mockClient).publish(anyString(), any(MqttMessage.class));

        injectClient(mockClient);

        boolean result = service.publishGreenLed(false);

        assertTrue(result);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockClient, times(1)).publish(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("protocol-lab/esp32/green/set", topicCaptor.getValue());

        MqttMessage sent = messageCaptor.getValue();
        assertNotNull(sent);
        assertEquals(1, sent.getQos());
        assertFalse(sent.isRetained());
        assertArrayEquals("{\"color\":\"GREEN\",\"on\":false}".getBytes(), sent.getPayload());
    }

    @Test
    void publishGreenLed_clientNull_returnsFalse_noPublish() throws Exception {
        // ensure client field is null
        injectClient(null);

        boolean result = service.publishGreenLed(true);
        assertFalse(result);
        // cannot verify publish on a null client; the absence of exceptions and the false return
        // indicate no publish was attempted
    }

    @Test
    void publishGreenLed_clientNotConnected_returnsFalse_noPublish() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);
        injectClient(mockClient);

        boolean result = service.publishGreenLed(true);
        assertFalse(result);

        verify(mockClient, never()).publish(anyString(), any(MqttMessage.class));
    }

    @Test
    void publishGreenLed_publishThrowsException_returnsFalse() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION)).when(mockClient).publish(anyString(), any(MqttMessage.class));

        injectClient(mockClient);

        boolean result = service.publishGreenLed(true);
        assertFalse(result);

        verify(mockClient, times(1)).publish(anyString(), any(MqttMessage.class));
    }
}
