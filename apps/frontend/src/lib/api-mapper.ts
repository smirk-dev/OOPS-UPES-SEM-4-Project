export type ApiEnvelope<T> = {
  success: boolean;
  data: T | null;
  error: {
    code: string;
    message: string;
    details?: string[];
  } | null;
  traceId: string;
  timestamp: string;
};

export function unwrapResponse<T>(payload: ApiEnvelope<T>): T {
  if (!payload.success) {
    throw new Error(payload.error?.message ?? "Request failed");
  }
  if (payload.data === null) {
    throw new Error("Request succeeded but response data was empty");
  }
  return payload.data;
}
