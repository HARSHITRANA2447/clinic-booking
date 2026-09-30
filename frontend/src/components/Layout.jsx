import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
    const { isLoggedIn, isStaff, logout } = useAuth()
    const navigate = useNavigate()

    return (
        <div className="min-h-screen bg-gray-50">
            <header className="border-b bg-white">
                <nav className="mx-auto flex max-w-4xl items-center justify-between px-4 py-3">
                    <Link
                        to="/"
                        className="font-bold text-blue-700"
                    >
                        ClinicBook
                    </Link>

                    <div className="flex gap-4 text-sm">
                        {isLoggedIn ? (
                            <>
                                {isStaff ? (
                                    <Link
                                        to="/clinic"
                                        className="text-gray-700"
                                    >
                                        Dashboard
                                    </Link>
                                ) : (
                                    <Link
                                        to="/my-bookings"
                                        className="text-gray-700"
                                    >
                                        My bookings
                                    </Link>
                                )}

                                <button
                                    onClick={() => {
                                        logout()
                                        navigate('/')
                                    }}
                                    className="text-gray-700"
                                >
                                    Logout
                                </button>
                            </>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="text-gray-700"
                                >
                                    Login
                                </Link>

                                <Link
                                    to="/register"
                                    className="font-medium text-blue-700"
                                >
                                    Register
                                </Link>
                            </>
                        )}
                    </div>
                </nav>
            </header>

            <main className="mx-auto max-w-4xl px-4 py-4">
                <Outlet />
            </main>
        </div>
    )
}