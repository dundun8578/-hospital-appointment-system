import request from './request'

// ---------- 用户 ----------
export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)

// ---------- 医院浏览 ----------
export const getHospitals = () => request.get('/hospital/list')
export const getDepartments = (hospitalId) => request.get(`/department/list/${hospitalId}`)
export const getDoctors = (departmentId) => request.get(`/doctor/list/${departmentId}`)
export const getDoctorSchedule = (doctorId) => request.get(`/schedule/doctor/${doctorId}`)

// ---------- 预约 ----------
export const createAppointment = (data) => request.post('/appointment/create', data)
export const getMyAppointments = (params) => request.get('/appointment/list', { params })
export const cancelAppointment = (appointmentId, cancelReason) =>
  request.post(`/appointment/${appointmentId}/cancel`, null, { params: { cancelReason } })

// ---------- AI导诊 ----------
export const aiTriage = (symptoms) => request.post('/ai/triage', { symptoms })
