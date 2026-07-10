import { Injectable, computed, signal } from '@angular/core';
import type { AccountDto, AccountType } from '../api/api-dtos';

const STORAGE_KEY = 'ride-challenge.session';

function readStoredAccount(): AccountDto | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AccountDto) : null;
  } catch {
    return null;
  }
}






@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly accountSignal = signal<AccountDto | null>(readStoredAccount());

  
  readonly account = this.accountSignal.asReadonly();

  
  readonly type = computed<AccountType | null>(() => this.accountSignal()?.type ?? null);

  login(account: AccountDto): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(account));
    this.accountSignal.set(account);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.accountSignal.set(null);
  }
}
