package com.device.esp32_protocol_lab.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Minimal MQTT client using Eclipse Paho v3.
 * Connects on application start, subscribes to protocol-lab/esp32/green/state and
 * exposes a simple publish method for protocol-lab/esp32/green/set.
 */
@Component
public class MqttClientService {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientService.class);

    private final String server;
    private final int port;
    private final String user;
    private final String password;

    private IMqttClient client;
    private volatile Consumer<String> incomingMessageHandler;
    private final String stateTopic = "protocol-lab/esp32/green/state";
    private final String setTopic = "protocol-lab/esp32/green/set";

    public MqttClientService(@Value("${mqtt.server}") String server,
                             @Value("${mqtt.port:1883}") int port,
                             @Value("${mqtt.user:}") String user,
                             @Value("${mqtt.password:}") String password) {
        this.server = server;
        this.port = port;
        this.user = user == null ? "" : user;
        this.password = password == null ? "" : password;
    }

    @PostConstruct
    public void start() {
        String brokerUrl = String.format("tcp://%s:%d", server, port);

        String clientId = makeClientId();

        try {
            client = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(false);
            options.setCleanSession(true);
            if (!user.isBlank()) {
                options.setUserName(user);
            }
            if (!password.isBlank()) {
                options.setPassword(password.toCharArray());
            }

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.error("MQTT connection lost: {}", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    logger.info("MQTT message arrived. topic='{}' payload='{}' qos={} retained={}",
                            topic, payload, message.getQos(), message.isRetained());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // no-op
                }
            });

            logger.info("Connecting MQTT client [{}] to {}", clientId, brokerUrl);
            client.connect(options);

            if (client.isConnected()) {
                logger.info("MQTT connected. Subscribing to '{}'", stateTopic) ;
                client.subscribe(stateTopic, (topic, msg) -> {
                    String payload = new String(msg.getPayload(), StandardCharsets.UTF_8);
                    logger.info("Received state message on topic '{}': {}", topic, payload);
                    Consumer<String> handler = incomingMessageHandler;
                    if (handler != null) {
                        handler.accept(payload);
                    }
                });
            } else {
                logger.warn("MQTT client did not connect to broker: {}", brokerUrl);
            }

        } catch (MqttException e) {
            logger.error("Failed to start MQTT client: {}", e.getMessage());
            // Keep application running even if MQTT fails
            client = null;
        } catch (Exception e) {
            logger.error("Unexpected error while starting MQTT client: {}", e.getMessage());
            client = null;
        }
    }

    @PreDestroy
    public void stop() {
        if (client != null && client.isConnected()) {
            try {
                logger.info("Disconnecting MQTT client");
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                logger.warn("Error while disconnecting MQTT client: {}", e.getMessage());
            }
        }
    }

    /**
     * Publish a simple JSON command to the ESP32 green set topic.
     * Example: {"color":"GREEN","on":true}
     *
     * Returns true when publish succeeded, false otherwise.
     */
    public boolean publishGreenLed(boolean on) {
        if (client == null || !client.isConnected()) {
            logger.warn("MQTT client not connected - cannot publish message");
            return false;
        }

        String json = String.format("{\"color\":\"GREEN\",\"on\":%s}", Boolean.toString(on));
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        MqttMessage message = new MqttMessage(payload);
        message.setQos(1);
        message.setRetained(false);

        try {
            logger.info("Publishing to {} payload={}", setTopic, json);
            client.publish(setTopic, message);
            return true;
        } catch (MqttException e) {
            logger.error("Failed to publish MQTT message: {}", e.getMessage());
            return false;
        }
    }

    public void setIncomingMessageHandler(Consumer<String> handler) {
        this.incomingMessageHandler = handler;
    }

    private String makeClientId() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            return "esp32-protocol-lab-backend-" + host;
        } catch (Exception e) {
            // fallback to a UUID to ensure uniqueness
            return "esp32-protocol-lab-backend-" + UUID.randomUUID();
        }
    }
}
