import { useEffect, useState } from 'react'
import { balance, myLeaves, type Leave } from './api'

export default function MyRequests({ email, version }: { email: string; version: number }) {
  const [rows, setRows] = useState<Leave[]>([])
  const [remaining, setRemaining] = useState<number | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!email) return
    let alive = true
    const load = async () => {
      try {
        const [requests, leaveBalance] = await Promise.all([myLeaves(email), balance(email)])
        if (alive) { setRows(requests); setRemaining(leaveBalance.remainingDays); setError('') }
      } catch (e) {
        if (alive) setError((e as Error).message)
      }
    }
    void load()
    const timer = setInterval(load, 3000)
    return () => { alive = false; clearInterval(timer) }
  }, [email, version])

  if (!email) return <p className="card">Enter your email above to see your requests.</p>

  return (
    <section className="card">
      <div className="section-heading"><span className="step">02</span><div><h2>My requests</h2><p>Track the status of your applications.</p></div></div>
      <div className="balance"><span>Annual leave remaining</span><strong>{remaining ?? '…'} <small>days</small></strong></div>
      {error && <p className="error">{error}</p>}
      <div className="table-wrap"><table>
        <thead><tr><th>#</th><th>Type</th><th>Dates</th><th>Days</th><th>Status</th><th>Note</th></tr></thead>
        <tbody>{rows.map(r => <tr key={r.id}>
          <td>{r.id}</td><td>{r.leaveType}</td><td>{r.startDate} → {r.endDate}</td><td>{r.days}</td>
          <td><span className={`badge ${r.status}`}>{r.status.replace('_', ' ')}</span></td><td>{r.statusMessage}</td>
        </tr>)}</tbody>
      </table></div>
      {rows.length === 0 && <p className="muted">No leave requests yet.</p>}
    </section>
  )
}
