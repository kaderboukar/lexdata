/**
 * Enums alignés sur lexdata-juridique-base (LegalDomain, TypeTexte).
 * Ne pas confondre avec constants/legalDomains.js (service utilisateur).
 */

export const JURIDIQUE_LEGAL_DOMAINS = [
  { value: "DROIT_AFFAIRES", label: "Droit des affaires" },
  { value: "DROIT_TRAVAIL", label: "Droit du travail" },
  { value: "DROIT_PENAL", label: "Droit pénal" },
  { value: "DROIT_FISCAL", label: "Droit fiscal" },
  { value: "DROIT_CONSTITUTIONNEL", label: "Droit constitutionnel" },
  { value: "DROIT_ADMINISTRATIF", label: "Droit administratif" },
  { value: "DROIT_COMMUNAUTAIRE_UEMOA", label: "Droit communautaire (UEMOA)" },
  { value: "DROIT_COMMUNAUTAIRE_CEDEAO", label: "Droit communautaire (CEDEAO)" },
  { value: "AUTRE", label: "Autre" },
];

export const JURIDIQUE_TYPE_TEXTES = [
  { value: "CONSTITUTION", label: "Constitution" },
  { value: "LOI_ORGANIQUE", label: "Loi organique" },
  { value: "LOI", label: "Loi" },
  { value: "ORDONNANCE", label: "Ordonnance" },
  { value: "DECRET", label: "Décret" },
  { value: "ARRETE", label: "Arrêté" },
  { value: "CIRCULAIRE", label: "Circulaire" },
  { value: "DECISION_JURISPRUDENCE", label: "Décision / jurisprudence" },
];
