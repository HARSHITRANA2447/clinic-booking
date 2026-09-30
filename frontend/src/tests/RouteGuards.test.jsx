import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import ProtectedRoute from '../components/ProtectedRoute'
import StaffRoute from '../components/StaffRoute'
import { useAuth } from '../context/AuthContext'

vi.mock('../context/AuthContext', () => ({ useAuth: vi.fn() }))

function renderAt(path, Guard) {
    render(
        <MemoryRouter initialEntries={[path]}>
            <Routes>
                <Route path="/" element={<p>home page</p>} />
                <Route path="/login" element={<p>login page</p>} />
                <Route path={path} element={<Guard><p>secret page</p></Guard>} />
            </Routes>
        </MemoryRouter>
    )
}

describe('ProtectedRoute', () => {
    it('sends logged-out users to login', () => {
        useAuth.mockReturnValue({ isLoggedIn: false })
        renderAt('/my-bookings', ProtectedRoute)
        expect(screen.getByText('login page')).toBeInTheDocument()
    })

    it('shows the page to logged-in users', () => {
        useAuth.mockReturnValue({ isLoggedIn: true })
        renderAt('/my-bookings', ProtectedRoute)
        expect(screen.getByText('secret page')).toBeInTheDocument()
    })
})

describe('StaffRoute', () => {
    it('sends logged-out users to login', () => {
        useAuth.mockReturnValue({ isLoggedIn: false, isStaff: false })
        renderAt('/clinic', StaffRoute)
        expect(screen.getByText('login page')).toBeInTheDocument()
    })

    it('sends patients home', () => {
        useAuth.mockReturnValue({ isLoggedIn: true, isStaff: false })
        renderAt('/clinic', StaffRoute)
        expect(screen.getByText('home page')).toBeInTheDocument()
    })

    it('lets staff in', () => {
        useAuth.mockReturnValue({ isLoggedIn: true, isStaff: true })
        renderAt('/clinic', StaffRoute)
        expect(screen.getByText('secret page')).toBeInTheDocument()
    })
})