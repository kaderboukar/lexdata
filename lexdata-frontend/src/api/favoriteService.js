import api from "./axiosInstance";

/**
 * lexdata-juridique-base — FavoriteController (/api/juridique/favorites)
 * Ajout possible uniquement pour un texte publié (403/400 sinon).
 */
const favoriteService = {
  /** GET /juridique/favorites/me */
  getMyFavorites: async () => {
    const { data } = await api.get("/juridique/favorites/me");
    return data;
  },

  /** POST /juridique/favorites/{textId} */
  addFavorite: async (textId) => {
    const { data } = await api.post(`/juridique/favorites/${textId}`);
    return data;
  },

  /** DELETE /juridique/favorites/{textId} — 204 */
  removeFavorite: async (textId) => {
    await api.delete(`/juridique/favorites/${textId}`);
  },
};

export default favoriteService;
