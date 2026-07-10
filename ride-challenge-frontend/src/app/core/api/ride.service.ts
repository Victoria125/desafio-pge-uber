import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-base-url.token';
import type {
  AcceptRideRequestDto,
  CreateRideRequestDto,
  CreateRideResponseDto,
  RideDto,
  RideStatusResponseDto,
  UpdateRideRequestDto,
} from './api-dtos';
import { apiRoutes } from './api-routes';

@Injectable({ providedIn: 'root' })
export class RideService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  createRide(body: CreateRideRequestDto): Observable<CreateRideResponseDto> {
    return this.http.post<CreateRideResponseDto>(apiRoutes.rides(this.baseUrl), body);
  }

  updateRide(rideId: string, body: UpdateRideRequestDto): Observable<RideDto> {
    return this.http.put<RideDto>(apiRoutes.updateRide(this.baseUrl, rideId), body);
  }

  listRides(): Observable<RideDto[]> {
    return this.http.get<RideDto[]>(apiRoutes.rides(this.baseUrl));
  }

  getRideById(rideId: string): Observable<RideDto> {
    return this.http.get<RideDto>(apiRoutes.rideById(this.baseUrl, rideId));
  }

  getRideStatus(rideId: string): Observable<RideStatusResponseDto> {
    return this.http.get<RideStatusResponseDto>(apiRoutes.rideStatus(this.baseUrl, rideId));
  }

  acceptRide(rideId: string, body: AcceptRideRequestDto): Observable<RideDto> {
    return this.http.post<RideDto>(apiRoutes.acceptRide(this.baseUrl, rideId), body);
  }
}