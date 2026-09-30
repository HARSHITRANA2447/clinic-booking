import { useEffect, useState } from 'react'
import client, { errorMessage } from '../api/client'
import { fmtDate, fmtTime } from '../utils/format'

export default function MyBookings() {
    const [items, setItems] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        client.get('/api/bookings/mine')
            .then((r) => setItems(r.data))
            .catch((e) => setError(errorMessage(e)))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <p className="text-sm text-gray-500">Loading...</p>
    if (error) return <p className="rounded bg-red-50 p-3 text-sm text-red-700">{error}</p>

    return (
        <div>
            <h1 className="mb-3 text-xl font-semibold">My bookings</h1>
            {items.length === 0 && <p className="text-sm text-gray-500">No bookings yet.</p>}
            <div className="space-y-3">
                {items.map((b) => (
                    <div key={b.bookingId} className="rounded-lg border border-gray-200 bg-white p-4">
                        <div className="flex justify-between gap-2">
                            <p className="font-medium">{b.doctorName}</p>
                            <span className="text-xs text-gray-600">{b.status}</span>
                        </div>
                        <p className="text-sm text-gray-600">{b.specialization}</p>
                        <p className="mt-1 text-sm">{b.clinicName}</p>
                        <p className="text-xs text-gray-500">{b.clinicAddress}</p>
                        <p className="mt-2 text-sm font-medium">
                            {fmtDate(b.startTime)}, {fmtTime(b.startTime)}
                        </p>
                    </div>
                ))}
            </div>
        </div>
    )
}