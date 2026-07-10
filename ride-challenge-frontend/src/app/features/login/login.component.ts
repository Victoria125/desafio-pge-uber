import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Tag } from 'primeng/tag';
import { finalize } from 'rxjs/operators';
import { AccountService } from '../../core/api/account.service';
import type { AccountDto, AccountType } from '../../core/api/api-dtos';
import { SessionService } from '../../core/session/session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    Button,
    Dialog,
    InputText,
    Select,
    Tag,
  ],
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

  protected readonly accounts = signal<AccountDto[]>([]);
  protected readonly selectedType = signal<AccountType>('CLIENT');
  protected readonly loading = signal(false);
  protected readonly createDialogVisible = signal(false);
  protected readonly creating = signal(false);

  protected readonly filteredAccounts = computed(() =>
    this.accounts().filter((account) => account.type === this.selectedType())
  );

  protected readonly typeOptions: { label: string; value: AccountType }[] = [
    { label: 'Cliente (passageiro)', value: 'CLIENT' },
    { label: 'Motorista', value: 'DRIVER' },
  ];

  protected createForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(1)]],
    email: ['', [Validators.required, Validators.email]],
    type: this.fb.nonNullable.control<AccountType>('CLIENT', Validators.required),
  });

  constructor() {
    this.loadAccounts();
  }

  protected loadAccounts(): void {
    this.loading.set(true);
    this.accountService
      .listAccounts()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (list) => this.accounts.set(list),
      });
  }

  protected selectType(type: AccountType): void {
    this.selectedType.set(type);
  }

  protected enter(account: AccountDto): void {
    this.session.login(account);
    this.router.navigate(['/']);
  }

  protected openCreateDialog(): void {
    this.createForm.reset({ name: '', email: '', type: this.selectedType() });
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
    const { name, email, type } = this.createForm.getRawValue();
    this.accountService
      .createAccount({ name: name.trim(), email: email.trim(), type })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.creating.set(false))
      )
      .subscribe({
        next: (res) => {
          this.closeCreateDialog();
          this.selectedType.set(type);
          this.enter({ id: res.id, name: name.trim(), email: email.trim(), type });
        },
      });
  }

  protected typeLabel(type: AccountType): string {
    return type === 'DRIVER' ? 'Motorista' : 'Cliente';
  }

  protected typeCount(type: AccountType): number {
    return this.accounts().filter((account) => account.type === type).length;
  }
}
