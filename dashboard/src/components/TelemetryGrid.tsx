"use client";

import React, { useState, useEffect } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';
import { ShieldCheck, Clock, Activity, Beaker, Trash2 } from 'lucide-react';
import Link from 'next/link';
import { ForensicLog } from '@/types';

export default function TelemetryGrid({ data }: { data: ForensicLog[] }) {
  const [logs, setLogs] = useState<ForensicLog[]>(data);

  // Sync state if initial data changes
  useEffect(() => {
    setLogs(data);
  }, [data]);

  const handleDelete = (test_id: string) => {
    setLogs(prev => prev.filter(log => log.test_id !== test_id));
  };

  if (!logs || logs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-slate-900/40 backdrop-blur-xl border border-slate-800/80 rounded-3xl text-slate-400 shadow-inner">
        <Activity className="w-12 h-12 mb-4 animate-pulse text-indigo-500/50" />
        <p className="text-lg font-medium">No telemetry data available.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 xl:grid-cols-2 gap-8 w-full max-w-7xl mx-auto">
      {logs.map((log) => {
        // Prepare chart data from 14-dimensional vectors
        const chartData = log.baseline_vector.map((baseVal: number, idx: number) => ({
          channel: `Ch ${idx + 1}`,
          Baseline: baseVal,
          Plateau: log.plateau_vector[idx],
          Delta: Math.abs(log.plateau_vector[idx] - baseVal)
        }));

        return (
          <div key={log.test_id} className="group relative bg-slate-900/40 backdrop-blur-2xl border border-slate-700/50 rounded-3xl overflow-hidden shadow-xl transition-all duration-500 hover:shadow-indigo-500/20 hover:border-indigo-500/30 hover:-translate-y-1 flex flex-col">
            
            {/* Ambient glow behind card on hover */}
            <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/5 via-transparent to-pink-500/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"></div>

            {/* Header */}
            <div className="relative p-6 border-b border-slate-700/50 bg-slate-800/20 flex items-center justify-between z-10">
              <div>
                <h3 className="text-2xl font-black text-transparent bg-clip-text bg-gradient-to-r from-white to-slate-400 flex items-center gap-3">
                  <span className="p-2 bg-indigo-500/20 rounded-xl ring-1 ring-indigo-500/30">
                    <Beaker className="w-5 h-5 text-indigo-300" />
                  </span>
                  {log.predicted_class}
                </h3>
                <p className="text-xs text-slate-500 mt-2 font-mono flex items-center gap-2">
                  <span className="px-2 py-0.5 bg-slate-800 rounded-md border border-slate-700">ID</span> 
                  {log.test_id}
                </p>
              </div>
              <div className="flex flex-col items-end">
                <span className="px-3 py-1.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-xl text-sm font-bold flex items-center gap-2 shadow-[0_0_15px_rgba(16,185,129,0.15)]">
                  Conf: {(log.confidence_score * 100).toFixed(1)}%
                </span>
                <span className="text-xs text-slate-400 mt-3 flex items-center gap-1.5 bg-slate-950/50 px-2 py-1 rounded-md">
                  <Clock className="w-3 h-3 text-blue-400" />
                  {new Date(
                    String(log.timestamp).length === 10 
                      ? Number(log.timestamp) * 1000 
                      : Number(log.timestamp)
                  ).toLocaleString()}
                </span>
              </div>
            </div>

            {/* Graph */}
            <div className="relative p-6 flex-grow z-10 bg-slate-900/20">
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} opacity={0.5} />
                    <XAxis dataKey="channel" stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
                    <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', borderColor: '#334155', borderRadius: '12px', backdropFilter: 'blur(8px)' }}
                      itemStyle={{ color: '#e2e8f0' }}
                    />
                    <Legend wrapperStyle={{ paddingTop: '20px' }} />
                    <Line type="monotone" dataKey="Baseline" stroke="#818cf8" strokeWidth={3} dot={false} activeDot={{ r: 6, fill: '#818cf8', stroke: '#312e81', strokeWidth: 2 }} />
                    <Line type="monotone" dataKey="Plateau" stroke="#f472b6" strokeWidth={3} dot={false} activeDot={{ r: 6, fill: '#f472b6', stroke: '#831843', strokeWidth: 2 }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Footer / Actions */}
            <div className="relative p-5 bg-slate-950/80 flex flex-col sm:flex-row items-start sm:items-center justify-between border-t border-slate-700/50 gap-4 z-10">
              <div className="flex items-center gap-2 text-xs text-slate-400 font-mono truncate max-w-[50%]">
                <ShieldCheck className="w-5 h-5 text-emerald-500 drop-shadow-[0_0_8px_rgba(16,185,129,0.5)] flex-shrink-0" />
                <span className="truncate" title={log.cryptographic_seal || log.baseline_hmac || 'Unverified'}>
                  {log.cryptographic_seal || log.baseline_hmac || 'Unverified'}
                </span>
              </div>
              
              <div className="flex items-center gap-3 w-full sm:w-auto">
                <button
                  onClick={() => handleDelete(log.test_id)}
                  className="flex items-center justify-center p-2.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/20 hover:border-red-500/40 rounded-xl transition-all shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500/40"
                  aria-label="Delete Log"
                  title="Delete this record"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
                <Link 
                  href={`/sample/${log.test_id}`}
                  className="flex-1 sm:flex-none text-center px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-semibold rounded-xl transition-all shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40 whitespace-nowrap"
                >
                  View Analysis
                </Link>
              </div>
            </div>
            
          </div>
        );
      })}
    </div>
  );
}
