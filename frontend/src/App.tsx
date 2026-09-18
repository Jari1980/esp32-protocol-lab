import { useState } from 'react'
import './App.css'
import { BenchmarkPanel } from './components/Benchmark/BenchmarkPanel'
import { LedControlPanel } from './components/LedControl/LedControlPanel'
import type { LedName } from './components/LedControl/ledTypes'
import { sendRedLedCommand } from './services/ledClient'
import { connect, sendBlueLedCommand } from './services/webSocketClient'
import { connect as connectMqtt, sendGreenLedCommand } from './services/mqttWebSocketClient'

function App() {
  const [ledStates, setLedStates] = useState<Record<LedName, boolean>>({
    RED: false,
    BLUE: false,
    GREEN: false,
  })
  const [requestError, setRequestError] = useState<string | null>(null)

  function setLedState(led: LedName, isOn: boolean) {
    setLedStates((currentStates) => ({ ...currentStates, [led]: isOn }))
  }

  async function handleLedStateChange(led: LedName, isOn: boolean) {
    setLedState(led, isOn)

    if (led !== 'RED') {
      if (led !== 'BLUE') {
        setRequestError(null)

        try {
          await connectMqtt()
          await sendGreenLedCommand({ color: 'GREEN', on: isOn })
        } catch (error) {
          const message = error instanceof Error ? error.message : 'Unknown MQTT WebSocket error'
          setRequestError(message)
          console.error(message, error)
        }

        return
      }

      setRequestError(null)

      try {
        await connect()
        await sendBlueLedCommand({ color: 'BLUE', on: isOn })
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unknown WebSocket error'
        setRequestError(message)
        console.error(message, error)
      }

      return
    }

    setRequestError(null)

    try {
      await sendRedLedCommand({ color: 'RED', on: isOn })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown HTTP error'
      setRequestError(message)
      console.error(message, error)
    }
  }

  return (
    <main className="control-panel">
      <header className="panel-header">
        <p className="eyebrow">ESP32 PROTOCOL LAB</p>
        <h1>Protocol lab</h1>
        <p>Control indicators manually, then compare protocol latency.</p>
      </header>
      <LedControlPanel ledStates={ledStates} onStateChange={(led, isOn) => void handleLedStateChange(led, isOn)} requestError={requestError} />
      <BenchmarkPanel />
    </main>
  )
}

export default App
