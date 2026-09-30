import { createContext, useContext, useEffect, useState } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem('token'))
    const [role, setRole] = useState(localStorage.getItem('role'))

    useEffect(() => {
        const onExpired = () => { setToken(null); setRole(null) }
        window.addEventListener('auth-expired', onExpired)
        return () => window.removeEventListener('auth-expired', onExpired)
    }, [])

    const save = (data) => {
        localStorage.setItem('token', data.token)
        localStorage.setItem('role', data.role)
        setToken(data.token)
        setRole(data.role)
    }

    const login = async (phone, password) => {
        const data = (await client.post('/api/auth/login', { phone, password })).data
        save(data)
        return data
    }

    const register = async (payload) => {
        const data = (await client.post('/api/auth/register', payload)).data
        save(data)
        return data
    }

    const logout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        setToken(null)
        setRole(null)
    }

    return (
        <AuthContext.Provider value={{
            token, role, isLoggedIn: !!token,
            isStaff: role === 'CLINIC_STAFF' || role === 'ADMIN',
            login, register, logout
        }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)