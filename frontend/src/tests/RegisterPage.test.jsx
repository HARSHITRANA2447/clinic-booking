import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RegisterPage from '../pages/RegisterPage'
import { useAuth } from '../context/AuthContext'

vi.mock('../context/AuthContext', () => ({ useAuth: vi.fn() }))
vi.mock('../api/client', () => ({
    default: {},
    errorMessage: (e) => e.response?.data?.message ?? 'Something went wrong.',
}))

const register = vi.fn()

function setup() {
    useAuth.mockReturnValue({ register })
    render(<MemoryRouter><RegisterPage /></MemoryRouter>)
    return userEvent.setup()
}

async function fill(user, { name = 'Asha', phone = '9876543210', password = 'secret123' } = {}) {
    if (name) await user.type(screen.getByPlaceholderText('Full name'), name)
    if (phone) await user.type(screen.getByPlaceholderText('Mobile number'), phone)
    if (password) await user.type(screen.getByPlaceholderText(/Password/), password)
}

describe('RegisterPage validation', () => {
    beforeEach(() => {
        register.mockReset()
        register.mockResolvedValue({})
    })

    it('rejects an invalid phone number without calling the API', async () => {
        const user = setup()
        await fill(user, { phone: '12345' })
        await user.click(screen.getByRole('checkbox'))
        await user.click(screen.getByRole('button', { name: 'Register' }))

        expect(screen.getByText('Enter a valid 10-digit mobile number')).toBeInTheDocument()
        expect(register).not.toHaveBeenCalled()
    })

    it('rejects a short password', async () => {
        const user = setup()
        await fill(user, { password: 'short' })
        await user.click(screen.getByRole('checkbox'))
        await user.click(screen.getByRole('button', { name: 'Register' }))

        expect(screen.getByText('Password must be at least 8 characters')).toBeInTheDocument()
        expect(register).not.toHaveBeenCalled()
    })

    it('requires consent', async () => {
        const user = setup()
        await fill(user)
        await user.click(screen.getByRole('button', { name: 'Register' }))

        expect(screen.getByText('Please accept the consent to continue')).toBeInTheDocument()
        expect(register).not.toHaveBeenCalled()
    })

    it('submits when everything is valid', async () => {
        const user = setup()
        await fill(user)
        await user.click(screen.getByRole('checkbox'))
        await user.click(screen.getByRole('button', { name: 'Register' }))

        expect(register).toHaveBeenCalledTimes(1)
        expect(register.mock.calls[0][0]).toMatchObject({
            name: 'Asha', phone: '9876543210', consent: true, email: null,
        })
    })
})