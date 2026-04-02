import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Scale, CheckCircle2, Eye, EyeOff, Loader2, ArrowLeft } from "lucide-react";
import authService from "../../api/authService";
import useAuthStore from "../../store/useAuthStore";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const loginStore = useAuthStore((s) => s.login);

  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [shake, setShake] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const from = location.state?.from?.pathname || null;

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (error) setError("");
  };

  const triggerShake = () => {
    setShake(false);
    setTimeout(() => setShake(true), 10);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data = await authService.login(form.username, form.password);
      const token = data.accessToken || data.token;
      const refreshToken = data.refreshToken;

      const baseUser = data.user || {
        id: data.id,
        username: data.username,
        email: data.email,
        roles: data.roles || [],
        features: data.features || [],
        firstName: data.firstName,
        lastName: data.lastName,
      };

      const userData = {
        ...baseUser,
        isEmailVerified:
          baseUser.isEmailVerified ??
          baseUser.emailVerified ??
          data.isEmailVerified ??
          data.emailVerified,
      };

      loginStore(userData, token, refreshToken);

      if (from) {
        navigate(from, { replace: true });
      } else {
        const isAdmin = userData.roles?.some((r) => r.includes("ADMIN"));
        navigate(isAdmin ? "/admin" : "/dashboard", { replace: true });
      }
    } catch (err) {
      triggerShake();
      if (!err.response) {
        setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
      } else {
        setError(
          err.response?.data?.message ||
            err.response?.data?.error ||
            "Identifiants incorrects ou requête invalide.",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const inputBaseClass =
    "w-full rounded-lg border px-4 py-3 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] transition-colors duration-300 " +
    "bg-[rgba(15,24,39,0.4)] focus:border-[var(--accent)] focus:outline-none focus:ring-2 focus:ring-[color:rgba(201,168,76,0.25)] " +
    "disabled:cursor-not-allowed disabled:opacity-60";

  const inputErrorClass = error ? "border-[var(--danger)] aria-invalid:border-[var(--danger)]" : "border-[var(--border-light)]";

  return (
    <div className="flex min-h-screen w-full bg-[var(--bg-body)] text-[var(--text-primary)]">
      {/* Branding — desktop split */}
      <aside className="relative hidden w-[45%] max-w-[600px] flex-col justify-center overflow-hidden border-r border-[var(--border-light)] bg-gradient-to-br from-[var(--surface)] to-[var(--bg-body)] px-10 py-14 lg:flex xl:px-16">
        {/* Halos premium (glow) */}
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
              Bienvenue dans
              <br />
              <span className="text-[var(--accent)]">votre espace.</span>
            </h2>
            <p className="mb-10 text-lg leading-relaxed text-[var(--text-secondary)]">
              Retrouvez vos dossiers, vos annotations et vos alertes de veille juridique là où vous les aviez laissés.
            </p>
          </div>

          <ul className="flex flex-col gap-4">
            {[
              { label: "Connexion chiffrée de bout en bout", delayClass: "delay-75" },
              { label: "Sauvegarde automatique Cloud", delayClass: "delay-150" },
              { label: "Accès multi-appareils synchronisé", delayClass: "delay-200" },
            ].map(({ label, delayClass }) => (
              <li
                key={label}
                className={`flex items-center gap-4 rounded-[var(--radius)] border border-[var(--border-light)] bg-white/[0.02] px-5 py-4 text-base font-medium text-[var(--text-primary)] transition-all duration-300 hover:translate-x-2 hover:border-[color:rgba(201,168,76,0.3)] hover:bg-[color:rgba(201,168,76,0.05)] ${delayClass}`}
              >
                <CheckCircle2 size={24} className="shrink-0 text-[var(--accent)]" aria-hidden />
                <span>{label}</span>
              </li>
            ))}
          </ul>
        </div>
      </aside>

      {/* Formulaire */}
      <main className="relative flex flex-1 items-center justify-center p-4 sm:p-6 lg:p-8">
        <Link
          to="/"
          className="absolute left-4 top-4 z-10 flex items-center gap-2 text-sm font-medium text-[var(--text-secondary)] no-underline transition-colors duration-300 hover:text-[var(--accent)] lg:hidden"
        >
          <ArrowLeft size={20} aria-hidden />
          Retour
        </Link>

        <div className="mt-12 w-full max-w-[440px] lg:mt-0">
          <div
            className={
              "rounded-[var(--radius-lg)] border p-8 shadow-2xl backdrop-blur-xl transition-shadow duration-300 sm:p-10 lg:border-[var(--border-light)] lg:bg-[var(--surface-card)] lg:shadow-[var(--shadow-lg)] " +
              "max-lg:border-transparent max-lg:bg-transparent max-lg:shadow-none max-lg:backdrop-blur-none " +
              (shake ? "animate-shake" : "")
            }
          >
            <header className="mb-10 text-center">
              <h1 className="mb-2 text-4xl font-bold text-[var(--text-primary)]">Se connecter</h1>
              <p className="text-sm text-[var(--text-muted)]">Entrez vos identifiants pour accéder à la plateforme.</p>
            </header>

            {location.search.includes("session_expired=true") && (
              <div
                className="mb-4 rounded-lg border border-[color:rgba(52,152,219,0.35)] bg-[color:rgba(52,152,219,0.12)] px-4 py-3 text-sm text-[var(--info)]"
                role="status"
                aria-live="polite"
              >
                Votre session a expiré par mesure de sécurité. Veuillez vous reconnecter.
              </div>
            )}

            {error && (
              <div
                id="login-error-summary"
                className="mb-4 rounded-lg border border-[color:rgba(231,76,60,0.35)] bg-[color:rgba(231,76,60,0.12)] px-4 py-3 text-sm text-[var(--danger)]"
                role="alert"
                aria-live="polite"
              >
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-6" noValidate>
              <div>
                <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="username">
                  Nom d&apos;utilisateur ou Email
                </label>
                <input
                  id="username"
                  className={`${inputBaseClass} ${inputErrorClass}`}
                  name="username"
                  placeholder="ex: a.diallo@cabinet.ne"
                  value={form.username}
                  onChange={handleChange}
                  required
                  autoFocus
                  disabled={loading}
                  autoComplete="username"
                  aria-invalid={!!error}
                  aria-describedby={error ? "login-error-summary" : undefined}
                />
              </div>

              <div>
                <div className="mb-2 flex items-center justify-between gap-2">
                  <label className="text-sm font-semibold text-[var(--text-secondary)]" htmlFor="password">
                    Mot de passe
                  </label>
                  <Link
                    to="/forgot-password"
                    className="text-xs font-medium text-[var(--text-muted)] no-underline transition-colors duration-300 hover:text-[var(--accent)]"
                    tabIndex={-1}
                  >
                    Mot de passe oublié ?
                  </Link>
                </div>
                <div className="relative">
                  <input
                    id="password"
                    className={`${inputBaseClass} ${inputErrorClass} pr-12`}
                    name="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    value={form.password}
                    onChange={handleChange}
                    required
                    disabled={loading}
                    autoComplete="current-password"
                    aria-invalid={!!error}
                    aria-describedby={error ? "login-error-summary" : undefined}
                  />
                  <button
                    type="button"
                    className="absolute right-3 top-1/2 flex -translate-y-1/2 items-center justify-center rounded p-1 text-[var(--text-muted)] transition-all duration-300 hover:scale-110 hover:text-[var(--text-primary)] active:scale-95"
                    onClick={() => setShowPassword(!showPassword)}
                    tabIndex={-1}
                    aria-label={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
                  >
                    {showPassword ? <EyeOff size={18} aria-hidden /> : <Eye size={18} aria-hidden />}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                className="mt-2 flex w-full items-center justify-center gap-2 rounded-[var(--radius)] bg-gradient-to-br from-[var(--accent)] to-[var(--accent-dark)] px-6 py-3.5 text-base font-bold text-[var(--primary-dark)] shadow-[var(--shadow-accent)] transition-all duration-300 hover:brightness-110 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-60"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="animate-spin shrink-0" aria-hidden />
                    Authentification...
                  </>
                ) : (
                  "Se connecter"
                )}
              </button>
            </form>

            <p className="mt-10 text-center text-sm text-[var(--text-muted)]">
              Pas encore de compte ?{" "}
              <Link
                to="/register"
                className="font-semibold text-[var(--accent)] no-underline transition-all duration-300 hover:text-[var(--accent-hover)]"
              >
                Créer un compte
              </Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
