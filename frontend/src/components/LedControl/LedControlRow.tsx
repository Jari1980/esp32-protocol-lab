import type { LedName } from './ledTypes'

type LedControlRowProps = {
  led: LedName
  protocol: string
  isOn: boolean
  onStateChange: (isOn: boolean) => void
}

export function LedControlRow({ led, protocol, isOn, onStateChange }: LedControlRowProps) {
  return (
    <article className={`led-row led-${led.toLowerCase()}`}>
      <div className="led-label">
        <span className={`status-light ${isOn ? 'is-on' : ''}`} aria-hidden="true" />
        <div>
          <h2>{led}</h2>
          <span className="protocol-label">{protocol}</span>
        </div>
        <span className="status-text">{isOn ? 'ON' : 'OFF'}</span>
      </div>
      <div className="led-actions">
        <button type="button" className={isOn ? 'active' : ''} onClick={() => onStateChange(true)} aria-pressed={isOn}>
          ON
        </button>
        <button type="button" className={!isOn ? 'active' : ''} onClick={() => onStateChange(false)} aria-pressed={!isOn}>
          OFF
        </button>
      </div>
    </article>
  )
}