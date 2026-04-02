import api from "./axiosInstance";

/**
 * lexdata-user-service : UserController (/api/users)
 * GET/PUT/DELETE /me, export RGPD
 */
const userMeService = {
  getMe: async () => {
    const response = await api.get("/users/me");
    return response.data;
  },

  updateMe: async (body) => {
    const response = await api.put("/users/me", body);
    return response.data;
  },

  /** Télécharge rgpd_export.json */
  exportMe: async () => {
    const response = await api.get("/users/me/export", {
      responseType: "blob",
    });
    return response;
  },

  deleteMe: async () => {
    const response = await api.delete("/users/me");
    return response.data;
  },
};

export default userMeService;
