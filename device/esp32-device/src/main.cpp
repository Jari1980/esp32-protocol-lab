#include <Arduino.h>
#include <ArduinoJson.h>
#include <WebServer.h>
#include <WiFi.h>
#include "LedController.h"
#include "WifiSecrets.h"

LedController leds;
WebServer server(80);

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

  server.on("/led", HTTP_POST, handleLed);
  server.begin();
  Serial.println("HTTP server ready: POST /led");
}

void loop() {
  server.handleClient();
}