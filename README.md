# ESP32 Protocol Lab

A small full-stack IoT project built to explore communication between a frontend, a Spring Boot backend, and an ESP32 using three different communication protocols.

The project controls three LEDs, where each LED demonstrates a different communication path.

## Architecture

The project consists of three main parts:

- **Frontend** – provides the user interface and communicates with the backend.
- **Backend** – built with Spring Boot and acts as the bridge between the frontend and ESP32.
- **ESP32** – controls the physical LEDs and exposes HTTP, WebSocket, and MQTT communication.

### Communication paths

Each LED uses a different communication path between the frontend, backend, and ESP32:

| LED | Frontend → Backend | Backend → ESP32 |
|---|---|---|
| 🔴 Red | HTTP | HTTP |
| 🔵 Blue | WebSocket | WebSocket |
| 🟢 Green | WebSocket | MQTT |

This gives the project three separate communication flows:

```text
                    FRONTEND
                   /    |    \
                HTTP    WS     WS
                  |     |       |
                  v     v       v
               BACKEND BACKEND BACKEND
                  |      |       |
                HTTP     WS     MQTT
                  |      |       |
                  v      v       v
                ESP32   ESP32   ESP32
                 RED     BLUE    GREEN
```                 

## Technologies
### Backend

* Java

* Spring Boot

* REST / HTTP

* WebSocket

* MQTT

* Eclipse Paho MQTT client

* JUnit 5

* Mockito

### ESP32

* C++

* Arduino framework

* Wi-Fi

* HTTP server

* WebSocket server

* MQTT using PubSubClient

* ArduinoJson

### Frontend

- React
- TypeScript
- Vite
- WebSocket
- HTTP / REST
- Recharts
- Vitest
- oxlint

## Frontend

The frontend provides the user interface for controlling the LEDs.

It communicates with the backend using HTTP and WebSocket connections.

## MQTT

The green LED uses MQTT for communication between the backend and ESP32.

The backend publishes commands to:

```text
protocol-lab/esp32/green/set
```

The ESP32 publishes the resulting state to:

```json
protocol-lab/esp32/green/state
```

Example command:

```json
{
  "color": "GREEN",
  "on": true
}
```

## Configuration

Sensitive configuration is kept outside the source code.

## ESP32

The device contains example files for the required secrets:

```text
MqttSecretsExample.h
WifiSecretsExample.h
```

These can be copied and filled in with the local MQTT and Wi-Fi credentials.

The actual secret files should not be committed to the repository.

## Backend

The backend uses environment variables for MQTT configuration:

```text
mqtt.server=${MQTT_SERVER}
mqtt.port=${MQTT_PORT:1883}
mqtt.user=${MQTT_USER}
mqtt.password=${MQTT_PASSWORD}
```

The required environment variables are:

```text
MQTT_SERVER
MQTT_PORT
MQTT_USER
MQTT_PASSWORD
```

## Testing

The project contains both unit tests and integration tests.

The tests currently cover:

* MQTT publishing through MqttClientService

* WebSocket command validation and message handling

* WebSocket/MQTT response forwarding

* HTTP LED controller behaviour

* HTTP request construction in Esp32LedClient

* Spring wiring between selected components

The backend tests use JUnit 5, Mockito, and Spring's test support.

The project currently focuses on backend testing. The ESP32 firmware and frontend are not covered by automated unit tests.

## Protocol Performance

The frontend was also used to measure the latency of the three communication paths.

Each protocol is tested with 100 requests and compared.


P95 (95th percentile) represents the latency at or below which 95% of the requests completed.
For example, the MQTT / WebSocket path has a P95 of 16.4 ms. This means that 95% of the measured requests completed in 16.4 ms or less, while the slowest 5% took longer.

<img width="791" height="1258" alt="ProtocolTests" src="https://github.com/user-attachments/assets/80d03346-abb3-4105-8313-aa5d890f937d" />


These measurements are intended as an experimental comparison between the communication paths in this project, rather than as a general benchmark of the protocols. Results can vary depending on network conditions, hardware, broker configuration, and test environment.

## Copilot

This project was also used as training with GitHub Copilot.

An important part of the project was learning how to work with AI-assisted development while still understanding, reviewing, and testing the generated code.

The project therefore also served as an opportunity to gain practical experience with Copilot as a development tool.

## Project Goal

The main goal of the project is to experiment with and understand different communication protocols in an IoT-style system.

Rather than using one protocol for everything, the project deliberately uses HTTP, WebSocket, and MQTT to demonstrate how different communication patterns can be implemented between a frontend, backend, and physical device.

The project also provides practical experience with automated testing, integration testing, performance measurements, and AI-assisted development using GitHub Copilot.
