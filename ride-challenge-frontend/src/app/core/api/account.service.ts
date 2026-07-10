import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type {
  AccountDto,
  CreateAccountRequestDto,
  CreateAccountResponseDto,
} from './api-dtos';
import { API_BASE_URL } from './api-base-url.token';
import { apiRoutes } from './api-routes';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  createAccount(body: CreateAccountRequestDto): Observable<CreateAccountResponseDto> {
    return this.http.post<CreateAccountResponseDto>(apiRoutes.accounts(this.baseUrl), body);
  }

  listAccounts(): Observable<AccountDto[]> {
    return this.http.get<AccountDto[]>(apiRoutes.accounts(this.baseUrl));
  }

  getAccountById(accountId: string): Observable<AccountDto> {
    return this.http.get<AccountDto>(apiRoutes.accountById(this.baseUrl, accountId));
  }
}