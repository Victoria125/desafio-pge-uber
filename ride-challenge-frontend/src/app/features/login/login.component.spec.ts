import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { Router, provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { of } from 'rxjs';
import type { AccountType, LoginResponseDto } from '../../core/api/api-dtos';
import { AccountService } from '../../core/api/account.service';
import { SessionService } from '../../core/session/session.service';
import { LoginComponent } from './login.component';

const loginResponse: LoginResponseDto = {
  token: 'jwt-token',
  expiresIn: 86_400,
  accountId: 'client-1',
  name: 'Maria Silva',
  email: 'maria@email.com',
  type: 'CLIENT',
};

interface LoginComponentInternals {
  loginForm: FormGroup;
  createForm: FormGroup;
  submitLogin(): void;
  submitCreate(): void;
  selectType(type: AccountType): void;
  openCreateDialog(): void;
}

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponentInternals;
  let accountService: jasmine.SpyObj<AccountService>;
  let session: SessionService;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    accountService = jasmine.createSpyObj<AccountService>('AccountService', [
      'login',
      'createAccount',
    ]);
    accountService.login.and.returnValue(of(loginResponse));
    accountService.createAccount.and.returnValue(of({ id: 'client-1' }));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        providePrimeNG(),
        { provide: AccountService, useValue: accountService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance as unknown as LoginComponentInternals;
    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should not call the API when the login form is invalid', () => {
    component.loginForm.setValue({ email: 'not-an-email', password: '' });

    component.submitLogin();

    expect(accountService.login).not.toHaveBeenCalled();
  });

  it('should authenticate, store the session and navigate on login', () => {
    component.loginForm.setValue({ email: 'maria@email.com', password: 'secret123' });

    component.submitLogin();

    expect(accountService.login).toHaveBeenCalledWith({
      email: 'maria@email.com',
      password: 'secret123',
    });
    expect(session.isAuthenticated()).toBeTrue();
    expect(session.token()).toBe('jwt-token');
    expect(router.navigate).toHaveBeenCalledWith(['/'], { replaceUrl: true });
  });

  it('should not call the API when the create account form is invalid', () => {
    component.openCreateDialog();
    component.createForm.setValue({ name: '', email: 'maria@email.com', password: '123' });

    component.submitCreate();

    expect(accountService.createAccount).not.toHaveBeenCalled();
  });

  it('should create the account with the selected type and log in right after', () => {
    component.openCreateDialog();
    component.selectType('DRIVER');
    component.createForm.setValue({
      name: ' Joao Souza ',
      email: 'joao@email.com',
      password: 'secret123',
    });

    component.submitCreate();

    expect(accountService.createAccount).toHaveBeenCalledWith({
      name: 'Joao Souza',
      email: 'joao@email.com',
      password: 'secret123',
      type: 'DRIVER',
    });
    expect(accountService.login).toHaveBeenCalledWith({
      email: 'joao@email.com',
      password: 'secret123',
    });
    expect(session.isAuthenticated()).toBeTrue();
    expect(router.navigate).toHaveBeenCalledWith(['/'], { replaceUrl: true });
  });
});
