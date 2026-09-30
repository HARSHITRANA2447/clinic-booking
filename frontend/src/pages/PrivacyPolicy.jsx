export default function PrivacyPolicy() {
    return (
        <div className="prose prose-sm max-w-none">
            <h1 className="text-xl font-semibold">Privacy Policy</h1>
            <p className="text-sm text-gray-600">Last updated: {new Date().toLocaleDateString('en-IN')}</p>

            <h2 className="mt-4 font-semibold">What we collect</h2>
            <p className="text-sm">Your name, phone number, and optionally your email, when you register or book an appointment.</p>

            <h2 className="mt-4 font-semibold">Why we collect it</h2>
            <p className="text-sm">To create your account, confirm bookings, and let clinic staff contact you about your appointment.</p>

            <h2 className="mt-4 font-semibold">Who can see it</h2>
            <p className="text-sm">Staff at the clinic you book with can see your name and phone number for that booking. We do not sell or share your data with anyone else.</p>

            <h2 className="mt-4 font-semibold">Your rights</h2>
            <p className="text-sm">You can request deletion of your account and data at any time from your account settings, or by contacting us.</p>

            <h2 className="mt-4 font-semibold">Contact</h2>
            <p className="text-sm">For privacy questions, contact support@yourapp.com.</p>
        </div>
    )
}