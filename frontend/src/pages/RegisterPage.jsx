import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import client, { errorMessage } from '../api/client'

export default function RegisterPage() {
    const { register } = useAuth()
    const navigate = useNavigate()

    const [form, setForm] = useState({
        name: '',
        phone: '',
        email: '',
        password: '',
        consent: false
    })

    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    // OTP states
    const [otpSent, setOtpSent] = useState(false)
    const [otpCode, setOtpCode] = useState('')
    const [sendingOtp, setSendingOtp] = useState(false)

    const set = (k) => (e) =>
        setForm({
            ...form,
            [k]: e.target.type === 'checkbox'
                ? e.target.checked
                : e.target.value
        })

    // Send OTP
    const sendOtp = async () => {
        setError('')

        if (!/^[6-9]\d{9}$/.test(form.phone)) {
            return setError(
                'Enter a valid 10-digit mobile number first'
            )
        }

        setSendingOtp(true)

        try {
            await client.post('/api/auth/otp/send', {
                phone: form.phone
            })

            setOtpSent(true)
            setOtpCode('')
        } catch (err) {
            setError(errorMessage(err))
        } finally {
            setSendingOtp(false)
        }
    }

    const submit = async (e) => {
        e.preventDefault()
        setError('')

        // 1. Name validation
        if (!form.name.trim()) {
            return setError('Enter your name')
        }

        // 2. Phone validation
        if (!/^[6-9]\d{9}$/.test(form.phone)) {
            return setError(
                'Enter a valid 10-digit mobile number'
            )
        }

        // 3. Password validation
        if (form.password.length < 8) {
            return setError(
                'Password must be at least 8 characters'
            )
        }

        // 4. Consent validation
        if (!form.consent) {
            return setError(
                'Please accept the consent to continue'
            )
        }

        // 5. OTP validation - MUST BE LAST
        if (!otpCode) {
            return setError(
                'Enter the verification code sent to your phone'
            )
        }

        setBusy(true)

        try {
            await register({
                ...form,
                email: form.email || null,
                otpCode
            })

            navigate('/', { replace: true })
        } catch (err) {
            setError(errorMessage(err))
        } finally {
            setBusy(false)
        }
    }

    const input = 'w-full rounded border border-gray-300 p-2'

    return (
        <form onSubmit={submit} className="space-y-3">

            <h1 className="text-xl font-semibold">
                Create account
            </h1>

            <input
                value={form.name}
                onChange={set('name')}
                placeholder="Full name"
                className={input}
            />

            <input
                value={form.phone}
                onChange={set('phone')}
                inputMode="numeric"
                maxLength={10}
                placeholder="Mobile number"
                className={input}
            />

            {/* Send OTP button */}
            <button
                type="button"
                onClick={sendOtp}
                disabled={sendingOtp}
                className="w-full rounded border border-blue-700 p-2 text-sm text-blue-700 disabled:opacity-60"
            >
                {sendingOtp
                    ? 'Sending...'
                    : otpSent
                        ? 'Resend code'
                        : 'Send verification code'}
            </button>

            {/* OTP input */}
            {otpSent && (
                <input
                    value={otpCode}
                    onChange={(e) =>
                        setOtpCode(e.target.value.trim())
                    }
                    inputMode="numeric"
                    maxLength={6}
                    placeholder="6-digit verification code"
                    className={input}
                />
            )}

            <input
                value={form.email}
                onChange={set('email')}
                type="email"
                placeholder="Email (optional)"
                className={input}
            />

            <input
                value={form.password}
                onChange={set('password')}
                type="password"
                placeholder="Password (min 8 characters)"
                className={input}
            />

            {/* Consent */}
            <label className="flex items-start gap-2 text-sm text-gray-700">
                <input
                    type="checkbox"
                    checked={form.consent}
                    onChange={set('consent')}
                    className="mt-1"
                />

                I agree to my name and phone number being stored
                to manage my appointments.
            </label>

            {/* Error message */}
            {error && (
                <p className="text-sm text-red-700">
                    {error}
                </p>
            )}

            {/* Register button */}
            <button
                disabled={busy}
                className="w-full rounded bg-blue-700 p-2 font-medium text-white disabled:opacity-60"
            >
                {busy ? 'Creating...' : 'Register'}
            </button>

            <p className="text-sm text-gray-600">
                Already registered?{' '}
                <Link
                    to="/login"
                    className="text-blue-700"
                >
                    Login
                </Link>
            </p>

        </form>
    )
}