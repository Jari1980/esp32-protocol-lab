import type { LedCommand } from '../components/LedControl/ledTypes'

export async function sendRedLedCommand(command: LedCommand): Promise<void> {
  const response = await fetch('http://localhost:8080/led', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(command),
  })

  if (!response.ok) {
    throw new Error(`RED LED request failed with status ${response.status}`)
  }
}