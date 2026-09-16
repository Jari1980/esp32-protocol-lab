import { useState } from 'react'
import { BenchmarkControls } from './BenchmarkControls'
import { BenchmarkResults } from './BenchmarkResults'
import { LatencyDistribution } from './LatencyDistribution'
import { LatencyPieChart } from './LatencyPieChart'
import type { BenchmarkProtocol, BenchmarkResult } from './benchmarkTypes'
import { runHttpBenchmark, runWebSocketBenchmark } from '../../services/benchmarkRunner'

function createEmptyResult(protocol: BenchmarkProtocol = 'HTTP'): BenchmarkResult {
  return {
    protocol,
    requestedCount: 100,
    successfulSamples: [],
    failures: [],
    summary: { average: null, median: null, p95: null, min: null, max: null },
    distribution: { samples: [] },
  }
}

const initialResult: BenchmarkResult = {
  protocol: 'HTTP',
  requestedCount: 100,
  successfulSamples: [],
  failures: [],
  summary: { average: null, median: null, p95: null, min: null, max: null },
  distribution: { samples: [] },
}

export function BenchmarkPanel() {
  const [protocol, setProtocol] = useState<BenchmarkProtocol>('HTTP')
  const [result, setResult] = useState<BenchmarkResult>(initialResult)
  const [isRunning, setIsRunning] = useState(false)
  

  async function handleStart() {
    if (protocol === 'MQTT') {
      return
    }

    setIsRunning(true)

    try {
      const nextResult = protocol === 'HTTP'
        ? await runHttpBenchmark()
        : await runWebSocketBenchmark()
        console.log('Benchmark result:', nextResult)
      setResult(nextResult)
    } finally {
      setIsRunning(false)
    }
  }

  function handleReset() {
    setResult(createEmptyResult(protocol))
  }

  return (
    <section className="panel-section benchmark-panel" aria-labelledby="benchmark-heading">
      <div className="section-heading">
        <p className="eyebrow">MEASUREMENT</p>
        <h2 id="benchmark-heading">Protocol benchmark</h2>
        <p>Run a fixed 100-request test and inspect its latency when measurement is available.</p>
      </div>
      <BenchmarkControls protocol={protocol} onProtocolChange={setProtocol} onStart={() => void handleStart()} isRunning={isRunning} />
      <BenchmarkResults summary={result.summary} />
      <div className="distribution-grid">
        <LatencyDistribution samples={result.successfulSamples} summary={result.summary} />
        <LatencyPieChart samples={result.successfulSamples} />
      </div>
      <button type="button" className="clear-results" onClick={handleReset}>Clear results</button>
    </section>
  )
}