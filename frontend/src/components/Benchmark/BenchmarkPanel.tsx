import { useState } from 'react'
import { BenchmarkControls } from './BenchmarkControls'
import { BenchmarkResults } from './BenchmarkResults'
import { LatencyDistribution } from './LatencyDistribution'
import { LatencyPieChart } from './LatencyPieChart'
import type { BenchmarkProtocol, BenchmarkResult } from './benchmarkTypes'

const emptyResult: BenchmarkResult = {
  protocol: 'HTTP',
  summary: { average: null, median: null, p95: null, min: null, max: null },
  distribution: { samples: [] },
}

export function BenchmarkPanel() {
  const [protocol, setProtocol] = useState<BenchmarkProtocol>('HTTP')
  const [result, setResult] = useState<BenchmarkResult>(emptyResult)

  function handleReset() {
    setResult({ ...emptyResult, protocol })
  }

  return (
    <section className="panel-section benchmark-panel" aria-labelledby="benchmark-heading">
      <div className="section-heading">
        <p className="eyebrow">MEASUREMENT</p>
        <h2 id="benchmark-heading">Protocol benchmark</h2>
        <p>Run a fixed 100-request test and inspect its latency when measurement is available.</p>
      </div>
      <BenchmarkControls protocol={protocol} onProtocolChange={setProtocol} onStart={() => undefined} isRunning={false} />
      <BenchmarkResults summary={result.summary} />
      <div className="distribution-grid">
        <LatencyDistribution data={result.distribution} />
        <LatencyPieChart data={result.distribution} />
      </div>
      <button type="button" className="clear-results" onClick={handleReset}>Clear results</button>
    </section>
  )
}