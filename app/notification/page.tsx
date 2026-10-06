'use client'

import { useEffect, useState } from 'react'
import { Bell, Plus, Pencil, Trash2, X, Loader2 } from 'lucide-react'

const API_URL = 'http://localhost:9123'

type Status = 'PENDING' | 'SENT' | 'FAILED'

type Notification = {
    id: number
    schedule: { id: number }
    resident: { id: number }
    phoneNumber: string
    message: string
    status: Status
    sentAt: string | null
    createdAt: string
}

export default function RoutesNotifications() {
    const [notifications, setNotifications] = useState<Notification[]>([])
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)

    const [showForm, setShowForm] = useState(false)
    const [editingNotification, setEditingNotification] = useState<Notification | null>(null)

    const [scheduleId, setScheduleId] = useState('')
    const [residentId, setResidentId] = useState('')
    const [phoneNumber, setPhoneNumber] = useState('')
    const [message, setMessage] = useState('')
    const [status, setStatus] = useState<Status>('PENDING')
    const [sentAt, setSentAt] = useState('')

    useEffect(() => {
        fetchNotifications()
    }, [])

    async function fetchNotifications() {
        setLoading(true)
        try {
            const res = await fetch(`${API_URL}/notifications`,{
                credentials: "include",
            })
            if (!res.ok) throw new Error('Failed to fetch notifications')
            const data = await res.json()
            setNotifications(data)
        } catch (err) {
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    function resetForm() {
        setScheduleId('')
        setResidentId('')
        setPhoneNumber('')
        setMessage('')
        setStatus('PENDING')
        setSentAt('')
        setEditingNotification(null)
        setShowForm(false)
    }

    function startEdit(notification: Notification) {
        setEditingNotification(notification)
        setScheduleId(String(notification.schedule.id))
        setResidentId(String(notification.resident.id))
        setPhoneNumber(notification.phoneNumber)
        setMessage(notification.message)
        setStatus(notification.status)
        setSentAt(notification.sentAt ? notification.sentAt.slice(0, 16) : '')
        setShowForm(true)
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setSaving(true)

        // Nested objects are required here because schedule/resident are
        // @ManyToOne relations on the backend, not flat foreign key fields.
        const payload = {
            schedule: { id: Number(scheduleId) },
            resident: { id: Number(residentId) },
            phoneNumber,
            message,
            status,
            sentAt: sentAt ? new Date(sentAt).toISOString() : null,
        }

        try {
            const url = editingNotification
                ? `${API_URL}/notifications/${editingNotification.id}`
                : `${API_URL}/notifications`
            const method = editingNotification ? 'PUT' : 'POST'

            const res = await fetch(url, {
                method,
                credentials: "include",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })

            if (!res.ok) throw new Error('Failed to save notification')

            await fetchNotifications()
            resetForm()
        } catch (err) {
            console.error(err)
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id: number) {
        if (!confirm('Delete this notification?')) return

        try {
            const res = await fetch(`${API_URL}/notifications/${id}`, {
                method: 'DELETE',
                credentials: 'include',
            })
            if (!res.ok) throw new Error('Failed to delete notification')
            setNotifications((prev) => prev.filter((n) => n.id !== id))
        } catch (err) {
            console.error(err)
        }
    }

    function statusColor(status: Status) {
        switch (status) {
            case 'SENT':
                return 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
            case 'FAILED':
                return 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
            default:
                return 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400'
        }
    }

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                    <Bell className="w-5 h-5 text-foreground" />
                    <h1 className="text-xl font-semibold text-foreground">Notifications</h1>
                </div>
                <button
                    onClick={() => {
                        resetForm()
                        setShowForm(true)
                    }}
                    className="flex items-center gap-1 bg-[#173a2e] text-white px-3 py-2 rounded-lg text-sm hover:bg-[#0f2a20]"
                >
                    <Plus className="w-4 h-4" />
                    New Notification
                </button>
            </div>

            {showForm && (
                <form
                    onSubmit={handleSubmit}
                    className="border border-border rounded-lg p-4 mb-6 space-y-3 bg-card"
                >
                    <div className="flex items-center justify-between">
                        <h2 className="font-medium text-foreground">
                            {editingNotification ? 'Edit Notification' : 'New Notification'}
                        </h2>
                        <button type="button" onClick={resetForm}>
                            <X className="w-4 h-4 text-muted-foreground hover:text-foreground" />
                        </button>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div>
                            <label className="text-sm text-muted-foreground">Schedule ID</label>
                            <input
                                type="number"
                                value={scheduleId}
                                onChange={(e) => setScheduleId(e.target.value)}
                                required
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Resident ID</label>
                            <input
                                type="number"
                                value={residentId}
                                onChange={(e) => setResidentId(e.target.value)}
                                required
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Phone Number</label>
                            <input
                                type="text"
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(e.target.value)}
                                required
                                placeholder="+254700000000"
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Status</label>
                            <select
                                value={status}
                                onChange={(e) => setStatus(e.target.value as Status)}
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            >
                                <option value="PENDING">Pending</option>
                                <option value="SENT">Sent</option>
                                <option value="FAILED">Failed</option>
                            </select>
                        </div>
                        <div className="col-span-2">
                            <label className="text-sm text-muted-foreground">Message</label>
                            <textarea
                                value={message}
                                onChange={(e) => setMessage(e.target.value)}
                                required
                                rows={3}
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Sent At</label>
                            <input
                                type="datetime-local"
                                value={sentAt}
                                onChange={(e) => setSentAt(e.target.value)}
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={saving}
                        className="flex items-center gap-2 bg-[#173a2e] text-white px-4 py-2 rounded-lg text-sm hover:bg-[#0f2a20] disabled:opacity-50"
                    >
                        {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                        {editingNotification ? 'Save Changes' : 'Create Notification'}
                    </button>
                </form>
            )}

            {loading ? (
                <div className="flex items-center gap-2 text-muted-foreground text-sm">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Loading notifications...
                </div>
            ) : notifications.length === 0 ? (
                <p className="text-sm text-muted-foreground">No notifications yet.</p>
            ) : (
                <div className="space-y-2">
                    {notifications.map((n) => (
                        <div
                            key={n.id}
                            className="flex items-center justify-between border border-border bg-card rounded-lg px-4 py-3"
                        >
                            <div>
                                <p className="text-sm font-medium text-foreground">
                                    {n.phoneNumber} · Resident #{n.resident?.id}
                                </p>
                                <p className="text-xs text-muted-foreground truncate max-w-md">
                                    {n.message}
                                </p>
                                <span
                                    className={`inline-block mt-1 text-xs px-2 py-0.5 rounded-full ${statusColor(
                                        n.status
                                    )}`}
                                >
                                    {n.status}
                                </span>
                            </div>
                            <div className="flex items-center gap-2">
                                <button onClick={() => startEdit(n)}>
                                    <Pencil className="w-4 h-4 text-muted-foreground hover:text-foreground" />
                                </button>
                                <button onClick={() => handleDelete(n.id)}>
                                    <Trash2 className="w-4 h-4 text-muted-foreground hover:text-red-600" />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}