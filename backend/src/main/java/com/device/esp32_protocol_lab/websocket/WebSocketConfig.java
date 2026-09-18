package com.device.esp32_protocol_lab.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TextSocketHandler textSocketHandler;
    private final MqttWebSocketHandler mqttWebSocketHandler;

    @Autowired
    public WebSocketConfig(TextSocketHandler textSocketHandler, MqttWebSocketHandler mqttWebSocketHandler) {
        this.textSocketHandler = textSocketHandler;
        this.mqttWebSocketHandler = mqttWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(textSocketHandler, "/ws")
                .setAllowedOrigins("http://localhost:5173");
        registry.addHandler(mqttWebSocketHandler, "/mqtt")
            .setAllowedOrigins("http://localhost:5173");
    }
}