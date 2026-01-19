import { httpClient } from './httpClient'

export const fetchNotifications = () => httpClient.get('/api/notifications')

export const fetchUnreadCount = () => httpClient.get('/api/notifications/unread-count')

export const markNotificationRead = (id) => httpClient.patch(`/api/notifications/${id}/read`)

export const markAllNotificationsRead = () => httpClient.patch('/api/notifications/read-all')
