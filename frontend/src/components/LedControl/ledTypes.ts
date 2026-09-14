export const leds = ['RED', 'BLUE', 'GREEN'] as const

export type LedName = (typeof leds)[number]

export type LedCommand = {
  color: 'RED'
  on: boolean
}

export const protocols: Record<LedName, string> = {
  RED: 'HTTP',
  BLUE: 'WebSocket',
  GREEN: 'MQTT',
}