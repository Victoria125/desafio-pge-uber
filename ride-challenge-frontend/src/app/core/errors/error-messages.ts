import type { ApiErrorResponseDto } from '../api/api-dtos';





const STATUS_MESSAGES: Record<number, string> = {
  400: 'Erro de validação. Verifique o formulário e tente novamente.',
  404: 'Recurso não encontrado.',
  409: 'Conflito: a corrida já foi aceita por outro motorista.',
  500: 'Ocorreu um erro inesperado. Tente novamente.',
};

const DEFAULT_MESSAGE = 'Ocorreu um erro.';






export function getMessageForHttpError(
  status: number,
  body: ApiErrorResponseDto | null
): string {
  if (body?.errors && typeof body.errors === 'object') {
    const messages = Object.values(body.errors).filter(
      (m) => typeof m === 'string' && m.length > 0
    );
    if (messages.length > 0) {
      return messages.join(' ');
    }
  }
  return STATUS_MESSAGES[status] ?? DEFAULT_MESSAGE;
}
