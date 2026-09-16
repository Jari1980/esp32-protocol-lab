import { useState } from 'react'
import { BenchmarkControls } from './BenchmarkControls'
import { BenchmarkComparison } from './BenchmarkComparison'
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

export function BenchmarkPanel() {
  const [protocol, setProtocol] = useState<BenchmarkProtocol>('HTTP')
  const [results, setResults] = useState<Partial<Record<BenchmarkProtocol, BenchmarkResult>>>({})
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
      setResults((currentResults) => ({ ...currentResults, [nextResult.protocol]: nextResult }))
    } finally {
      setIsRunning(false)
    }
  }

  function handleReset() {
    setResults({})
  }

  const result = results[protocol] ?? createEmptyResult(protocol)

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
      <BenchmarkComparison results={results} />
      <button type="button" className="clear-results" onClick={handleReset}>Clear results</button>
    </section>
  )
}