import axios from 'axios'

const client = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 30000,
})

client.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

client.interceptors.response.use(
    (res) => res,
    (err) => {
        // Expired or invalid token: clear it so the UI falls back to logged-out
        if (err.response?.status === 401 && localStorage.getItem('token')) {
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            window.dispatchEvent(new Event('auth-expired'))
        }
        return Promise.reject(err)
    }
)

export const errorMessage = (err) =>
    err.response?.data?.message ||
    (err.code === 'ECONNABORTED' ? 'Request timed out. Check your connection.' : null) ||
    (err.request && !err.response ? 'Cannot reach the server.' : 'Something went wrong.')

export default client