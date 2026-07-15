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
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { finalize } from 'rxjs/operators';
import type { RideDto, UpdateRideRequestDto } from '../../core/api/api-dtos';
import { RideService } from '../../core/api/ride.service';
import {
  rideDriverLabel,
  rideTrackById,
  shortRideId,
} from '../../core/rides/ride-view.helpers';
import { SessionService } from '../../core/session/session.service';
import { RideStatusTagComponent } from '../../shared/components/ride-status-tag/ride-status-tag.component';

@Component({
  selector: 'app-client-rides',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    Button,
    Dialog,
    InputText,
    DatePipe,
    RideStatusTagComponent,
  ],
  templateUrl: './client-rides.component.html',
  styleUrl: './client-rides.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientRidesComponent {
  private readonly rideService = inject(RideService);
  private readonly session = inject(SessionService);
  private readonly messageService = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  protected readonly account = this.session.account;
  protected readonly rides = signal<RideDto[]>([]);
  protected readonly loading = signal(false);
  protected readonly createDialogVisible = signal(false);
  protected readonly profileMenuOpen = signal(false);
  protected readonly creating = signal(false);
  protected readonly editingRideId = signal<string | null>(null);
  protected readonly savingRideId = signal<string | null>(null);
  protected readonly cancellingRideId = signal<string | null>(null);
  protected readonly driverLabel = rideDriverLabel;
  protected readonly shortId = shortRideId;
  protected readonly trackById = rideTrackById;

  protected readonly myRides = computed(() => {
    const account = this.account();
    if (!account) return [];
    return this.rides().filter((ride) => ride.userId === account.id);
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

  protected readonly createForm = this.fb.nonNullable.group({
    startAddress: ['', [Validators.required, Validators.minLength(2)]],
    destinationAddress: ['', [Validators.required, Validators.minLength(2)]],
  });

  protected readonly editForm = this.fb.nonNullable.group({
    startAddress: ['', [Validators.required, Validators.minLength(2)]],
    destinationAddress: ['', [Validators.required, Validators.minLength(2)]],
  });

  constructor() {
    this.loadRides();
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

  protected openCreateDialog(): void {
    this.createForm.reset({ startAddress: '', destinationAddress: '' });
    this.createDialogVisible.set(true);
  }

  protected closeCreateDialog(): void {
    this.createDialogVisible.set(false);
  }

  protected toggleProfileMenu(): void {
    this.profileMenuOpen.update((open) => !open);
  }

  protected logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }

  protected submitCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const account = this.account();
    if (!account) return;

    this.creating.set(true);
    const { startAddress, destinationAddress } = this.createForm.getRawValue();
    this.rideService
      .createRide({
        userId: account.id,
        startAddress: startAddress.trim(),
        destinationAddress: destinationAddress.trim(),
      })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.creating.set(false))
      )
      .subscribe({
        next: () => {
          this.closeCreateDialog();
          this.messageService.add({
            severity: 'success',
            summary: 'Corrida criada',
            detail: 'A corrida foi enviada para os motoristas.',
          });
          this.loadRides();
        },
        error: () => undefined,
      });
  }

  protected canEditRide(ride: RideDto): boolean {
    return ride.status !== 'COMPLETED' && ride.status !== 'CANCELLED';
  }

  protected cancelRide(ride: RideDto): void {
    if (!this.canEditRide(ride) || this.cancellingRideId()) return;

    this.cancellingRideId.set(ride.id);
    this.rideService
      .cancelRide(ride.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cancellingRideId.set(null))
      )
      .subscribe({
        next: (cancelledRide) => {
          this.rides.update((current) =>
            current.map((item) => (item.id === cancelledRide.id ? cancelledRide : item))
          );
          this.messageService.add({
            severity: 'success',
            summary: 'Corrida cancelada',
            detail: 'Os motoristas foram avisados do cancelamento.',
          });
        },
        error: () => undefined,
      });
  }

  protected startEditingRide(ride: RideDto): void {
    if (!this.canEditRide(ride) || this.savingRideId()) return;

    this.editForm.reset({
      startAddress: ride.startAddress,
      destinationAddress: ride.destinationAddress,
    });
    this.editingRideId.set(ride.id);
  }

  protected cancelEditingRide(): void {
    if (this.savingRideId()) return;
    this.editingRideId.set(null);
    this.editForm.reset({ startAddress: '', destinationAddress: '' });
  }

  protected saveEditingRide(ride: RideDto): void {
    if (!this.canEditRide(ride)) return;

    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const account = this.account();
    if (!account) return;

    const { startAddress, destinationAddress } = this.editForm.getRawValue();
    const body: UpdateRideRequestDto = {
      userId: account.id,
      startAddress: startAddress.trim(),
      destinationAddress: destinationAddress.trim(),
    };

    this.savingRideId.set(ride.id);
    this.rideService
      .updateRide(ride.id, body)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.savingRideId.set(null))
      )
      .subscribe({
        next: (updatedRide) => {
          this.rides.update((current) =>
            current.map((item) => (item.id === updatedRide.id ? updatedRide : item))
          );
          this.editingRideId.set(null);
          this.messageService.add({
            severity: 'success',
            summary: 'Corrida atualizada',
            detail: 'Origem e destino foram atualizados.',
          });
        },
        error: () => undefined,
      });
  }
}
