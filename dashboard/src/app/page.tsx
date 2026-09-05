import TelemetryGrid from '@/components/TelemetryGrid';
import { Activity, ShieldCheck, Globe } from 'lucide-react';
import { ForensicLog } from '@/types';
import dynamic from 'next/dynamic';

// Dynamically import map to avoid SSR issues with Leaflet
const MapWithNoSSR = dynamic(() => import('@/components/GlobalMap'), {
  ssr: false,
  loading: () => (
    <div className="w-full h-[400px] rounded-3xl bg-slate-900/50 flex items-center justify-center border border-slate-800">
      <Activity className="w-8 h-8 text-indigo-500 animate-pulse" />
    </div>
  )
});

export default async function Dashboard() {
  let logs: ForensicLog[] = [];
  const substances = ['Cathinone', 'Cocaine', 'MDMA', 'Methamphetamine'];

  try {
    const fetchPromises = substances.map(substance => 
      fetch(
        `https://szcon5jvb0.execute-api.us-east-1.amazonaws.com/default/BioChemicalAnalyser?select_best=true&substance=${substance}&limit=50`, 
        { next: { revalidate: 30 } }
      ).then(res => res.ok ? res.json() : [])
    );
    
    const results = await Promise.all(fetchPromises);
    logs = results.flat().sort((a, b) => b.timestamp - a.timestamp);
  } catch (err) {
    console.error("Failed to fetch telemetry data:", err);
  }
  
  return (
    <main className="min-h-screen bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-slate-900 via-slate-950 to-black text-slate-200 py-12 px-4 sm:px-6 lg:px-8 selection:bg-indigo-500/30">
      <div className="max-w-7xl mx-auto space-y-12">
        {/* Premium Header */}
        <header className="relative bg-slate-900/40 backdrop-blur-2xl border border-slate-800/80 rounded-3xl p-10 overflow-hidden shadow-2xl">
          {/* Decorative glowing blobs */}
          <div className="absolute -top-24 -right-24 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl opacity-50 mix-blend-screen pointer-events-none"></div>
          <div className="absolute -bottom-24 -left-24 w-64 h-64 bg-pink-500/10 rounded-full blur-3xl opacity-50 mix-blend-screen pointer-events-none"></div>
          
          <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-8">
            <div>
              <div className="inline-flex items-center gap-3 px-4 py-2 bg-indigo-500/10 rounded-full mb-6 ring-1 ring-indigo-500/20 backdrop-blur-sm">
                <ShieldCheck className="w-5 h-5 text-indigo-400" />
                <span className="text-sm font-medium text-indigo-300 tracking-wide uppercase">Real-Time Operations</span>
              </div>
              <h1 className="text-4xl md:text-6xl font-black text-transparent bg-clip-text bg-gradient-to-r from-indigo-300 via-purple-300 to-pink-300 tracking-tight leading-tight">
                Forensic Analytics<br/>Command Center
              </h1>
              <p className="mt-4 text-slate-400 max-w-xl text-lg font-light leading-relaxed">
                Biochemical spectral analysis and cryptographic verification for edge-computed telemetry streams.
              </p>
            </div>
            
            {/* Quick Stats */}
            <div className="flex gap-4">
              <div className="bg-slate-950/50 border border-slate-800 rounded-2xl p-6 flex flex-col items-center justify-center min-w-[140px] shadow-inner">
                <span className="text-4xl font-bold text-indigo-400">{logs.length}</span>
                <span className="text-xs text-slate-500 font-medium uppercase tracking-wider mt-2">Total Logs</span>
              </div>
            </div>
          </div>
        </header>

        {/* Global Map Section */}
        <section className="space-y-6">
          <div className="flex items-center gap-3 px-2">
            <Globe className="w-6 h-6 text-indigo-400" />
            <h2 className="text-2xl font-bold text-white tracking-tight">Global Telemetry Map</h2>
          </div>
          <MapWithNoSSR logs={logs} />
        </section>
        
        {/* Telemetry Grid Section */}
        <section className="space-y-6 pt-4">
          <div className="flex items-center gap-3 px-2">
            <Activity className="w-6 h-6 text-pink-400" />
            <h2 className="text-2xl font-bold text-white tracking-tight">Recent Acquisitions</h2>
          </div>
          <TelemetryGrid data={logs} />
        </section>
      </div>
    </main>
  );
}
