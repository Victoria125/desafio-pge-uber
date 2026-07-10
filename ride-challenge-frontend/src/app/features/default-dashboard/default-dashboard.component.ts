import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import { SessionService } from '../../core/session/session.service';






@Component({
  selector: 'app-default-dashboard',
  standalone: true,
  template: `<p class="redirect-msg">Redirecionando…</p>`,
  styles: `
    .redirect-msg {
      padding: 2rem;
      text-align: center;
      color: var(--cinza-700, #495057);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DefaultDashboardComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  ngOnInit(): void {
    const type = this.session.type();
    if (type === 'CLIENT') {
      this.router.navigate(['/client/rides'], { replaceUrl: true });
      return;
    }
    if (type === 'DRIVER') {
      this.router.navigate(['/driver/rides/available'], { replaceUrl: true });
      return;
    }
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
