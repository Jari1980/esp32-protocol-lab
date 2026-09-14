import type { LatencyDistributionData } from './benchmarkTypes'

type LatencyDistributionProps = {
  data: LatencyDistributionData
}

export function LatencyDistribution({ data }: LatencyDistributionProps) {
  return (
    <div className="visualization-placeholder" aria-label="Latency distribution">
      {data.samples.length === 0 ? 'Latency distribution will appear here after a benchmark.' : 'Latency distribution'}
    </div>
  )
}