import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { SessionService } from './session.service';

const STORAGE_KEY = 'ride-challenge.session';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  function setup(): void {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  }

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should add the Authorization header when there is a session', () => {
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
    setup();

    http.get('/rides').subscribe();

    const request = httpMock.expectOne('/rides');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('should not add the Authorization header without a session', () => {
    localStorage.clear();
    setup();

    http.get('/rides').subscribe();

    const request = httpMock.expectOne('/rides');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush([]);
  });

  it('should stop sending the header after logout', () => {
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
    setup();
    TestBed.inject(SessionService).logout();

    http.get('/rides').subscribe();

    const request = httpMock.expectOne('/rides');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush([]);
  });
});
