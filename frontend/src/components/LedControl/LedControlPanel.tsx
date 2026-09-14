import { LedControlRow } from './LedControlRow'
import { leds, protocols, type LedName } from './ledTypes'

type LedControlPanelProps = {
  ledStates: Record<LedName, boolean>
  onStateChange: (led: LedName, isOn: boolean) => void
  requestError: string | null
}

export function LedControlPanel({ ledStates, onStateChange, requestError }: LedControlPanelProps) {
  return (
    <section className="panel-section" aria-labelledby="led-control-heading">
      <div className="section-heading">
        <p className="eyebrow">MANUAL CONTROL</p>
        <h2 id="led-control-heading">LED control</h2>
        <p>Set the local test state for each indicator.</p>
      </div>
      <div className="led-list" aria-label="LED controls">
        {leds.map((led) => (
          <LedControlRow
            key={led}
            led={led}
            protocol={protocols[led]}
            isOn={ledStates[led]}
            onStateChange={(isOn) => onStateChange(led, isOn)}
          />
        ))}
      </div>
      {requestError && <p className="request-error" role="alert">{requestError}</p>}
    </section>
  )
}