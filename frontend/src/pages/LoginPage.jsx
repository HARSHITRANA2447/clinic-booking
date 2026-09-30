import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../api/client'

export default function LoginPage() {
    const { login } = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const [phone, setPhone] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    const submit = async (e) => {
        e.preventDefault()
        setError('')

        if (!/^[6-9]\d{9}$/.test(phone)) {
            return setError('Enter a valid 10-digit mobile number')
        }

        if (!password) {
            return setError('Enter your password')
        }

        setBusy(true)

        try {
            const data = await login(phone, password)

            const staff =
                data.role === 'CLINIC_STAFF' ||
                data.role === 'ADMIN'

            navigate(
                staff
                    ? '/clinic'
                    : (location.state?.from || '/'),
                { replace: true }
            )
        } catch (err) {
            setError(errorMessage(err))
        } finally {
            setBusy(false)
        }
    }

    return (
        <form onSubmit={submit} className="space-y-3">
            <h1 className="text-xl font-semibold">Login</h1>

            <input
                value={phone}
                onChange={(e) => setPhone(e.target.value.trim())}
                inputMode="numeric"
                maxLength={10}
                placeholder="Mobile number"
                className="w-full rounded border border-gray-300 p-2"
            />

            <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
                className="w-full rounded border border-gray-300 p-2"
            />

            {error && (
                <p className="text-sm text-red-700">
                    {error}
                </p>
            )}

            <button
                disabled={busy}
                className="w-full rounded bg-blue-700 p-2 font-medium text-white disabled:opacity-60"
            >
                {busy ? 'Logging in...' : 'Login'}
            </button>

            <p className="text-sm text-gray-600">
                New here?{' '}
                <Link
                    to="/register"
                    className="text-blue-700"
                >
                    Create an account
                </Link>
            </p>
        </form>
    )
}