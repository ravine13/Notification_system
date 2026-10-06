'use client'

import { useEffect, useState } from 'react'
import {
  Bell, CalendarDays, Check, FileText, Home,
  Loader2, LogIn, LogOut, Map, Menu, Moon, Plus,
  Recycle, Search, ShieldCheck, Sun,UserCog, Users, X,
} from 'lucide-react'


import RoutesZones from './zones/page'
// import RoutesCollections from './collections/page'
import Collections from '../components/Collections'
// import RoutesSchedules from './schedule/page'
import Schedule from '../components/Schedules'
import RoutesResidents from './resident/page'
import RoutesNotifications from './notification/page'
import Overview from '../components/Overview'
import RoutesUsers from './user/page'


const API_URL = 'http://localhost:9123'

type Role = 'admin' | 'staff'
type View = 'Overview' | 'Collection schedules' | 'Routes & zones' | 'Residents' | 'Notifications' | 'Reports' | 'Users'

type RouteRecord = {
  id: number | string
  name: string
  truck?: string
  status?: string
  time?: string
}

type ToastType = 'success' | 'error' | 'info'
type ToastState = { message: string; type: ToastType } | null

const navItems: { label: View; icon: typeof Home; adminOnly?: boolean }[] = [
  { label: 'Overview', icon: Home },
  { label: 'Collection schedules', icon: CalendarDays },
  { label: 'Routes & zones', icon: Map },
  { label: 'Residents', icon: Users },
  { label: 'Notifications', icon: Bell, adminOnly: true },
  { label: 'Reports', icon: FileText, adminOnly: true },
  {label: 'Users',icon: UserCog},
]

export default function Page() {
  // ---- auth state (real, backend-driven) ----
  const [authenticated, setAuthenticated] = useState(false)
  const [authChecking, setAuthChecking] = useState(true)
  const [role, setRole] = useState<Role>('admin')
  const [userEmail, setUserEmail] = useState('')
  const [userName, setUserName] = useState('')

  // ---- ui state ----
  const [activeNav, setActiveNav] = useState<View>('Overview')
  const [dark, setDark] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [showSchedule, setShowSchedule] = useState(false)
  const [toast, setToast] = useState<ToastState>(null)

  // ---- data from backend (empty until wired up) ----
  const [routes, setRoutes] = useState<RouteRecord[]>([])

  function notify(message: string, type: ToastType = 'success') {
    setToast({ message, type })
    window.setTimeout(() => setToast(null), 2600)
  }

  useEffect(() => {
    async function checkAuthentication() {
      try {
        const response = await fetch(`${API_URL}/api/auth/me`, {
          method: 'GET',
          credentials: 'include',
        })

        if (!response.ok) {
          setAuthenticated(false)
          return
        }

        const data = await response.json()
        const backendRole = String(data.role).toLowerCase()

        if (backendRole !== 'admin' && backendRole !== 'staff') {
          setAuthenticated(false)
          return
        }

        setRole(backendRole as Role)
        setUserName(data.name || '')
        setUserEmail(data.email || '')
        setAuthenticated(true)
      } catch (error) {
        console.error('Authentication check failed:', error)
        setAuthenticated(false)
      } finally {
        setAuthChecking(false)
      }
    }

    checkAuthentication()
  }, [])

  async function logout() {
    try {
      await fetch(`${API_URL}/api/auth/logout`, { method: 'POST', credentials: 'include' })
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      setAuthenticated(false)
      setUserEmail('')
      setUserName('')
      setRole('admin')
      setActiveNav('Overview')
      setSidebarOpen(false)
      setRoutes([])
    }
  }

  const displayInitials = userName
      ? userName.trim().split(/\s+/).map((p) => p[0]).join('').slice(0, 2).toUpperCase()
      : 'U'
  const firstName = userName ? userName.trim().split(/\s+/)[0] : 'there'
  const today = new Date().toLocaleDateString('en-KE', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })

  // ---- checking session ----
  if (authChecking) {
    return (
        <div className="flex min-h-screen items-center justify-center bg-[#173a2e]">
          <div className="flex flex-col items-center gap-3 text-[#d8ed65]">
            <Loader2 className="animate-spin" size={28} />
            <p className="text-sm font-medium text-[#c1d2c7]">Checking your session…</p>
          </div>
        </div>
    )
  }

  // ---- landing page: always the login screen when not authenticated ----
  if (!authenticated) {
    return (
        <LoginScreen
            onLogin={(loggedRole, email, name) => {
              setRole(loggedRole)
              setUserEmail(email)
              setUserName(name)
              setAuthenticated(true)
            }}
        />
    )
  }

  const isAdmin = role === 'admin'
  const visibleNav = navItems.filter((item) => !item.adminOnly || isAdmin)
  const title = activeNav === 'Overview' ? (isAdmin ? 'Admin operations overview' : 'Staff operations workspace') : activeNav

  // CHANGED: these views now render their real, self-contained components
  // directly (same treatment 'Routes & zones' already had), instead of going
  // through the generic DataView card. Each of these components has its own
  // header + create button, so wrapping them in DataView's card would have
  // produced two stacked headers.
  const rendersOwnComponent =
      activeNav === 'Routes & zones' ||
      activeNav === 'Residents' ||
      activeNav === 'Notifications' ||
      activeNav === 'Collection schedules' ||
      activeNav === 'Users'

  return (
      <div className={dark ? 'dark' : ''}>
        <div className="min-h-screen bg-background text-foreground transition-colors">
          <aside className={`fixed inset-y-0 left-0 z-40 flex w-[272px] flex-col bg-[#173a2e] px-5 py-6 text-white transition-transform lg:translate-x-0 ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
            <div className="flex items-center gap-3 px-2">
              <div className="flex size-10 items-center justify-center rounded-xl bg-[#d8ed65] text-[#173a2e]"><Recycle size={23} /></div>
              <div><p className="text-lg font-semibold">Waste Track</p><p className="text-[11px] uppercase tracking-[0.2em] text-[#b6cbbd]">Kenya · Eldoret</p></div>
              <button className="ml-auto lg:hidden" onClick={() => setSidebarOpen(false)} aria-label="Close navigation"><X /></button>
            </div>
            <div className="mt-11 px-2 text-[10px] font-semibold uppercase tracking-[0.2em] text-[#8da99a]">{isAdmin ? 'Administrator' : 'Staff workspace'}</div>
            <nav className="mt-3 flex flex-col gap-1" aria-label="Main navigation">
              {visibleNav.map(({ label, icon: Icon }) => (
                  <button key={label} onClick={() => { setActiveNav(label); setSidebarOpen(false) }} className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm transition ${activeNav === label ? 'bg-[#d8ed65] font-semibold text-[#173a2e]' : 'text-[#c1d2c7] hover:bg-white/10 hover:text-white'}`}>
                    <Icon size={18} /><span>{label === 'Users' && !isAdmin ? 'My Profile' : label}</span>
                  </button>
              ))}
            </nav>
            <div className="mt-auto flex flex-col gap-1 border-t border-white/10 pt-5">
              <button onClick={() => setDark(!dark)} className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-[#c1d2c7] hover:bg-white/10">
                {dark ? <Sun size={18} /> : <Moon size={18} />}{dark ? 'Light mode' : 'Dark mode'}
              </button>
              <button onClick={logout} className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-[#c1d2c7] hover:bg-white/10"><LogOut size={18} />Sign out</button>
              <div className="mt-4 flex items-center gap-3 rounded-xl bg-white/10 p-3">
                <div className="flex size-9 items-center justify-center rounded-full bg-[#f5ad62] text-sm font-bold text-[#173a2e]">{displayInitials}</div>
                <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{userName || 'User'}</p><p className="truncate text-xs text-[#9eb7a9]">{isAdmin ? 'System administrator' : 'Collection staff'}</p></div>
              </div>
            </div>
          </aside>
          {sidebarOpen && <button aria-label="Close navigation overlay" className="fixed inset-0 z-30 bg-black/40 lg:hidden" onClick={() => setSidebarOpen(false)} />}
          <div className="lg:pl-[272px]">
            <header className="flex h-[76px] items-center justify-between border-b bg-card px-5 sm:px-8">
              <div className="flex items-center gap-3">
                <button className="rounded-lg p-2 lg:hidden" onClick={() => setSidebarOpen(true)} aria-label="Open navigation"><Menu /></button>
                <div><p className="text-xs font-medium text-muted-foreground">{today}</p><h1 className="text-xl font-semibold tracking-tight sm:text-2xl">Good morning, {firstName} <span className="text-[#e1a65c]">.</span></h1></div>
              </div>
              <div className="flex items-center gap-3">
                <button className="hidden items-center gap-2 rounded-xl border bg-background px-3 py-2 text-sm text-muted-foreground sm:flex"><Search size={16} />Search</button>
                <button className="relative rounded-xl border bg-background p-2.5 text-muted-foreground" aria-label="Notifications"><Bell size={18} /><span className="absolute right-1.5 top-1.5 size-1.5 rounded-full bg-destructive" /></button>
                <div className="hidden size-9 items-center justify-center rounded-full bg-[#d8ed65] text-xs font-bold text-[#173a2e] sm:flex">{displayInitials}</div>
              </div>
            </header>
            <main className="mx-auto max-w-[1500px] p-5 sm:p-8">
              {/* CHANGED: header row (title + "Create schedule" button) now only
                  shows on Overview, since the other views render their own
                  component with its own header + create button */}
              {!rendersOwnComponent && (
                  <div className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
                    <div><p className="mb-1 text-sm font-medium text-muted-foreground">{isAdmin ? 'Full system access' : 'Read routes and schedules · update field status'}</p><h2 className="text-2xl font-semibold tracking-tight">{title}</h2></div>
                    {isAdmin && activeNav === 'Overview' && <button onClick={() => setShowSchedule(true)} className="flex w-fit items-center gap-2 rounded-xl bg-[#173a2e] px-4 py-3 text-sm font-semibold text-white shadow-lg"><Plus size={17} />Create schedule</button>}
                  </div>
              )}

              {activeNav === 'Overview' && (
                  <Overview
                      isAdmin={isAdmin}
                      onOpenRoutes={() => setActiveNav('Routes & zones')}
                  />
              )}

              {activeNav === 'Routes & zones' && <RoutesZones />}

              {/* CHANGED: Residents and Notifications now render their real
                  components directly instead of falling through to DataView's
                  generic "No data available yet" placeholder */}
              {activeNav === 'Residents' && <RoutesResidents />}

              {activeNav === 'Notifications' && isAdmin && <RoutesNotifications />}

              {/* CHANGED: "Collection schedules" now renders a sub-tab switcher
                  between the Schedules entity and the Collection entity, per your
                  choice to show both rather than pick one */}
              {activeNav === 'Collection schedules' && <CollectionSchedulesView />}

              {activeNav === 'Users' && <RoutesUsers />}

              {activeNav === 'Reports' && (
                  <DataView
                      view={activeNav}
                      isAdmin={isAdmin}
                      routes={routes}
                      onCreate={() => setShowSchedule(true)}
                      onNotify={notify}
                  />
              )}
            </main>
          </div>
          {showSchedule && (
              <ScheduleModal
                  routes={routes}
                  onClose={() => setShowSchedule(false)}
                  onPublish={() => { setShowSchedule(false); notify('Schedules published and residents notified') }}
              />
          )}
          {toast && (
              <div role="status" className="fixed bottom-5 right-5 z-50 flex items-center gap-2 rounded-xl bg-[#173a2e] px-4 py-3 text-sm font-medium text-white shadow-xl">
                {toast.type === 'error' ? <X size={17} className="text-red-300" /> : <Check size={17} className="text-[#d8ed65]" />}
                {toast.message}
              </div>
          )}
        </div>
      </div>
  )
}

/* =========================================================
   COLLECTION SCHEDULES — sub-tab switcher between the Schedules
   entity (zone/date/status) and the Collection entity (per-resident
   pickup record).
   ========================================================= */
function CollectionSchedulesView() {
  const [subTab, setSubTab] = useState<'schedules' | 'collections'>('schedules')

  return (
      <div>
        <div className="mb-5 inline-flex rounded-xl border bg-card p-1">
          <button
              onClick={() => setSubTab('schedules')}
              className={`rounded-lg px-4 py-2 text-sm font-semibold transition ${subTab === 'schedules' ? 'bg-[#173a2e] text-white' : 'text-muted-foreground'}`}
          >
            Schedules
          </button>
          <button
              onClick={() => setSubTab('collections')}
              className={`rounded-lg px-4 py-2 text-sm font-semibold transition ${subTab === 'collections' ? 'bg-[#173a2e] text-white' : 'text-muted-foreground'}`}
          >
            Collections
          </button>
        </div>

        {subTab === 'schedules' ? < Schedule /> : <Collections />}
      </div>
  )
}

/* =========================================================
   LOGIN SCREEN — real fetch to the backend, original visuals
   ========================================================= */
function LoginScreen({ onLogin }: { onLogin: (role: Role, email: string, name: string) => void }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const response = await fetch(`${API_URL}/api/auth/authenticate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username: email, password }),
      })

      if (!response.ok) {
        const message = await response.text()
        throw new Error(message || 'Incorrect username or password')
      }

      const data = await response.json()
      const backendRole = String(data.role).toLowerCase()
      if (backendRole !== 'admin' && backendRole !== 'staff') throw new Error('Invalid user role')

      onLogin(backendRole as Role, email, data.name || '')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
      <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#173a2e] p-5">
        <div className="absolute -left-24 -top-24 size-80 rounded-full bg-[#d8ed65]/15 blur-3xl animate-pulse" />
        <div className="absolute -bottom-32 -right-20 size-96 rounded-full bg-[#f5ad62]/15 blur-3xl animate-pulse" />
        <div className="relative grid w-full max-w-5xl overflow-hidden rounded-3xl bg-[#fbfcf9] shadow-2xl lg:grid-cols-[1fr_0.9fr]">
          <div className="hidden flex-col justify-between bg-[#d8ed65] p-10 text-[#173a2e] lg:flex">
            <div>
              <div className="flex items-center gap-3"><div className="flex size-11 items-center justify-center rounded-xl bg-[#173a2e] text-[#d8ed65]"><Recycle /></div><span className="text-xl font-semibold">Waste Track</span></div>
              <h1 className="mt-20 max-w-sm text-5xl font-semibold leading-[1.05] tracking-tight">Cleaner routes. Stronger communities.</h1>
              <p className="mt-6 max-w-sm text-sm leading-6 text-[#3c5b40]">A single workspace for Eldoret administrators and collection teams.</p>
            </div>
            <p className="text-xs font-semibold uppercase tracking-[0.18em]">Waste Track Kenya · 2026</p>
          </div>
          <form onSubmit={submit} className="p-7 sm:p-12">
            <div className="flex size-11 items-center justify-center rounded-xl bg-[#173a2e] text-[#d8ed65] lg:hidden"><Recycle /></div>
            <p className="mt-8 text-sm font-medium text-[#789086]">Welcome back</p>
            <h2 className="mt-1 text-3xl font-semibold text-[#19332a]">Sign in to Waste Track</h2>
            <p className="mt-3 text-sm text-[#789086]">Enter your credentials to continue.</p>

            {error && (
                <div className="mt-5 flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                  <X size={15} />{error}
                </div>
            )}

            <label className="mt-8 block text-sm font-semibold text-[#19332a]">
              Email address
              <input required type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@wastetrack.ke" className="mt-2 w-full rounded-xl border border-[#dfe6dc] bg-white px-4 py-3 outline-none focus:border-[#6b9d72]" />
            </label>
            <label className="mt-4 block text-sm font-semibold text-[#19332a]">
              Password
              <input required type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" className="mt-2 w-full rounded-xl border border-[#dfe6dc] bg-white px-4 py-3 outline-none focus:border-[#6b9d72]" />
            </label>

            <button disabled={loading} className="mt-6 flex w-full items-center justify-center gap-2 rounded-xl bg-[#173a2e] px-4 py-3.5 font-semibold text-white transition hover:bg-[#245843] disabled:opacity-70">
              {loading ? 'Signing in…' : 'Sign in'}<LogIn size={17} />
            </button>
            <p className="mt-5 text-center text-xs text-[#9aaa9f]">Waste Track Kenya Inc</p>
          </form>
        </div>
      </main>
  )
}

function EmptyState({ icon: Icon, title, subtitle, compact }: { icon: typeof Map; title: string; subtitle: string; compact?: boolean }) {
  return (
      <div className={compact ? 'text-center' : 'w-full rounded-xl bg-muted/60 p-8 text-center'}>
        <Icon className="mx-auto text-muted-foreground" size={compact ? 22 : 26} />
        <p className="mt-3 text-sm font-semibold">{title}</p>
        <p className="mt-1 text-xs text-muted-foreground">{subtitle}</p>
      </div>
  )
}


function DataView({ view, isAdmin }: { view: View; isAdmin: boolean; routes: RouteRecord[]; onCreate: () => void; onNotify: (m: string, t?: ToastType) => void }) {
  const adminOnly = view === 'Notifications' || view === 'Reports'

  const descriptions: Record<View, string> = {
    Overview: '',
    'Collection schedules': 'Create and manage waste collection schedules.',
    'Routes & zones': 'Manage collection routes and zones.',
    Residents: 'Manage resident records and service requests.',
    Notifications: 'View and manage waste collection notifications.',
    Reports: 'View waste management reports and statistics.',
    Users: '',
  }

  return (
      <div className="rounded-2xl border bg-card shadow-sm">
        <div className="flex flex-col justify-between gap-3 border-b p-5 sm:flex-row sm:items-center">
          <div><h3 className="font-semibold">{view}</h3><p className="mt-1 text-xs text-muted-foreground">{descriptions[view]}</p></div>
        </div>

        {adminOnly && !isAdmin ? (
            <div className="p-10 text-center"><ShieldCheck className="mx-auto text-muted-foreground" /><p className="mt-3 font-semibold">Administrator access required</p></div>
        ) : (
            <div className="p-10"><EmptyState icon={FileText} title="No data available yet" subtitle="This section will be connected to the database and backend API." /></div>
        )}
      </div>
  )
}

/* =========================================================
   SCHEDULE MODAL
   ========================================================= */
function ScheduleModal({ routes, onClose, onPublish }: { routes: RouteRecord[]; onClose: () => void; onPublish: () => void }) {
  const [routeId, setRouteId] = useState('')
  const [date, setDate] = useState('')

  function submit(e: React.FormEvent) {
    e.preventDefault()
    // Schedules-creation endpoint isn't wired up yet — hook a POST to
    // `${API_URL}/api/schedules` here once the backend route exists.
    onPublish()
  }

  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
        <form onSubmit={submit} role="dialog" aria-modal="true" className="w-full max-w-md rounded-2xl bg-card p-6 shadow-2xl">
          <div className="flex items-start justify-between">
            <div><h2 className="text-lg font-semibold">Create collection schedule</h2><p className="mt-1 text-sm text-muted-foreground">Publish a route and notify residents.</p></div>
            <button type="button" onClick={onClose} aria-label="Close dialog"><X /></button>
          </div>
          <div className="mt-5 flex flex-col gap-4">
            <label className="text-sm font-medium">
              Collection route
              <select value={routeId} onChange={(e) => setRouteId(e.target.value)} required={routes.length > 0} disabled={routes.length === 0} className="mt-2 w-full rounded-xl border bg-background px-3 py-3 text-sm disabled:opacity-60">
                {routes.length === 0
                    ? <option value="">No routes available</option>
                    : <>
                      <option value="">Select a route</option>
                      {routes.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
                    </>}
              </select>
              {routes.length === 0 && <span className="mt-1.5 block text-xs text-muted-foreground">Add routes to the database before creating a schedule.</span>}
            </label>
            <label className="text-sm font-medium">
              Collection date
              <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required className="mt-2 w-full rounded-xl border bg-background px-3 py-3 text-sm" />
            </label>
          </div>
          <div className="mt-6 flex gap-3">
            <button type="button" onClick={onClose} className="flex-1 rounded-xl border px-4 py-3 text-sm font-semibold">Cancel</button>
            <button type="submit" className="flex-1 rounded-xl bg-[#173a2e] px-4 py-3 text-sm font-semibold text-white">Publish schedule</button>
          </div>
        </form>
      </div>
  )
}