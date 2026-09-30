import { fmtTime } from '../utils/format'

export default function SlotGrid({ slots, selectedId, onSelect }) {
    return (
        <div className="grid grid-cols-3 gap-2 sm:grid-cols-4">
            {slots.map((s) => (
                <button
                    key={s.id}
                    onClick={() => onSelect(s)}
                    className={`rounded border p-2 text-sm ${selectedId === s.id
                            ? 'border-blue-700 bg-blue-700 text-white'
                            : 'border-gray-300 bg-white text-gray-800 active:bg-gray-100'
                        }`}
                >
                    {fmtTime(s.startTime)}
                </button>
            ))}
        </div>
    )
}