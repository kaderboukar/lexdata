import { useEffect, useId } from "react";
import { AlertTriangle, X } from "lucide-react";

export default function ConfirmDialog({
  open,
  title = "Confirmer",
  description,
  confirmText = "Confirmer",
  cancelText = "Annuler",
  danger = false,
  onConfirm,
  onCancel,
  loading = false,
}) {
  const titleId = useId();
  const descId = useId();

  useEffect(() => {
    if (!open) return;
    const onKeyDown = (e) => {
      if (e.key === "Escape") onCancel?.();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onCancel]);

  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[100]">
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm"
        onClick={() => onCancel?.()}
        aria-hidden
      />

      <div className="relative mx-auto flex min-h-full max-w-lg items-center justify-center px-4 py-10">
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby={titleId}
          aria-describedby={description ? descId : undefined}
          className="w-full rounded-2xl border border-white/10 bg-slate-900/70 p-6 shadow-2xl backdrop-blur-md"
        >
          <div className="flex items-start justify-between gap-4">
            <div className="flex items-start gap-3">
              <div
                className={[
                  "mt-0.5 flex h-10 w-10 items-center justify-center rounded-xl border",
                  danger
                    ? "border-red-500/25 bg-red-500/10 text-red-300"
                    : "border-amber-500/25 bg-amber-500/10 text-amber-300",
                ].join(" ")}
                aria-hidden
              >
                <AlertTriangle size={18} />
              </div>

              <div className="min-w-0">
                <h2 id={titleId} className="text-lg font-extrabold text-slate-100">
                  {title}
                </h2>
                {description && (
                  <p id={descId} className="mt-1 text-sm leading-relaxed text-slate-300/80">
                    {description}
                  </p>
                )}
              </div>
            </div>

            <button
              type="button"
              className="rounded-lg p-2 text-slate-400 transition-colors hover:bg-white/5 hover:text-slate-100"
              onClick={() => onCancel?.()}
              aria-label="Fermer"
              disabled={loading}
            >
              <X size={18} aria-hidden />
            </button>
          </div>

          <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              className="inline-flex items-center justify-center rounded-xl border border-white/10 bg-white/5 px-4 py-2.5 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10 disabled:cursor-not-allowed disabled:opacity-50"
              onClick={() => onCancel?.()}
              disabled={loading}
            >
              {cancelText}
            </button>

            <button
              type="button"
              className={[
                "inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-extrabold transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-50",
                danger
                  ? "bg-red-500 text-white hover:bg-red-400"
                  : "bg-amber-500 text-slate-950 hover:bg-amber-400",
              ].join(" ")}
              onClick={() => onConfirm?.()}
              disabled={loading}
            >
              {loading ? "…" : confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

