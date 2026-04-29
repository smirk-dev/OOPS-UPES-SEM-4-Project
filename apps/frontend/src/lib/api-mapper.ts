export type ApiEnvelope<T> = {
  success: boolean;
  data?: T | null;
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
    const message = payload.error?.message ?? "Request failed";
    const details = payload.error?.details;
    if (details && details.length > 0) {
      throw new Error(`${message} (${details.join("; ")})`);
    }
    throw new Error(message);
  }
  if (payload.data == null) {
    throw new Error("Request succeeded but response data was empty");
  }
  return payload.data;
}
