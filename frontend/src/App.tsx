import { useState } from 'react'
import './App.css'

const leds = ['RED', 'BLUE', 'GREEN'] as const
type LedName = (typeof leds)[number]
type LedCommand = {
  color: 'RED'
  on: boolean
}

const protocols: Record<LedName, string> = {
  RED: 'HTTP',
  BLUE: 'WebSocket',
  GREEN: 'MQTT',
}

async function sendRedLedCommand(command: LedCommand): Promise<void> {
  const response = await fetch('http://localhost:8080/led', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(command),
  })

  if (!response.ok) {
    throw new Error(`RED LED request failed with status ${response.status}`)
  }
}

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
        <h1>LED control</h1>
        <p>Set the local test state for each indicator.</p>
      </header>

      <section className="led-list" aria-label="LED controls">
        {leds.map((led) => {
          const isOn = ledStates[led]

          return (
            <article className={`led-row led-${led.toLowerCase()}`} key={led}>
              <div className="led-label">
                <span className={`status-light ${isOn ? 'is-on' : ''}`} aria-hidden="true" />
                <div>
                  <h2>{led}</h2>
                  <span className="protocol-label">{protocols[led]}</span>
                </div>
                <span className="status-text">{isOn ? 'ON' : 'OFF'}</span>
              </div>
              <div className="led-actions">
                <button type="button" className={isOn ? 'active' : ''} onClick={() => void handleLedStateChange(led, true)} aria-pressed={isOn}>
                  ON
                </button>
                <button type="button" className={!isOn ? 'active' : ''} onClick={() => void handleLedStateChange(led, false)} aria-pressed={!isOn}>
                  OFF
                </button>
              </div>
            </article>
          )
        })}
      </section>
      {requestError && <p className="request-error" role="alert">{requestError}</p>}
    </main>
  )
}

export default App
