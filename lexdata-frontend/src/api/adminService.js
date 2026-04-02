import api from "./axiosInstance";

/**
 * Service gérant toutes les communications réservées aux Administrateurs
 */
const adminService = {
  // ==========================================
  // 1. STATISTIQUES GLOBALES
  // ==========================================
  getDashboardStats: async () => {
    const response = await api.get("/admin/stats");
    return response.data; // ex: { totalUsers, totalTextes, activeSubscriptions }
  },

  // ==========================================
  // 2. GESTION DES TEXTES (CRUD)
  // ==========================================
  getAllTextes: async (params = {}) => {
    const response = await api.get("/juridique/textes", {
      params: { ...params, includeNonPublie: true },
    });
    return response.data;
  },

  createTexte: async (texteData) => {
    const response = await api.post("/juridique/textes", texteData);
    return response.data;
  },

  updateTexte: async (id, texteData) => {
    const response = await api.put(`/juridique/textes/${id}`, texteData);
    return response.data;
  },

  updateTexteStatus: async (id, status) => {
    // Note: on utilise params car Spring Boot attend @RequestParam("status")
    const response = await api.patch(`/juridique/textes/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  deleteTexte: async (id) => {
    const response = await api.delete(`/juridique/textes/${id}`);
    return response.data;
  },

  // ==========================================
  // 3. GESTION DES UTILISATEURS
  // ==========================================
  getAllUsers: async (params = {}) => {
    const response = await api.get("/auth/users", { params });
    return response.data;
  },

  updateUserRole: async (userId, role) => {
    const response = await api.patch(`/auth/users/${userId}/role`, {
      role,
    });
    return response.data;
  },


  // ==========================================
  // VEILLE JURIDIQUE
  // ==========================================

  getAllAlertes: async (params = {}) => {
    const response = await api.get("/veille/admin/alertes", { params });
    return response.data;
  },

  createAlerte: async (data) => {
    const response = await api.post("/veille/admin/alertes", data);
    return response.data;
  },

  // Note: D'après votre controller, vous n'avez pas encore de PUT /admin/alertes/{id}
  updateAlerte: async (id, data) => {
    const response = await api.put(`/veille/admin/alertes/${id}`, data);
    return response.data;
  },

  // Note: D'après votre controller, vous n'avez pas de DELETE /admin/alertes/{id}
  deleteAlerte: async (id) => {
    const response = await api.delete(`/veille/admin/alertes/${id}`);
    return response.data;
  },

  updateAlerteStatus: async (id, status) => {
    const response = await api.patch(
      `/veille/admin/alertes/${id}/status`,
      null,
      {
        params: { status },
      },
    );
    return response.data;
  },
};

export default adminService;
