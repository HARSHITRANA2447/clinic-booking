import { NavLink, Outlet } from 'react-router-dom'

const tab = ({ isActive }) =>
    `rounded px-3 py-1.5 text-sm ${isActive ? 'bg-blue-700 text-white' : 'bg-white text-gray-700 border'}`

export default function ClinicLayout() {
    return (
        <div>
            <div className="mb-4 flex gap-2">
                <NavLink to="/clinic" end className={tab}>Queue</NavLink>
                <NavLink to="/clinic/slots" className={tab}>Slots</NavLink>
                <NavLink to="/clinic/doctors" className={tab}>Doctors</NavLink>
            </div>
            <Outlet />
        </div>
    )
}