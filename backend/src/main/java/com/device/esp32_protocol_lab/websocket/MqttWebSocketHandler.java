package com.device.esp32_protocol_lab.websocket;

import com.device.esp32_protocol_lab.client.MqttClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MqttWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(MqttWebSocketHandler.class);

    private final MqttClientService mqttClientService;
    private volatile WebSocketSession pendingSession;

    // Pattern allows either ordering of fields and optional whitespace (color must be exactly GREEN)
    private static final Pattern CMD_PATTERN = Pattern.compile(
            "^\\s*\\{\\s*(?:\\\"color\\\"\\s*:\\s*\\\"GREEN\\\"\\s*,\\s*\\\"on\\\"\\s*:\\s*(true|false)|\\\"on\\\"\\s*:\\s*(true|false)\\s*,\\s*\\\"color\\\"\\s*:\\s*\\\"GREEN\\\")\\s*\\}\\s*$"
    );

    public MqttWebSocketHandler(MqttClientService mqttClientService) {
        this.mqttClientService = mqttClientService;
        this.mqttClientService.setIncomingMessageHandler(this::forwardResponse);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        if (pendingSession != null) {
            sendError(session, "Another MQTT command is already pending");
            return;
        }

        try {
            boolean on = parseAndValidate(message.getPayload());

            pendingSession = session;
            log.info("Received MQTT WebSocket command: sessionId={}, payload={}", session.getId(), message.getPayload());
            boolean published = mqttClientService.publishGreenLed(on);
            if (!published) {
                // clear pending session and inform the browser of the failure
                pendingSession = null;
                sendError(session, "Failed to publish MQTT command");
            }
        } catch (Exception error) {
            sendError(session, error.getMessage() == null ? "Malformed MQTT command" : error.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        if (pendingSession == session) {
            pendingSession = null;
        }
    }

    private void forwardResponse(String payload) {
        WebSocketSession session = pendingSession;
        if (session == null) {
            return;
        }

        pendingSession = null;
        if (!session.isOpen()) {
            return;
        }

        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException error) {
            log.warn("Failed to forward MQTT response to browser session", error);
        }
    }

    private boolean parseAndValidate(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Expected a GREEN LED command");
        }
        Matcher m = CMD_PATTERN.matcher(payload);
        if (!m.matches()) {
            throw new IllegalArgumentException("Expected a GREEN LED command");
        }
        String g1 = m.group(1);
        String g2 = m.group(2);
        String boolStr = g1 != null ? g1 : g2;
        if (boolStr == null) {
            throw new IllegalArgumentException("Expected boolean 'on' value");
        }
        return Boolean.parseBoolean(boolStr);
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage("{\"error\":\"" + message.replace("\"", "'") + "\"}"));
        }
    }
}
