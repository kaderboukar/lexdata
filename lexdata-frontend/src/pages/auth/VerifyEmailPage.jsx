import { useEffect, useMemo, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Scale, Loader2, MailCheck, ShieldCheck, XCircle, ArrowRight } from "lucide-react";
import authService from "../../api/authService";

export default function VerifyEmailPage() {
  const location = useLocation();

  const token = useMemo(() => {
    const params = new URLSearchParams(location.search);
    return params.get("token");
  }, [location.search]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [shake, setShake] = useState(false);

  const triggerShake = () => {
    setShake(false);
    setTimeout(() => setShake(true), 10);
  };

  useEffect(() => {
    let isMounted = true;

    const verifyToken = async () => {
      setLoading(true);
      setError("");
      setSuccess("");

      try {
        if (!token) {
          throw new Error("Jeton de sécurité manquant dans l'URL.");
        }

        // Simuler un léger délai pour que l'animation de chargement soit visible (UX premium)
        await new Promise((resolve) => setTimeout(resolve, 800));

        const data = await authService.verifyEmail(token);

        if (isMounted) {
          setSuccess(data?.message || "Votre adresse email a été vérifiée avec succès.");
        }
      } catch (err) {
        if (isMounted) triggerShake();
        if (!err.response) {
          if (isMounted) setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
        } else {
          if (isMounted) {
            setError(
              err.response?.data?.message ||
                err.response?.data?.error ||
                "Impossible de vérifier l'email. Le lien est peut-être expiré ou déjà utilisé.",
            );
          }
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    verifyToken();
    return () => {
      isMounted = false;
    };
  }, [token]);

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
              Vérification d&apos;identité,
              <br />
              <span className="text-[var(--accent)]">sécurité garantie.</span>
            </h2>
            <p className="mb-10 text-lg leading-relaxed text-[var(--text-secondary)]">
              L&apos;accès à LexData requiert une adresse email validée pour garantir un réseau d&apos;experts certifiés
              et fiables.
            </p>
          </div>

          <ul className="flex flex-col gap-4">
            <li className="delay-75 flex items-center gap-4 rounded-[var(--radius)] border border-[var(--border-light)] bg-white/[0.02] px-5 py-4 text-base font-medium text-[var(--text-primary)] transition-all duration-300 hover:translate-x-2 hover:border-[color:rgba(201,168,76,0.3)] hover:bg-[color:rgba(201,168,76,0.05)]">
              <ShieldCheck size={24} className="shrink-0 text-[var(--accent)]" aria-hidden />
              <span>Accès exclusif aux professionnels</span>
            </li>
          </ul>
        </div>
      </aside>

      <main className="relative flex flex-1 items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-[460px]">
          <div
            className={
              "rounded-[var(--radius-lg)] border border-white/10 bg-white/10 px-8 py-16 text-center shadow-2xl backdrop-blur-md transition-shadow duration-300 sm:px-12 " +
              "lg:border-[var(--border-light)] lg:bg-[var(--surface-card)] lg:shadow-[var(--shadow-lg)] lg:backdrop-blur-xl " +
              "max-lg:border-white/10 max-lg:bg-white/[0.06] " +
              (shake ? "animate-shake" : "")
            }
          >
            {loading && (
              <div>
                <Loader2 size={64} className="mb-4 shrink-0 animate-spin text-[var(--accent)] mx-auto" aria-hidden />
                <h1 className="mb-2 text-3xl font-bold text-[var(--text-primary)]">Vérification en cours</h1>
                <p className="text-sm text-[var(--text-muted)]">
                  Nous validons votre jeton de sécurité, veuillez patienter quelques instants...
                </p>
              </div>
            )}

            {!loading && error && (
              <div>
                <XCircle
                  size={64}
                  className="mx-auto mb-4 text-[var(--danger)]"
                  strokeWidth={1.75}
                  aria-hidden
                />
                <h1 className="mb-2 text-3xl font-bold text-[var(--text-primary)]">Lien invalide</h1>
                <div
                  className="mb-4 rounded-md bg-red-50 p-4 text-left text-sm text-red-600 dark:bg-red-950/30 dark:text-red-400"
                  role="alert"
                  aria-live="polite"
                >
                  {error}
                </div>
                <Link
                  to="/resend-verification"
                  className="mt-4 inline-flex w-full items-center justify-center rounded-[var(--radius)] bg-gradient-to-br from-[var(--accent)] to-[var(--accent-dark)] px-6 py-3.5 text-base font-bold text-[var(--primary-dark)] shadow-[var(--shadow-accent)] no-underline transition-all duration-300 hover:brightness-110 hover:shadow-lg"
                >
                  Demander un nouveau lien
                </Link>
                <div className="mt-4">
                  <Link
                    to="/login"
                    className="text-sm font-semibold text-[var(--accent)] no-underline transition-colors duration-300 hover:text-[var(--accent-hover)]"
                  >
                    Retour à la connexion
                  </Link>
                </div>
              </div>
            )}

            {!loading && success && (
              <div>
                <div
                  className="mx-auto mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-emerald-500/10"
                  aria-hidden
                >
                  <MailCheck size={40} className="text-emerald-500 dark:text-emerald-400" />
                </div>

                <h1 className="mb-2 text-3xl font-bold text-[var(--text-primary)]">Compte activé !</h1>
                <p className="mb-4 text-sm text-[var(--text-muted)]">{success}</p>

                <Link
                  to="/login"
                  className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-[var(--radius)] bg-gradient-to-br from-[var(--accent)] to-[var(--accent-dark)] px-6 py-3.5 text-base font-bold text-[var(--primary-dark)] shadow-[var(--shadow-accent)] no-underline transition-all duration-300 hover:brightness-110 hover:shadow-lg"
                >
                  Accéder à mon espace
                  <ArrowRight size={18} className="shrink-0" aria-hidden />
                </Link>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
