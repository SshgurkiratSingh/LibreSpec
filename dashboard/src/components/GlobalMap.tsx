"use client";

import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { ForensicLog } from '@/types';
import Link from 'next/link';

// Fix for default Leaflet marker icons in React
const icon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// Component to dynamically fit bounds of all markers
function MapBounds({ logs }: { logs: ForensicLog[] }) {
  const map = useMap();
  
  React.useEffect(() => {
    if (logs.length === 0) return;
    const bounds = L.latLngBounds(
      logs.filter(log => log.latitude !== undefined || log.gps_latitude !== undefined).map(log => {
        const lat = Number(log.latitude ?? log.gps_latitude ?? 37.7749);
        const lng = Number(log.longitude ?? log.gps_longitude ?? -122.4194);
        return [lat, lng];
      })
    );
    if (bounds.isValid()) {
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [logs, map]);

  return null;
}

export default function GlobalMap({ logs }: { logs: ForensicLog[] }) {
  // Default center if no logs
  const center: [number, number] = [37.7749, -122.4194];

  return (
    <div className="w-full h-[400px] rounded-3xl overflow-hidden shadow-2xl border border-slate-700/50 relative z-0">
      <MapContainer 
        center={center} 
        zoom={3} 
        scrollWheelZoom={true} 
        style={{ height: "100%", width: "100%", zIndex: 1 }}
        attributionControl={false}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          className="map-tiles"
        />
        <MapBounds logs={logs} />
        {logs.map((log) => {
          const lat = Number(log.latitude ?? log.gps_latitude ?? 37.7749);
          const lng = Number(log.longitude ?? log.gps_longitude ?? -122.4194);
          
          return (
            <Marker key={log.test_id} position={[lat, lng]} icon={icon}>
              <Popup className="custom-popup">
                <div className="flex flex-col gap-1 p-1 min-w-[120px]">
                  <span className="font-bold text-slate-800">{log.predicted_class}</span>
                  <span className="text-xs text-slate-500 font-mono">ID: {log.test_id.substring(0, 8)}...</span>
                  <span className="text-sm font-semibold text-emerald-600">
                    Conf: {(log.confidence_score * 100).toFixed(1)}%
                  </span>
                  <Link 
                    href={`/sample/${log.test_id}`} 
                    className="text-xs text-indigo-600 hover:text-indigo-800 font-medium mt-1 inline-block"
                  >
                    View Details
                  </Link>
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
}
