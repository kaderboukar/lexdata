import api from "./axiosInstance";

/**
 * lexdata-juridique-base — TexteJuridiqueController (/api/juridique/textes)
 * AnnotationController (/api/juridique/annotations) : PUT/DELETE seulement (création via POST …/textes/{id}/annotations)
 */
/** @see com.lexdata.juridique.payload.AnnotationRequest */
export const ANNOTATION_NOTE_MAX_LENGTH = 2000;

const juridiqueService = {
  /**
   * Liste paginée (Spring Data).
   * Backend : GET ...?recherche=&domaine=&type=&includeNonPublie=&page=&size=&sort=
   * @param {Object} params - recherche ou q, domaine, type ou typeTexte, page, size, sort, includeNonPublie (admin)
   */
  searchTextes: async (params = {}) => {
    const {
      q,
      recherche,
      domaine,
      type,
      typeTexte,
      page = 0,
      size = 10,
      sort,
      includeNonPublie,
    } = params;

    const queryParams = { page, size };
    const term = recherche ?? q;
    if (term != null && String(term).trim() !== "") {
      queryParams.recherche = String(term).trim();
    }
    if (domaine) queryParams.domaine = domaine;
    const typeVal = type ?? typeTexte;
    if (typeVal) queryParams.type = typeVal;
    if (includeNonPublie === true) queryParams.includeNonPublie = true;
    if (sort) queryParams.sort = sort;

    const response = await api.get("/juridique/textes", { params: queryParams });
    return response.data;
  },

  /**
   * Recherche plein texte (Elasticsearch). Min. 2 caractères côté API.
   * GET /juridique/textes/search?q=
   */
  advancedSearch: async (query, includeNonPublie = false) => {
    const params = { q: query.trim() };
    if (includeNonPublie) params.includeNonPublie = true;
    const response = await api.get("/juridique/textes/search", { params });
    return response.data;
  },

  getTexteById: async (id) => {
    const response = await api.get(`/juridique/textes/${id}`);
    return response.data;
  },

  getVersionsTexte: async (id) => {
    const response = await api.get(`/juridique/textes/${id}/versions`);
    return response.data;
  },

  /**
   * Crée une version figeant le contenu actuel. Corps JSON = chaîne brute (résumé).
   */
  createTextVersion: async (id, summary = "") => {
    const response = await api.post(
      `/juridique/textes/${id}/versions`,
      JSON.stringify(summary),
      {
        headers: { "Content-Type": "application/json" },
      },
    );
    return response.data;
  },

  /** Annotations de l'utilisateur pour ce texte uniquement */
  getAnnotationsForText: async (texteId) => {
    const response = await api.get(`/juridique/textes/${texteId}/annotations`);
    return response.data;
  },

  addAnnotation: async (texteId, noteContent) => {
    const response = await api.post(`/juridique/textes/${texteId}/annotations`, {
      note: noteContent,
    });
    return response.data;
  },

  getMyAnnotations: async () => {
    const response = await api.get("/juridique/textes/annotations/me");
    return response.data;
  },

  /** PUT /juridique/annotations/{id} — corps { note } (@Valid AnnotationRequest) */
  updateAnnotation: async (noteId, noteContent) => {
    const response = await api.put(`/juridique/annotations/${noteId}`, {
      note: noteContent,
    });
    return response.data;
  },

  /** DELETE /juridique/annotations/{id} — 204 No Content */
  deleteAnnotation: async (noteId) => {
    await api.delete(`/juridique/annotations/${noteId}`);
  },
};

export default juridiqueService;
