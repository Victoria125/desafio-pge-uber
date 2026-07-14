import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { finalize, switchMap } from 'rxjs/operators';
import { AccountService } from '../../core/api/account.service';
import type { AccountType, LoginResponseDto } from '../../core/api/api-dtos';
import { SessionService } from '../../core/session/session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, Button, Dialog, InputText],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly accountService = inject(AccountService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  protected readonly loggingIn = signal(false);
  protected readonly createDialogVisible = signal(false);
  protected readonly creating = signal(false);
  protected readonly selectedType = signal<AccountType>('CLIENT');

  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected readonly createForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(1)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loggingIn.set(true);
    const { email, password } = this.loginForm.getRawValue();
    this.accountService
      .login({ email: email.trim(), password })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loggingIn.set(false))
      )
      .subscribe({
        next: (response) => this.completeLogin(response),
      });
  }

  protected selectType(type: AccountType): void {
    this.selectedType.set(type);
  }

  protected openCreateDialog(): void {
    this.createForm.reset({ name: '', email: '', password: '' });
    this.createDialogVisible.set(true);
  }

  protected closeCreateDialog(): void {
    this.createDialogVisible.set(false);
  }

  protected submitCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.creating.set(true);
    const { name, email, password } = this.createForm.getRawValue();
    const trimmedEmail = email.trim();

    this.accountService
      .createAccount({
        name: name.trim(),
        email: trimmedEmail,
        password,
        type: this.selectedType(),
      })
      .pipe(
        switchMap(() => this.accountService.login({ email: trimmedEmail, password })),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.creating.set(false))
      )
      .subscribe({
        next: (response) => {
          this.closeCreateDialog();
          this.completeLogin(response);
        },
      });
  }

  private completeLogin(response: LoginResponseDto): void {
    this.session.login(response);
    this.router.navigate(['/'], { replaceUrl: true });
  }
}
