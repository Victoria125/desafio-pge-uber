import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import { sessionGuard } from './session.guard';

const STORAGE_KEY = 'ride-challenge.session';

function runGuard(): boolean | UrlTree {
  return TestBed.runInInjectionContext(() =>
    sessionGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
  ) as boolean | UrlTree;
}

describe('sessionGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])],
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow navigation when authenticated', () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: 'jwt-token',
        expiresAt: Date.now() + 60_000,
        account: {
          id: 'client-1',
          name: 'Ana Cliente',
          email: 'ana@example.com',
          type: 'CLIENT',
        },
      })
    );

    expect(runGuard()).toBeTrue();
  });

  it('should redirect to /login without a session', () => {
    localStorage.clear();

    const result = runGuard();

    expect(result instanceof UrlTree).toBeTrue();
    expect(result.toString()).toBe('/login');
  });
});
