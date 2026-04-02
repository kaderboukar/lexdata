/**
 * Normalise un rôle (ajoute ROLE_ si absent).
 * @param {string} role
 * @returns {string}
 */
export const normalizeRole = (role) => {
  if (!role) return role;
  return role.startsWith("ROLE_") ? role : `ROLE_${role}`;
};

/**
 * Vérifie si un tableau de rôles contient au moins un rôle autorisé.
 * @param {string[]} userRoles
 * @param {string[]} allowedRoles
 * @returns {boolean}
 */
export const hasAnyRole = (userRoles = [], allowedRoles = []) => {
  const u = (userRoles || []).map(normalizeRole);
  const a = (allowedRoles || []).map(normalizeRole);
  return u.some((r) => a.includes(r));
};

/**
 * Vérifie si l'utilisateur est admin (contient ADMIN dans un rôle).
 * @param {string[]} userRoles
 * @returns {boolean}
 */
export const isAdminUser = (userRoles = []) => {
  const u = (userRoles || []).map(normalizeRole);
  return u.some((r) => r.includes("ADMIN"));
};

/**
 * Retourne la route home selon le rôle.
 * @param {string[]} userRoles
 * @returns {"/admin"|"/dashboard"}
 */
export const getHomeRoute = (userRoles = []) => {
  const u = (userRoles || []).map(normalizeRole);
  if (u.includes("ROLE_SUPER_ADMIN")) return "/admin";
  if (u.includes("ROLE_AGENT_ADMIN")) return "/admin";
  if (u.includes("ROLE_JURISTE")) return "/dashboard";
  if (u.includes("ROLE_AVOCAT")) return "/dashboard";
  return "/dashboard";
};

