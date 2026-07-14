import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import type { AccountType } from '../api/api-dtos';
import { createRoleGuard } from './role.guard';

const STORAGE_KEY = 'ride-challenge.session';

function storeSession(type: AccountType): void {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: 'jwt-token',
      expiresAt: Date.now() + 60_000,
      account: {
        id: 'account-1',
        name: 'Conta Teste',
        email: 'conta@example.com',
        type,
      },
    })
  );
}

function runGuard(requiredType: AccountType): boolean | UrlTree {
  return TestBed.runInInjectionContext(() =>
    createRoleGuard(requiredType)({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
  ) as boolean | UrlTree;
}

describe('createRoleGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow access when the session type matches', () => {
    storeSession('DRIVER');

    expect(runGuard('DRIVER')).toBeTrue();
  });

  it('should redirect to /forbidden when the session type differs', () => {
    storeSession('CLIENT');

    const result = runGuard('DRIVER');

    expect(result instanceof UrlTree).toBeTrue();
    expect(result.toString()).toBe('/forbidden');
  });

  it('should redirect to /forbidden without a session', () => {
    localStorage.clear();

    const result = runGuard('CLIENT');

    expect(result instanceof UrlTree).toBeTrue();
    expect(result.toString()).toBe('/forbidden');
  });
});
