import { useCallback, useEffect, useState } from 'react'
import client, { errorMessage } from '../../api/client'
import { fmtTime, toISODate } from '../../utils/format'

const STYLE = {
    AVAILABLE: 'border-gray-300 bg-white text-gray-800',
    BOOKED: 'border-blue-200 bg-blue-100 text-blue-800 cursor-not-allowed',
    BLOCKED: 'border-gray-300 bg-gray-200 text-gray-500 line-through',
}

export default function SlotsPage() {
    const [doctors, setDoctors] = useState([])
    const [doctorId, setDoctorId] = useState('')
    const [date, setDate] = useState(toISODate(new Date()))
    const [slots, setSlots] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [info, setInfo] = useState('')
    const [gen, setGen] = useState({ startTime: '09:00', endTime: '13:00', minutes: 15 })
    const [generating, setGenerating] = useState(false)

    useEffect(() => {
        client.get('/api/clinic/doctors')
            .then((r) => { setDoctors(r.data); if (r.data[0]) setDoctorId(String(r.data[0].id)) })
            .catch((e) => setError(errorMessage(e)))
    }, [])

    const load = useCallback(async () => {
        if (!doctorId) return
        setLoading(true)
        setError('')
        try {
            setSlots((await client.get(`/api/clinic/doctors/${doctorId}/slots`, { params: { date } })).data)
        } catch (e) {
            setError(errorMessage(e))
        } finally {
            setLoading(false)
        }
    }, [doctorId, date])

    useEffect(() => { setInfo(''); load() }, [load])

    const toggle = async (s) => {
        if (s.status === 'BOOKED') return
        setError('')
        try {
            const next = s.status === 'AVAILABLE' ? 'BLOCKED' : 'AVAILABLE'
            const r = await client.patch(`/api/clinic/slots/${s.id}/status`, { status: next })
            setSlots((list) => list.map((x) => (x.id === s.id ? r.data : x)))
        } catch (e) {
            setError(errorMessage(e))
            load()
        }
    }

    const generate = async (e) => {
        e.preventDefault()
        setError('')
        setInfo('')
        if (gen.endTime <= gen.startTime) return setError('End time must be after start time')
        setGenerating(true)
        try {
            const r = await client.post(`/api/clinic/doctors/${doctorId}/slots/generate`, {
                date, startTime: gen.startTime, endTime: gen.endTime, minutes: Number(gen.minutes),
            })
            setInfo(`Created ${r.data.created} slots, skipped ${r.data.skippedExisting}.`)
            load()
        } catch (e2) {
            setError(errorMessage(e2))
        } finally {
            setGenerating(false)
        }
    }

    const field = 'rounded border border-gray-300 bg-white p-2 text-sm'

    return (
        <div>
            <div className="mb-4 flex flex-wrap gap-2">
                <select value={doctorId} onChange={(e) => setDoctorId(e.target.value)} className={field}>
                    {doctors.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
                <input type="date" value={date} onChange={(e) => setDate(e.target.value)} className={field} />
            </div>

            {error && <p className="mb-3 rounded bg-red-50 p-3 text-sm text-red-700">{error}</p>}
            {info && <p className="mb-3 rounded bg-green-50 p-3 text-sm text-green-800">{info}</p>}

            <h2 className="mb-2 font-semibold">Slots</h2>
            <p className="mb-2 text-xs text-gray-500">Tap an open slot to block it, or a blocked slot to reopen it. Booked slots are managed from the Queue.</p>
            {loading && <p className="text-sm text-gray-500">Loading...</p>}
            {!loading && slots.length === 0 && <p className="text-sm text-gray-500">No slots on this date. Generate some below.</p>}
            <div className="grid grid-cols-3 gap-2 sm:grid-cols-5">
                {slots.map((s) => (
                    <button key={s.id} onClick={() => toggle(s)} disabled={s.status === 'BOOKED'}
                        className={`rounded border p-2 text-sm ${STYLE[s.status]}`}>
                        {fmtTime(s.startTime)}
                    </button>
                ))}
            </div>

            <form onSubmit={generate} className="mt-6 rounded-lg border border-gray-200 bg-white p-4">
                <h2 className="mb-1 font-semibold">Generate slots for {date}</h2>
                <p className="mb-3 text-xs text-gray-500">Existing and past slots are skipped, so it's safe to run twice.</p>
                <div className="flex flex-wrap items-end gap-3 text-sm">
                    <label>From<input type="time" value={gen.startTime} className={`${field} block`}
                        onChange={(e) => setGen({ ...gen, startTime: e.target.value })} /></label>
                    <label>To<input type="time" value={gen.endTime} className={`${field} block`}
                        onChange={(e) => setGen({ ...gen, endTime: e.target.value })} /></label>
                    <label>Minutes each
                        <select value={gen.minutes} className={`${field} block`}
                            onChange={(e) => setGen({ ...gen, minutes: e.target.value })}>
                            {[10, 15, 20, 30].map((m) => <option key={m} value={m}>{m}</option>)}
                        </select></label>
                    <button disabled={generating || !doctorId}
                        className="rounded bg-blue-700 px-4 py-2 text-white disabled:opacity-60">
                        {generating ? 'Generating...' : 'Generate'}
                    </button>
                </div>
            </form>
        </div>
    )
}