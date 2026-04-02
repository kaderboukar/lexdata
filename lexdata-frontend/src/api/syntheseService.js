import api from "./axiosInstance";

/**
 * lexdata-synthese — SyntheseController (/api/synthese)
 * Statuts : DRAFT, PUBLISHED, ARCHIVED (FicheSynthetique.SyntheseStatus)
 */

/** Agrège les champs structurés en contenu unique (@NotBlank sur SyntheseRequest.content) */
export function buildSyntheseContent(form) {
  const blocks = [];
  if (form.objectifPrincipal?.trim()) {
    blocks.push(`## Objectif principal\n${form.objectifPrincipal.trim()}`);
  }
  if (form.changementsCles?.trim()) {
    blocks.push(`## Changements clés\n${form.changementsCles.trim()}`);
  }
  if (form.obligations?.trim()) {
    blocks.push(`## Obligations\n${form.obligations.trim()}`);
  }
  if (form.sanctions?.trim()) {
    blocks.push(`## Sanctions\n${form.sanctions.trim()}`);
  }
  if (form.conseilsPratiques?.trim()) {
    blocks.push(`## Conseils pratiques\n${form.conseilsPratiques.trim()}`);
  }
  if (form.exemplesConcrets?.trim()) {
    blocks.push(`## Exemples\n${form.exemplesConcrets.trim()}`);
  }
  const joined = blocks.join("\n\n");
  return joined || form.objectifPrincipal?.trim() || "—";
}

/**
 * Corps aligné sur SyntheseRequest (titre, texteJuridiqueId, content, version, status, …)
 * @param {'DRAFT'|'PUBLISHED'} status
 */
export function buildSyntheseRequestBody(form, texteJuridiqueId, version, status) {
  return {
    titre: form.titre?.trim() || "Sans titre",
    texteJuridiqueId,
    content: buildSyntheseContent(form),
    version,
    status,
    objectifPrincipal: form.objectifPrincipal?.trim() || undefined,
    changementsCles: form.changementsCles?.trim() || undefined,
    obligations: form.obligations?.trim() || undefined,
    sanctions: form.sanctions?.trim() || undefined,
    conseilsPratiques: form.conseilsPratiques?.trim() || undefined,
    exemplesConcrets: form.exemplesConcrets?.trim() || undefined,
    commentaireVersion: form.commentaireVersion?.trim() || undefined,
    texteJuridiqueVersionId: form.texteJuridiqueVersionId ?? undefined,
  };
}

async function fetchPublishedFicheByTexteId(texteId) {
  try {
    const response = await api.get(`/synthese/fiches/texte/${texteId}`);
    return response.data;
  } catch (e) {
    if (e.response?.status === 404) return null;
    throw e;
  }
}

const syntheseService = {
  createFiche: async (ficheData) => {
    const response = await api.post("/synthese/admin/fiches", ficheData);
    return response.data;
  },

  updateFiche: async (ficheId, ficheData) => {
    const response = await api.put(`/synthese/admin/fiches/${ficheId}`, ficheData);
    return response.data;
  },

  /** @param {'DRAFT'|'PUBLISHED'|'ARCHIVED'} status — nom d’énum Java exact */
  updateStatus: async (ficheId, status) => {
    const response = await api.patch(`/synthese/admin/fiches/${ficheId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  deleteFiche: async (ficheId) => {
    await api.delete(`/synthese/admin/fiches/${ficheId}`);
  },

  /** Liste paginée des fiches publiées (utilisateur authentifié) */
  getPublishedFiches: async (params = {}) => {
    const response = await api.get("/synthese/fiches", { params });
    return response.data;
  },

  /** Fiche publiée par id (ou brouillon si admin) */
  getFicheById: async (ficheId) => {
    const response = await api.get(`/synthese/fiches/${ficheId}`);
    return response.data;
  },

  getPublishedFicheByTexteId: fetchPublishedFicheByTexteId,

  /** @deprecated Préférer getPublishedFicheByTexteId — garde [fiche] pour ancien code */
  getFichesByTexteId: async (texteId) => {
    const f = await fetchPublishedFicheByTexteId(texteId);
    return f ? [f] : [];
  },

  downloadPdf: async (ficheId) => {
    const response = await api.get(`/synthese/fiches/${ficheId}/pdf`, {
      responseType: "blob",
    });
    return response.data;
  },

  getFicheVersions: async (ficheId) => {
    const response = await api.get(`/synthese/admin/fiches/${ficheId}/versions`);
    return response.data;
  },
};

export default syntheseService;
