'use client'

import { useEffect, useState } from 'react'
import { UserCog, Plus, Pencil, Trash2, X, Loader2 } from 'lucide-react'

const API_URL = 'http://localhost:9123'

type Role = 'ADMIN' | 'STAFF'

type User = {
    id: number
    name: string
    email: string
    role: Role
    createdAt: string
    updatedAt: string
    // password is intentionally absent: the backend never sends it back
}

export default function RoutesUsers() {
    const [currentUser, setCurrentUser] = useState<User | null>(null)
    const [authChecked, setAuthChecked] = useState(false)

    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState('')

    const [showForm, setShowForm] = useState(false)
    const [editingUser, setEditingUser] = useState<User | null>(null)

    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [role, setRole] = useState<Role>('STAFF')

    const isAdmin = currentUser?.role === 'ADMIN'
    const canEdit = (u: User) => isAdmin || u.id === currentUser?.id
    const canDelete = (u: User) => isAdmin && u.id !== currentUser?.id

    // 1. Find out who is logged in (the JWT cookie is HttpOnly, so we ask the backend)
    useEffect(() => {
        async function loadMe() {
            try {
                const res = await fetch(`${API_URL}/users/me`, { credentials: 'include' })
                setCurrentUser(res.ok ? await res.json() : null)
            } catch (err) {
                console.error(err)
                setCurrentUser(null)
            } finally {
                setAuthChecked(true)
            }
        }
        loadMe()
    }, [])

    // 2. Once we know who they are, load the right data
    useEffect(() => {
        if (currentUser) fetchUsers()
        else if (authChecked) setLoading(false)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [currentUser, authChecked])

    async function fetchUsers() {
        setLoading(true)
        setError('')
        try {
            // Admins see everyone, staff only see themselves
            const url = isAdmin ? `${API_URL}/users` : `${API_URL}/users/me`
            const res = await fetch(url, { credentials: 'include' })
            if (!res.ok) throw new Error('Failed to fetch users')
            const data = await res.json()
            setUsers(isAdmin ? data : [data])
        } catch (err) {
            console.error(err)
            setError('Could not load users.')
        } finally {
            setLoading(false)
        }
    }

    function resetForm() {
        setName('')
        setEmail('')
        setPassword('')
        setRole('STAFF')
        setEditingUser(null)
        setShowForm(false)
    }

    function startEdit(user: User) {
        setEditingUser(user)
        setName(user.name)
        setEmail(user.email)
        setPassword('') // never prefilled
        setRole(user.role)
        setShowForm(true)
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setSaving(true)
        setError('')

        // Role is only ever sent when creating a user, and the backend ignores it on update.
        const payload: Record<string, unknown> = { name }
        if (!editingUser) {
            payload.email = email
            payload.role = role
            payload.password = password
        } else {
            if (isAdmin) payload.email = email
            if (password.trim() !== '') payload.password = password
        }

        try {
            const url = editingUser
                ? `${API_URL}/users/${editingUser.id}`
                : `${API_URL}/users`
            const method = editingUser ? 'PUT' : 'POST'

            const res = await fetch(url, {
                method,
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })

            if (!res.ok) throw new Error('Failed to save user')

            // If the user edited their own profile, refresh the logged-in user too
            if (editingUser && editingUser.id === currentUser?.id) {
                const me = await fetch(`${API_URL}/users/me`, { credentials: 'include' })
                if (me.ok) setCurrentUser(await me.json())
            }

            await fetchUsers()
            resetForm()
        } catch (err) {
            console.error(err)
            setError('Could not save changes.')
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id: number) {
        if (!confirm('Delete this user?')) return

        try {
            const res = await fetch(`${API_URL}/users/${id}`, {
                method: 'DELETE',
                credentials: 'include',
            })
            if (!res.ok) throw new Error('Failed to delete user')
            setUsers((prev) => prev.filter((u) => u.id !== id))
        } catch (err) {
            console.error(err)
            setError('Could not delete user.')
        }
    }

    function roleColor(role: Role) {
        return role === 'ADMIN'
            ? 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400'
            : 'bg-gray-100 text-gray-700 dark:bg-gray-700/40 dark:text-gray-300'
    }

    if (authChecked && !currentUser) {
        return (
            <div className="p-6 max-w-3xl mx-auto text-sm text-muted-foreground">
                Please log in to view this page.
            </div>
        )
    }

    // Staff can't change their own email; admins can change others' emails.
    // Editing your own email is blocked for everyone, because your login token is tied to it.
    const emailLocked =
        !!editingUser && (!isAdmin || editingUser.id === currentUser?.id)

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                    <UserCog className="w-5 h-5 text-foreground" />
                    <h1 className="text-xl font-semibold text-foreground">
                        {isAdmin ? 'Users' : 'My Profile'}
                    </h1>
                </div>
                {isAdmin && (
                    <button
                        onClick={() => {
                            resetForm()
                            setShowForm(true)
                        }}
                        className="flex items-center gap-1 bg-[#173a2e] text-white px-3 py-2 rounded-lg text-sm hover:bg-[#0f2a20]"
                    >
                        <Plus className="w-4 h-4" />
                        New User
                    </button>
                )}
            </div>

            {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

            {showForm && (
                <form
                    onSubmit={handleSubmit}
                    className="border border-border rounded-lg p-4 mb-6 space-y-3 bg-card"
                >
                    <div className="flex items-center justify-between">
                        <h2 className="font-medium text-foreground">
                            {editingUser ? 'Edit User' : 'New User'}
                        </h2>
                        <button type="button" onClick={resetForm}>
                            <X className="w-4 h-4 text-muted-foreground hover:text-foreground" />
                        </button>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="col-span-2">
                            <label className="text-sm text-muted-foreground">Name</label>
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div className="col-span-2">
                            <label className="text-sm text-muted-foreground">Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                disabled={emailLocked}
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm disabled:opacity-60"
                            />
                        </div>
                        <div className="col-span-2">
                            <label className="text-sm text-muted-foreground">
                                {editingUser ? 'New Password (leave blank to keep current)' : 'Password'}
                            </label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required={!editingUser}
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Role</label>
                            {editingUser ? (
                                <p>
                                    <span
                                        className={`inline-block mt-1 text-xs px-2 py-0.5 rounded-full ${roleColor(
                                            editingUser.role
                                        )}`}
                                    >
                                        {editingUser.role}
                                    </span>
                                </p>
                            ) : (
                                <select
                                    value={role}
                                    onChange={(e) => setRole(e.target.value as Role)}
                                    className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                                >
                                    <option value="STAFF">Staff</option>
                                    <option value="ADMIN">Admin</option>
                                </select>
                            )}
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={saving}
                        className="flex items-center gap-2 bg-[#173a2e] text-white px-4 py-2 rounded-lg text-sm hover:bg-[#0f2a20] disabled:opacity-50"
                    >
                        {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                        {editingUser ? 'Save Changes' : 'Create User'}
                    </button>
                </form>
            )}

            {loading ? (
                <div className="flex items-center gap-2 text-muted-foreground text-sm">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Loading...
                </div>
            ) : users.length === 0 ? (
                <p className="text-sm text-muted-foreground">No users yet.</p>
            ) : (
                <div className="space-y-2">
                    {users.map((u) => (
                        <div
                            key={u.id}
                            className="flex items-center justify-between border border-border bg-card rounded-lg px-4 py-3"
                        >
                            <div>
                                <p className="text-sm font-medium text-foreground">{u.name}</p>
                                <p className="text-xs text-muted-foreground">{u.email}</p>
                                <span
                                    className={`inline-block mt-1 text-xs px-2 py-0.5 rounded-full ${roleColor(
                                        u.role
                                    )}`}
                                >
                                    {u.role}
                                </span>
                            </div>
                            <div className="flex items-center gap-2">
                                {canEdit(u) && (
                                    <button onClick={() => startEdit(u)}>
                                        <Pencil className="w-4 h-4 text-muted-foreground hover:text-foreground" />
                                    </button>
                                )}
                                {canDelete(u) && (
                                    <button onClick={() => handleDelete(u.id)}>
                                        <Trash2 className="w-4 h-4 text-muted-foreground hover:text-red-600" />
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}