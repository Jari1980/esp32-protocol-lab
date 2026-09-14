export type BenchmarkProtocol = 'HTTP' | 'WebSocket' | 'MQTT'

export type LatencySummary = {
  average: number | null
  median: number | null
  p95: number | null
  min: number | null
  max: number | null
}

export type LatencyDistributionData = {
  samples: number[]
}

export type BenchmarkResult = {
  protocol: BenchmarkProtocol
  summary: LatencySummary
  distribution: LatencyDistributionData
}