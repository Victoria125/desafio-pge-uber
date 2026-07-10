import { inject } from '@angular/core';
import { Router, type CanActivateFn, type UrlTree } from '@angular/router';
import type { AccountType } from '../api/api-dtos';
import { SessionService } from './session.service';






export function createRoleGuard(requiredType: AccountType): CanActivateFn {
  return (): boolean | UrlTree => {
    const session = inject(SessionService);
    if (session.type() === requiredType) {
      return true;
    }
    return inject(Router).createUrlTree(['/forbidden']);
  };
}
