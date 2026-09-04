"use client";

import React from 'react';
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
import { Beaker, ShieldCheck, Clock, Activity } from 'lucide-react';

import Link from 'next/link';

export default function TelemetryGrid({ data }: { data: any[] }) {
  if (!data || data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-slate-400">
        <Activity className="w-12 h-12 mb-4 animate-pulse text-indigo-500" />
        <p className="text-lg">No telemetry data available.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 xl:grid-cols-2 gap-8 w-full max-w-7xl mx-auto">
      {data.map((log) => {
        // Prepare chart data from 14-dimensional vectors
        const chartData = log.baseline_vector.map((baseVal: number, idx: number) => ({
          channel: `Ch ${idx + 1}`,
          Baseline: baseVal,
          Plateau: log.plateau_vector[idx],
          Delta: Math.abs(log.plateau_vector[idx] - baseVal)
        }));

        return (
          <div key={log.test_id} className="bg-slate-900/50 backdrop-blur-xl border border-slate-800 rounded-2xl overflow-hidden shadow-2xl transition hover:shadow-indigo-500/10 flex flex-col">
            {/* Header */}
            <div className="p-6 border-b border-slate-800 bg-slate-900/80 flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-white flex items-center gap-2">
                  <Beaker className="w-5 h-5 text-indigo-400" />
                  {log.predicted_class}
                </h3>
                <p className="text-sm text-slate-400 mt-1 font-mono">ID: {log.test_id}</p>
              </div>
              <div className="flex flex-col items-end">
                <span className="px-3 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full text-sm font-semibold flex items-center gap-2">
                  Confidence: {(log.confidence_score * 100).toFixed(1)}%
                </span>
                <span className="text-xs text-slate-500 mt-2 flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  {new Date(log.timestamp).toLocaleString()}
                </span>
              </div>
            </div>

            {/* Graph */}
            <div className="p-6 flex-grow">
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                    <XAxis dataKey="channel" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                    <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px' }}
                      itemStyle={{ color: '#e2e8f0' }}
                    />
                    <Legend wrapperStyle={{ paddingTop: '20px' }} />
                    <Line type="monotone" dataKey="Baseline" stroke="#6366f1" strokeWidth={2} dot={{ r: 3, fill: '#6366f1' }} />
                    <Line type="monotone" dataKey="Plateau" stroke="#ec4899" strokeWidth={2} dot={{ r: 3, fill: '#ec4899' }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Footer / Cryptographic Seal */}
            <div className="p-4 bg-slate-950 flex flex-col sm:flex-row items-start sm:items-center justify-between border-t border-slate-800 gap-4">
              <div className="flex items-center gap-2 text-xs text-slate-400 font-mono truncate max-w-full">
                <ShieldCheck className="w-4 h-4 text-emerald-500 flex-shrink-0" />
                <span className="truncate">Seal: {log.cryptographic_seal}</span>
              </div>
              <Link 
                href={`/sample/${log.test_id}`}
                className="px-4 py-2 bg-indigo-500 hover:bg-indigo-600 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap"
              >
                View Details
              </Link>
            </div>
          </div>
        );
      })}
    </div>
  );
}
