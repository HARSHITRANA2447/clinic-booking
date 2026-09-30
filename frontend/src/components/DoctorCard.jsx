import { Link } from 'react-router-dom'
import CrowdBadge from './CrowdBadge'

export default function DoctorCard({ doctor }) {
    return (
        <Link
            to={`/doctors/${doctor.doctorId}`}
            className="block rounded-lg border border-gray-200 bg-white p-4 shadow-sm active:bg-gray-50"
        >
            <div className="flex items-start justify-between gap-2">
                <div>
                    <h3 className="font-semibold text-gray-900">{doctor.doctorName}</h3>
                    <p className="text-sm text-gray-600">{doctor.specialization}</p>
                </div>
                <CrowdBadge level={doctor.crowdLevel} />
            </div>
            <p className="mt-2 text-sm text-gray-700">{doctor.clinicName}</p>
            <p className="text-xs text-gray-500">{doctor.address}</p>
            <div className="mt-2 flex justify-between text-sm">
                <span className="text-gray-600">{doctor.distanceKm} km away</span>
                {doctor.consultationFee != null && (
                    <span className="font-medium">₹{doctor.consultationFee}</span>
                )}
            </div>
        </Link>
    )
}