import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, NavLink, useNavigate, useLocation } from "react-router-dom";
import {
  BookOpen,
  BellRing,
  Landmark,
  Menu,
  X,
  Scale,
  LogOut,
  ShieldAlert,
} from "lucide-react";
import useAuthStore from "../../store/useAuthStore";
import "./Navbar.css";

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();

  // Récupération de l'état global avec Zustand
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated); // ⚠️ Ensure useAuthStore exposes computed booleans
  const isAdmin = useAuthStore((s) => s.isAdmin); // ⚠️ Ensure useAuthStore exposes computed booleans
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);

  const dashboardLink = useMemo(() => (isAdmin ? "/admin" : "/dashboard"), [isAdmin]);

  // Gestion du scroll pour l'effet Glassmorphism
  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20); // Déclenchement plus rapide pour réagir vite
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Gestion du Body Lock (Empêcher le scroll derrière le menu mobile)
  useEffect(() => {
    if (mobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => { document.body.style.overflow = 'unset'; };
  }, [mobileMenuOpen]);

  // Fermer le menu mobile si la route change
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

  const closeMenu = useCallback(() => setMobileMenuOpen(false), []);

  const handleLogout = useCallback(() => {
    logout();
    closeMenu();
    navigate("/login");
  }, [logout, closeMenu, navigate]);

  return (
    <header className={`navbar ${scrolled ? "navbar-scrolled" : ""}`}>
      <div className="container navbar-container">

        {/* LOGO */}
        <Link to="/" className="navbar-brand" onClick={closeMenu}>
          <Scale size={28} className="brand-icon" />
          <span className="brand-text">LexData</span>
        </Link>

        {/* LIENS DE NAVIGATION */}
        {/* Overlay (mobile) : bloque les clics derrière le drawer */}
        {mobileMenuOpen && (
          <button
            type="button"
            className="navbar-overlay"
            aria-hidden="true"
            tabIndex={-1}
            onClick={closeMenu}
          />
        )}

        <nav className={`navbar-links ${mobileMenuOpen ? "mobile-open" : ""}`}>
          <NavLink to="/textes" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <BookOpen size={18} className="nav-icon" /> Base Juridique
          </NavLink>
          <NavLink to="/veille" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <BellRing size={18} className="nav-icon" /> Veille
          </NavLink>
          <NavLink to="/annuaire" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
            <Landmark size={18} className="nav-icon" /> Annuaire
          </NavLink>

          {/* ACTIONS MOBILE (Visibles uniquement dans le menu burger) */}
          <div className="navbar-actions mobile-only fade-in">
            {isAuthenticated ? (
              <>
                <Link to={dashboardLink} className="btn btn-secondary btn-mobile" onClick={closeMenu}>
                  <UserAvatar user={user} isAdmin={isAdmin} />
                  <span>Mon Espace</span>
                </Link>
                <button onClick={handleLogout} className="btn btn-danger btn-mobile" type="button">
                  <LogOut size={18} /> Déconnexion
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary btn-mobile" onClick={closeMenu}>
                  Connexion
                </Link>
                <Link to="/register" className="btn btn-primary btn-mobile" onClick={closeMenu}>
                  S'inscrire
                </Link>
              </>
            )}
          </div>
        </nav>

        {/* ACTIONS DESKTOP */}
        <div className="navbar-actions desktop-only">
          {isAuthenticated ? (
            <div className="user-menu-desktop">
              <Link to={dashboardLink} className="btn btn-ghost user-badge">
                <UserAvatar user={user} isAdmin={isAdmin} />
                <span>{user?.firstName || user?.username || "Profil"}</span>
              </Link>
              <button onClick={handleLogout} className="btn btn-ghost logout-btn" aria-label="Se déconnecter">
                <LogOut size={18} />
              </button>
            </div>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost">Connexion</Link>
              <Link to="/register" className="btn btn-primary scale-in">S'inscrire</Link>
            </>
          )}
        </div>

        {/* BOUTON MENU MOBILE */}
        <button
          className="mobile-toggle"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-expanded={mobileMenuOpen}
          aria-label={mobileMenuOpen ? "Fermer le menu" : "Ouvrir le menu"}
        >
          {mobileMenuOpen ? <X size={28} className="text-accent" /> : <Menu size={28} />}
        </button>
      </div>
    </header>
  );
}

const UserAvatar = ({ user, isAdmin }) => {
  const first = (user?.firstName || "").trim();
  const last = (user?.lastName || "").trim();
  const initials = `${first.slice(0, 1)}${last.slice(0, 1)}`.toUpperCase() || "U";
  const profilePicture = user?.profilePicture || user?.profilePictureUrl || user?.avatarUrl;

  return (
    <span className="user-avatar" aria-hidden="true">
      {profilePicture ? (
        <img className="user-avatar__img" src={profilePicture} alt="" />
      ) : (
        <span className="user-avatar__initials">{initials}</span>
      )}
      {isAdmin && (
        <span className="user-avatar__badge" aria-hidden="true">
          <ShieldAlert size={12} />
        </span>
      )}
    </span>
  );
};

// ⚡ NEXT STEPS:
// 1. Extract UserAvatar → src/components/common/UserAvatar.jsx
// 2. Sync useAuthStore: ensure isAuthenticated + isAdmin are boolean getters
// 3. Build PortalLayout Sidebar using same design tokens