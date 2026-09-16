package com.device.esp32_protocol_lab.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Esp32WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(Esp32WebSocketClient.class);
    private static final URI ESP_WS_URI = URI.create("ws://192.168.0.236:81");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Volatile to ensure visibility across threads
    private volatile WebSocket webSocket;

    // Lightweight incoming message handler to avoid depending on Spring types
    private volatile Consumer<String> incomingMessageHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info("Starting Esp32WebSocketClient, connecting to {}", ESP_WS_URI);

        httpClient.newWebSocketBuilder()
                .buildAsync(ESP_WS_URI, new SimpleListener())
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    log.info("WebSocket connected: subprotocol={}", ws.getSubprotocol());
                })
                .exceptionally(ex -> {
                    log.error("Failed to establish WebSocket connection to {}", ESP_WS_URI, ex);
                    return null;
                });
    }

    public CompletableFuture<WebSocket> sendText(String json) {
        Objects.requireNonNull(json, "json must not be null");
        WebSocket ws = this.webSocket;
        if (ws == null) {
            CompletableFuture<WebSocket> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("WebSocket is not connected"));
            return failed;
        }
        log.info("Sending WebSocket text message: {}", json);
        return ws.sendText(json, true).toCompletableFuture();
    }

    public void setIncomingMessageHandler(Consumer<String> handler) {
        this.incomingMessageHandler = handler;
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down Esp32WebSocketClient");
        WebSocket ws = this.webSocket;
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown").toCompletableFuture().join();
                log.info("WebSocket closed cleanly");
            } catch (Exception e) {
                log.warn("Error while closing WebSocket", e);
            }
        }
    }

    private class SimpleListener implements WebSocket.Listener {

        private static final Logger log = LoggerFactory.getLogger(SimpleListener.class);

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("WebSocket onOpen");
            // Request the first incoming message from the server
            webSocket.request(1);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String payload = data == null ? null : data.toString();
            log.info("Received WebSocket text message: {}", payload);
            Consumer<String> handler = incomingMessageHandler;
            if (handler != null && payload != null) {
                try {
                    handler.accept(payload);
                } catch (Exception e) {
                    log.warn("Incoming message handler threw an exception", e);
                }
            }
            // Request the next message after processing the current one
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.info("WebSocket closed: status={}, reason={}", statusCode, reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("WebSocket error", error);
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}