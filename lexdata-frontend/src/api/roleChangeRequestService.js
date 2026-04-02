import api from "./axiosInstance";

const roleChangeRequestService = {
  // POST /api/auth/role-requests
  createRequest: async (requestedRole) => {
    const response = await api.post("/auth/role-requests", {
      requestedRole,
    });
    return response.data;
  },

  // GET /api/auth/role-requests/admin
  listAllAdmin: async () => {
    const response = await api.get("/auth/role-requests/admin");
    return response.data;
  },

  // POST /api/auth/role-requests/admin/{id}/approve
  approveAdmin: async (id) => {
    const response = await api.post(`/auth/role-requests/admin/${id}/approve`);
    return response.data;
  },

  // POST /api/auth/role-requests/admin/{id}/reject
  rejectAdmin: async (id, reason) => {
    const response = await api.post(
      `/auth/role-requests/admin/${id}/reject`,
      { reason },
    );
    return response.data;
  },
};

export default roleChangeRequestService;

