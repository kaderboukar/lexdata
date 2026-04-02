import React, { Suspense, lazy } from "react";
import { BrowserRouter, Routes, Route, Outlet } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "react-hot-toast";

// --- COMPONENTS & GUARDS ---
import { PrivateRoute, RoleGuard, PublicOnlyRoute, EmailVerifiedGuard } from "./router/guards";
import { ErrorBoundary } from "./components/common/ErrorBoundary"; // À créer (optionnel mais recommandé)
import { AuthProvider } from "./context/AuthContext";
import { PageLoader } from "./components/common/PageLoader";
import PublicLayout from "./components/layout/PublicLayout";
import PortalLayout from "./components/layout/PortalLayout";
import BareLayout from "./components/layout/BareLayout";

// --- PAGES (Lazy Loading pour une app ultra-rapide) ---
const LandingPage = lazy(() => import("./pages/public/LandingPage")); // Page: landing
const LoginPage = lazy(() => import("./pages/auth/LoginPage")); // Page: login
const RegisterPage = lazy(() => import("./pages/auth/RegisterPage")); // Page: register
const VerifyEmailPage = lazy(() => import("./pages/auth/VerifyEmailPage")); // Page: verify email
const ResendVerificationPage = lazy(() => import("./pages/auth/ResendVerificationPage")); // Page: resend verification
const ForgotPasswordPage = lazy(() => import("./pages/auth/ForgotPasswordPage")); // Page: forgot password
const ResetPasswordPage = lazy(() => import("./pages/auth/ResetPasswordPage")); // Page: reset password
const DashboardPage = lazy(() => import("./pages/portal/UserDashboard")); // Page: dashboard
const TextesPage = lazy(() => import("./pages/portal/Recherche")); // Page: textes
const TexteDetailPage = lazy(() => import("./pages/portal/TexteDetailPage")); // Page: texte detail
const VeillePage = lazy(() => import("./pages/portal/VeillePage")); // Page: veille
const ProfilePage = lazy(() => import("./pages/portal/ProfilePage")); // Page: profile
const SettingsPage = lazy(() => import("./pages/portal/SettingsPage")); // Page: settings
const AdminDashboardPage = lazy(() => import("./pages/admin/AdminDashboard")); // Page: admin dashboard
const AdminUsersPage = lazy(() => import("./pages/admin/AdminUsersPage")); // Page: admin users
const NotFoundPage = lazy(() => import("./pages/public/NotFoundPage")); // Page: 404
const ContactPage = lazy(() => import("./pages/public/ContactPage")); // Page: contact

// ==========================================
// 1. CONFIGURATION TANSTACK QUERY (Optimisée)
// ==========================================
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
      refetchOnWindowFocus: false,
      throwOnError: false,
    },
    mutations: {
      throwOnError: false,
    },
  },
});

// React Router ne passe pas de children aux routes parentes : les sous-routes passent par <Outlet />.
// Sans Outlet ici, ErrorBoundary rendait undefined → page vide (seul le fond --bg-body visible).
const AppErrorBoundary = () => (
  <ErrorBoundary>
    <Outlet />
  </ErrorBoundary>
);

// ==========================================
// 3. CONFIGURATION DES RÔLES
// ==========================================
const CLIENT_ROLES = ["ROLE_USER", "ROLE_JURISTE", "ROLE_AVOCAT"];
const ADMIN_ROLES = ["ROLE_AGENT_ADMIN", "ROLE_SUPER_ADMIN"];

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        {/* Toast configuré pour matcher ton index.css doré */}
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            className: "glass-card",
            style: {
              background: "var(--surface-card)",
              color: "var(--text-primary)",
              border: "1px solid var(--border)",
              backdropFilter: "blur(10px)",
            },
            success: {
              iconTheme: { primary: "var(--success)", secondary: "#fff" },
            },
            error: {
              iconTheme: { primary: "var(--danger)", secondary: "#fff" },
            },
          }}
        />

        <BrowserRouter>
          <Suspense fallback={<PageLoader fullscreen label="Chargement de l’application..." />}>
            <Routes>
              <Route element={<AppErrorBoundary />}>
                <Route element={<PublicLayout />}>
                  <Route path="/" element={<LandingPage />} />
                  <Route path="/contact" element={<ContactPage />} />
                </Route>

                <Route element={<BareLayout />}>
                  <Route element={<PublicOnlyRoute />}>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                  </Route>
                  <Route path="/verify-email" element={<VerifyEmailPage />} />
                  <Route path="/resend-verification" element={<ResendVerificationPage />} />
                  <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                  <Route path="/reset-password" element={<ResetPasswordPage />} />
                </Route>

                {/* ROYAUME PORTAL (PRIVÉ) — routes imbriquées via <Outlet /> */}
                <Route element={<PrivateRoute />}>
                  {/* Guard order: PrivateRoute -> EmailVerifiedGuard -> RoleGuard */}
                  <Route element={<EmailVerifiedGuard />}>
                    {/* <Route element={<PortalLayout />}> */}
                      <Route element={<RoleGuard roles={CLIENT_ROLES} />}>
                        <Route path="/dashboard" element={<DashboardPage />} />
                        <Route path="/textes" element={<TextesPage />} />
                        <Route path="/textes/:id" element={<TexteDetailPage />} />
                        <Route path="/veille" element={<VeillePage />} />
                        <Route path="/profile" element={<ProfilePage />} />
                        <Route path="/settings" element={<SettingsPage />} />
                      </Route>

                      <Route element={<RoleGuard roles={ADMIN_ROLES} />}>
                        <Route path="/admin" element={<AdminDashboardPage />} />
                        <Route path="/admin/users" element={<AdminUsersPage />} />
                      </Route>
                    {/* </Route> */}
                  </Route>
                </Route>

                <Route element={<PublicLayout />}>
                  <Route path="*" element={<NotFoundPage />} />
                </Route>
              </Route>
            </Routes>
          </Suspense>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

// ⚡ NEXT STEPS:
// 1. Implement PortalLayout with real Sidebar → src/components/layout/PortalLayout.jsx
// 2. Implement PrivateRoute & RoleGuard → src/router/guards.jsx
// 3. Connect AuthContext.login() to your Spring Boot /api/auth/login endpoint