"use client";

import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

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

// Component to dynamically update map center if location changes
function ChangeView({ center }: { center: [number, number] }) {
  const map = useMap();
  map.setView(center, map.getZoom());
  return null;
}

export default function MapLocation({ lat, lng }: { lat: number; lng: number }) {
  return (
    <div className="w-full h-full rounded-2xl overflow-hidden shadow-inner border border-slate-700/50">
      <MapContainer 
        center={[lat, lng]} 
        zoom={14} 
        scrollWheelZoom={true} 
        style={{ height: "100%", width: "100%" }}
        attributionControl={false}
      >
        <ChangeView center={[lat, lng]} />
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={[lat, lng]} icon={icon}>
          <Popup>
            Sample Acquisition Location <br />
            Lat: {lat.toFixed(6)}, Lng: {lng.toFixed(6)}
          </Popup>
        </Marker>
      </MapContainer>
    </div>
  );
}
