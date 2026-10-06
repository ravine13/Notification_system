'use client'

import { useEffect, useState } from 'react'
import { Map, Plus, Pencil, Trash2, X, Loader2 } from 'lucide-react'

const API_URL = 'http://localhost:9123'

type Zone = {
    id: number
    name: string
    description: string | null
    collectionDay: string | null
    createdAt: string
    updatedAt: string
}

export default function RoutesZones() {
    const [zones, setZones] = useState<Zone[]>([])
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)

    const [showForm, setShowForm] = useState(false)
    const [editingZone, setEditingZone] = useState<Zone | null>(null)

    const [zoneName, setZoneName] = useState('')
    const [description, setDescription] = useState('')
    const [collectionDay, setCollectionDay] = useState('')

    useEffect(() => {
        fetchZones()
    }, [])

    async function fetchZones() {
        setLoading(true)
        try {
            const res = await fetch(`${API_URL}/zones`, {
                credentials: "include",
            })
            if (!res.ok) throw new Error('Failed to fetch zones')
            const data = await res.json()
            setZones(data)
        } catch (err) {
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    function resetForm() {
        setZoneName('')
        setDescription('')
        setCollectionDay('')
        setEditingZone(null)
        setShowForm(false)
    }

    function startEdit(zone: Zone) {
        setEditingZone(zone)
        setZoneName(zone.name)
        setDescription(zone.description ?? '')
        setCollectionDay(zone.collectionDay ?? '')
        setShowForm(true)
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setSaving(true)

        const payload = {
            name: zoneName,
            description: description || null,
            collectionDay: collectionDay || null,
        }

        try {
            const url = editingZone
                ? `${API_URL}/zones/${editingZone.id}`
                : `${API_URL}/zones`
            const method = editingZone ? 'PUT' : 'POST'

            const res = await fetch(url, {
                method,
                credentials: "include",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })

            if (!res.ok) {
                const text = await res.text()
                console.error('Save zone failed:', res.status, text)
                throw new Error(`Failed to save zone: ${res.status} ${text}`)
            }

            await fetchZones()
            resetForm()
        } catch (err) {
            console.error(err)
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id: number) {
        if (!confirm('Delete this zone?')) return

        try {
            const res = await fetch(`${API_URL}/zones/${id}`, {
                method: 'DELETE',
                credentials: "include",
            })
            if (!res.ok) throw new Error('Failed to delete zone')
            setZones((prev) => prev.filter((z) => z.id !== id))
        } catch (err) {
            console.error(err)
        }
    }

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-2">
                    <Map className="w-5 h-5 text-foreground" />
                    <h1 className="text-xl font-semibold text-foreground">Zones</h1>
                </div>
                <button
                    onClick={() => {
                        resetForm()
                        setShowForm(true)
                    }}
                    className="flex items-center gap-1 bg-[#173a2e] text-white px-3 py-2 rounded-lg text-sm hover:bg-[#0f2a20]"
                >
                    <Plus className="w-4 h-4" />
                    New Zone
                </button>
            </div>

            {showForm && (
                <form
                    onSubmit={handleSubmit}
                    className="border border-border rounded-lg p-4 mb-6 space-y-3 bg-card"
                >
                    <div className="flex items-center justify-between">
                        <h2 className="font-medium text-foreground">
                            {editingZone ? 'Edit Zone' : 'New Zone'}
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
                                value={zoneName}
                                onChange={(e) => setZoneName(e.target.value)}
                                required
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            />
                        </div>
                        <div>
                            <label className="text-sm text-muted-foreground">Collection Day</label>
                            <select
                                value={collectionDay}
                                onChange={(e) => setCollectionDay(e.target.value)}
                                className="w-full border border-border bg-background text-foreground rounded-md px-3 py-2 text-sm"
                            >
                                <option value="">-- Select --</option>
                                <option value="MONDAY">Monday</option>
                                <option value="TUESDAY">Tuesday</option>
                                <option value="WEDNESDAY">Wednesday</option>
                                <option value="THURSDAY">Thursday</option>
                                <option value="FRIDAY">Friday</option>
                                <option value="SATURDAY">Saturday</option>
                                <option value="SUNDAY">Sunday</option>
                            </select>
                        </div>
                        <div className="col-span-2">
                            <label className="text-sm text-muted-foreground">Description</label>
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                rows={2}
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
                        {editingZone ? 'Save Changes' : 'Create Zone'}
                    </button>
                </form>
            )}

            {loading ? (
                <div className="flex items-center gap-2 text-muted-foreground text-sm">
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Loading zones...
                </div>
            ) : zones.length === 0 ? (
                <p className="text-sm text-muted-foreground">No zones yet.</p>
            ) : (
                <div className="space-y-2">
                    {zones.map((z) => (
                        <div
                            key={z.id}
                            className="flex items-center justify-between border border-border bg-card rounded-lg px-4 py-3"
                        >
                            <div>
                                <p className="text-sm font-medium text-foreground">{z.name}</p>
                                <p className="text-xs text-muted-foreground">
                                    {z.collectionDay ? z.collectionDay : 'No collection day set'}
                                    {z.description ? ` · ${z.description}` : ''}
                                </p>
                            </div>
                            <div className="flex items-center gap-2">
                                <button onClick={() => startEdit(z)}>
                                    <Pencil className="w-4 h-4 text-muted-foreground hover:text-foreground" />
                                </button>
                                <button onClick={() => handleDelete(z.id)}>
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