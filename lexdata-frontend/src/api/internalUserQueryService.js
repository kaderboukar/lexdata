import api from "./axiosInstance";

const DEFAULT_INTERNAL_KEY = import.meta.env.VITE_INTERNAL_KEY || "";

const internalHeaders = (internalKey) => ({
  "X-Lexdata-Internal-Key": internalKey || DEFAULT_INTERNAL_KEY,
});

const internalUserQueryService = {
  // POST /api/auth/internal/user-ids/by-usernames
  // body: ["username1", ...]
  getUserIdsByUsernames: async (usernames, internalKey) => {
    const response = await api.post(
      "/auth/internal/user-ids/by-usernames",
      usernames,
      { headers: internalHeaders(internalKey) },
    );
    return response.data; // List<Long>
  },

  // POST /api/auth/internal/users/resolve
  // body: [1,2,3]
  resolveIdsToUsernames: async (userIds, internalKey) => {
    const response = await api.post(
      "/auth/internal/users/resolve",
      userIds,
      { headers: internalHeaders(internalKey) },
    );
    return response.data; // List<IdUsernameDto>
  },

  // GET /api/auth/internal/user-ids?role=...&page=...&size=...
  // role est optionnel, controller parseRole s'il n'a pas le préfixe ROLE_
  listUserIds: async ({ role, page = 0, size = 500 } = {}, internalKey) => {
    const response = await api.get("/auth/internal/user-ids", {
      headers: internalHeaders(internalKey),
      params: {
        ...(role ? { role } : {}),
        page,
        size,
      },
    });
    return response.data; // UserIdPageResponse
  },
};

export default internalUserQueryService;

