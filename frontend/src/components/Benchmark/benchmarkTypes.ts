export type BenchmarkProtocol = 'HTTP' | 'WebSocket' | 'MQTT'

export type LatencySummary = {
  average: number | null
  median: number | null
  p95: number | null
  min: number | null
  max: number | null
}

export type BenchmarkCommand = {
  index: number
  color: 'RED' | 'BLUE' | 'GREEN'
  on: boolean
}

export type LatencySample = {
  requestIndex: number
  command: BenchmarkCommand
  latencyMs: number
}

export type BenchmarkFailure = {
  requestIndex: number
  command: BenchmarkCommand
  durationMs: number
  message: string
}

export type LatencyDistributionData = {
  samples: LatencySample[]
}

export type BenchmarkResult = {
  protocol: BenchmarkProtocol
  requestedCount: 100
  successfulSamples: LatencySample[]
  failures: BenchmarkFailure[]
  summary: LatencySummary
  distribution: LatencyDistributionData
}