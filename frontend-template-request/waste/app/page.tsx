'use client'

import { useState } from 'react'
import {
  Bell,
  CalendarDays,
  Check,
  ChevronDown,
  CircleHelp,
  Clock3,
  FileText,
  Home,
  Leaf,
  LogOut,
  Map,
  Menu,
  MoreHorizontal,
  PackageCheck,
  Plus,
  Recycle,
  Search,
  Settings,
  Truck,
  Users,
  X,
} from 'lucide-react'

const navItems = [
  { label: 'Overview', icon: Home },
  { label: 'Collection schedules', icon: CalendarDays },
  { label: 'Routes & zones', icon: Map },
  { label: 'Residents', icon: Users },
  { label: 'Notifications', icon: Bell },
  { label: 'Reports', icon: FileText },
]

const activities = [
  { title: 'Route Kesses–Kapseret completed', meta: 'Truck WT-04 · 10:42 AM', tone: 'green', icon: Check },
  { title: 'New schedule published', meta: 'Uasin Gishu zone · 09:18 AM', tone: 'amber', icon: CalendarDays },
  { title: 'Notification delivery issue', meta: '12 residents · 08:56 AM', tone: 'red', icon: Bell },
  { title: 'Truck WT-07 checked in', meta: 'Huruma collection point · 08:31 AM', tone: 'blue', icon: Truck },
]

export default function Page() {
  const [activeNav, setActiveNav] = useState('Overview')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [showSchedule, setShowSchedule] = useState(false)
  const [toast, setToast] = useState('')

  function publishSchedule() {
    setShowSchedule(false)
    setToast('Schedule published and residents notified')
    window.setTimeout(() => setToast(''), 3000)
  }

  return (
    <div className="min-h-screen bg-[#f7f8f4] text-[#19332a]">
      <aside className={`fixed inset-y-0 left-0 z-40 flex w-[272px] flex-col bg-[#173a2e] px-5 py-6 text-white transition-transform lg:translate-x-0 ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex items-center gap-3 px-2">
          <div className="flex size-10 items-center justify-center rounded-xl bg-[#d8ed65] text-[#173a2e]"><Recycle size={23} strokeWidth={2.5} /></div>
          <div><p className="text-lg font-semibold tracking-tight">Waste Track</p><p className="text-[11px] uppercase tracking-[0.2em] text-[#b6cbbd]">Kenya · Eldoret</p></div>
          <button className="ml-auto lg:hidden" onClick={() => setSidebarOpen(false)} aria-label="Close navigation"><X size={20} /></button>
        </div>
        <div className="mt-11 px-2 text-[10px] font-semibold uppercase tracking-[0.2em] text-[#8da99a]">Workspace</div>
        <nav className="mt-3 space-y-1" aria-label="Main navigation">
          {navItems.map(({ label, icon: Icon }) => <button key={label} onClick={() => { setActiveNav(label); setSidebarOpen(false) }} className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm transition ${activeNav === label ? 'bg-[#d8ed65] font-semibold text-[#173a2e]' : 'text-[#c1d2c7] hover:bg-white/10 hover:text-white'}`}><Icon size={18} /><span>{label}</span>{label === 'Notifications' && <span className="ml-auto rounded-full bg-[#f5ad62] px-2 py-0.5 text-[10px] font-bold text-[#173a2e]">12</span>}</button>)}
        </nav>
        <div className="mt-auto space-y-1 border-t border-white/10 pt-5"><button className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-[#c1d2c7] hover:bg-white/10"><Settings size={18} />Settings</button><button className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-[#c1d2c7] hover:bg-white/10"><CircleHelp size={18} />Help centre</button><div className="mt-4 flex items-center gap-3 rounded-xl bg-white/10 p-3"><div className="flex size-9 items-center justify-center rounded-full bg-[#f5ad62] text-sm font-bold text-[#173a2e]">AM</div><div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">Alice Muthoni</p><p className="truncate text-xs text-[#9eb7a9]">Operations manager</p></div><LogOut size={16} className="text-[#9eb7a9]" /></div></div>
      </aside>
      {sidebarOpen && <button aria-label="Close navigation overlay" className="fixed inset-0 z-30 bg-[#10261e]/40 lg:hidden" onClick={() => setSidebarOpen(false)} />}
      <div className="lg:pl-[272px]">
        <header className="flex h-[76px] items-center justify-between border-b border-[#e0e6dc] bg-[#fbfcf9] px-5 sm:px-8">
          <div className="flex items-center gap-3"><button className="rounded-lg p-2 hover:bg-[#eef2e9] lg:hidden" onClick={() => setSidebarOpen(true)} aria-label="Open navigation"><Menu size={21} /></button><div><p className="text-xs font-medium text-[#789086]">Tuesday, 22 September 2026</p><h1 className="text-xl font-semibold tracking-tight sm:text-2xl">Good morning, Alice <span className="text-[#e1a65c]">.</span></h1></div></div>
          <div className="flex items-center gap-3"><button className="hidden items-center gap-2 rounded-xl border border-[#dfe6dc] bg-white px-3 py-2 text-sm text-[#62776d] shadow-sm sm:flex"><Search size={16} />Search</button><button className="relative rounded-xl border border-[#dfe6dc] bg-white p-2.5 text-[#516b5e] shadow-sm" aria-label="Notifications"><Bell size={18} /><span className="absolute right-1.5 top-1.5 size-1.5 rounded-full bg-[#e66f5f]" /></button><div className="hidden size-9 items-center justify-center rounded-full bg-[#d8ed65] text-xs font-bold text-[#173a2e] sm:flex">AM</div></div>
        </header>
        <main className="mx-auto max-w-[1500px] p-5 sm:p-8">
          <div className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-end"><div><p className="mb-1 text-sm font-medium text-[#6f887b]">Operations overview</p><h2 className="text-2xl font-semibold tracking-tight text-[#19332a]">Keep Eldoret moving cleanly.</h2></div><button onClick={() => setShowSchedule(true)} className="flex w-fit items-center gap-2 rounded-xl bg-[#173a2e] px-4 py-3 text-sm font-semibold text-white shadow-[0_8px_20px_rgba(23,58,46,0.16)] transition hover:bg-[#245843]"><Plus size={17} />Create schedule</button></div>
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard icon={CalendarDays} label="Collections today" value="28" detail="4 more than yesterday" color="green" />
            <StatCard icon={Truck} label="Active trucks" value="07 / 09" detail="2 trucks in maintenance" color="amber" />
            <StatCard icon={Users} label="Residents served" value="1,284" detail="91% notification delivery" color="blue" />
            <StatCard icon={PackageCheck} label="Waste collected" value="18.6 t" detail="This week · +8.4%" color="purple" />
          </section>
          <section className="mt-6 grid gap-6 xl:grid-cols-[1.4fr_0.8fr]">
            <div className="overflow-hidden rounded-2xl border border-[#e0e6dc] bg-white shadow-sm"><div className="flex items-center justify-between border-b border-[#edf0eb] px-5 py-4"><div><h3 className="font-semibold">Today&apos;s collection coverage</h3><p className="mt-1 text-xs text-[#789086]">Live route status across Eldoret zones</p></div><button className="flex items-center gap-1 text-xs font-semibold text-[#39705a]">View routes <ChevronDown size={14} /></button></div><div className="relative h-[270px] overflow-hidden bg-[#edf1e8] p-5"><div className="absolute inset-0 opacity-60" style={{ backgroundImage: 'linear-gradient(#d5dfd1 1px, transparent 1px), linear-gradient(90deg, #d5dfd1 1px, transparent 1px)', backgroundSize: '42px 42px' }} /><div className="relative flex h-full items-center justify-center"><div className="absolute h-[170px] w-[330px] rotate-[-12deg] rounded-[48%] border-[3px] border-dashed border-[#78a98d] bg-[#cfe2c7]/40 sm:w-[490px]" /><div className="absolute flex h-[110px] w-[230px] rotate-[13deg] items-center justify-center rounded-[45%] border-2 border-[#e7ac61] bg-[#f8e0b9]/45 sm:w-[350px]"><span className="rotate-[-13deg] rounded-full bg-white px-3 py-1 text-[11px] font-semibold text-[#89683f] shadow-sm">CBD zone</span></div><MapPin left="27%" top="25%" label="Huruma" tone="green" /><MapPin left="63%" top="32%" label="Langas" tone="amber" /><MapPin left="47%" top="67%" label="Kapsoya" tone="green" /><MapPin left="72%" top="70%" label="Pioneer" tone="blue" /></div><div className="absolute bottom-4 left-5 flex gap-4 rounded-lg bg-white/90 px-3 py-2 text-[10px] font-medium shadow-sm"><span className="flex items-center gap-1.5"><i className="size-2 rounded-full bg-[#4d9a70]" />Completed</span><span className="flex items-center gap-1.5"><i className="size-2 rounded-full bg-[#e7ac61]" />In progress</span></div></div><div className="grid grid-cols-3 divide-x border-t border-[#edf0eb] text-center"><div className="p-3"><p className="text-lg font-semibold">18</p><p className="text-[11px] text-[#789086]">Completed</p></div><div className="p-3"><p className="text-lg font-semibold text-[#b77a35]">07</p><p className="text-[11px] text-[#789086]">In progress</p></div><div className="p-3"><p className="text-lg font-semibold text-[#9a6259]">03</p><p className="text-[11px] text-[#789086]">Pending</p></div></div></div>
            <div className="rounded-2xl border border-[#e0e6dc] bg-white p-5 shadow-sm"><div className="flex items-start justify-between"><div><h3 className="font-semibold">Weekly collections</h3><p className="mt-1 text-xs text-[#789086]">Completed pickups by day</p></div><button aria-label="More chart options" className="text-[#789086]"><MoreHorizontal size={18} /></button></div><div className="mt-6 flex h-[204px] items-end justify-between gap-2 border-b border-[#e3e9e0] pb-0">{[58, 76, 51, 88, 70, 94, 42].map((height, i) => <div key={i} className="flex h-full flex-1 flex-col items-center justify-end gap-2"><div className={`w-full max-w-[28px] rounded-t-md ${i === 5 ? 'bg-[#173a2e]' : 'bg-[#b6d5a7]'}`} style={{ height: `${height}%` }} /><span className="mb-[-22px] text-[10px] text-[#789086]">{['M','T','W','T','F','S','S'][i]}</span></div>)}</div><div className="mt-8 flex items-center justify-between"><div><p className="text-2xl font-semibold">154</p><p className="text-xs text-[#789086]">Total pickups</p></div><div className="rounded-lg bg-[#edf6e9] px-2.5 py-1.5 text-xs font-semibold text-[#4d8054]">+12.6%</div></div></div>
          </section>
          <section className="mt-6 rounded-2xl border border-[#e0e6dc] bg-white shadow-sm"><div className="flex flex-col justify-between gap-3 border-b border-[#edf0eb] px-5 py-4 sm:flex-row sm:items-center"><div><h3 className="font-semibold">Recent activity</h3><p className="mt-1 text-xs text-[#789086]">Updates from your collection network</p></div><button className="w-fit rounded-lg bg-[#f1f5ee] px-3 py-2 text-xs font-semibold text-[#39705a]">View all activity</button></div><div className="divide-y divide-[#edf0eb]">{activities.map(({ title, meta, tone, icon: Icon }) => <div key={title} className="flex items-center gap-3 px-5 py-4"><div className={`flex size-9 shrink-0 items-center justify-center rounded-xl ${tone === 'green' ? 'bg-[#e4f2df] text-[#4c8c62]' : tone === 'amber' ? 'bg-[#fbefd9] text-[#b17b39]' : tone === 'red' ? 'bg-[#fbe6e3] text-[#bc6a60]' : 'bg-[#e2edf3] text-[#5d8297]'}`}><Icon size={17} /></div><div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{title}</p><p className="mt-0.5 text-xs text-[#789086]">{meta}</p></div><span className="hidden text-xs text-[#a1b0a7] sm:block">Today</span><MoreHorizontal size={17} className="text-[#a1b0a7]" /></div>)}</div></section>
          <div className="mt-5 flex items-center gap-2 text-xs text-[#8b9c92]"><Leaf size={14} className="text-[#6b9d72]" />Built for cleaner neighborhoods and stronger communities in Kenya.</div>
        </main>
      </div>
      {showSchedule && <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#10261e]/40 p-4"><div role="dialog" aria-modal="true" className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl"><div className="flex items-start justify-between"><div><h2 className="text-lg font-semibold">Create collection schedule</h2><p className="mt-1 text-sm text-[#789086]">Publish a route and notify residents.</p></div><button onClick={() => setShowSchedule(false)} aria-label="Close dialog" className="rounded-lg p-1 text-[#789086] hover:bg-[#f1f5ee]"><X size={19} /></button></div><div className="mt-5 space-y-4"><label className="block text-sm font-medium">Collection zone<select className="mt-2 w-full rounded-xl border border-[#dfe6dc] bg-white px-3 py-3 text-sm outline-none focus:border-[#6b9d72]"><option>CBD zone</option><option>Huruma & Kapsoya</option><option>Langas & Pioneer</option></select></label><label className="block text-sm font-medium">Collection date<input type="date" defaultValue="2026-09-24" className="mt-2 w-full rounded-xl border border-[#dfe6dc] bg-white px-3 py-3 text-sm outline-none focus:border-[#6b9d72]" /></label><label className="block text-sm font-medium">Assigned truck<select className="mt-2 w-full rounded-xl border border-[#dfe6dc] bg-white px-3 py-3 text-sm outline-none focus:border-[#6b9d72]"><option>WT-04 · Isuzu NPR</option><option>WT-07 · Mitsubishi Canter</option></select></label></div><div className="mt-6 flex gap-3"><button onClick={() => setShowSchedule(false)} className="flex-1 rounded-xl border border-[#dfe6dc] px-4 py-3 text-sm font-semibold text-[#62776d]">Cancel</button><button onClick={publishSchedule} className="flex-1 rounded-xl bg-[#173a2e] px-4 py-3 text-sm font-semibold text-white">Publish schedule</button></div></div></div>}
      {toast && <div role="status" className="fixed bottom-5 right-5 z-[60] flex items-center gap-2 rounded-xl bg-[#173a2e] px-4 py-3 text-sm font-medium text-white shadow-xl"><Check size={17} className="text-[#d8ed65]" />{toast}</div>}
    </div>
  )
}

function StatCard({ icon: Icon, label, value, detail, color }: { icon: typeof CalendarDays; label: string; value: string; detail: string; color: string }) {
  const styles: Record<string, string> = { green: 'bg-[#e3f1de] text-[#4c8c62]', amber: 'bg-[#fbefd9] text-[#b17b39]', blue: 'bg-[#e1edf2] text-[#5f879a]', purple: 'bg-[#eee7f2] text-[#826a91]' }
  return <div className="rounded-2xl border border-[#e0e6dc] bg-white p-5 shadow-sm"><div className="flex items-start justify-between"><div className={`flex size-10 items-center justify-center rounded-xl ${styles[color]}`}><Icon size={19} /></div><span className="text-xs font-semibold text-[#4d9362]">+8.4%</span></div><p className="mt-5 text-sm text-[#789086]">{label}</p><p className="mt-1 text-2xl font-semibold tracking-tight">{value}</p><p className="mt-1 text-xs text-[#9aaa9f]">{detail}</p></div>
}

function MapPin({ left, top, label, tone }: { left: string; top: string; label: string; tone: string }) {
  const colors: Record<string, string> = { green: 'bg-[#4d9a70]', amber: 'bg-[#dfa15a]', blue: 'bg-[#6d9caf]' }
  return <div className="absolute flex flex-col items-center gap-1" style={{ left, top }}><div className={`flex size-7 items-center justify-center rounded-full border-4 border-white ${colors[tone]} text-white shadow-md`}><div className="size-1.5 rounded-full bg-white" /></div><span className="rounded bg-white/90 px-1.5 py-0.5 text-[10px] font-semibold text-[#516b5e] shadow-sm">{label}</span></div>
}
