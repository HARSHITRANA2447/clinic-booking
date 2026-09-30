import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DoctorPage from '../pages/DoctorPage'
import client from '../api/client'
import { useAuth } from '../context/AuthContext'

vi.mock('../context/AuthContext', () => ({ useAuth: vi.fn() }))
vi.mock('../api/client', () => ({
    default: { get: vi.fn(), post: vi.fn() },
    errorMessage: (e) => e.response?.data?.message ?? 'Something went wrong.',
}))

const doctor = {
    doctorId: 1, doctorName: 'Dr. Test', specialization: 'General Physician',
    qualification: 'MBBS', consultationFee: 400, clinicId: 1,
    clinicName: 'Test Clinic', address: 'Madhapur', crowdLevel: 'LOW',
}
const slots = [
    { id: 11, startTime: '2026-09-29T09:00:00', endTime: '2026-09-29T09:15:00' },
    { id: 12, startTime: '2026-09-29T09:15:00', endTime: '2026-09-29T09:30:00' },
]

function renderPage() {
    render(
        <MemoryRouter initialEntries={['/doctors/1']}>
            <Routes>
                <Route path="/doctors/:id" element={<DoctorPage />} />
                <Route path="/booking-confirmed" element={<p>confirmed page</p>} />
                <Route path="/login" element={<p>login page</p>} />
            </Routes>
        </MemoryRouter>
    )
    return userEvent.setup()
}

describe('DoctorPage booking flow', () => {
    beforeEach(() => {
        client.get.mockReset()
        client.post.mockReset()
        client.get.mockImplementation((url) =>
            Promise.resolve({ data: url.endsWith('/slots') ? slots : doctor }))
    })

    it('shows the doctor and lets a logged-out user pick a slot, then asks them to log in', async () => {
        useAuth.mockReturnValue({ isLoggedIn: false })
        const user = renderPage()

        expect(await screen.findByText('Dr. Test')).toBeInTheDocument()
        await user.click(await screen.findByRole('button', { name: /9:00/ }))
        await user.click(screen.getByRole('button', { name: 'Login to book this slot' }))

        expect(client.post).not.toHaveBeenCalled()
        expect(screen.getByText('login page')).toBeInTheDocument()
    })

    it('books the selected slot for a logged-in user', async () => {
        useAuth.mockReturnValue({ isLoggedIn: true })
        client.post.mockResolvedValue({ data: { bookingId: 5 } })
        const user = renderPage()

        await user.click(await screen.findByRole('button', { name: /9:15/ }))
        await user.click(screen.getByRole('button', { name: /^Book / }))

        expect(client.post).toHaveBeenCalledWith('/api/bookings', { slotId: 12 })
        expect(await screen.findByText('confirmed page')).toBeInTheDocument()
    })

    it('shows the server error and refreshes slots when the slot was just taken', async () => {
        useAuth.mockReturnValue({ isLoggedIn: true })
        client.post.mockRejectedValue({
            response: { status: 409, data: { message: 'This slot is no longer available' } },
        })
        const user = renderPage()

        await user.click(await screen.findByRole('button', { name: /9:00/ }))
        const slotFetchesBefore = client.get.mock.calls.filter(([u]) => u.endsWith('/slots')).length
        await user.click(screen.getByRole('button', { name: /^Book / }))

        expect(await screen.findByText('This slot is no longer available')).toBeInTheDocument()
        // Booking button is gone (selection cleared) and the slot list was re-fetched
        expect(screen.queryByRole('button', { name: /^Book / })).not.toBeInTheDocument()
        const slotFetchesAfter = client.get.mock.calls.filter(([u]) => u.endsWith('/slots')).length
        expect(slotFetchesAfter).toBeGreaterThan(slotFetchesBefore)
    })
})