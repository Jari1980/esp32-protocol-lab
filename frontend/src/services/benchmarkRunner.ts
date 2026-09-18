import { sendRedLedCommand } from './ledClient'
import { connect, sendBlueLedCommand } from './webSocketClient'
import { connect as connectMqtt, sendGreenLedCommand } from './mqttWebSocketClient'
import type {
  BenchmarkCommand,
  BenchmarkFailure,
  BenchmarkResult,
  LatencySample,
  LatencySummary,
} from '../components/Benchmark/benchmarkTypes'

const BENCHMARK_REQUEST_COUNT = 100

function calculateSummary(samples: LatencySample[]): LatencySummary {
  if (samples.length === 0) {
    return { average: null, median: null, p95: null, min: null, max: null }
  }

  const values = samples.map((sample) => sample.latencyMs)
  const sortedValues = [...values].sort((a, b) => a - b)
  const middle = Math.floor(sortedValues.length / 2)
  const median = sortedValues.length % 2 === 0
    ? (sortedValues[middle - 1] + sortedValues[middle]) / 2
    : sortedValues[middle]
  const p95Rank = Math.ceil(0.95 * sortedValues.length)

  return {
    average: values.reduce((total, value) => total + value, 0) / values.length,
    median,
    p95: sortedValues[p95Rank - 1],
    min: sortedValues[0],
    max: sortedValues[sortedValues.length - 1],
  }
}

export async function runHttpBenchmark(): Promise<BenchmarkResult> {
  const successfulSamples: LatencySample[] = []
  const failures: BenchmarkFailure[] = []

  for (let index = 0; index < BENCHMARK_REQUEST_COUNT; index += 1) {
    const command: BenchmarkCommand = {
      index,
      color: 'RED',
      on: index % 2 === 0,
    }
    const startedAt = performance.now()

    try {
      await sendRedLedCommand({ color: 'RED', on: command.on })
      successfulSamples.push({
        requestIndex: index,
        command,
        latencyMs: performance.now() - startedAt,
      })
    } catch (error) {
      failures.push({
        requestIndex: index,
        command,
        durationMs: performance.now() - startedAt,
        message: error instanceof Error ? error.message : 'Unknown HTTP error',
      })
    }
  }

  return {
    protocol: 'HTTP',
    requestedCount: BENCHMARK_REQUEST_COUNT,
    successfulSamples,
    failures,
    summary: calculateSummary(successfulSamples),
    distribution: { samples: successfulSamples },
  }
}

export async function runWebSocketBenchmark(): Promise<BenchmarkResult> {
  await connect()

  const successfulSamples: LatencySample[] = []
  const failures: BenchmarkFailure[] = []

  for (let index = 0; index < BENCHMARK_REQUEST_COUNT; index += 1) {
    const command: BenchmarkCommand = {
      index,
      color: 'BLUE',
      on: index % 2 === 0,
    }
    const startedAt = performance.now()

    try {
      await sendBlueLedCommand({ color: 'BLUE', on: command.on })
      successfulSamples.push({
        requestIndex: index,
        command,
        latencyMs: performance.now() - startedAt,
      })
    } catch (error) {
      failures.push({
        requestIndex: index,
        command,
        durationMs: performance.now() - startedAt,
        message: error instanceof Error ? error.message : 'Unknown WebSocket error',
      })
    }
  }

  return {
    protocol: 'WebSocket',
    requestedCount: BENCHMARK_REQUEST_COUNT,
    successfulSamples,
    failures,
    summary: calculateSummary(successfulSamples),
    distribution: { samples: successfulSamples },
  }
}

export async function runMqttBenchmark(): Promise<BenchmarkResult> {
  await connectMqtt()

  const successfulSamples: LatencySample[] = []
  const failures: BenchmarkFailure[] = []

  for (let index = 0; index < BENCHMARK_REQUEST_COUNT; index += 1) {
    const command: BenchmarkCommand = {
      index,
      color: 'GREEN',
      on: index % 2 === 0,
    }
    const startedAt = performance.now()

    try {
      await sendGreenLedCommand({ color: 'GREEN', on: command.on })
      successfulSamples.push({
        requestIndex: index,
        command,
        latencyMs: performance.now() - startedAt,
      })
    } catch (error) {
      failures.push({
        requestIndex: index,
        command,
        durationMs: performance.now() - startedAt,
        message: error instanceof Error ? error.message : 'Unknown MQTT WebSocket error',
      })
    }
  }

  return {
    protocol: 'MQTT',
    requestedCount: BENCHMARK_REQUEST_COUNT,
    successfulSamples,
    failures,
    summary: calculateSummary(successfulSamples),
    distribution: { samples: successfulSamples },
  }
}