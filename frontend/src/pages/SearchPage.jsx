import { useEffect, useState } from 'react'
import client, { errorMessage } from '../api/client'
import DoctorCard from '../components/DoctorCard'

// Fallback if location is denied: central Hyderabad
const DEFAULT_COORDS = { lat: 17.385, lng: 78.4867 }
const SPECS = ['', 'General Physician', 'Pediatrician']

export default function SearchPage() {
    const [coords, setCoords] = useState(null)
    const [locNote, setLocNote] = useState('Finding your location...')
    const [spec, setSpec] = useState('')
    const [radius, setRadius] = useState(10)
    const [results, setResults] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        if (!navigator.geolocation) {
            setCoords(DEFAULT_COORDS)
            setLocNote('Location not supported. Showing results near central Hyderabad.')
            return
        }
        navigator.geolocation.getCurrentPosition(
            (p) => {
                setCoords({ lat: p.coords.latitude, lng: p.coords.longitude })
                setLocNote('Using your current location.')
            },
            () => {
                setCoords(DEFAULT_COORDS)
                setLocNote('Location unavailable. Showing results near central Hyderabad.')
            },
            { timeout: 8000 }
        )
    }, [])

    useEffect(() => {
        if (!coords) return
        let cancelled = false
        setLoading(true)
        setError('')
        client
            .get('/api/search', {
                params: { lat: coords.lat, lng: coords.lng, radiusKm: radius, spec: spec || undefined },
            })
            .then((res) => !cancelled && setResults(res.data))
            .catch((err) => !cancelled && setError(errorMessage(err)))
            .finally(() => !cancelled && setLoading(false))
        return () => { cancelled = true }
    }, [coords, spec, radius])

    return (
        <div>
            <h1 className="text-xl font-semibold">Find a doctor</h1>
            <p className="mb-3 text-xs text-gray-500">{locNote}</p>

            <div className="mb-4 flex gap-2">
                <select
                    value={spec}
                    onChange={(e) => setSpec(e.target.value)}
                    className="flex-1 rounded border border-gray-300 bg-white p-2 text-sm"
                >
                    {SPECS.map((s) => <option key={s} value={s}>{s || 'All specializations'}</option>)}
                </select>
                <select
                    value={radius}
                    onChange={(e) => setRadius(Number(e.target.value))}
                    className="rounded border border-gray-300 bg-white p-2 text-sm"
                >
                    {[5, 10, 25, 50].map((r) => <option key={r} value={r}>{r} km</option>)}
                </select>
            </div>

            {loading && <p className="text-sm text-gray-500">Searching...</p>}
            {error && <p className="rounded bg-red-50 p-3 text-sm text-red-700">{error}</p>}
            {!loading && !error && coords && results.length === 0 && (
                <p className="text-sm text-gray-500">No doctors found. Try a larger radius.</p>
            )}

            <div className="space-y-3">
                {results.map((d) => <DoctorCard key={d.doctorId} doctor={d} />)}
            </div>
        </div>
    )
}