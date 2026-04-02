import api from "./axiosInstance";

/**
 * lexdata-user-service : UserProfileController (/api/profiles)
 * Accès : AGENT_ADMIN, SUPER_ADMIN
 */
const userProfileService = {
  /** GET /profiles?page=&size=&sort= */
  getAllProfiles: async (params = {}) => {
    const response = await api.get("/profiles", {
      params: { page: 0, size: 50, ...params },
    });
    return response.data;
  },

  /** GET /profiles/{username} */
  getProfileByUsername: async (username) => {
    const response = await api.get(
      `/profiles/${encodeURIComponent(username)}`,
    );
    return response.data;
  },

  /** PUT /profiles/{username}/verify — body { status, comment? } */
  verifyProfile: async (username, payload) => {
    const response = await api.put(
      `/profiles/${encodeURIComponent(username)}/verify`,
      payload,
    );
    return response.data;
  },
};

export default userProfileService;
