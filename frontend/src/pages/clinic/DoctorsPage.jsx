import { useEffect, useState } from 'react'
import client, { errorMessage } from '../../api/client'

function DoctorEditor({ doctor, onSaved }) {
    const [fee, setFee] = useState(doctor.consultationFee ?? '')
    const [qual, setQual] = useState(doctor.qualification ?? '')
    const [active, setActive] = useState(doctor.active)
    const [busy, setBusy] = useState(false)
    const [msg, setMsg] = useState({ text: '', ok: true })

    const save = async () => {
        if (fee !== '' && (Number(fee) < 0 || Number(fee) > 100000)) {
            return setMsg({ text: 'Fee must be between 0 and 100000', ok: false })
        }
        setBusy(true)
        setMsg({ text: '', ok: true })
        try {
            const r = await client.patch(`/api/clinic/doctors/${doctor.id}`, {
                consultationFee: fee === '' ? null : Number(fee),
                qualification: qual,
                active,
            })
            onSaved(r.data)
            setMsg({ text: 'Saved', ok: true })
        } catch (e) {
            setMsg({ text: errorMessage(e), ok: false })
        } finally {
            setBusy(false)
        }
    }

    const field = 'w-full rounded border border-gray-300 p-2 text-sm'
    return (
        <div className="rounded-lg border border-gray-200 bg-white p-4">
            <p className="font-semibold">{doctor.name}</p>
            <p className="mb-3 text-sm text-gray-600">{doctor.specialization}</p>
            <div className="grid gap-3 sm:grid-cols-2">
                <label className="text-sm">Fee (₹)
                    <input type="number" min="0" value={fee} onChange={(e) => setFee(e.target.value)} className={field} /></label>
                <label className="text-sm">Qualification
                    <input value={qual} maxLength={150} onChange={(e) => setQual(e.target.value)} className={field} /></label>
            </div>
            <label className="mt-3 flex items-center gap-2 text-sm">
                <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} />
                Visible to patients (uncheck when the doctor is away)
            </label>
            <div className="mt-3 flex items-center gap-3">
                <button onClick={save} disabled={busy}
                    className="rounded bg-blue-700 px-4 py-2 text-sm text-white disabled:opacity-60">
                    {busy ? 'Saving...' : 'Save'}
                </button>
                {msg.text && <span className={`text-sm ${msg.ok ? 'text-green-700' : 'text-red-700'}`}>{msg.text}</span>}
            </div>
        </div>
    )
}

export default function DoctorsPage() {
    const [doctors, setDoctors] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        client.get('/api/clinic/doctors')
            .then((r) => setDoctors(r.data))
            .catch((e) => setError(errorMessage(e)))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <p className="text-sm text-gray-500">Loading...</p>
    if (error) return <p className="rounded bg-red-50 p-3 text-sm text-red-700">{error}</p>

    return (
        <div className="space-y-3">
            {doctors.map((d) => (
                <DoctorEditor key={d.id} doctor={d}
                    onSaved={(u) => setDoctors((l) => l.map((x) => (x.id === u.id ? u : x)))} />
            ))}
        </div>
    )
}