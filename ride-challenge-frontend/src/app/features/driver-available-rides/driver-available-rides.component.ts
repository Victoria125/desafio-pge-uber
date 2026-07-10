import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { finalize } from 'rxjs/operators';
import type { RideDto, RideNotificationDto } from '../../core/api/api-dtos';
import { RideService } from '../../core/api/ride.service';
import { RideNotificationService } from '../../core/notifications/ride-notification.service';
import { rideTrackById, shortRideId } from '../../core/rides/ride-view.helpers';
import { SessionService } from '../../core/session/session.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { RideStatusTagComponent } from '../../shared/components/ride-status-tag/ride-status-tag.component';

@Component({
  selector: 'app-driver-available-rides',
  standalone: true,
  imports: [TableModule, Button, DatePipe, PageHeaderComponent, RideStatusTagComponent],
  templateUrl: './driver-available-rides.component.html',
  styleUrl: './driver-available-rides.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DriverAvailableRidesComponent {
  private readonly rideService = inject(RideService);
  private readonly notifications = inject(RideNotificationService);
  private readonly session = inject(SessionService);
  private readonly messageService = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly account = this.session.account;
  protected readonly rides = signal<RideDto[]>([]);
  protected readonly loading = signal(false);
  protected readonly acceptingRideId = signal<string | null>(null);
  protected readonly shortId = shortRideId;
  protected readonly trackById = rideTrackById;

  protected readonly availableRides = computed(() =>
    this.rides().filter((ride) => ride.status === 'CREATED' && !ride.driverId)
  );

  constructor() {
    this.loadRides();
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
        next: (rides) => this.rides.set(rides),
      });
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
        next: () => {
          this.rides.update((current) => current.filter((item) => item.id !== ride.id));
          this.messageService.add({
            severity: 'success',
            summary: 'Corrida aceita',
            detail: 'A corrida foi vinculada ao seu motorista.',
          });
        },
        error: () => this.loadRides(),
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
    if (notification.status !== 'CREATED' || notification.driverId) {
      this.rides.update((current) =>
        current.filter((ride) => ride.id !== notification.rideId)
      );
      return;
    }

    const ride = this.notificationToRide(notification);
    this.rides.update((current) => [
      ride,
      ...current.filter((item) => item.id !== ride.id),
    ]);
    this.messageService.add({
      severity: 'info',
      summary: 'Nova corrida disponível',
      detail: `${ride.startAddress} -> ${ride.destinationAddress}`,
    });
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