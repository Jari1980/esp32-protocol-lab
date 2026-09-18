import type { BenchmarkProtocol, BenchmarkResult, LatencySummary } from './benchmarkTypes'

type BenchmarkComparisonProps = {
  results: Partial<Record<BenchmarkProtocol, BenchmarkResult>>
}

type ComparisonMetric = {
  label: string
  getValue: (result: BenchmarkResult) => number | null
  format: (value: number | null) => string
}

const formatLatency = (value: number | null): string => value === null ? '--' : `${value.toFixed(1)} ms`
const formatCount = (value: number | null): string => value === null ? '--' : String(value)

const latencyMetric = (key: keyof LatencySummary, label: string): ComparisonMetric => ({
  label,
  getValue: (result) => result.summary[key],
  format: formatLatency,
})

const metrics: ComparisonMetric[] = [
  latencyMetric('average', 'Average'),
  latencyMetric('median', 'Median'),
  latencyMetric('p95', 'P95'),
  latencyMetric('min', 'Min'),
  latencyMetric('max', 'Max'),
  { label: 'Successful requests', getValue: (result) => result.successfulSamples.length, format: formatCount },
  { label: 'Failed requests', getValue: (result) => result.failures.length, format: formatCount },
]

const protocols: Array<{ value: BenchmarkProtocol; label: string }> = [
  { value: 'HTTP', label: 'HTTP' },
  { value: 'WebSocket', label: 'WebSocket' },
  { value: 'MQTT', label: 'MQTT / WebSocket' },
]

export function BenchmarkComparison({ results }: BenchmarkComparisonProps) {
  return (
    <div className="benchmark-comparison">
      <h3>Protocol comparison</h3>
      <table>
        <thead>
          <tr>
            <th scope="col">Metric</th>
            {protocols.map(({ value, label }) => <th scope="col" key={value}>{label}</th>)}
          </tr>
        </thead>
        <tbody>
          {metrics.map(({ label, getValue, format }) => (
            <tr key={label}>
              <th scope="row">{label}</th>
              {protocols.map(({ value: protocol }) => {
                const result = results[protocol]
                const metricValue = result === undefined ? null : getValue(result)
                return <td key={protocol}>{format(metricValue)}</td>
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
