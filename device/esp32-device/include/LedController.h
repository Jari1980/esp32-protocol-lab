#pragma once

#include <Arduino.h>
#include "Pins.h"

class LedController {
public:
  void begin() {
    pinMode(RED_LED, OUTPUT);
    pinMode(BLUE_LED, OUTPUT);
    pinMode(GREEN_LED, OUTPUT);
  }

  void turnOn(int ledPin) {
    digitalWrite(ledPin, HIGH);
  }

  void turnOff(int ledPin) {
    digitalWrite(ledPin, LOW);
  }

  void setBlue(bool on) {
    if (on) {
      turnOn(BLUE_LED);
    } else {
      turnOff(BLUE_LED);
    }
  }
};