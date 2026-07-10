import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { errorInterceptor } from './error.interceptor';
import { HttpClient } from '@angular/common/http';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(() => {
    messageService = jasmine.createSpyObj<MessageService>('MessageService', ['add']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messageService },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should show a clear toast message for API validation errors', () => {
    http.get('/rides').subscribe({ error: () => undefined });

    const request = httpMock.expectOne('/rides');
    request.flush(
      { errors: { startAddress: 'Ponto de partida é obrigatório.' } },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(messageService.add).toHaveBeenCalledWith(
      jasmine.objectContaining({
        severity: 'error',
        summary: 'Erro',
        detail: 'Ponto de partida é obrigatório.',
      })
    );
  });

  it('should show a conflict toast when another driver already accepted the ride', () => {
    http.post('/rides/ride-1/accept', { driverId: 'driver-1' }).subscribe({
      error: () => undefined,
    });

    const request = httpMock.expectOne('/rides/ride-1/accept');
    request.flush(null, { status: 409, statusText: 'Conflict' });

    expect(messageService.add).toHaveBeenCalledWith(
      jasmine.objectContaining({
        severity: 'error',
        summary: 'Erro',
        detail: 'Conflito: a corrida já foi aceita por outro motorista.',
      })
    );
  });
});