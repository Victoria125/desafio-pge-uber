import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import { MessageService } from 'primeng/api';
import Aura from '@primeuix/themes/aura';

import { environment } from '../environments/environment';
import { API_BASE_URL } from './core/api/api-base-url.token';
import { errorInterceptor } from './core/errors/error.interceptor';
import { authInterceptor } from './core/session/auth.interceptor';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: API_BASE_URL, useValue: environment.apiUrl },
    MessageService,
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideRouter(routes),
    providePrimeNG({
      theme: {
        preset: Aura,
      },
    }),
  ],
};
