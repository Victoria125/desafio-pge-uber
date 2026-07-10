import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Tag } from 'primeng/tag';
import type { RideStatus } from '../../../core/api/api-dtos';
import {
  clientRideStatusLabel,
  clientRideStatusSeverity,
  driverRideStatusLabel,
  driverRideStatusSeverity,
} from '../../../core/rides/ride-view.helpers';

export type RideStatusPerspective = 'client' | 'driver';

@Component({
  selector: 'app-ride-status-tag',
  standalone: true,
  imports: [Tag],
  template: `<p-tag [value]="label()" [severity]="severity()" />`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RideStatusTagComponent {
  readonly status = input.required<RideStatus>();
  readonly perspective = input.required<RideStatusPerspective>();

  protected readonly label = computed(() =>
    this.perspective() === 'client'
      ? clientRideStatusLabel(this.status())
      : driverRideStatusLabel(this.status())
  );

  protected readonly severity = computed(() =>
    this.perspective() === 'client'
      ? clientRideStatusSeverity(this.status())
      : driverRideStatusSeverity(this.status())
  );
}
