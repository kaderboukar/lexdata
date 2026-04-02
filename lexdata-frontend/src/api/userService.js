import api from "./axiosInstance";

// Récupérer la liste des utilisateurs (avec pagination/recherche)
export const getUsers = (params) => api.get("/auth/users", { params });

// Mettre à jour le rôle d'un utilisateur
// On suppose que le backend attend un paramètre ou un body avec le nouveau rôle
export const updateUserRole = (id, role) =>
  api.patch(`/auth/users/${id}/role`, { role });

// Supprimer un utilisateur
export const deleteUser = (id) => api.delete(`/auth/users/${id}`);
