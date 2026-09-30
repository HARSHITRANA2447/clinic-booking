import { useCallback, useEffect, useState } from 'react'
import client, { errorMessage } from '../../api/client'
import { fmtTime, toISODate } from '../../utils/format'

const BADGE = {
    CONFIRMED: 'bg-blue-100 text-blue-800',
    COMPLETED: 'bg-green-100 text-green-800',
    NO_SHOW: 'bg-yellow-100 text-yellow-800',
    CANCELLED: 'bg-gray-200 text-gray-600',
}

export default function QueuePage() {
    const [date, setDate] = useState(toISODate(new Date()))
    const [items, setItems] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [busyId, setBusyId] = useState(null)

    const load = useCallback(async () => {
        setLoading(true)
        setError('')
        try {
            setItems((await client.get('/api/clinic/queue', { params: { date } })).data)
        } catch (e) {
            setError(errorMessage(e))
        } finally {
            setLoading(false)
        }
    }, [date])

    useEffect(() => { load() }, [load])

    const update = async (item, status) => {
        if (status === 'CANCELLED' && !window.confirm(`Cancel ${item.patientName}'s booking? The slot will reopen.`)) return
        setBusyId(item.bookingId)
        setError('')
        try {
            const r = await client.patch(`/api/clinic/bookings/${item.bookingId}/status`, { status })
            setItems((list) => list.map((x) => (x.bookingId === item.bookingId ? r.data : x)))
        } catch (e) {
            setError(errorMessage(e))
            load()
        } finally {
            setBusyId(null)
        }
    }

    const waiting = items.filter((i) => i.status === 'CONFIRMED').length

    return (
        <div>
            <div className="mb-3 flex items-center justify-between gap-2">
                <input type="date" value={date} onChange={(e) => setDate(e.target.value)}
                    className="rounded border border-gray-300 bg-white p-2 text-sm" />
                <button onClick={load} className="text-sm text-blue-700">Refresh</button>
            </div>
            <p className="mb-3 text-sm text-gray-600">{waiting} waiting · {items.length} total</p>

            {error && <p className="mb-3 rounded bg-red-50 p-3 text-sm text-red-700">{error}</p>}
            {loading && <p className="text-sm text-gray-500">Loading...</p>}
            {!loading && items.length === 0 && <p className="text-sm text-gray-500">No bookings on this date.</p>}

            <div className="space-y-2">
                {items.map((i) => (
                    <div key={i.bookingId} className="rounded-lg border border-gray-200 bg-white p-3">
                        <div className="flex items-start justify-between gap-2">
                            <div>
                                <p className="font-medium">{fmtTime(i.startTime)} · {i.patientName}</p>
                                <p className="text-sm text-gray-600">{i.doctorName}</p>
                                <a href={`tel:${i.patientPhone}`} className="text-sm text-blue-700">{i.patientPhone}</a>
                            </div>
                            <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${BADGE[i.status]}`}>
                                {i.status.replace('_', ' ')}
                            </span>
                        </div>
                        {i.status === 'CONFIRMED' && (
                            <div className="mt-3 flex flex-wrap gap-2 text-sm">
                                <button disabled={busyId === i.bookingId} onClick={() => update(i, 'COMPLETED')}
                                    className="rounded bg-green-700 px-3 py-1.5 text-white disabled:opacity-60">Completed</button>
                                <button disabled={busyId === i.bookingId} onClick={() => update(i, 'NO_SHOW')}
                                    className="rounded border px-3 py-1.5 disabled:opacity-60">No-show</button>
                                <button disabled={busyId === i.bookingId} onClick={() => update(i, 'CANCELLED')}
                                    className="rounded border border-red-300 px-3 py-1.5 text-red-700 disabled:opacity-60">Cancel</button>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    )
}