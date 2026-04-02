import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { PageLoader } from "../components/common/PageLoader";
import { getHomeRoute, hasAnyRole, normalizeRole } from "../utils/roles";

/**
 * 1. PRIVATE ROUTE (Le portail d'entrée général)
 * Bloque les visiteurs non connectés et les renvoie vers /login
 */
export const PrivateRoute = () => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  // CRITICAL: never render protected Outlet while auth is loading (prevents content flash).
  if (isLoading || (isAuthenticated && !user)) return <PageLoader fullscreen />;
  if (!isAuthenticated || !user) {
    // Redirige vers /login, mais sauvegarde l'URL que l'utilisateur essayait d'atteindre
    // (Pour le rediriger automatiquement au bon endroit après sa connexion)
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />; // Laisse passer vers les routes enfants
};

/**
 * 2. PUBLIC ONLY ROUTE (L'anti-retour)
 * Empêche un utilisateur DÉJÀ connecté de retourner sur les pages Login ou Register.
 */
export const PublicOnlyRoute = () => {
  const { isAuthenticated, user, isLoading } = useAuth();

  if (isLoading) return <PageLoader fullscreen />;
  if (isAuthenticated) {
    // Redirection intelligente selon le rôle de l'utilisateur
    return <Navigate to={getHomeRoute(user?.roles || [])} replace />;
  }

  return <Outlet />;
};

/**
 * 2b. EMAIL VERIFIED GUARD (conformité LegalTech)
 * Force la vérification email avant d'accéder au portail.
 */
export const EmailVerifiedGuard = () => {
  const { user, isLoading } = useAuth();
  if (isLoading) return <PageLoader fullscreen />;
  if (!user) return <Navigate to="/login" replace />;
  // Seul un refus explicite bloque : si l'API ne renvoie pas encore le flag, on n'envoie pas
  // vers resend (JwtResponse inclut désormais `emailVerified` côté auth-service).
  if (user.isEmailVerified === false) {
    return <Navigate to="/resend-verification" replace />;
  }
  return <Outlet />;
};

/**
 * 3. ROLE GUARD (Le videur VIP)
 * Vérifie si l'utilisateur possède le bon rôle pour entrer dans un Royaume spécifique.
 * Sans {@code children} : rend {@code <Outlet />} (routes imbriquées).
 */
export const RoleGuard = ({ roles }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) return <PageLoader fullscreen />;
  if (!user) return <Navigate to="/login" replace />;

  // On vérifie si l'utilisateur possède au moins un des rôles autorisés pour cette zone
  const userRoles = (user.roles || []).map(normalizeRole);
  const allowedRoles = (roles || []).map(normalizeRole);
  const hasRequiredRole = hasAnyRole(userRoles, allowedRoles);

  if (!hasRequiredRole) {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.warn(
        `[RoleGuard] Access denied. User roles: ${userRoles.join(",")}. Required: ${allowedRoles.join(",")}`,
      );
    }
    return <Navigate to={getHomeRoute(userRoles)} replace />;
  }

  return <Outlet />;
};

/**
 * 4. ADMIN ROUTE (raccourci)
 */
export const AdminRoute = () => <RoleGuard roles={["ROLE_AGENT_ADMIN", "ROLE_SUPER_ADMIN"]} />;

/**
 * 5. HOOK: check auth/roles inside pages (no redirect).
 */
export const useRequireAuth = (requiredRoles = []) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const isAuthorized =
    !isLoading &&
    Boolean(isAuthenticated && user) &&
    (requiredRoles.length ? hasAnyRole(user.roles || [], requiredRoles) : true);
  return { user, isAuthorized, isLoading };
};

// ⚡ NEXT STEPS:
// 1. Connect AuthContext.refreshToken() to intercept 401 responses in axios
// 2. Create src/pages/portal/UpgradePage.jsx for SubscriptionGuard redirect
// 3. Add audit logging service: src/services/auditLog.js (track denied accesses)
