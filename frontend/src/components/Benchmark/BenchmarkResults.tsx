import type { LatencySummary } from './benchmarkTypes'

type BenchmarkResultsProps = {
  summary: LatencySummary
}

const metrics: Array<{ key: keyof LatencySummary; label: string }> = [
  { key: 'average', label: 'Average' },
  { key: 'median', label: 'Median' },
  { key: 'p95', label: 'P95' },
  { key: 'min', label: 'Min' },
  { key: 'max', label: 'Max' },
]

export function BenchmarkResults({ summary }: BenchmarkResultsProps) {
  return (
    <div className="benchmark-results" aria-label="Benchmark results">
      {metrics.map(({ key, label }) => (
        <article className="stat-card" key={key}>
          <span>{label}</span>
          <strong>{summary[key] === null ? '--' : `${summary[key]} ms`}</strong>
        </article>
      ))}
    </div>
  )
}