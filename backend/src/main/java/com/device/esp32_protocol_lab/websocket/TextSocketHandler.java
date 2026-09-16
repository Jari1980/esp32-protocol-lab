package com.device.esp32_protocol_lab.websocket;

import com.device.esp32_protocol_lab.client.Esp32WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;

@Component
public class TextSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(TextSocketHandler.class);

    private final Esp32WebSocketClient esp32WebSocketClient;

    // Most-recently active browser session (simple heuristic for this lab)
    private volatile WebSocketSession lastSession;

    public TextSocketHandler(Esp32WebSocketClient esp32WebSocketClient) {
        this.esp32WebSocketClient = esp32WebSocketClient;

        // Register a simple incoming message handler from the ESP32 client that forwards
        // payloads to the most recently active browser session.
        this.esp32WebSocketClient.setIncomingMessageHandler(payload -> {
            WebSocketSession s = lastSession;
            if (s != null && s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    log.warn("Failed to forward message to browser session", e);
                }
            } else {
                log.warn("No browser session to forward incoming ESP32 message: {}", payload);
            }
        });
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Update the most-recent browser session so ESP32 responses can be forwarded back
        this.lastSession = session;
        log.info("Received WebSocket text message: sessionId={}, payload={}", session.getId(), message.getPayload());
        // Forward the raw payload to the ESP32 WebSocket client (no parsing or business logic here)
        esp32WebSocketClient.sendText(message.getPayload());
    }
}