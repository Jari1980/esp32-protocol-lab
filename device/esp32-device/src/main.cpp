#include <Arduino.h>
#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <WebServer.h>
#include <WebSocketsServer.h>
#include <WiFi.h>
#include "LedController.h"
#include "WifiSecrets.h"
#include "MqttSecrets.h"

LedController leds;
WebServer server(80);
WebSocketsServer webSocket(81);

WiFiClient mqttTransport;
PubSubClient mqttClient(mqttTransport);

void handleLed() {
  JsonDocument request;
  if (deserializeJson(request, server.arg("plain"))) {
    server.send(400, "application/json", "{\"error\":\"invalid JSON\"}");
    return;
  }

  const char* color = request["color"];
  bool on = request["on"] | false;
  int pin = 0;

  if (strcmp(color, "RED") == 0) {
    pin = RED_LED;
  } else if (strcmp(color, "BLUE") == 0) {
    pin = BLUE_LED;
  } else if (strcmp(color, "GREEN") == 0) {
    pin = GREEN_LED;
  } else {
    server.send(400, "application/json", "{\"error\":\"invalid color\"}");
    return;
  }

  if (on) {
    leds.turnOn(pin);
  } else {
    leds.turnOff(pin);
  }

  server.send(200, "application/json", "{\"success\":true}");
}

void handleWebSocketEvent(
    uint8_t clientNum,
    WStype_t type,
    uint8_t* payload,
    size_t length) {
  if (type != WStype_TEXT) {
    return;
  }

  JsonDocument request;
  if (deserializeJson(request, payload, length)) {
    webSocket.sendTXT(clientNum, "{\"error\":\"invalid JSON\"}");
    return;
  }

  const char* color = request["color"];
  if (color == nullptr || strcmp(color, "BLUE") != 0) {
    webSocket.sendTXT(clientNum, "{\"error\":\"invalid color\"}");
    return;
  }

  bool on = request["on"] | false;
  leds.setBlue(on);
  webSocket.sendTXT(clientNum, "{\"success\":true}");
}

const char* MQTT_GREEN_SET_TOPIC = "protocol-lab/esp32/green/set";
const char* MQTT_GREEN_STATE_TOPIC = "protocol-lab/esp32/green/state";

void connectMqtt() {
  while (!mqttClient.connected()) {
    Serial.print("Connecting to MQTT...");

    if (mqttClient.connect("esp32-protocol-lab", MQTT_USER, MQTT_PASS)) {
      Serial.println("connected");

      mqttClient.subscribe(MQTT_GREEN_SET_TOPIC);
      Serial.print("Subscribed to: ");
      Serial.println(MQTT_GREEN_SET_TOPIC);
    } else {
      Serial.print("failed, rc=");
      Serial.println(mqttClient.state());
      delay(2000);
    }
  }
}

void handleMqttMessage(char* topic, byte* payload, unsigned int length) {
  Serial.print("MQTT message received on: ");
  Serial.println(topic);

  JsonDocument request;

  DeserializationError error = deserializeJson(request, payload, length);

  if (error) {
    Serial.print("Invalid MQTT JSON: ");
    Serial.println(error.c_str());
    return;
  }

  const char* color = request["color"];
  bool on = request["on"] | false;

  if (color == nullptr || strcmp(color, "GREEN") != 0) {
    Serial.println("Invalid MQTT color");
    return;
  }

  if (on) {
    leds.turnOn(GREEN_LED);
  } else {
    leds.turnOff(GREEN_LED);
  }

  Serial.print("GREEN LED: ");
  Serial.println(on ? "ON" : "OFF");

  mqttClient.publish(
      MQTT_GREEN_STATE_TOPIC,
      "{\"success\":true}"
  );
}

void setup() {
  Serial.begin(115200);
  Serial.println();
  Serial.println("ESP32 LED controller starting");

  leds.begin();

  WiFi.mode(WIFI_STA);
  Serial.println("Connecting to Wi-Fi...");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();
  Serial.println("Wi-Fi connected");
  Serial.print("ESP32 local IP: ");
  Serial.println(WiFi.localIP());

  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setCallback(handleMqttMessage);
  connectMqtt();

  server.on("/led", HTTP_POST, handleLed);
  server.begin();
  Serial.println("HTTP server ready: POST /led");

  webSocket.begin();
  webSocket.onEvent(handleWebSocketEvent);
  Serial.print("WebSocket server ready: ws://");
  Serial.print(WiFi.localIP());
  Serial.println(":81");
}

void loop() {
  server.handleClient();
  webSocket.loop();

  if (!mqttClient.connected()) {
    connectMqtt();
  }

  mqttClient.loop();
}