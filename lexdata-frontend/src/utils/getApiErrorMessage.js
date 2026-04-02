/**
 * Message d’erreur lisible depuis une réponse API (Axios ou fetch).
 * Compatible avec lexdata-notifications GlobalExceptionHandler : `{ error: "..." }`.
 */
export function getApiErrorMessage(error, fallback = "Une erreur est survenue.") {
  const data = error?.response?.data;

  if (data == null) {
    if (
      typeof error?.message === "string" &&
      error.message &&
      error.message !== "Network Error"
    ) {
      return error.message;
    }
    return fallback;
  }

  if (typeof data === "string" && data.trim()) return data;

  if (typeof data.error === "string" && data.error.trim()) return data.error;
  if (typeof data.message === "string" && data.message.trim()) return data.message;
  if (typeof data.detail === "string" && data.detail.trim()) return data.detail;

  if (Array.isArray(data.errors) && data.errors.length > 0) {
    const first = data.errors[0];
    if (typeof first === "string") return first;
    if (first?.defaultMessage) return String(first.defaultMessage);
    if (first?.message) return String(first.message);
  }

  return fallback;
}
