import api from "./axiosInstance";

function notificationsAsList(data) {
  if (Array.isArray(data)) return data;
  return data?.content ?? [];
}

function omitEmptyParams(params) {
  const out = {};
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === "") continue;
    out[k] = v;
  }
  return out;
}

async function fetchNotificationsPage({
  page = 0,
  size = 20,
  unreadOnly,
  type,
  from,
  to,
} = {}) {
  const params = omitEmptyParams({
    page,
    size,
    unreadOnly,
    type,
    from,
    to,
  });
  const response = await api.get("/notifications", { params });
  const d = response.data;
  return {
    content: notificationsAsList(d),
    totalElements: d.totalElements ?? 0,
    totalPages: d.totalPages ?? 0,
    number: d.number ?? page,
    size: d.size ?? size,
  };
}

const notificationService = {
  getNotificationsPage: fetchNotificationsPage,

  /** Liste simple (tableau) — filtres optionnels alignés sur le controller */
  getMyNotifications: async (page = 0, size = 20, filters = {}) => {
    const p = await fetchNotificationsPage({ page, size, ...filters });
    return p.content;
  },

  /** Nombre total de notifications non lues (via pagination Spring) */
  getUnreadCount: async () => {
    const p = await fetchNotificationsPage({
      page: 0,
      size: 1,
      unreadOnly: true,
    });
    return p.totalElements;
  },

  /** POST /api/admin/notifications/send — SUPER_ADMIN / AGENT_ADMIN */
  sendAdminBroadcast: async (body) => {
    await api.post("/admin/notifications/send", body);
  },

  markAsRead: async (id) => {
    const response = await api.patch(`/notifications/${id}/read`);
    return response.data;
  },

  markAsUnread: async (id) => {
    const response = await api.patch(`/notifications/${id}/unread`);
    return response.data;
  },

  /** Soft-delete côté serveur (204 No Content) */
  deleteNotification: async (id) => {
    await api.delete(`/notifications/${id}`);
  },

  getPreferences: async () => {
    try {
      const response = await api.get("/notifications/preferences");
      return response.data;
    } catch (e) {
      if (e.response?.status === 404) return null;
      throw e;
    }
  },

  updatePreferences: async (preferencesData) => {
    const response = await api.put(
      "/notifications/preferences",
      preferencesData,
    );
    return response.data;
  },
};

export default notificationService;
