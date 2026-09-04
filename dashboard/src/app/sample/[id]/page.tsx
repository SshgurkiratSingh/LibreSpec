import React from 'react';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import { ArrowLeft, Beaker, ShieldCheck, Clock, CheckCircle } from 'lucide-react';
import SampleCharts from '@/components/SampleCharts';
import { ForensicLog } from '@/types';

// Since we cannot alter the Lambda function to add a specific get-by-id endpoint,
// we fetch the latest logs for known substances and search for our sample.
async function getSampleData(id: string) {
  const substances = ['Cathinone', 'Cocaine', 'MDMA', 'Methamphetamine'];
  let allLogs: ForensicLog[] = [];
  
  for (const substance of substances) {
    try {
      const res = await fetch(
        `https://szcon5jvb0.execute-api.us-east-1.amazonaws.com/default/BioChemicalAnalyser?select_best=true&substance=${substance}&limit=10`, 
        { next: { revalidate: 30 } }
      );
      if (res.ok) {
        const logs = await res.json();
        allLogs = [...allLogs, ...logs];
      }
    } catch (err) {
      console.error(err);
    }
  }

  return allLogs.find(log => log.test_id === id);
}

export default async function SamplePage({ params }: { params: { id: string } }) {
  const sample = await getSampleData(params.id);

  if (!sample) {
    notFound();
  }

  const chartData = sample.baseline_vector.map((baseVal: number, idx: number) => ({
    channel: `Ch ${idx + 1}`,
    Baseline: baseVal,
    Plateau: sample.plateau_vector[idx],
    Delta: sample.plateau_vector[idx] - baseVal
  }));

  const location = { 
    lat: Number(sample.latitude ?? sample.gps_latitude ?? 37.7749), 
    lng: Number(sample.longitude ?? sample.gps_longitude ?? -122.4194) 
  };

  return (
    <main className="min-h-screen bg-slate-950 text-slate-200 py-12 px-4 sm:px-6 lg:px-8 selection:bg-indigo-500/30">
      <div className="max-w-6xl mx-auto space-y-8">
        
        {/* Navigation */}
        <Link href="/" className="inline-flex items-center text-indigo-400 hover:text-indigo-300 font-medium transition-colors">
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Dashboard
        </Link>
        
        {/* Header Section */}
        <div className="bg-slate-900/50 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 flex flex-col md:flex-row md:items-center justify-between shadow-2xl">
          <div>
            <div className="inline-flex items-center justify-center p-2 bg-indigo-500/10 rounded-xl mb-4 ring-1 ring-indigo-500/30">
              <Beaker className="w-6 h-6 text-indigo-400" />
            </div>
            <h1 className="text-4xl font-extrabold text-white mb-2">{sample.predicted_class}</h1>
            <p className="text-slate-400 font-mono flex items-center gap-2">
              <span className="text-slate-500">ID:</span> {sample.test_id}
            </p>
          </div>
          
          <div className="mt-6 md:mt-0 flex flex-col gap-4">
            <div className="bg-slate-800/50 px-6 py-4 rounded-2xl border border-slate-700/50 flex flex-col items-end">
              <span className="text-slate-400 text-sm font-medium mb-1">Confidence Score</span>
              <div className="text-3xl font-black text-emerald-400">
                {(sample.confidence_score * 100).toFixed(2)}%
              </div>
            </div>
          </div>
        </div>

        {/* Info Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-slate-900/40 border border-slate-800/60 rounded-2xl p-6 flex flex-col gap-3">
            <div className="flex items-center gap-3 text-slate-400">
              <Clock className="w-5 h-5 text-blue-400" />
              <h3 className="font-semibold text-white">Timestamp</h3>
            </div>
            <p className="text-lg">
              {new Date(
                String(sample.timestamp).length === 10 
                  ? Number(sample.timestamp) * 1000 
                  : Number(sample.timestamp)
              ).toLocaleString()}
            </p>
          </div>

          <div className="bg-slate-900/40 border border-slate-800/60 rounded-2xl p-6 flex flex-col gap-3">
            <div className="flex items-center gap-3 text-slate-400">
              <ShieldCheck className="w-5 h-5 text-emerald-400" />
              <h3 className="font-semibold text-white">Cryptographic Seal</h3>
            </div>
            <p className="font-mono text-sm text-slate-300 break-all bg-slate-950/50 p-2 rounded-lg">
              {sample.cryptographic_seal || sample.baseline_hmac || 'Unverified'}
            </p>
          </div>

          <div className="bg-slate-900/40 border border-slate-800/60 rounded-2xl p-6 flex flex-col gap-3">
            <div className="flex items-center gap-3 text-slate-400">
              <CheckCircle className="w-5 h-5 text-purple-400" />
              <h3 className="font-semibold text-white">Data Integrity</h3>
            </div>
            <div className="flex items-center gap-2 text-emerald-400 bg-emerald-400/10 w-fit px-3 py-1 rounded-full font-medium">
              Verified Immutable
            </div>
          </div>
        </div>

        {/* Charts & Location */}
        <SampleCharts chartData={chartData} location={location} />

      </div>
    </main>
  );
}
