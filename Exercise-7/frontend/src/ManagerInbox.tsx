import { useCallback, useEffect, useState } from 'react'
import { decide, pendingLeaves, type Leave } from './api'

export default function ManagerInbox() {
  const [rows, setRows] = useState<Leave[]>([])
  const [comments, setComments] = useState<Record<number, string>>({})
  const [hidden, setHidden] = useState<number[]>([])
  const [busy, setBusy] = useState<number | null>(null)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    pendingLeaves().then(setRows).catch((e: Error) => setError(e.message))
  }, [])

  useEffect(() => {
    load()
    const timer = setInterval(load, 3000)
    return () => clearInterval(timer)
  }, [load])

  async function act(id: number, approved: boolean) {
    setBusy(id)
    setError('')
    try {
      await decide(id, approved, comments[id] ?? '')
      setHidden(current => [...current, id])
      load()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setBusy(null)
    }
  }

  const visible = rows.filter(row => !hidden.includes(row.id))
  return (
    <section>
      <div className="section-heading"><span className="step">03</span><div><h2>Manager inbox</h2><p>Review requests that passed automatic validation.</p></div></div>
      {error && <p className="error">{error}</p>}
      {visible.length === 0 && <div className="card muted">No requests are waiting for approval.</div>}
      {visible.map(r => <article key={r.id} className="card request-card">
        <div className="request-top"><strong>#{r.id} · {r.employeeName}</strong><span className="badge PENDING_APPROVAL">PENDING</span></div>
        <div className="muted">{r.employeeEmail}</div>
        <p>{r.leaveType} · {r.startDate} to {r.endDate} · {r.days} working day(s)</p>
        {r.reason && <p className="muted">{r.reason}</p>}
        <label>Manager comment
          <input placeholder="Optional comment" value={comments[r.id] ?? ''}
            onChange={e => setComments({ ...comments, [r.id]: e.target.value })} />
        </label>
        <div className="actions">
          <button className="primary" disabled={busy === r.id} onClick={() => act(r.id, true)}>Approve</button>
          <button className="danger" disabled={busy === r.id} onClick={() => act(r.id, false)}>Reject</button>
        </div>
      </article>)}
    </section>
  )
}
