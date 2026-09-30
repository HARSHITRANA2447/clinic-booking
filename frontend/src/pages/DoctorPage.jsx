import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import client, { errorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'
import CrowdBadge from '../components/CrowdBadge'
import SlotGrid from '../components/SlotGrid'
import { fmtDate, fmtTime, nextDays, toISODate } from '../utils/format'

export default function DoctorPage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const { isLoggedIn } = useAuth()

    const days = nextDays(7)
    const [date, setDate] = useState(toISODate(days[0]))
    const [doctor, setDoctor] = useState(null)
    const [slots, setSlots] = useState([])
    const [selected, setSelected] = useState(null)
    const [loading, setLoading] = useState(true)
    const [slotsLoading, setSlotsLoading] = useState(false)
    const [error, setError] = useState('')
    const [bookError, setBookError] = useState('')
    const [booking, setBooking] = useState(false)

    useEffect(() => {
        client.get(`/api/doctors/${id}`)
            .then((r) => setDoctor(r.data))
            .catch((e) => setError(errorMessage(e)))
            .finally(() => setLoading(false))
    }, [id])

    const loadSlots = useCallback(async () => {
        setSlotsLoading(true)
        try {
            const r = await client.get(`/api/doctors/${id}/slots`, { params: { date } })
            setSlots(r.data)
        } catch (e) {
            setBookError(errorMessage(e))
        } finally {
            setSlotsLoading(false)
        }
    }, [id, date])

    useEffect(() => {
        setSelected(null)
        setBookError('')
        loadSlots()
    }, [loadSlots])

    const book = async () => {
        if (!isLoggedIn) {
            navigate('/login', { state: { from: `/doctors/${id}` } })
            return
        }
        setBooking(true)
        setBookError('')
        try {
            const r = await client.post('/api/bookings', { slotId: selected.id })
            navigate('/booking-confirmed', { state: { booking: r.data } })
        } catch (e) {
            setBookError(errorMessage(e))
            setSelected(null)
            loadSlots() // slot may have just been taken, so refresh the grid
        } finally {
            setBooking(false)
        }
    }

    if (loading) return <p className="text-sm text-gray-500">Loading...</p>
    if (error) return <p className="rounded bg-red-50 p-3 text-sm text-red-700">{error}</p>

    return (
        <div>
            <div className="rounded-lg border border-gray-200 bg-white p-4">
                <div className="flex items-start justify-between gap-2">
                    <div>
                        <h1 className="text-lg font-semibold">{doctor.doctorName}</h1>
                        <p className="text-sm text-gray-600">
                            {doctor.specialization}{doctor.qualification && ` · ${doctor.qualification}`}
                        </p>
                    </div>
                    <CrowdBadge level={doctor.crowdLevel} />
                </div>
                <p className="mt-2 text-sm text-gray-800">{doctor.clinicName}</p>
                <p className="text-xs text-gray-500">{doctor.address}</p>
                {doctor.consultationFee != null && (
                    <p className="mt-1 text-sm font-medium">Fee: ₹{doctor.consultationFee}</p>
                )}
            </div>

            <h2 className="mb-2 mt-5 font-semibold">Pick a date</h2>
            <div className="mb-4 flex gap-2 overflow-x-auto pb-1">
                {days.map((d) => {
                    const iso = toISODate(d)
                    return (
                        <button
                            key={iso}
                            onClick={() => setDate(iso)}
                            className={`shrink-0 rounded border px-3 py-2 text-sm ${date === iso ? 'border-blue-700 bg-blue-50 font-medium text-blue-800'
                                    : 'border-gray-300 bg-white text-gray-700'
                                }`}
                        >
                            {fmtDate(iso)}
                        </button>
                    )
                })}
            </div>

            <h2 className="mb-2 font-semibold">Available times</h2>
            {slotsLoading && <p className="text-sm text-gray-500">Loading slots...</p>}
            {!slotsLoading && slots.length === 0 && (
                <p className="text-sm text-gray-500">No slots available on this date.</p>
            )}
            {!slotsLoading && (
                <SlotGrid slots={slots} selectedId={selected?.id} onSelect={setSelected} />
            )}

            {bookError && <p className="mt-3 rounded bg-red-50 p-3 text-sm text-red-700">{bookError}</p>}

            {selected && (
                <div className="sticky bottom-0 mt-4 border-t bg-white py-3">
                    <button
                        onClick={book}
                        disabled={booking}
                        className="w-full rounded bg-blue-700 p-3 font-medium text-white disabled:opacity-60"
                    >
                        {booking ? 'Booking...'
                            : isLoggedIn ? `Book ${fmtDate(selected.startTime)}, ${fmtTime(selected.startTime)}`
                                : 'Login to book this slot'}
                    </button>
                </div>
            )}
        </div>
    )
}