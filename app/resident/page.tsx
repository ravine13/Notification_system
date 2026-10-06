'use client'

import { useEffect, useState } from 'react'
import { Users, Plus, Pencil, Trash2, X, Loader2 } from 'lucide-react'

const API_URL = 'http://localhost:9123'

type Resident = {
    id: number
    name: string
    phoneNumber: string
    address: string | null
    zone: { id: number }
    createdAt: string
    updatedAt: string
}

export default function RoutesResidents() {
    const [residents, setResidents] = useState<Resident[]>([])
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)

    const [showForm, setShowForm] = useState(false)
    const [editingResident, setEditingResident] = useState<Resident | null>(null)

    const [name, setName] = useState('')
    const [phoneNumber, setPhoneNumber] = useState('')
    const [address, setAddress] = useState('')
    const [zoneId, setZoneId] = useState('')

    useEffect(() => {
        fetchResidents()
    }, [])

    async function fetchResidents() {
        setLoading(true)
        try {
            const res = await fetch(`${API_URL}/residents`,{
                credentials: "include",
            })
            if (!res.ok) throw new Error('Failed to fetch residents')
            const data = await res.json()
            setResidents(data)
        } catch (err) {
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    function resetForm() {
        setName('')
        setPhoneNumber('')
        setAddress('')
        setZoneId('')
        setEditingResident(null)
        setShowForm(false)
    }

    function startEdit(resident: Resident) {
        setEditingResident(resident)
        setName(resident.name)
        setPhoneNumber(resident.phoneNumber)
        setAddress(resident.address ?? '')
        setZoneId(String(resident.zone.id))
        setShowForm(true)
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setSaving(true)

        // zone is a @ManyToOne relation on the backend, so it needs to be
        // sent as a nested object with an id, not a flat zoneId field.
        const payload = {
            name,
            phoneNumber,
            address: address || null,
            zone: { id: Number(zoneId) },
        }

        try {
            const url = editingResident
                ? `${API_URL}/residents/${editingResident.id}`
                : `${API_URL}/residents`
            const method = editingResident ? 'PUT' : 'POST'

            const res = await fetch(url, {
                method,
                credentials: "include",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })

            if (!res.ok) throw new Error('Failed to save resident')

            await fetchResidents()
            resetForm()
        } catch (err) {
            console.error(err)
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id: number) {
        if (!confirm('Delete this resident?')) return

        try {
            const res = await fetch(`${API_URL}/residents/${id}`, {
                method: 'DELETE',
                credentials: 'include',
            })
            if (!res.ok) throw new Error('Failed to delete resident')
            setResidents((prev) => prev.filter((r) => r.id !== id))
        } catch (err) {
            console.error(err)
        }
    }

    // CHANGED: every class below was swapped from hardcoded light colors
    // (bg-white/bg-gray-50/bg-black/text-gray-*) to the app's semantic theme
    // classes (bg-card/bg-background/text-foreground/text-muted-foreground)
    // so this card follows dark mode instead of always rendering bright white.
    return (
        <div className="p-6 max-w-3xl mx-auto text-foreground">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                    <Users className="w-5 h-5" />
                    <h1 className="text-xl font-semibold">Residents</h1>
                </div>
                <button
                    onClick={() => {
                        resetForm()
                        setShowForm(true)
                    }}
                    className="flex items-center gap-1 bg-[#173a2e] text-white px-3 py-2 rounded-lg text-sm hover:bg-[#245843]"
                >
                    <Plus className="w-4 h-4" />
                    New Resident
                </button>
            </div>

            {showForm && (
                <form
                    onSubmit={handleSubmit}
                    className="border rounded-lg p-4 mb-6 space-y-3 bg-card"
                >
                    <div className="flex items-center justify-between">
                        <h2 className="font-medium">
                            {editingResident ? 'Edit Resident' : 'New Resident'}
                        </h2>
                        <button type="button" onClick={resetForm}>
                            <X className="w-4 h-4" />
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
                                className="w-full border rounded-md px-3 py-2 text-sm bg-background text-foreground"
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
                                className="w-full border rounded-md px-3 py-2 text-sm bg-background text-foreground"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Zone ID</label>
                            <input
                                type="number"
                                value={zoneId}
                                onChange={(e) => setZoneId(e.target.value)}
                                required
                                className="w-full border rounded-md px-3 py-2 text-sm bg-background text-foreground"
                            />
                        </div>
                        <div className="col-span-2">
                            <label className="text-sm text-muted-foreground">Address</label>
                            <input
                                type="text"
                                value={address}
                                onChange={(e) => setAddress(e.target.value)}
                                className="w-full border rounded-md px-3 py-2 text-sm bg-background text-foreground"
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={saving}
                        className="flex items-center gap-2 bg-[#173a2e] text-white px-4 py-2 rounded-lg text-sm hover:bg-[#245843] disabled:opacity-50"
                    >
                        {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                        {editingResident ? 'Save Changes' : 'Create Resident'}
                    </button>
                </form>
            )}

            {loading ? (
                <div className="flex items-center gap-2 text-muted-foreground text-sm">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Loading residents...
                </div>
            ) : residents.length === 0 ? (
                <p className="text-sm text-muted-foreground">No residents yet.</p>
            ) : (
                <div className="space-y-2">
                    {residents.map((r) => (
                        <div
                            key={r.id}
                            className="flex items-center justify-between border rounded-lg px-4 py-3 bg-card"
                        >
                            <div>
                                <p className="text-sm font-medium">{r.name}</p>
                                <p className="text-xs text-muted-foreground">
                                    {r.phoneNumber} · Zone #{r.zone?.id}
                                    {r.address ? ` · ${r.address}` : ''}
                                </p>
                            </div>
                            <div className="flex items-center gap-2">
                                <button onClick={() => startEdit(r)}>
                                    <Pencil className="w-4 h-4 text-muted-foreground hover:text-foreground" />
                                </button>
                                <button onClick={() => handleDelete(r.id)}>
                                    <Trash2 className="w-4 h-4 text-muted-foreground hover:text-red-500" />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}