// Espejo de com.cinemahub.cinemahub.membership.dto.* (backend)

export type MembershipLevel = 'BASIC' | 'SILVER' | 'GOLD' | 'PLATINUM';

export interface Membership {
  id: number;
  userId: number;
  level: MembershipLevel;
  points: number;
}

// delta puede ser negativo (redención de puntos) o positivo; el backend rechaza 0.
export interface AdjustPointsRequest {
  delta: number;
  reason?: string;
}

export interface ChangeLevelRequest {
  level: MembershipLevel;
}

export interface PointHistory {
  id: number;
  points: number;
  reason: string | null;
  createdAt: string;
}