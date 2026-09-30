// Slot times are IST wall-clock strings like "2026-09-29T09:00:00" (no timezone).
// Parsing without a "Z" keeps them as-is for display.
export const fmtTime = (iso) =>
    new Date(iso).toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit', hour12: true })

export const fmtDate = (iso) =>
    new Date(iso).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' })

export const toISODate = (d) => {
    const p = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

export const nextDays = (count = 7) =>
    Array.from({ length: count }, (_, i) => {
        const d = new Date()
        d.setDate(d.getDate() + i)
        return d
    })