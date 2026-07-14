import { TestBed } from '@angular/core/testing';
import type { LoginResponseDto } from '../api/api-dtos';
import { SessionService } from './session.service';

const STORAGE_KEY = 'ride-challenge.session';

const loginResponse: LoginResponseDto = {
  token: 'jwt-token',
  expiresIn: 86_400,
  accountId: 'client-1',
  name: 'Ana Cliente',
  email: 'ana@example.com',
  type: 'CLIENT',
};

describe('SessionService', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  function createService(): SessionService {
    TestBed.configureTestingModule({});
    return TestBed.inject(SessionService);
  }

  it('should start unauthenticated when there is no stored session', () => {
    const service = createService();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.account()).toBeNull();
    expect(service.token()).toBeNull();
  });

  it('should store the session and expose account data after login', () => {
    const service = createService();

    service.login(loginResponse);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.token()).toBe('jwt-token');
    expect(service.type()).toBe('CLIENT');
    expect(service.account()?.id).toBe('client-1');
    expect(localStorage.getItem(STORAGE_KEY)).not.toBeNull();
  });

  it('should clear the session on logout', () => {
    const service = createService();
    service.login(loginResponse);

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.token()).toBeNull();
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('should restore a valid stored session', () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: 'stored-token',
        expiresAt: Date.now() + 60_000,
        account: {
          id: 'client-1',
          name: 'Ana Cliente',
          email: 'ana@example.com',
          type: 'CLIENT',
        },
      })
    );

    const service = createService();

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.token()).toBe('stored-token');
  });

  it('should drop an expired stored session', () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: 'expired-token',
        expiresAt: Date.now() - 1,
        account: {
          id: 'client-1',
          name: 'Ana Cliente',
          email: 'ana@example.com',
          type: 'CLIENT',
        },
      })
    );

    const service = createService();

    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('should ignore malformed stored sessions', () => {
    localStorage.setItem(STORAGE_KEY, '{not-json');

    const service = createService();

    expect(service.isAuthenticated()).toBeFalse();
  });
});
