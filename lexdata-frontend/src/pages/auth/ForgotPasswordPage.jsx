import { useState } from "react";
import { Link } from "react-router-dom";
import { Scale, Loader2, Mail, ArrowLeft, ShieldCheck } from "lucide-react";
import authService from "../../api/authService";
import "./AuthPage.css";

export default function ForgotPasswordPage() {
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
    if (error) setError(""); // Efface l'erreur dès que l'utilisateur retape
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      const data = await authService.forgotPassword(email);
      setSuccess(data?.message || "Un lien de réinitialisation a été envoyé à votre adresse email.");
    } catch (err) {
      triggerShake();
      if (!err.response) {
        setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
      } else {
        setError(
          err.response?.data?.message ||
          err.response?.data?.error ||
          "Impossible de trouver un compte associé à cet email."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      {/* COLONNE GAUCHE : Branding */}
      <aside className="auth-sidebar">
        <div className="auth-sidebar-content scale-in">
          <Link to="/" className="sidebar-logo">
            <Scale size={36} color="var(--accent)" />
            <span>Lex<strong>Data</strong></span>
          </Link>

          <div className="sidebar-quote">
            <h2>Mot de passe oublié ?<br /><span className="text-accent">Pas de panique.</span></h2>
            <p>Renseignez l'adresse email associée à votre compte pour recevoir un lien de réinitialisation sécurisé.</p>
          </div>

          <div className="sidebar-features mt-5">
            <div className="feature-item" style={{ transitionDelay: "0.1s" }}>
              <ShieldCheck size={24} className="text-accent" />
              <span>Processus de récupération sécurisé</span>
            </div>
          </div>
        </div>
        <div className="sidebar-glow"></div>
      </aside>

      {/* COLONNE DROITE : Formulaire */}
      <main className="auth-form-container fade-in">
        {/* Bouton retour mobile */}
        <Link to="/login" className="mobile-back-btn">
          <ArrowLeft size={20} /> Retour
        </Link>

        <div className="auth-form-wrapper" style={{ maxWidth: '440px' }}>
          <div className={`auth-box ${shake ? "shake" : ""}`}>
            <div className="form-header">
              <h1>Réinitialiser</h1>
              <p>Entrez l'adresse email de votre compte LexData.</p>
            </div>

            {error && <div className="alert alert-error mb-4 fade-in">{error}</div>}
            {success && <div className="alert alert-success mb-4 scale-in">{success}</div>}

            <form onSubmit={handleSubmit} className="auth-form">
              <div className="form-group">
                <label className="form-label" htmlFor="email">Email Professionnel</label>
                {/* Utilisation de la classe de wrapper existante pour positionner l'icône */}
                <div className="password-input-wrapper">
                  <input
                    id="email"
                    className={`form-control ${error ? 'error' : ''}`}
                    name="email"
                    type="email"
                    placeholder="vous@exemple.com"
                    value={email}
                    onChange={handleChange}
                    required
                    disabled={loading || success}
                    autoFocus
                    autoComplete="email"
                  />
                  <Mail
                    size={18}
                    className="text-muted"
                    style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }}
                  />
                </div>
              </div>

              <button
                type="submit"
                className={`btn btn-primary btn-full mt-4 ${loading ? 'is-loading' : ''}`}
                disabled={loading || success || !email.trim()}
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="spinner-icon" style={{ animation: "spin 1s linear infinite" }} />
                    Envoi en cours...
                  </>
                ) : (
                  "Envoyer le lien"
                )}
              </button>
            </form>

            <div className="auth-footer mt-5 text-center">
              <span className="text-muted">Je me souviens de mon mot de passe. </span>
              <Link to="/login" className="auth-link">
                Retour à la connexion
              </Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}