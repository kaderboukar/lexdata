import axios from "axios";
import toast from "react-hot-toast";
import useAuthStore from "../store/useAuthStore";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

// 1. Utilisation des variables d'environnement (Vite)
const API_URL = import.meta.env.VITE_API_URL || "/api";

const api = axios.create({
  baseURL: API_URL,
  headers: { "Content-Type": "application/json" },
});

/** Chemins sans JWT : un ancien token (expiré/signé autrement) provoque un 401 sur la gateway avant d'atteindre le service auth. */
function isPublicAuthPath(url) {
  if (!url) return false;
  return (
    url.includes("/auth/login") ||
    url.includes("/auth/register") ||
    url.includes("/auth/refreshtoken") ||
    url.includes("/auth/forgot-password") ||
    url.includes("/auth/reset-password") ||
    url.includes("/auth/verify-email") ||
    url.includes("/auth/resend-verification")
  );
}

// Intercepteur request : injecte le JWT (sauf sur les routes publiques ci-dessus)
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    const path = `${config.baseURL || ""}${config.url || ""}`;
    if (token && !isPublicAuthPath(path) && !isPublicAuthPath(config.url || "")) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

// Intercepteur response : gère le 401 avec refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 2. PIÈGE ÉVITÉ : On ne tente jamais de Refresh si l'erreur 401 vient de la page de Login !
    if (originalRequest.url.includes("/auth/login")) {
      return Promise.reject(error);
    }

    if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const { refreshToken, setTokens, logout } = useAuthStore.getState();

      if (!refreshToken) {
        logout();
        return Promise.reject(error);
      }

      try {
        // Appel direct avec axios (et non 'api') pour éviter de repasser dans cet intercepteur
        const response = await axios.post(`${API_URL}/auth/refreshtoken`, {
          refreshToken,
        });

        // Assurez-vous que les noms correspondent à ce que renvoie votre backend Spring Boot
        const { accessToken, refreshToken: newRefresh } = response.data;

        setTokens(accessToken, newRefresh);
        processQueue(null, accessToken);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        logout();

        // 3. FORCE REDIRECT : Si le refresh échoue définitivement, on renvoie à l'accueil
        window.location.href = "/login?session_expired=true";

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // --- GESTION GLOBALE DES ERREURS (Toasts) ---
    // Les pages d'auth affichent elles-mêmes leurs messages d'erreur,
    // on évite donc les toasts en double sur les endpoints d'auth.
    const isAuthRequest =
      originalRequest.url.includes("/auth/login") ||
      originalRequest.url.includes("/auth/register") ||
      originalRequest.url.includes("/auth/role-requests") ||
      originalRequest.url.includes("/auth/forgot-password") ||
      originalRequest.url.includes("/auth/reset-password") ||
      originalRequest.url.includes("/auth/verify-email") ||
      originalRequest.url.includes("/auth/resend-verification") ||
      originalRequest.url.includes("/auth/logout") ||
      originalRequest.url.includes("/auth/logout-all");

    if (!error.response) {
      if (!isAuthRequest) {
        toast.error("Erreur de connexion au serveur. Vérifiez votre réseau.");
      }
    } else {
      const status = error.response.status;

      if (!isAuthRequest) {
        // On ignore 401 car il est géré au-dessus (et s'il échoue, il redirige)
        if (status === 403) {
          toast.error("Accès refusé. Vous n'avez pas les droits nécessaires.");
        } else if (status >= 500) {
          toast.error(
            getApiErrorMessage(
              error,
              "Erreur interne du serveur. Veuillez réessayer plus tard.",
            ),
          );
        } else if (status === 400) {
          toast.error(
            getApiErrorMessage(error, "Requête invalide."),
          );
        }
      }
    }

    return Promise.reject(error);
  },
);

export default api;
