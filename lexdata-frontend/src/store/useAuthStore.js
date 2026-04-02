import axios from "axios";
import { create } from "zustand";
import { persist } from "zustand/middleware";

const API_URL = import.meta.env.VITE_API_URL || "/api";

const normalizeRole = (role) => {
  if (!role) return role;
  return role.startsWith("ROLE_") ? role : `ROLE_${role}`;
};

const computeIsAuthenticated = (token, logoutInProgress) => Boolean(token) && !logoutInProgress;
const computeIsAdmin = (user) => {
  const roles = (user?.roles || []).map(normalizeRole);
  return roles.includes("ROLE_AGENT_ADMIN") || roles.includes("ROLE_SUPER_ADMIN");
};

const useAuthStore = create(
  persist(
    (set, get) => ({
      // L'objet user contiendra désormais : { id, username, email, roles: [], features: [] }
      user: null,
      token: null,
      refreshToken: null,
      logoutInProgress: false,
      isAuthenticated: false,
      isAdmin: false,

      // --- ACTIONS ---
      login: (userData, token, refreshToken) =>
        set({
          user: userData,
          token,
          refreshToken,
          logoutInProgress: false,
          isAuthenticated: computeIsAuthenticated(token, false),
          isAdmin: computeIsAdmin(userData),
        }),

      // Déconnexion "propre" : appel backend (révocation refresh token)
      // + vidage immédiat du state côté frontend.
      logout: async () => {
        const accessToken = get().token;
        const refreshToken = get().refreshToken;

        // On coupe tout de suite l'accès côté UI (redirection, guards, etc.)
        set({
          user: null,
          token: null,
          refreshToken: null,
          logoutInProgress: true,
          isAuthenticated: false,
          isAdmin: false,
        });

        try {
          if (refreshToken) {
            await axios.post(
              `${API_URL}/auth/logout`,
              { refreshToken },
              {
                headers: {
                  "Content-Type": "application/json",
                  ...(accessToken
                    ? { Authorization: `Bearer ${accessToken}` }
                    : {}),
                },
              },
            );
          }
        } catch {
          // On ignore les erreurs de déconnexion : côté frontend, la session est déjà invalidée.
        } finally {
          set({ logoutInProgress: false });
        }
      },

      setTokens: (token, refreshToken) =>
        set((state) => ({
          token,
          refreshToken,
          isAuthenticated: computeIsAuthenticated(token, state.logoutInProgress),
        })),

      // Permet de mettre à jour uniquement le profil (ex: après un achat d'abonnement)
      updateUser: (newUserData) =>
        set((state) => ({
          user: { ...state.user, ...newUserData },
          isAdmin: computeIsAdmin({ ...state.user, ...newUserData }),
        })),

      // --- VÉRIFICATIONS DE RÔLES (Identité) ---
      hasRole: (role) => {
        const roles = (get().user?.roles || []).map(normalizeRole);
        return roles.includes(normalizeRole(role));
      },

      isSuperAdmin: () =>
        (get().user?.roles || []).map(normalizeRole).includes("ROLE_SUPER_ADMIN"),

      // --- VÉRIFICATIONS DE DROITS (Abonnements & Monétisation) ---

      // NOUVEAU : La méthode clé pour le Paywall (ex: hasFeature('READ_SYNTHESIS'))
      hasFeature: (featureName) => {
        // Les admins ont accès à tout par défaut pour pouvoir tester/modérer
        if (get().isAdmin) return true;

        const features = get().user?.features || [];
        return features.includes(featureName);
      },

      // REFONTE : isPremium vérifie maintenant si l'utilisateur possède un pass Premium,
      // et non plus s'il a le rôle "Avocat" (un avocat peut avoir un compte gratuit !)
      isPremium: () => {
        if (get().isAdmin) return true;

        const features = get().user?.features || [];
        return features.includes("PREMIUM_ACCESS");
      },

      // --- ÉTAT DE SESSION ---
      // ⚠️ Ensure useAuthStore exposes isAuthenticated and isAdmin
      // as computed booleans, not functions, for clean selector usage.
    }),
    {
      name: "lexdata-auth", // Nom de la clé dans le localStorage
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
        isAdmin: state.isAdmin,
      }),
    },
  ),
);

export default useAuthStore;
