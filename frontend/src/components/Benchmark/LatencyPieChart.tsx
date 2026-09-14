import type { LatencyDistributionData } from './benchmarkTypes'

type LatencyPieChartProps = {
  data: LatencyDistributionData
}

export function LatencyPieChart({ data }: LatencyPieChartProps) {
  return (
    <div className="visualization-placeholder pie-placeholder" aria-label="Circular latency distribution">
      {data.samples.length === 0 ? 'Circular distribution will appear here after a benchmark.' : 'Circular latency distribution'}
    </div>
  )
}