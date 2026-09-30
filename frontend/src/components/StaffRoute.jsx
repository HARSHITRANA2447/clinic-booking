import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function StaffRoute({ children }) {
    const { isLoggedIn, isStaff } = useAuth()
    const location = useLocation()
    if (!isLoggedIn) return <Navigate to="/login" state={{ from: location.pathname }} replace />
    if (!isStaff) return <Navigate to="/" replace />
    return children
}