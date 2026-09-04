export interface ForensicLog {
  test_id: string;
  timestamp: number;
  server_ingest_time?: number;
  predicted_class: string;
  confidence_score: number;
  baseline_vector: number[];
  plateau_vector: number[];
  cryptographic_seal?: string;
  latitude?: number;
  longitude?: number;
  gps_latitude?: string;
  gps_longitude?: string;
  baseline_hmac?: string;
}
