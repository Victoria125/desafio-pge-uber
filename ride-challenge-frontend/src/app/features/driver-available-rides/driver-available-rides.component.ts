import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { finalize } from 'rxjs/operators';
import type { RideDto, RideNotificationDto } from '../../core/api/api-dtos';
import { AccountService } from '../../core/api/account.service';
import { RideService } from '../../core/api/ride.service';
import { RideNotificationService } from '../../core/notifications/ride-notification.service';
import { rideTrackById, shortRideId } from '../../core/rides/ride-view.helpers';
import { SessionService } from '../../core/session/session.service';
import { RideStatusTagComponent } from '../../shared/components/ride-status-tag/ride-status-tag.component';

type DriverView = 'rides' | 'history';

type DriverNoticeKind = 'nova' | 'editada' | 'cancelada';

interface DriverNotice {
  kind: DriverNoticeKind;
  rideId: string;
  startAddress: string;
  destinationAddress: string;
  at: number;
}

const NEW_RIDE_WINDOW_MS = 2 * 60 * 1000;

@Component({
  selector: 'app-driver-available-rides',
  standalone: true,
  imports: [Button, RideStatusTagComponent],
  templateUrl: './driver-available-rides.component.html',
  styleUrl: './driver-available-rides.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DriverAvailableRidesComponent {
  private readonly rideService = inject(RideService);
  private readonly accountService = inject(AccountService);
  private readonly notifications = inject(RideNotificationService);
  private readonly session = inject(SessionService);
  private readonly messageService = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  protected readonly account = this.session.account;
  protected readonly rides = signal<RideDto[]>([]);
  protected readonly loading = signal(false);
  protected readonly acceptingRideId = signal<string | null>(null);
  protected readonly declinedRideIds = signal<Set<string>>(new Set());
  protected readonly updatedAt = signal<number | null>(null);
  protected readonly view = signal<DriverView>('rides');
  protected readonly noticesOpen = signal(false);
  protected readonly profileMenuOpen = signal(false);
  protected readonly notices = signal<DriverNotice[]>([]);
  protected readonly unreadCount = signal(0);
  protected readonly passengerNames = signal<Record<string, string>>({});
  protected readonly shortId = shortRideId;
  protected readonly trackById = rideTrackById;

  protected readonly panelRides = computed(() => {
    const account = this.account();
    const declined = this.declinedRideIds();
    return this.rides().filter((ride) => {
      const isAvailable = ride.status === 'CREATED' && !ride.driverId && !declined.has(ride.id);
      const isMineInProgress =
        Boolean(account) && ride.driverId === account?.id && ride.status === 'IN_PROGRESS';
      return isAvailable || isMineInProgress;
    });
  });

  protected readonly myRides = computed(() => {
    const account = this.account();
    if (!account) return [];
    return this.rides().filter((ride) => ride.driverId === account.id);
  });

  protected readonly initials = computed(() => {
    const account = this.account();
    if (!account) return '';
    return account.name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  });

  constructor() {
    this.loadRides();
    this.loadPassengerNames();
    this.watchRideNotifications();
  }

  protected loadRides(): void {
    this.loading.set(true);
    this.rideService
      .listRides()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (rides) => {
          this.rides.set(rides);
          this.updatedAt.set(Date.now());
        },
      });
  }

  protected changeView(view: DriverView): void {
    this.view.set(view);
    this.noticesOpen.set(false);
    this.profileMenuOpen.set(false);
  }

  protected toggleNotices(): void {
    const willOpen = !this.noticesOpen();
    this.noticesOpen.set(willOpen);
    if (willOpen) {
      this.profileMenuOpen.set(false);
    }
    if (willOpen) {
      this.unreadCount.set(0);
    }
  }

  protected toggleProfileMenu(): void {
    const willOpen = !this.profileMenuOpen();
    this.profileMenuOpen.set(willOpen);
    if (willOpen) {
      this.noticesOpen.set(false);
    }
  }

  protected logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }

  protected acceptRide(ride: RideDto): void {
    const account = this.account();
    if (!account || this.acceptingRideId()) return;

    this.acceptingRideId.set(ride.id);
    this.rideService
      .acceptRide(ride.id, { driverId: account.id })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.acceptingRideId.set(null))
      )
      .subscribe({
        next: (acceptedRide) => {
          this.declinedRideIds.update((current) => {
            const next = new Set(current);
            next.delete(acceptedRide.id);
            return next;
          });
          this.rides.update((current) =>
            current.map((item) => (item.id === acceptedRide.id ? acceptedRide : item))
          );
          this.messageService.add({
            severity: 'success',
            summary: 'Corrida aceita',
            detail: 'A corrida foi vinculada ao seu motorista.',
          });
        },
        error: () => this.loadRides(),
      });
  }

  protected declineRide(ride: RideDto): void {
    if (!this.canAcceptRide(ride)) return;
    this.declinedRideIds.update((current) => new Set(current).add(ride.id));
    this.messageService.add({
      severity: 'info',
      summary: 'Corrida ocultada',
      detail: 'Essa corrida nao vai aparecer no seu painel por enquanto.',
    });
  }

  protected canAcceptRide(ride: RideDto): boolean {
    return ride.status === 'CREATED' && !ride.driverId;
  }

  protected passengerName(userId: string): string {
    const name = this.passengerNames()[userId];
    return name || shortRideId(userId);
  }

  protected isNewRide(ride: RideDto): boolean {
    const createdAt = Date.parse(ride.createdAt);
    return Number.isFinite(createdAt) && Date.now() - createdAt <= NEW_RIDE_WINDOW_MS;
  }

  protected relativeTime(isoDate: string | number): string {
    const timestamp = typeof isoDate === 'number' ? isoDate : Date.parse(isoDate);
    if (!Number.isFinite(timestamp)) return '';

    const elapsedMinutes = Math.floor((Date.now() - timestamp) / 60_000);
    if (elapsedMinutes < 1) return 'Agora';
    if (elapsedMinutes < 60) return `${elapsedMinutes} min`;
    return `${Math.floor(elapsedMinutes / 60)} h`;
  }

  protected noticeTitle(kind: DriverNoticeKind): string {
    switch (kind) {
      case 'nova':
        return 'Nova corrida disponível';
      case 'editada':
        return 'Corrida atualizada pelo passageiro';
      case 'cancelada':
        return 'Corrida cancelada pelo passageiro';
    }
  }

  protected noticeIcon(kind: DriverNoticeKind): string {
    switch (kind) {
      case 'nova':
        return 'pi pi-plus-circle';
      case 'editada':
        return 'pi pi-pencil';
      case 'cancelada':
        return 'pi pi-times-circle';
    }
  }

  private loadPassengerNames(): void {
    this.accountService
      .listAccounts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (accounts) => {
          const names: Record<string, string> = {};
          for (const account of accounts) {
            names[account.id] = account.name;
          }
          this.passengerNames.set(names);
        },
        error: () => undefined,
      });
  }

  private watchRideNotifications(): void {
    this.notifications
      .watchRides()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (notification) => this.handleNotification(notification),
      });
  }

  private handleNotification(notification: RideNotificationDto): void {
    const known = this.rides().some((ride) => ride.id === notification.rideId);
    const account = this.account();

    if (notification.status === 'CANCELLED') {
      this.rides.update((current) =>
        current.filter((ride) => ride.id !== notification.rideId)
      );
      if (known) {
        this.pushNotice('cancelada', notification);
      }
      return;
    }

    if (notification.driverId === account?.id && notification.status === 'IN_PROGRESS') {
      const ride = this.notificationToRide(notification);
      this.rides.update((current) =>
        known ? current.map((item) => (item.id === ride.id ? ride : item)) : [ride].concat(current)
      );
      return;
    }

    if (notification.status !== 'CREATED' || notification.driverId) {
      this.rides.update((current) =>
        current.filter((ride) => ride.id !== notification.rideId)
      );
      return;
    }

    const ride = this.notificationToRide(notification);
    if (known) {
      this.rides.update((current) =>
        current.map((item) => (item.id === ride.id ? this.withUpdatedRoute(item, notification) : item))
      );
      this.pushNotice('editada', notification);
      return;
    }

    this.rides.update((current) => [ride].concat(current));
    this.pushNotice('nova', notification);
    this.messageService.add({
      severity: 'info',
      summary: 'Nova corrida disponível',
      detail: `${ride.startAddress} -> ${ride.destinationAddress}`,
    });
  }

  private pushNotice(kind: DriverNoticeKind, notification: RideNotificationDto): void {
    const notice: DriverNotice = {
      kind,
      rideId: notification.rideId,
      startAddress: notification.startAddress,
      destinationAddress: notification.destinationAddress,
      at: Date.now(),
    };
    this.notices.update((current) => [notice].concat(current).slice(0, 20));
    if (!this.noticesOpen()) {
      this.unreadCount.update((count) => count + 1);
    }
  }

  private withUpdatedRoute(ride: RideDto, notification: RideNotificationDto): RideDto {
    return {
      id: ride.id,
      userId: ride.userId,
      driverId: ride.driverId,
      startAddress: notification.startAddress,
      destinationAddress: notification.destinationAddress,
      status: ride.status,
      createdAt: ride.createdAt,
      updatedAt: ride.updatedAt,
    };
  }

  private notificationToRide(notification: RideNotificationDto): RideDto {
    return {
      id: notification.rideId,
      userId: notification.userId,
      driverId: notification.driverId,
      startAddress: notification.startAddress,
      destinationAddress: notification.destinationAddress,
      status: notification.status,
      createdAt: new Date().toISOString(),
    };
  }
}
