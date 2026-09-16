package com.device.esp32_protocol_lab.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TextSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(TextSocketHandler.class);

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received WebSocket text message: sessionId={}, payload={}", session.getId(), message.getPayload());
        // No business logic yet — just logging to verify messages arrive from the React frontend
    }
}