import TelemetryGrid from '@/components/TelemetryGrid';
import { Activity } from 'lucide-react';

export default async function Dashboard() {
  let logs: any[] = [];
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
    <main className="min-h-screen bg-slate-950 text-slate-200 py-12 px-4 sm:px-6 lg:px-8 selection:bg-indigo-500/30">
      <div className="max-w-7xl mx-auto">
        <header className="mb-12 text-center">
          <div className="inline-flex items-center justify-center p-3 bg-indigo-500/10 rounded-full mb-6 ring-1 ring-indigo-500/30">
            <Activity className="w-8 h-8 text-indigo-400" />
          </div>
          <h1 className="text-4xl md:text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 tracking-tight">
            Forensic Analytics Dashboard
          </h1>
          <p className="mt-4 text-slate-400 max-w-2xl mx-auto text-lg">
            Real-time biochemical spectral analysis and cryptographic verification for edge-computed telemetry.
          </p>
        </header>
        
        <TelemetryGrid data={logs} />
      </div>
    </main>
  );
}
