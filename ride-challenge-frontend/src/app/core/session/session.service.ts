import { Injectable, computed, signal } from '@angular/core';
import type { AccountDto, AccountType, LoginResponseDto } from '../api/api-dtos';

const STORAGE_KEY = 'ride-challenge.session';

interface StoredSession {
  token: string;
  expiresAt: number;
  account: AccountDto;
}

function isStoredSession(value: unknown): value is StoredSession {
  if (typeof value !== 'object' || value === null) return false;

  const candidate = value as Partial<StoredSession>;
  return (
    typeof candidate.token === 'string' &&
    typeof candidate.expiresAt === 'number' &&
    typeof candidate.account === 'object' &&
    candidate.account !== null
  );
}

function readStoredSession(): StoredSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;

    const parsed = JSON.parse(raw) as unknown;
    if (!isStoredSession(parsed) || parsed.expiresAt <= Date.now()) {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }

    return parsed;
  } catch {
    return null;
  }
}






@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly sessionSignal = signal<StoredSession | null>(readStoredSession());

  
  readonly account = computed<AccountDto | null>(() => this.sessionSignal()?.account ?? null);

  
  readonly type = computed<AccountType | null>(() => this.account()?.type ?? null);

  readonly token = computed<string | null>(() => this.sessionSignal()?.token ?? null);

  readonly isAuthenticated = computed<boolean>(() => this.sessionSignal() !== null);

  login(response: LoginResponseDto): void {
    const session: StoredSession = {
      token: response.token,
      expiresAt: Date.now() + response.expiresIn * 1000,
      account: {
        id: response.accountId,
        name: response.name,
        email: response.email,
        type: response.type,
      },
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.sessionSignal.set(session);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.sessionSignal.set(null);
  }
}
