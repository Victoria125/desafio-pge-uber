import { inject } from '@angular/core';
import type { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { MessageService } from 'primeng/api';
import type { ApiErrorResponseDto } from '../api/api-dtos';
import { getMessageForHttpError } from './error-messages';





export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((err: unknown) => {
      const httpErr = err as HttpErrorResponse;

      const body =
        typeof httpErr.error === 'object' && httpErr.error !== null
          ? (httpErr.error as ApiErrorResponseDto)
          : null;

      const detail = getMessageForHttpError(httpErr.status, body);
      messageService.add({
        severity: 'error',
        summary: 'Erro',
        detail,
      });

      return throwError(() => err);
    })
  );
};
