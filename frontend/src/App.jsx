import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import SearchPage from './pages/SearchPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DoctorPage from './pages/DoctorPage'
import BookingConfirmation from './pages/BookingConfirmation'
import MyBookings from './pages/MyBookings'
import StaffRoute from './components/StaffRoute'
import ClinicLayout from './pages/clinic/ClinicLayout'
import QueuePage from './pages/clinic/QueuePage'
import SlotsPage from './pages/clinic/SlotsPage'
import DoctorsPage from './pages/clinic/DoctorsPage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/clinic" element={<StaffRoute><ClinicLayout /></StaffRoute>}>
          <Route index element={<QueuePage />} />
          <Route path="slots" element={<SlotsPage />} />
          <Route path="doctors" element={<DoctorsPage />} />
        </Route>
        <Route path="/" element={<SearchPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/doctors/:id" element={<DoctorPage />} />
        <Route path="/booking-confirmed" element={
          <ProtectedRoute><BookingConfirmation /></ProtectedRoute>} />
        <Route path="/my-bookings" element={
          <ProtectedRoute><MyBookings /></ProtectedRoute>} />
        <Route path="*" element={<p>Page not found.</p>} />
      </Route>
    </Routes>
  )
}