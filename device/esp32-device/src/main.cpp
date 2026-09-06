#include <Arduino.h>
#include "LedController.h"

LedController leds;

void setup() {
  leds.begin();
}

void loop() {
  leds.turnOn(GREEN_LED);
  delay(1000);
  leds.turnOff(GREEN_LED);
  delay(1000);
}