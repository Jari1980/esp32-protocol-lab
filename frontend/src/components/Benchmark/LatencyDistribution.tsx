import {
  CartesianGrid,
  ReferenceLine,
  ResponsiveContainer,
  Scatter,
  ScatterChart,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { LatencySample, LatencySummary } from './benchmarkTypes'

type LatencyDistributionProps = {
  samples: LatencySample[]
  summary: LatencySummary
}

type ScatterPoint = {
  requestIndex: number
  latencyMs: number
  command: LatencySample['command']
}

function DistributionTooltip({ active, payload }: { active?: boolean; payload?: Array<{ payload?: ScatterPoint }> }) {
  if (!active || !payload?.length) {
    return null
  }

  const point = payload[0].payload

  if (!point) {
    return null
  }

  return (
    <div className="chart-tooltip">
      <strong>Request {point.requestIndex + 1}</strong>
      <span>{point.command.on ? 'RED ON' : 'RED OFF'}</span>
      <span>{point.latencyMs.toFixed(1)} ms</span>
    </div>
  )
}

export function LatencyDistribution({ samples, summary }: LatencyDistributionProps) {
  if (samples.length === 0) {
    return <div className="visualization-placeholder" aria-label="Latency distribution">Latency distribution will appear here after a benchmark.</div>
  }

  const points: ScatterPoint[] = samples.map((sample) => ({
    requestIndex: sample.requestIndex + 1,
    latencyMs: sample.latencyMs,
    command: sample.command,
  }))

  return (
    <div className="chart-panel" aria-label="Latency distribution">
      <h3>Latency by request</h3>
      <div className="chart-frame">
        <ResponsiveContainer width="100%" height="100%">
          <ScatterChart margin={{ top: 12, right: 20, bottom: 20, left: 4 }}>
            <CartesianGrid stroke="var(--border)" strokeDasharray="3 3" />
            <XAxis dataKey="requestIndex" name="Request" type="number" tick={{ fill: 'var(--muted)', fontSize: 11 }} />
            <YAxis dataKey="latencyMs" name="Latency" type="number" scale="log" domain={[1, 'auto']} tick={{ fill: 'var(--muted)', fontSize: 11 }} tickFormatter={(value: number) => `${value} ms`} />
            <Tooltip content={<DistributionTooltip />} />
            {summary.median !== null && <ReferenceLine y={summary.median} stroke="var(--accent)" strokeDasharray="4 4" label="Median" />}
            {summary.p95 !== null && <ReferenceLine y={summary.p95} stroke="#3f7de8" strokeDasharray="4 4" label="P95" />}
            <Scatter data={points} fill="var(--accent)" />
          </ScatterChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}