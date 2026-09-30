import { Link, Navigate, useLocation } from 'react-router-dom'
import { fmtDate, fmtTime } from '../utils/format'

export default function BookingConfirmation() {
    const b = useLocation().state?.booking
    // Opened directly or refreshed: no booking in memory, so send them to the list
    if (!b) return <Navigate to="/my-bookings" replace />

    return (
        <div className="rounded-lg border border-green-200 bg-green-50 p-4">
            <h1 className="text-lg font-semibold text-green-800">Booking confirmed</h1>
            <p className="mt-3 font-medium">{b.doctorName}</p>
            <p className="text-sm text-gray-700">{b.specialization}</p>
            <p className="mt-2 text-sm">{b.clinicName}</p>
            <p className="text-xs text-gray-600">{b.clinicAddress}</p>
            <p className="mt-3 text-sm font-medium">
                {fmtDate(b.startTime)}, {fmtTime(b.startTime)}
            </p>
            <p className="mt-1 text-xs text-gray-500">Booking #{b.bookingId}</p>
            <div className="mt-4 flex gap-3 text-sm">
                <Link to="/my-bookings" className="font-medium text-blue-700">My bookings</Link>
                <Link to="/" className="text-gray-700">Find another doctor</Link>
            </div>
        </div>
    )
}