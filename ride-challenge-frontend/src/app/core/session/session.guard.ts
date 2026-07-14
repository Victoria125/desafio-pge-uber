import { inject } from '@angular/core';
import { Router, type CanActivateFn, type UrlTree } from '@angular/router';
import { SessionService } from './session.service';





export const sessionGuard: CanActivateFn = (): boolean | UrlTree => {
  const session = inject(SessionService);
  if (session.isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login']);
};
