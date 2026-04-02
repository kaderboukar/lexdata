import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Scale, CheckCircle2, Loader2, Eye, EyeOff, ArrowLeft } from "lucide-react";
import authService from "../../api/authService";

export default function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    firstName: "",
    lastName: "",
    telephone: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [shake, setShake] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (error) setError(""); // Efface l'erreur à la saisie
  };

  const triggerShake = () => {
    setShake(false);
    setTimeout(() => setShake(true), 10);
  };

  // Vérification visuelle rapide pour l'UX
  const passwordsMatch = form.password && form.confirmPassword && form.password === form.confirmPassword;
  const passwordMismatch = form.confirmPassword && form.password !== form.confirmPassword;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (form.password !== form.confirmPassword) {
      triggerShake();
      return setError("Les mots de passe ne correspondent pas.");
    }

    if (form.password.length < 6) {
      triggerShake();
      return setError("Le mot de passe doit contenir au moins 6 caractères.");
    }

    setLoading(true);
    try {
      const payload = {
        username: form.username,
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        telephone: form.telephone,
      };

      await authService.register(payload);

      setSuccess("Compte créé avec succès ! Redirection vers la connexion...");

      // On redirige vers le login avec le nom d'utilisateur pré-rempli
      setTimeout(() => navigate(`/login?username=${form.username}`), 2500);
    } catch (err) {
      triggerShake();
      if (!err.response) {
        setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
      } else {
        setError(
          err.response?.data?.message ||
            err.response?.data?.error ||
            "Erreur lors de l'inscription. L'email ou le nom d'utilisateur est peut-être déjà pris.",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const inputBaseClass =
    "w-full rounded-lg border px-4 py-3 text-[var(--text-primary)] placeholder:text-[var(--text-muted)] transition-colors duration-300 " +
    "bg-[rgba(15,24,39,0.4)] focus:outline-none disabled:cursor-not-allowed disabled:opacity-60";

  const inputDefaultFocus = "focus:border-[var(--accent)] focus:ring-2 focus:ring-[color:rgba(201,168,76,0.25)]";

  const inputNeutral = `${inputBaseClass} ${inputDefaultFocus} border-[var(--border-light)]`;

  const inputConfirmClass = [
    inputBaseClass,
    passwordMismatch
      ? "border-red-500 focus:border-red-500 focus:ring-2 focus:ring-red-500/30"
      : passwordsMatch
        ? "border-emerald-500 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/30"
        : `${inputDefaultFocus} border-[var(--border-light)]`,
  ].join(" ");

  return (
    <div className="flex min-h-screen w-full bg-[var(--bg-body)] text-[var(--text-primary)]">
      {/* Branding — desktop split */}
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
              L&apos;excellence juridique,
              <br />
              <span className="text-[var(--accent)]">centralisée.</span>
            </h2>
            <p className="mb-10 text-lg leading-relaxed text-[var(--text-secondary)]">
              Rejoignez des milliers de professionnels qui gagnent un temps précieux chaque jour grâce à notre base
              de données et nos outils d&apos;IA.
            </p>
          </div>

          <ul className="flex flex-col gap-4">
            {[
              { label: "Accès à +10 000 textes de loi", delayClass: "delay-75" },
              { label: "Veille personnalisée en temps réel", delayClass: "delay-150" },
              { label: "Réseau d'experts certifiés", delayClass: "delay-200" },
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

      <main className="relative flex flex-1 items-center justify-center p-4 sm:p-6 lg:p-8">
        <Link
          to="/"
          className="absolute left-4 top-4 z-10 flex items-center gap-2 text-sm font-medium text-[var(--text-secondary)] no-underline transition-colors duration-300 hover:text-[var(--accent)] lg:hidden"
        >
          <ArrowLeft size={20} aria-hidden />
          Retour
        </Link>

        <div className="mt-12 w-full max-w-xl lg:mt-0">
          <div
            className={
              "rounded-[var(--radius-lg)] border p-8 shadow-2xl backdrop-blur-xl transition-shadow duration-300 sm:p-10 lg:border-[var(--border-light)] lg:bg-[var(--surface-card)] lg:shadow-[var(--shadow-lg)] " +
              "max-lg:border-transparent max-lg:bg-transparent max-lg:shadow-none max-lg:backdrop-blur-none " +
              (shake ? "animate-shake" : "")
            }
          >
            <header className="mb-8 text-center">
              <h1 className="mb-2 text-3xl font-bold text-[var(--text-primary)] sm:text-4xl">Créer un compte</h1>
              <p className="text-sm text-[var(--text-muted)]">Remplissez vos informations pour commencer.</p>
            </header>

            {error && (
              <div
                id="register-error-summary"
                className="mb-4 rounded-lg border border-[color:rgba(231,76,60,0.35)] bg-[color:rgba(231,76,60,0.12)] px-4 py-3 text-sm text-[var(--danger)]"
                role="alert"
                aria-live="assertive"
              >
                {error}
              </div>
            )}
            {success && (
              <div
                id="register-success-summary"
                className="mb-4 rounded-lg border border-[color:rgba(39,174,96,0.35)] bg-[color:rgba(39,174,96,0.12)] px-4 py-3 text-sm text-emerald-600 dark:text-emerald-400"
                role="alert"
                aria-live="assertive"
              >
                {success}
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-5" noValidate>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="firstName">
                    Prénom
                  </label>
                  <input
                    id="firstName"
                    className={inputNeutral}
                    name="firstName"
                    value={form.firstName}
                    onChange={handleChange}
                    required
                    disabled={loading || success}
                    autoComplete="given-name"
                    aria-invalid={!!error}
                    aria-describedby={error ? "register-error-summary" : undefined}
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="lastName">
                    Nom
                  </label>
                  <input
                    id="lastName"
                    className={inputNeutral}
                    name="lastName"
                    value={form.lastName}
                    onChange={handleChange}
                    required
                    disabled={loading || success}
                    autoComplete="family-name"
                    aria-invalid={!!error}
                    aria-describedby={error ? "register-error-summary" : undefined}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="username">
                    Nom d&apos;utilisateur
                  </label>
                  <input
                    id="username"
                    className={inputNeutral}
                    name="username"
                    placeholder="ex: oumar.d"
                    value={form.username}
                    onChange={handleChange}
                    required
                    disabled={loading || success}
                    autoComplete="username"
                    aria-invalid={!!error}
                    aria-describedby={error ? "register-error-summary" : undefined}
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="telephone">
                    Téléphone
                  </label>
                  <input
                    id="telephone"
                    className={inputNeutral}
                    name="telephone"
                    placeholder="+227..."
                    value={form.telephone}
                    onChange={handleChange}
                    disabled={loading || success}
                    autoComplete="tel"
                    aria-invalid={!!error}
                    aria-describedby={error ? "register-error-summary" : undefined}
                  />
                </div>
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="email">
                  Email Professionnel
                </label>
                <input
                  id="email"
                  className={inputNeutral}
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  required
                  disabled={loading || success}
                  autoComplete="email"
                  aria-invalid={!!error}
                  aria-describedby={error ? "register-error-summary" : undefined}
                />
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]" htmlFor="password">
                    Mot de passe
                  </label>
                  <div className="relative">
                    <input
                      id="password"
                      className={`${inputNeutral} pr-12`}
                      name="password"
                      type={showPassword ? "text" : "password"}
                      value={form.password}
                      onChange={handleChange}
                      required
                      minLength={6}
                      disabled={loading || success}
                      autoComplete="new-password"
                      aria-invalid={!!error}
                      aria-describedby={error ? "register-error-summary" : undefined}
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

                <div>
                  <label
                    className="mb-2 block text-sm font-semibold text-[var(--text-secondary)]"
                    htmlFor="confirmPassword"
                  >
                    Confirmer
                  </label>
                  <input
                    id="confirmPassword"
                    className={inputConfirmClass}
                    name="confirmPassword"
                    type={showPassword ? "text" : "password"}
                    value={form.confirmPassword}
                    onChange={handleChange}
                    required
                    disabled={loading || success}
                    autoComplete="new-password"
                    aria-invalid={!!passwordMismatch}
                    aria-describedby={
                      [error && "register-error-summary", passwordMismatch && "register-confirm-hint"]
                        .filter(Boolean)
                        .join(" ") || undefined
                    }
                  />
                  {passwordMismatch && (
                    <p id="register-confirm-hint" className="mt-1 text-xs text-red-500">
                      Ne correspond pas
                    </p>
                  )}
                </div>
              </div>

              <button
                type="submit"
                className="mt-1 flex w-full items-center justify-center gap-2 rounded-[var(--radius)] bg-gradient-to-br from-[var(--accent)] to-[var(--accent-dark)] px-6 py-3.5 text-base font-bold text-[var(--primary-dark)] shadow-[var(--shadow-accent)] transition-all duration-300 hover:brightness-110 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-50"
                disabled={loading || success}
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="shrink-0 animate-spin" aria-hidden />
                    Création en cours...
                  </>
                ) : (
                  "Créer mon compte"
                )}
              </button>
            </form>

            <p className="mt-8 text-center text-sm text-[var(--text-muted)]">
              Déjà inscrit ?{" "}
              <Link
                to="/login"
                className="font-semibold text-[var(--accent)] no-underline transition-all duration-300 hover:text-[var(--accent-hover)]"
              >
                Connectez-vous ici
              </Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
