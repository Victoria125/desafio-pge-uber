import type { RideDto, RideStatus } from '../api/api-dtos';

export type TagSeverity = 'success' | 'secondary' | 'info' | 'warn' | 'danger' | 'contrast';

const CLIENT_STATUS_LABELS: Record<RideStatus, string> = {
  CREATED: 'Aguardando motorista',
  IN_PROGRESS: 'Em andamento',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
};

const DRIVER_STATUS_LABELS: Record<RideStatus, string> = {
  CREATED: 'Disponível',
  IN_PROGRESS: 'Em andamento',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
};

const CLIENT_STATUS_SEVERITIES: Record<RideStatus, TagSeverity> = {
  CREATED: 'warn',
  IN_PROGRESS: 'info',
  COMPLETED: 'success',
  CANCELLED: 'danger',
};

const DRIVER_STATUS_SEVERITIES: Record<RideStatus, TagSeverity> = {
  CREATED: 'success',
  IN_PROGRESS: 'info',
  COMPLETED: 'secondary',
  CANCELLED: 'danger',
};

export function clientRideStatusLabel(status: RideStatus): string {
  return CLIENT_STATUS_LABELS[status];
}

export function driverRideStatusLabel(status: RideStatus): string {
  return DRIVER_STATUS_LABELS[status];
}

export function clientRideStatusSeverity(status: RideStatus): TagSeverity {
  return CLIENT_STATUS_SEVERITIES[status];
}

export function driverRideStatusSeverity(status: RideStatus): TagSeverity {
  return DRIVER_STATUS_SEVERITIES[status];
}

export function shortRideId(id: string): string {
  return id.slice(0, 8);
}

export function rideDriverLabel(driverId: string | null): string {
  return driverId ? shortRideId(driverId) : 'Ainda não definido';
}

export function rideTrackById(_index: number, item: RideDto): string {
  return item.id;
}