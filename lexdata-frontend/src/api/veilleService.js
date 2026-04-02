import api from "./axiosInstance";

/**
 * lexdata-veille — VeilleController (/api/veille)
 * Alertes utilisateur : id = UserAlertDto.id (clé pour PATCH read / DELETE).
 * GET /feed renvoie des AlerteVeilleDto avec id = alertId (ne pas utiliser pour markRead).
 */

const veilleService = {
  /** Fil simplifié (page/size uniquement côté contrôleur) */
  getFeed: async (params = {}) => {
    const response = await api.get("/veille/feed", { params });
    return response.data;
  },

  /**
   * Alertes personnalisées paginées (UserAlertDto).
   * Params : unreadOnly, domaine, type, fromDate, toDate, page, size, sort
   */
  getMyAlerts: async (params = {}) => {
    const response = await api.get("/veille/alerts/me", { params });
    return response.data;
  },

  /** Alerte source publiée par id */
  getAlerteById: async (id) => {
    const response = await api.get(`/veille/alertes/${id}`);
    return response.data;
  },

  /** PATCH /veille/alerts/me/{userAlertId}/read?read= */
  markAlertRead: async (userAlertId, read = true) => {
    const response = await api.patch(
      `/veille/alerts/me/${userAlertId}/read`,
      null,
      { params: { read } },
    );
    return response.data;
  },

  /** DELETE /veille/alerts/me/{userAlertId} */
  deleteMyAlert: async (userAlertId) => {
    await api.delete(`/veille/alerts/me/${userAlertId}`);
  },

  getMySubscriptions: async () => {
    const response = await api.get("/veille/subscriptions/me");
    return response.data;
  },

  createSubscription: async (body) => {
    const response = await api.post("/veille/subscriptions", body);
    return response.data;
  },

  updateSubscription: async (id, body) => {
    const response = await api.put(`/veille/subscriptions/${id}`, body);
    return response.data;
  },

  toggleSubscriptionActive: async (id, active) => {
    const response = await api.patch(`/veille/subscriptions/${id}/active`, null, {
      params: { active },
    });
    return response.data;
  },
};

export default veilleService;
