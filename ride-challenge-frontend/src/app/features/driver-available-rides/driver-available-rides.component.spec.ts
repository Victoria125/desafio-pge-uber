import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import { of, Subject, throwError } from 'rxjs';
import type { AccountDto, RideDto, RideNotificationDto } from '../../core/api/api-dtos';
import { AccountService } from '../../core/api/account.service';
import { RideService } from '../../core/api/ride.service';
import { RideNotificationService } from '../../core/notifications/ride-notification.service';
import { SessionService } from '../../core/session/session.service';
import { DriverAvailableRidesComponent } from './driver-available-rides.component';

const STORAGE_KEY = 'ride-challenge.session';

const account: AccountDto = {
  id: 'driver-1',
  name: 'Bruno Motorista',
  email: 'bruno@example.com',
  type: 'DRIVER',
};

const clientAccount: AccountDto = {
  id: 'client-1',
  name: 'Ana Cliente',
  email: 'ana@example.com',
  type: 'CLIENT',
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
  declineRide(ride: RideDto): void;
  changeView(view: 'rides' | 'history'): void;
  toggleNotices(): void;
  logout(): void;
}

describe('DriverAvailableRidesComponent', () => {
  let fixture: ComponentFixture<DriverAvailableRidesComponent>;
  let component: DriverComponentInternals;
  let notifications$: Subject<RideNotificationDto>;
  let rideService: jasmine.SpyObj<RideService>;
  let accountService: jasmine.SpyObj<AccountService>;
  let notificationService: jasmine.SpyObj<RideNotificationService>;

  function pageText(): string {
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent || '';
  }

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
        id: 'ride-1',
        userId: 'client-1',
        driverId: 'driver-1',
        startAddress: 'Unifor',
        destinationAddress: 'Aldeota',
        status: 'IN_PROGRESS',
        createdAt: '2026-07-09T10:00:00Z',
        updatedAt: '2026-07-09T10:01:00Z',
      } satisfies RideDto)
    );

    accountService = jasmine.createSpyObj<AccountService>('AccountService', ['listAccounts']);
    accountService.listAccounts.and.returnValue(of([account, clientAccount]));

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
        provideRouter([]),
        { provide: RideService, useValue: rideService },
        { provide: AccountService, useValue: accountService },
        { provide: RideNotificationService, useValue: notificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DriverAvailableRidesComponent);
    component = fixture.componentInstance as unknown as DriverComponentInternals;
    fixture.detectChanges();
  });

  afterEach(() => {
    notifications$.complete();
    localStorage.clear();
  });

  it('should render only available rides as cards', async () => {
    await fixture.whenStable();

    const text = pageText();
    expect(text).toContain('Unifor');
    expect(text).toContain('Aldeota');
    expect(text).not.toContain('Corrida em andamento');
  });

  it('should show the passenger name on the ride card', async () => {
    await fixture.whenStable();

    expect(pageText()).toContain('Ana Cliente');
  });

  it('should accept a ride with the selected driver id and keep it visible as in progress', async () => {
    await fixture.whenStable();

    component.acceptRide(availableRide);

    expect(rideService.acceptRide).toHaveBeenCalledWith('ride-1', {
      driverId: 'driver-1',
    });
    expect(pageText()).toContain('Unifor');
    expect(pageText()).toContain('Em andamento');

    component.changeView('history');
    expect(pageText()).toContain('Unifor');
  });

  it('should hide a ride from the panel when the driver does not accept it', async () => {
    await fixture.whenStable();

    component.declineRide(availableRide);

    expect(pageText()).not.toContain('Unifor');
    expect(rideService.acceptRide).not.toHaveBeenCalled();
  });

  it('should reload available rides when accepting a ride fails', () => {
    rideService.acceptRide.and.returnValue(
      throwError(() => new Error('Ride was already accepted'))
    );

    component.acceptRide(availableRide);

    expect(rideService.acceptRide).toHaveBeenCalledWith('ride-1', {
      driverId: 'driver-1',
    });
    expect(rideService.listRides).toHaveBeenCalledTimes(2);
  });

  it('should append rides received by WebSocket and count unread notifications', () => {
    notifications$.next({
      rideId: 'ride-3',
      userId: 'client-3',
      driverId: null,
      startAddress: 'Shopping Iguatemi',
      destinationAddress: 'Meireles',
      status: 'CREATED',
    });

    const text = pageText();
    expect(text).toContain('Shopping Iguatemi');
    expect(text).toContain('Meireles');

    const badge = (fixture.nativeElement as HTMLElement).querySelector('.bell-badge');
    expect(badge).not.toBeNull();
    const badgeText = badge && badge.textContent ? badge.textContent.trim() : '';
    expect(badgeText).toBe('1');

    component.toggleNotices();
    expect(pageText()).toContain('Nova corrida disponível');
  });

  it('should update the card and log a notice when the passenger edits the ride', () => {
    notifications$.next({
      rideId: 'ride-1',
      userId: 'client-1',
      driverId: null,
      startAddress: 'Unifor - portao 2',
      destinationAddress: 'Aldeota',
      status: 'CREATED',
    });

    expect(pageText()).toContain('Unifor - portao 2');

    component.toggleNotices();
    expect(pageText()).toContain('Corrida atualizada pelo passageiro');
  });

  it('should remove the card and log a notice when the passenger cancels the ride', () => {
    notifications$.next({
      rideId: 'ride-1',
      userId: 'client-1',
      driverId: null,
      startAddress: 'Unifor',
      destinationAddress: 'Aldeota',
      status: 'CANCELLED',
    });

    expect(pageText()).not.toContain('Unifor');

    component.toggleNotices();
    expect(pageText()).toContain('Corrida cancelada pelo passageiro');
  });

  it('should clear the session and navigate to login on logout', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = spyOn(router, 'navigate').and.resolveTo(true);
    const session = TestBed.inject(SessionService);

    component.logout();

    expect(session.isAuthenticated()).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
