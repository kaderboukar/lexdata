import api from "./axiosInstance";

/**
 * lexdata-user-service : UserPreferenceController (/api/preferences)
 * GET /preferences, POST /preferences
 * (GET /preferences/by-domain/{domain} — listings internes / admin, non exposé ici)
 */

/**
 * Corps aligné sur PreferenceRequest (topics, mots-clés, canaux, timezone IANA).
 * @param {object} form — même shape que le state du profil (followedTopics, alertKeywords, …)
 */
export function buildPreferenceRequest(form) {
  const keywords = (form.alertKeywords || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  const payload = {
    emailEnabled: form.emailEnabled,
    pushEnabled: form.pushEnabled,
    smsEnabled: form.smsEnabled,
    timezone: form.timezone?.trim() || "Europe/Paris",
  };

  if (form.followedTopics?.length > 0) {
    payload.followedTopics = [...form.followedTopics];
  }
  if (keywords.length > 0) {
    payload.alertKeywords = keywords;
  }

  return payload;
}

const userPreferenceService = {
  /** @returns {Promise<object|null>} null si aucune ligne préférences (404) */
  getMyPreferences: async () => {
    try {
      const { data } = await api.get("/preferences");
      return data;
    } catch (e) {
      if (e.response?.status === 404) return null;
      throw e;
    }
  },

  updatePreferences: async (body) => {
    const { data } = await api.post("/preferences", body);
    return data;
  },
};

export default userPreferenceService;
