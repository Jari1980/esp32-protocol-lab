package com.device.esp32_protocol_lab.websocket;

import com.device.esp32_protocol_lab.client.Esp32WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TextSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(TextSocketHandler.class);

    private final Esp32WebSocketClient esp32WebSocketClient;

    public TextSocketHandler(Esp32WebSocketClient esp32WebSocketClient) {
        this.esp32WebSocketClient = esp32WebSocketClient;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received WebSocket text message: sessionId={}, payload={}", session.getId(), message.getPayload());
        // Forward the raw payload to the ESP32 WebSocket client (no parsing or business logic here)
        esp32WebSocketClient.sendText(message.getPayload());
    }
}