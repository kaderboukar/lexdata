import { useState } from "react";
import { Link } from "react-router-dom";
import { Scale, Loader2, Mail, ArrowLeft, MailCheck } from "lucide-react";
import authService from "../../api/authService";

export default function ResendVerificationPage() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [shake, setShake] = useState(false);

  const triggerShake = () => {
    setShake(false);
    setTimeout(() => setShake(true), 10);
  };

  const handleChange = (e) => {
    setEmail(e.target.value);
    if (error) setError(""); // On efface l'erreur dès que l'utilisateur corrige
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const data = await authService.resendVerification(email);
      setSuccess(data?.message || "Un nouvel email de vérification vous a été envoyé.");
      setEmail(""); // On vide le champ après un succès
    } catch (err) {
      triggerShake();
      if (!err.response) {
        setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
      } else {
        setError(
          err.response?.data?.message ||
            err.response?.data?.error ||
            "Impossible de renvoyer l'email. L'adresse est peut-être incorrecte ou déjà vérifiée.",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const inputBaseClass =
    "w-full rounded-lg border px-4 py-3 pr-11 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] transition-colors duration-300 " +
    "bg-[rgba(15,24,39,0.4)] focus:border-[var(--accent)] focus:outline-none focus:ring-2 focus:ring-[color:rgba(201,168,76,0.25)] " +
    "disabled:cursor-not-allowed disabled:opacity-60";

  const inputBorderClass = error ? "border-red-500" : "border-[var(--border-light)]";

  return (
    <div className="flex min-h-screen w-full bg-[var(--bg-body)] text-[var(--text-primary)]">
      <aside className="relative hidden w-[45%] max-w-[600px] flex-col justify-center overflow-hidden border-r border-[var(--border-light)] bg-gradient-to-br from-[var(--surface)] to-[var(--bg-body)] px-10 py-14 lg:flex xl:px-16">
        <div
          className="pointer-events-none absolute -bottom-40 -left-40 h-[min(50vw,28rem)] w-[min(50vw,28rem)] rounded-full bg-amber-500/10 blur-3xl"
          aria-hidden
        />
        <div
          className="pointer-events-none absolute -top-20 right-0 h-72 w-72 rounded-full bg-amber-400/5 blur-3xl"
          aria-hidden
        />
        <div
          className="pointer-events-none absolute bottom-1/4 right-1/4 h-48 w-48 rounded-full bg-amber-500/5 blur-2xl"
          aria-hidden
        />

        <div className="relative z-[2]">
          <Link
            to="/"
            className="mb-14 inline-flex items-center gap-3 text-2xl font-semibold text-[var(--text-primary)] no-underline transition-transform duration-300 hover:scale-105"
          >
            <Scale size={36} className="shrink-0 text-[var(--accent)]" aria-hidden />
            <span>
              Lex<strong className="font-extrabold">Data</strong>
            </span>
          </Link>

          <div className="max-w-[95%]">
            <h2 className="mb-6 text-[clamp(2rem,3vw,3rem)] font-extrabold leading-tight tracking-tight">
              Lien expiré ?
              <br />
              <span className="text-[var(--accent)]">On vous en renvoie un.</span>
            </h2>
            <p className="mb-10 text-lg leading-relaxed text-[var(--text-secondary)]">
              La sécurité de vos données est notre priorité. Si votre précédent lien d&apos;activation n&apos;est plus
              valide, demandez-en un nouveau ici.
            </p>
          </div>

          <ul className="flex flex-col gap-4">
            <li className="delay-75 flex items-center gap-4 rounded-[var(--radius)] border border-[var(--border-light)] bg-white/[0.02] px-5 py-4 text-base font-medium text-[var(--text-primary)] transition-all duration-300 hover:translate-x-2 hover:border-[color:rgba(201,168,76,0.3)] hover:bg-[color:rgba(201,168,76,0.05)]">
              <MailCheck size={24} className="shrink-0 text-[var(--accent)]" aria-hidden />
              <span>Réception instantanée dans votre boîte</span>
            </li>
          </ul>
        </div>
      </aside>

      <main className="relative flex flex-1 items-center justify-center p-4 sm:p-6 lg:p-8">
        <Link
          to="/login"
          className="absolute left-4 top-4 z-10 flex items-center gap-2 text-sm font-medium text-[var(--text-secondary)] no-underline transition-colors duration-300 hover:text-[var(--accent)] lg:hidden"
        >
          <ArrowLeft size={20} aria-hidden />
          Retour
        </Link>

        <div className="mt-12 w-full max-w-[440px] lg:mt-0">
          <div
            className={
              "rounded-[var(--radius-lg)] border border-white/10 bg-white/10 p-8 shadow-2xl backdrop-blur-md transition-shadow duration-300 sm:p-10 " +
              "lg:border-[var(--border-light)] lg:bg-[var(--surface-card)] lg:shadow-[var(--shadow-lg)] lg:backdrop-blur-xl " +
              "max-lg:border-white/10 max-lg:bg-white/[0.06] " +
              (shake ? "animate-shake" : "")
            }
          >
            <header className="mb-10 text-center">
              <h1 className="mb-2 text-4xl font-bold text-[var(--text-primary)]">Renvoyer le lien</h1>
              <p className="text-sm text-[var(--text-muted)]">Renseignez l&apos;adresse email liée à votre compte.</p>
            </header>

            {error && (
              <div
                id="resend-verification-error"
                className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600 dark:border-red-500/35 dark:bg-red-950/30 dark:text-red-400"
                role="alert"
                aria-live="polite"
              >
                {error}
              </div>
            )}
            {success && (
              <div
                id="resend-verification-success"
                className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600 dark:border-emerald-500/35 dark:bg-emerald-950/30 dark:text-emerald-400"
                role="alert"
                aria-live="polite"
              >
                {success}
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-6" noValidate>
              <div>
                <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="email">
                  Email Professionnel
                </label>
                <div className="relative">
                  <input
                    id="email"
                    className={`${inputBaseClass} ${inputBorderClass}`}
                    name="email"
                    type="email"
                    placeholder="vous@exemple.com"
                    value={email}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    autoFocus
                    autoComplete="email"
                    aria-invalid={!!error}
                    aria-describedby={
                      error ? "resend-verification-error" : success ? "resend-verification-success" : undefined
                    }
                  />
                  <Mail
                    size={18}
                    className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-zinc-500"
                    aria-hidden="true"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="mt-2 flex w-full items-center justify-center gap-2 rounded-[var(--radius)] bg-gradient-to-br from-[var(--accent)] to-[var(--accent-dark)] px-6 py-3.5 text-base font-bold text-[var(--primary-dark)] shadow-[var(--shadow-accent)] transition-all duration-300 hover:brightness-110 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-50"
                disabled={loading || !email.trim()}
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="shrink-0 animate-spin" aria-hidden />
                    Envoi en cours...
                  </>
                ) : (
                  "Renvoyer l'email"
                )}
              </button>
            </form>

            <p className="mt-10 text-center text-sm text-[var(--text-muted)]">
              Vous avez retrouvé votre email ?{" "}
              <Link
                to="/login"
                className="font-semibold text-[var(--accent)] no-underline transition-all duration-300 hover:text-[var(--accent-hover)]"
              >
                Retour à la connexion
              </Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
