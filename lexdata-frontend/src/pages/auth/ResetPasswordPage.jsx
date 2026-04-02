import { useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Scale, Loader2, ShieldCheck, Eye, EyeOff, ArrowLeft } from "lucide-react";
import authService from "../../api/authService";

export default function ResetPasswordPage() {
  const location = useLocation();
  const navigate = useNavigate();

  // Récupération du token depuis l'URL (ex: ?token=abcde...)
  const token = useMemo(() => {
    const params = new URLSearchParams(location.search);
    return params.get("token");
  }, [location.search]);

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [shake, setShake] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const triggerShake = () => {
    setShake(false);
    setTimeout(() => setShake(true), 10);
  };

  const handlePasswordChange = (e, setter) => {
    setter(e.target.value);
    if (error) setError("");
  };

  // Validation visuelle en temps réel
  const passwordsMatch = newPassword && confirmPassword && newPassword === confirmPassword;
  const passwordMismatch = confirmPassword && newPassword !== confirmPassword;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!token) {
      triggerShake();
      setError("Lien de réinitialisation invalide ou expiré. Veuillez refaire une demande.");
      return;
    }

    if (newPassword !== confirmPassword) {
      triggerShake();
      setError("Les mots de passe ne correspondent pas.");
      return;
    }

    if (newPassword.length < 6) {
      triggerShake();
      setError("Le mot de passe doit contenir au moins 6 caractères.");
      return;
    }

    setLoading(true);
    try {
      const data = await authService.resetPassword(token, newPassword);
      setSuccess(data?.message || "Mot de passe mis à jour avec succès. Redirection...");
      setTimeout(() => navigate("/login"), 2500);
    } catch (err) {
      triggerShake();
      if (!err.response) {
        setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
      } else {
        setError(
          err.response?.data?.message ||
            err.response?.data?.error ||
            "Impossible de réinitialiser le mot de passe. Le lien est peut-être expiré.",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const inputBaseClass =
    "w-full rounded-lg border px-4 py-3 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] transition-colors duration-300 " +
    "bg-[rgba(15,24,39,0.4)] focus:outline-none disabled:cursor-not-allowed disabled:opacity-60";

  const focusAccent = "focus:border-[var(--accent)] focus:ring-2 focus:ring-[color:rgba(201,168,76,0.25)]";

  const newPasswordClass = `${inputBaseClass} ${focusAccent} border-[var(--border-light)] pr-12`;

  const confirmClass = [
    inputBaseClass,
    passwordMismatch
      ? "border-red-500 focus:border-red-500 focus:ring-2 focus:ring-red-500/30"
      : passwordsMatch
        ? "border-emerald-500 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/30"
        : `${focusAccent} border-[var(--border-light)]`,
  ].join(" ");

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
              Nouveau départ,
              <br />
              <span className="text-[var(--accent)]">nouveau code.</span>
            </h2>
            <p className="mb-10 text-lg leading-relaxed text-[var(--text-secondary)]">
              Saisissez un mot de passe robuste pour protéger vos dossiers et vos recherches juridiques.
            </p>
          </div>

          <ul className="flex flex-col gap-4">
            <li className="delay-75 flex items-center gap-4 rounded-[var(--radius)] border border-[var(--border-light)] bg-white/[0.02] px-5 py-4 text-base font-medium text-[var(--text-primary)] transition-all duration-300 hover:translate-x-2 hover:border-[color:rgba(201,168,76,0.3)] hover:bg-[color:rgba(201,168,76,0.05)]">
              <ShieldCheck size={24} className="shrink-0 text-[var(--accent)]" aria-hidden />
              <span>Cryptage de bout en bout</span>
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
          Annuler
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
              <h1 className="mb-2 text-4xl font-bold text-[var(--text-primary)]">Créer un mot de passe</h1>
              <p className="text-sm text-[var(--text-muted)]">Choisissez un nouveau mot de passe sécurisé.</p>
            </header>

            {!token && (
              <div
                className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-left text-sm text-red-600 dark:border-red-500/35 dark:bg-red-950/30 dark:text-red-400"
                role="alert"
                aria-live="polite"
              >
                Aucun jeton de sécurité trouvé dans l&apos;URL. Veuillez utiliser le lien exact reçu par email.
              </div>
            )}

            {error && (
              <div
                id="reset-password-error"
                className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600 dark:border-red-500/35 dark:bg-red-950/30 dark:text-red-400"
                role="alert"
                aria-live="polite"
              >
                {error}
              </div>
            )}
            {success && (
              <div
                id="reset-password-success"
                className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600 dark:border-emerald-500/35 dark:bg-emerald-950/30 dark:text-emerald-400"
                role="alert"
                aria-live="polite"
              >
                {success}
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-6" noValidate>
              <div className="flex flex-col gap-6">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="newPassword">
                    Nouveau mot de passe
                  </label>
                  <div className="relative">
                    <input
                      id="newPassword"
                      className={newPasswordClass}
                      name="newPassword"
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={newPassword}
                      onChange={(e) => handlePasswordChange(e, setNewPassword)}
                      required
                      minLength={6}
                      disabled={loading || success || !token}
                      autoFocus
                      autoComplete="new-password"
                      aria-invalid={!!error}
                      aria-describedby={error ? "reset-password-error" : undefined}
                    />
                    <button
                      type="button"
                      className="absolute right-3 top-1/2 flex -translate-y-1/2 items-center justify-center rounded p-1 text-[var(--text-muted)] transition-all duration-300 hover:scale-110 hover:text-[var(--text-primary)] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
                      onClick={() => setShowPassword(!showPassword)}
                      tabIndex={-1}
                      aria-label={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
                      disabled={loading || success || !token}
                    >
                      {showPassword ? <EyeOff size={18} aria-hidden /> : <Eye size={18} aria-hidden />}
                    </button>
                  </div>
                </div>

                <div>
                  <label
                    className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]"
                    htmlFor="confirmPassword"
                  >
                    Confirmer le mot de passe
                  </label>
                  <input
                    id="confirmPassword"
                    className={confirmClass}
                    name="confirmPassword"
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    value={confirmPassword}
                    onChange={(e) => handlePasswordChange(e, setConfirmPassword)}
                    required
                    disabled={loading || success || !token}
                    autoComplete="new-password"
                    aria-invalid={!!passwordMismatch}
                    aria-describedby={
                      passwordMismatch ? "reset-confirm-hint" : error ? "reset-password-error" : undefined
                    }
                  />
                  {passwordMismatch && (
                    <p id="reset-confirm-hint" className="mt-1 text-xs text-red-500">
                      Les mots de passe ne correspondent pas
                    </p>
                  )}
                </div>
              </div>

              <button
                type="submit"
                className="mt-2 flex w-full items-center justify-center gap-2 rounded-[var(--radius)] bg-gradient-to-br from-[var(--accent)] to-[var(--accent-dark)] px-6 py-3.5 text-base font-bold text-[var(--primary-dark)] shadow-[var(--shadow-accent)] transition-all duration-300 hover:brightness-110 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-50"
                disabled={loading || success || !token || !newPassword || !confirmPassword}
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="shrink-0 animate-spin" aria-hidden />
                    Mise à jour...
                  </>
                ) : (
                  "Sauvegarder et se connecter"
                )}
              </button>
            </form>

            <p className="mt-10 text-center text-sm text-[var(--text-muted)]">
              Vous avez retrouvé la mémoire ?{" "}
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
