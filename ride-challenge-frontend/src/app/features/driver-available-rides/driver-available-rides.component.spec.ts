import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import { of, Subject, throwError } from 'rxjs';
import type { AccountDto, RideDto, RideNotificationDto } from '../../core/api/api-dtos';
import { RideService } from '../../core/api/ride.service';
import { RideNotificationService } from '../../core/notifications/ride-notification.service';
import { DriverAvailableRidesComponent } from './driver-available-rides.component';

const STORAGE_KEY = 'ride-challenge.session';

const account: AccountDto = {
  id: 'driver-1',
  name: 'Bruno Motorista',
  email: 'bruno@example.com',
  type: 'DRIVER',
};

const availableRide: RideDto = {
  id: 'ride-1',
  userId: 'client-1',
  driverId: null,
  startAddress: 'Unifor',
  destinationAddress: 'Aldeota',
  status: 'CREATED',
  createdAt: '2026-07-09T10:00:00Z',
};

const unavailableRide: RideDto = {
  id: 'ride-2',
  userId: 'client-2',
  driverId: 'driver-2',
  startAddress: 'Corrida em andamento',
  destinationAddress: 'Centro',
  status: 'IN_PROGRESS',
  createdAt: '2026-07-09T11:00:00Z',
};

interface DriverComponentInternals {
  acceptRide(ride: RideDto): void;
}

describe('DriverAvailableRidesComponent', () => {
  let fixture: ComponentFixture<DriverAvailableRidesComponent>;
  let notifications$: Subject<RideNotificationDto>;
  let rideService: jasmine.SpyObj<RideService>;
  let notificationService: jasmine.SpyObj<RideNotificationService>;

  beforeEach(async () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: 'jwt-token',
        expiresAt: Date.now() + 86_400_000,
        account,
      })
    );
    notifications$ = new Subject<RideNotificationDto>();

    rideService = jasmine.createSpyObj<RideService>('RideService', [
      'listRides',
      'acceptRide',
    ]);
    rideService.listRides.and.returnValue(of([availableRide, unavailableRide]));
    rideService.acceptRide.and.returnValue(
      of({
        ...availableRide,
        driverId: 'driver-1',
        status: 'IN_PROGRESS',
        updatedAt: '2026-07-09T10:01:00Z',
      } satisfies RideDto)
    );

    notificationService = jasmine.createSpyObj<RideNotificationService>(
      'RideNotificationService',
      ['watchRides']
    );
    notificationService.watchRides.and.returnValue(notifications$.asObservable());

    await TestBed.configureTestingModule({
      imports: [DriverAvailableRidesComponent],
      providers: [
        MessageService,
        providePrimeNG(),
        { provide: RideService, useValue: rideService },
        { provide: RideNotificationService, useValue: notificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DriverAvailableRidesComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    notifications$.complete();
    localStorage.clear();
  });

  it('should render only available rides', async () => {
    await fixture.whenStable();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Unifor');
    expect(text).toContain('Aldeota');
    expect(text).not.toContain('Corrida em andamento');
  });

  it('should accept a ride with the selected driver id', () => {
    const component = fixture.componentInstance as unknown as DriverComponentInternals;

    component.acceptRide(availableRide);

    expect(rideService.acceptRide).toHaveBeenCalledWith('ride-1', {
      driverId: 'driver-1',
    });
  });

  it('should reload available rides when accepting a ride fails', () => {
    const component = fixture.componentInstance as unknown as DriverComponentInternals;
    rideService.acceptRide.and.returnValue(
      throwError(() => new Error('Ride was already accepted'))
    );

    component.acceptRide(availableRide);

    expect(rideService.acceptRide).toHaveBeenCalledWith('ride-1', {
      driverId: 'driver-1',
    });
    expect(rideService.listRides).toHaveBeenCalledTimes(2);
  });

  it('should append rides received by WebSocket notification', () => {
    notifications$.next({
      rideId: 'ride-3',
      userId: 'client-3',
      driverId: null,
      startAddress: 'Shopping Iguatemi',
      destinationAddress: 'Meireles',
      status: 'CREATED',
    });
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Shopping Iguatemi');
    expect(text).toContain('Meireles');
  });
});
