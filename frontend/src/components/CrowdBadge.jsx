const STYLES = {
    LOW: 'bg-green-100 text-green-800',
    MEDIUM: 'bg-yellow-100 text-yellow-800',
    HIGH: 'bg-red-100 text-red-800',
    UNKNOWN: 'bg-gray-100 text-gray-600',
}
const LABELS = { LOW: 'Not busy', MEDIUM: 'Moderately busy', HIGH: 'Very busy', UNKNOWN: 'No data' }

export default function CrowdBadge({ level }) {
    return (
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STYLES[level] || STYLES.UNKNOWN}`}>
            {LABELS[level] || LABELS.UNKNOWN}
        </span>
    )
}