import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import type { LatencySample } from './benchmarkTypes'

type LatencyPieChartProps = {
  samples: LatencySample[]
}

type LatencyRange = {
  name: string
  count: number
  percentage: number
}

const colors = ['#c66a2d', '#3f7de8', '#3f9c72', '#a43f32']

function formatLatency(value: number) {
  return `${value.toFixed(1)} ms`
}

function quantile(sortedValues: number[], probability: number) {
  return sortedValues[Math.ceil(probability * sortedValues.length) - 1]
}

function createLatencyRanges(samples: LatencySample[]): LatencyRange[] {
  const sortedValues = samples.map((sample) => sample.latencyMs).sort((a, b) => a - b)
  const median = quantile(sortedValues, 0.5)
  const p75 = quantile(sortedValues, 0.75)
  const p95 = quantile(sortedValues, 0.95)
  const boundaries = [median, p75, p95]
  const ranges: LatencyRange[] = []
  let previousBoundary = -Infinity
  let previousCount = 0

  boundaries.forEach((boundary, index) => {
    if (boundary === previousBoundary) {
      return
    }

    const count = sortedValues.filter((value) => value <= boundary).length - previousCount
    const name = index === 0
      ? `<= ${formatLatency(boundary)}`
      : `${formatLatency(previousBoundary)} - ${formatLatency(boundary)}`
    ranges.push({ name, count, percentage: (count / samples.length) * 100 })
    previousBoundary = boundary
    previousCount += count
  })

  const outlierCount = samples.length - previousCount
  if (outlierCount > 0) {
    ranges.push({
      name: `> ${formatLatency(p95)}`,
      count: outlierCount,
      percentage: (outlierCount / samples.length) * 100,
    })
  }

  return ranges
}

function RangeTooltip({ active, payload }: { active?: boolean; payload?: Array<{ payload: LatencyRange }> }) {
  if (!active || !payload?.length) {
    return null
  }

  const range = payload[0].payload

  return (
    <div className="chart-tooltip">
      <strong>{range.name}</strong>
      <span>{range.count} requests</span>
      <span>{range.percentage.toFixed(1)}%</span>
    </div>
  )
}

export function LatencyPieChart({ samples }: LatencyPieChartProps) {
  if (samples.length === 0) {
    return <div className="visualization-placeholder pie-placeholder" aria-label="Circular latency distribution">Circular distribution will appear here after a benchmark.</div>
  }

  const ranges = createLatencyRanges(samples)

  return (
    <div className="chart-panel" aria-label="Circular latency distribution">
      <h3>Latency ranges</h3>
      <div className="pie-frame">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={ranges} dataKey="count" nameKey="name" cx="50%" cy="50%" outerRadius="70%" label={({ name, percent }) => `${name} ${((percent ?? 0) * 100).toFixed(0)}%`} labelLine={false}>
              {ranges.map((range, index) => <Cell key={range.name} fill={colors[index % colors.length]} />)}
            </Pie>
            <Tooltip content={<RangeTooltip />} />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}