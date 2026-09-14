import type { BenchmarkProtocol } from './benchmarkTypes'

type BenchmarkControlsProps = {
  protocol: BenchmarkProtocol
  onProtocolChange: (protocol: BenchmarkProtocol) => void
  onStart: () => void
  isRunning: boolean
}

export function BenchmarkControls({ protocol, onProtocolChange, onStart, isRunning }: BenchmarkControlsProps) {
  return (
    <div className="benchmark-controls">
      <label htmlFor="benchmark-protocol">Protocol</label>
      <select id="benchmark-protocol" value={protocol} onChange={(event) => onProtocolChange(event.target.value as BenchmarkProtocol)}>
        <option value="HTTP">HTTP</option>
        <option value="WebSocket">WebSocket</option>
        <option value="MQTT">MQTT</option>
      </select>
      <span className="request-count">100 requests</span>
      <button type="button" className="benchmark-start" onClick={onStart} disabled={isRunning}>
        {isRunning ? 'Running...' : 'Run benchmark'}
      </button>
    </div>
  )
}