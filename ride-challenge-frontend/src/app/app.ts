import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Toast } from 'primeng/toast';
import type { AccountType } from './core/api/api-dtos';
import { SessionService } from './core/session/session.service';

interface NavItem {
  label: string;
  route: string;
  type: AccountType | null; 
}

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  
  protected readonly account = this.session.account;

  private readonly navItems: NavItem[] = [
    { label: 'Minhas corridas', route: '/client/rides', type: 'CLIENT' },
    { label: 'Corridas disponíveis', route: '/driver/rides/available', type: 'DRIVER' },
  ];

  protected readonly visibleNavItems = computed(() => {
    const type = this.session.type();
    if (!type) return [];
    return this.navItems.filter((item) => !item.type || item.type === type);
  });

  protected typeLabel(type: AccountType): string {
    return type === 'DRIVER' ? 'Motorista' : 'Cliente';
  }

  protected logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
