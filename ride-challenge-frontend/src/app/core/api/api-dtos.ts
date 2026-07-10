export type AccountType = 'CLIENT' | 'DRIVER';

export interface AccountDto {
  id: string;
  name: string;
  email: string;
  type: AccountType;
}

export interface CreateAccountRequestDto {
  name: string;
  email: string;
  type: AccountType;
}

export interface CreateAccountResponseDto {
  id: string;
}

export type RideStatus = 'CREATED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface RideDto {
  id: string;
  userId: string;
  driverId: string | null;
  startAddress: string;
  destinationAddress: string;
  status: RideStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateRideRequestDto {
  userId: string;
  startAddress: string;
  destinationAddress: string;
}

export interface CreateRideResponseDto {
  id: string;
}

export interface UpdateRideRequestDto {
  userId: string;
  startAddress: string;
  destinationAddress: string;
}

export interface AcceptRideRequestDto {
  driverId: string;
}

export interface RideStatusResponseDto {
  rideId: string;
  status: RideStatus;
  driverId: string | null;
  source: string;
}

export interface RideNotificationDto {
  rideId: string;
  userId: string;
  driverId: string | null;
  startAddress: string;
  destinationAddress: string;
  status: RideStatus;
}

export interface ApiErrorResponseDto {
  errors: Record<string, string>;
}