"use client";

import React from 'react';
import dynamic from 'next/dynamic';
import { MapPin } from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';

const DynamicMapLocation = dynamic(() => import('./MapLocation'), {
  ssr: false,
  loading: () => <div className="w-full h-full bg-slate-800 animate-pulse flex items-center justify-center text-slate-500">Loading Map...</div>
});

export default function SampleCharts({ chartData, location }: { chartData: any[], location: { lat: number, lng: number } }) {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
      {/* Main Chart */}
      <div className="lg:col-span-2 bg-slate-900/50 backdrop-blur-xl border border-slate-800 rounded-3xl p-6 shadow-xl">
        <h3 className="text-xl font-bold text-white mb-6">Spectral Absorbance (Baseline vs Plateau)</h3>
        <div className="h-80 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
              <XAxis dataKey="channel" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
              <YAxis stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
              <Tooltip
                contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }}
                itemStyle={{ color: '#f8fafc' }}
              />
              <Legend wrapperStyle={{ paddingTop: '20px' }} />
              <Line type="monotone" dataKey="Baseline" stroke="#818cf8" strokeWidth={3} dot={{ r: 4, strokeWidth: 2 }} activeDot={{ r: 6 }} />
              <Line type="monotone" dataKey="Plateau" stroke="#f472b6" strokeWidth={3} dot={{ r: 4, strokeWidth: 2 }} activeDot={{ r: 6 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Delta Area Chart & Location */}
      <div className="space-y-8">
        <div className="bg-slate-900/50 backdrop-blur-xl border border-slate-800 rounded-3xl p-6 shadow-xl">
          <h3 className="text-lg font-bold text-white mb-4">Absorbance Delta (Δ)</h3>
          <div className="h-40 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData} margin={{ top: 0, right: 0, left: -30, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorDelta" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#34d399" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#34d399" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                <XAxis dataKey="channel" hide />
                <YAxis stroke="#64748b" fontSize={10} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px' }}
                />
                <Area type="monotone" dataKey="Delta" stroke="#34d399" fillOpacity={1} fill="url(#colorDelta)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-slate-900/50 backdrop-blur-xl border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col gap-4 h-[240px]">
          <div className="flex items-center gap-2 text-white">
            <MapPin className="w-5 h-5 text-rose-400" />
            <h3 className="font-bold text-lg">Acquisition Location</h3>
          </div>
          <div className="flex-grow rounded-2xl bg-slate-800 overflow-hidden relative group h-[200px]">
            <DynamicMapLocation lat={location.lat} lng={location.lng} />
          </div>
        </div>
      </div>
    </div>
  );
}
