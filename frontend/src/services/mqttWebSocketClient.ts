export type GreenLedCommand = {
  color: 'GREEN'
  on: boolean
}

const MQTT_WEB_SOCKET_URL = 'ws://localhost:8080/mqtt'

let socket: WebSocket | null = null
let connectionPromise: Promise<void> | null = null
let rejectConnection: ((error: Error) => void) | null = null
let pendingCommand: {
  resolve: () => void
  reject: (error: Error) => void
} | null = null

function rejectPendingCommand(error: Error): void {
  const command = pendingCommand
  pendingCommand = null
  command?.reject(error)
}

function handleMessage(event: MessageEvent): void {
  if (!pendingCommand) {
    return
  }

  try {
    if (typeof event.data !== 'string') {
      throw new Error('MQTT WebSocket response was not text')
    }

    const response: unknown = JSON.parse(event.data)
    if (
      typeof response !== 'object'
      || response === null
      || Object.keys(response).length !== 1
      || !('success' in response)
      || response.success !== true
    ) {
      throw new Error('MQTT WebSocket response was not a successful response')
    }

    const command = pendingCommand
    pendingCommand = null
    command.resolve()
  } catch (error) {
    rejectPendingCommand(error instanceof Error ? error : new Error('Malformed MQTT WebSocket response'))
  }
}

export function connect(): Promise<void> {
  if (socket?.readyState === WebSocket.OPEN) {
    return Promise.resolve()
  }

  if (connectionPromise) {
    return connectionPromise
  }

  const nextSocket = new WebSocket(MQTT_WEB_SOCKET_URL)
  socket = nextSocket

  connectionPromise = new Promise<void>((resolve, reject) => {
    rejectConnection = reject

    nextSocket.addEventListener('open', () => {
      connectionPromise = null
      rejectConnection = null
      resolve()
    })

    nextSocket.addEventListener('message', handleMessage)

    nextSocket.addEventListener('error', () => {
      const error = new Error('MQTT WebSocket connection error')
      if (socket === nextSocket) {
        socket = null
      }
      rejectPendingCommand(error)
      rejectConnection?.(error)
      rejectConnection = null
      nextSocket.close()
    })

    nextSocket.addEventListener('close', () => {
      const error = new Error('MQTT WebSocket connection closed')
      if (socket === nextSocket) {
        socket = null
      }
      rejectPendingCommand(error)
      rejectConnection?.(error)
      rejectConnection = null
      connectionPromise = null
    })
  })

  return connectionPromise
}

export function disconnect(): void {
  const currentSocket = socket
  socket = null
  connectionPromise = null
  rejectConnection?.(new Error('MQTT WebSocket disconnected'))
  rejectConnection = null
  rejectPendingCommand(new Error('MQTT WebSocket disconnected'))
  currentSocket?.close()
}

export function sendGreenLedCommand(command: GreenLedCommand): Promise<void> {
  if (socket?.readyState !== WebSocket.OPEN) {
    return Promise.reject(new Error('MQTT WebSocket is not connected'))
  }

  if (pendingCommand) {
    return Promise.reject(new Error('Another MQTT WebSocket command is already pending'))
  }

  const responsePromise = new Promise<void>((resolve, reject) => {
    pendingCommand = { resolve, reject }
  })

  try {
    socket.send(JSON.stringify({ color: 'GREEN', on: command.on }))
  } catch (error) {
    rejectPendingCommand(error instanceof Error ? error : new Error('MQTT WebSocket send failed'))
  }

  return responsePromise
}