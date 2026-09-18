import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import {
  connect,
  disconnect,
  sendGreenLedCommand,
} from './mqttWebSocketClient'

type Listener = (event: Event | MessageEvent) => void

class FakeWebSocket {
  static readonly OPEN = 1
  static readonly CLOSED = 3
  static instances: FakeWebSocket[] = []

  readonly url: string
  readonly sentMessages: string[] = []
  readyState = 0
  private readonly listeners = new Map<string, Listener[]>()

  constructor(url: string) {
    this.url = url
    FakeWebSocket.instances.push(this)
  }

  addEventListener(type: string, listener: Listener): void {
    const listeners = this.listeners.get(type) ?? []
    listeners.push(listener)
    this.listeners.set(type, listeners)
  }

  send(message: string): void {
    if (this.readyState !== FakeWebSocket.OPEN) {
      throw new Error('Socket is not open')
    }
    this.sentMessages.push(message)
  }

  close(): void {
    this.readyState = FakeWebSocket.CLOSED
    this.emit('close', new Event('close'))
  }

  open(): void {
    this.readyState = FakeWebSocket.OPEN
    this.emit('open', new Event('open'))
  }

  receive(data: string): void {
    this.emit('message', new MessageEvent('message', { data }))
  }

  private emit(type: string, event: Event | MessageEvent): void {
    for (const listener of this.listeners.get(type) ?? []) {
      listener(event)
    }
  }
}

describe('mqttWebSocketClient', () => {
  const originalWebSocket = globalThis.WebSocket

  beforeEach(() => {
    FakeWebSocket.instances = []
    globalThis.WebSocket = FakeWebSocket as unknown as typeof WebSocket
  })

  afterEach(() => {
    disconnect()
    globalThis.WebSocket = originalWebSocket
  })

  it('sends a GREEN command and resolves after a successful response', async () => {
    const connection = connect()
    const socket = FakeWebSocket.instances[0]
    socket.open()
    await connection

    const command = sendGreenLedCommand({ color: 'GREEN', on: true })

    expect(socket.url).toBe('ws://localhost:8080/mqtt')
    expect(socket.sentMessages).toEqual(['{"color":"GREEN","on":true}'])

    socket.receive('{"success":true}')

    await expect(command).resolves.toBeUndefined()
  })

  it('rejects when the response is not successful', async () => {
    const connection = connect()
    const socket = FakeWebSocket.instances[0]
    socket.open()
    await connection

    const command = sendGreenLedCommand({ color: 'GREEN', on: true })
    socket.receive('{"success":false}')

    await expect(command).rejects.toThrow('MQTT WebSocket response was not a successful response')
  })
})