import api from "./axiosInstance";

/**
 * Service gérant toutes les communications avec lexdata-auth-service
 */
const authService = {
  // ==========================================
  // 1. AUTHENTIFICATION CLASSIQUE
  // ==========================================

  login: async (username, password) => {
    const response = await api.post("/auth/login", { username, password });
    return response.data; // Retourne { accessToken, refreshToken, user: { ... } }
  },

  register: async (userData) => {
    // userData contient : firstName, lastName, email, password, roleRequested...
    const response = await api.post("/auth/register", userData);
    return response.data;
  },

  // ==========================================
  // 2. GESTION DES SESSIONS
  // ==========================================

  // On envoie le refreshToken au backend pour qu'il le supprime de la base de données
  logout: async (refreshToken) => {
    const response = await api.post("/auth/logout", { refreshToken });
    return response.data;
  },

  // Le fameux "Coupe-Circuit" : Déconnecte l'utilisateur de tous ses appareils
  logoutAll: async () => {
    const response = await api.post("/auth/logout-all");
    return response.data;
  },

  // Optionnel ici car notre intercepteur Axios s'en occupe déjà,
  // mais utile si on veut forcer un rafraîchissement manuellement.
  refreshToken: async (token) => {
    const response = await api.post("/auth/refreshtoken", {
      refreshToken: token,
    });
    return response.data;
  },

  // ==========================================
  // 3. CYCLE DE VIE (Mot de passe oublié)
  // ==========================================

  forgotPassword: async (email) => {
    const response = await api.post("/auth/forgot-password", { email });
    return response.data;
  },

  resetPassword: async (token, newPassword) => {
    const response = await api.post("/auth/reset-password", {
      token,
      newPassword,
    });
    return response.data;
  },

  // ==========================================
  // 4. EMAIL VERIFICATION
  // ==========================================
  verifyEmail: async (token) => {
    const response = await api.get("/auth/verify-email", {
      params: { token },
    });
    return response.data;
  },

  resendVerification: async (email) => {
    // Backend: POST /api/auth/resend-verification?email=...
    const response = await api.post(
      "/auth/resend-verification",
      null,
      { params: { email } },
    );
    return response.data;
  },
};

export default authService;
