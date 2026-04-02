import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Plus, Search, Edit, Trash2, ArrowLeft, Save, AlertCircle,
  Loader2, CheckCircle, XCircle, Send, Bell, AlertTriangle, Database
} from "lucide-react";
import adminService from "../../api/adminService";
import { JURIDIQUE_LEGAL_DOMAINS, JURIDIQUE_TYPE_TEXTES } from "../../constants/juridiqueEnums";

const WORKFLOW = {
  DRAFT: { label: "Brouillon", color: "#9ca3af", bg: "rgba(156, 163, 175, 0.15)", icon: Bell, nextActions: [{ status: "PUBLISHED", label: "Publier (notifications abonnés)", icon: Send, color: "#10b981" }] },
  PUBLISHED: { label: "Publiée", color: "#10b981", bg: "rgba(16, 185, 129, 0.15)", icon: CheckCircle, nextActions: [{ status: "ARCHIVED", label: "Archiver", icon: Trash2, color: "#64748b" }] },
  ARCHIVED: { label: "Archivée", color: "#64748b", bg: "rgba(100, 116, 139, 0.15)", icon: XCircle, nextActions: [{ status: "DRAFT", label: "Remettre en brouillon", icon: Edit, color: "#3b82f6" }] },
};

const URGENCE_COLORS = {
  BASSE: { color: "#10b981", label: "Basse" },
  MOYENNE: { color: "#f59e0b", label: "Moyenne" },
  ELEVEE: { color: "#ef4444", label: "Élevée" },
};

const initialFormState = {
  titre: "", texteJuridiqueId: "", texteType: "", datePublicationOfficielle: "", dateEntreeEnVigueur: "",
  resumeClarifie: "", pointsClesModifies: "", impactsPratiques: "", conseilsConformite: "",
  urgence: "BASSE", eventType: "NEW_TEXT", domainesCibles: [], secteursImpactes: [],
};

/**
 * @param {{ prefillFromTexte?: { texteJuridiqueId: number, titre?: string, datePublicationJO?: string, dateEntreeEnVigueur?: string, domaine?: string, type?: string } | null, onPrefillConsumed?: () => void }} props
 */
export default function VeilleManager({ prefillFromTexte = null, onPrefillConsumed }) {
  const queryClient = useQueryClient();
  const [view, setView] = useState("list");
  const [searchTerm, setSearchTerm] = useState("");
  const [currentAlerte, setCurrentAlerte] = useState(null);

  const [modalConfig, setModalConfig] = useState({ isOpen: false, title: "", message: "", confirmText: "Confirmer", confirmColor: "var(--primary)", icon: AlertCircle, onConfirm: () => { } });

  const [formData, setFormData] = useState(initialFormState);

  // Liste des textes juridiques (formulaire création / édition)
  const { data: textesData, isLoading: textesLoading } = useQuery({
    queryKey: ["admin-textes"],
    queryFn: () => adminService.getAllTextes({ size: 500 }),
    staleTime: 60_000,
    enabled: view === "form",
  });
  const textesJuridiques = textesData?.content ?? textesData ?? [];

  useEffect(() => {
    if (!prefillFromTexte?.texteJuridiqueId) return;
    const domaines = prefillFromTexte.domaine ? [prefillFromTexte.domaine] : [];
    setCurrentAlerte(null);
    setFormData({
      ...initialFormState,
      texteJuridiqueId: String(prefillFromTexte.texteJuridiqueId),
      titre: prefillFromTexte.titre?.trim() || "",
      texteType: prefillFromTexte.type?.trim() || "",
      datePublicationOfficielle: prefillFromTexte.datePublicationJO || "",
      dateEntreeEnVigueur: prefillFromTexte.dateEntreeEnVigueur || "",
      domainesCibles: domaines,
    });
    setView("form");
    onPrefillConsumed?.();
  }, [prefillFromTexte, onPrefillConsumed]);

  // 1. DATA FETCHING
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin-alertes"],
    queryFn: () => adminService.getAllAlertes({ size: 100 }),
  });

  const alertes = data?.content || data || [];
  const filteredAlertes = alertes.filter((a) =>
    a.titre?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    a.resumeClarifie?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 2. MUTATIONS
  const saveMutation = useMutation({
    mutationFn: (dataToSave) => currentAlerte ? adminService.updateAlerte(currentAlerte.id, dataToSave) : adminService.createAlerte(dataToSave),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ["admin-alertes"] }); setView("list"); setCurrentAlerte(null); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => adminService.deleteAlerte(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-alertes"] }),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }) => adminService.updateAlerteStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-alertes"] }),
  });

  // 3. HANDLERS
  const handleOpenForm = (alerte = null) => {
    if (alerte) {
      setCurrentAlerte(alerte);
      setFormData({
        ...initialFormState,
        ...alerte,
        texteJuridiqueId: alerte.texteJuridiqueId != null ? String(alerte.texteJuridiqueId) : "",
        urgence: alerte.urgence || "BASSE",
        eventType: alerte.eventType || "NEW_TEXT",
        texteType: alerte.texteType || "",
      });
    } else {
      setCurrentAlerte(null);
      setFormData(initialFormState);
    }
    setView("form");
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.texteJuridiqueId) return window.alert("Veuillez sélectionner le texte juridique associé.");

    saveMutation.mutate({
      ...formData,
      texteJuridiqueId: Number(formData.texteJuridiqueId),
      status: currentAlerte?.statut || "DRAFT",
      eventType: formData.eventType || "NEW_TEXT",
      texteType: formData.texteType?.trim() ? formData.texteType.trim() : null,
    });
  };

  const closeModal = () => setModalConfig((prev) => ({ ...prev, isOpen: false }));

  const handleMultiSelectChange = (e) => {
    const { name, options } = e.target;
    setFormData((prev) => ({ ...prev, [name]: Array.from(options).filter((opt) => opt.selected).map((opt) => opt.value) }));
  };

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  /** Remplit le type de texte à partir du texte juridique choisi (cohérent avec les abonnements veille). */
  const handleTexteJuridiqueChange = (e) => {
    const id = e.target.value;
    const texte = textesJuridiques.find((t) => String(t.id) === id);
    setFormData((prev) => ({
      ...prev,
      texteJuridiqueId: id,
      texteType: texte?.type ? String(texte.type) : "",
    }));
  };

  const handleDelete = (id, titre) => {
    setModalConfig({
      isOpen: true,
      title: "Supprimer l'alerte",
      message: `Voulez-vous vraiment supprimer l'alerte :\n"${titre}" ?\n\nCette action est irréversible.`,
      confirmText: "Oui, supprimer",
      confirmColor: "#ef4444",
      icon: AlertTriangle,
      onConfirm: () => { deleteMutation.mutate(id); closeModal(); },
    });
  };

  const handleStatusChange = (alerte, nextStatusInfo) => {
    setModalConfig({
      isOpen: true,
      title: "Confirmation du Workflow",
      message: `Passer l'alerte à l'état "${nextStatusInfo.label}" ?\n\n${nextStatusInfo.status === "PUBLISHED" ? "⚠️ Attention : Cette action va envoyer des notifications à tous les abonnés concernés." : ""}`,
      confirmText: "Confirmer l'action",
      confirmColor: nextStatusInfo.color,
      icon: nextStatusInfo.icon,
      onConfirm: () => { statusMutation.mutate({ id: alerte.id, status: nextStatusInfo.status }); closeModal(); },
    });
  };

  // ==========================================
  // RENDU : FORMULAIRE
  // ==========================================
  if (view === "form") {
    const inputClass =
      "w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all";

    return (
      <div className="max-w-5xl mx-auto fade-in bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-8">
        <header className="flex items-center gap-4 mb-8 pb-4 border-b border-white/10">
          <button
            className="p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 transition-colors"
            onClick={() => setView("list")}
            type="button"
            aria-label="Retour à la liste"
          >
            <ArrowLeft size={24} aria-hidden />
          </button>
          <h2 className="text-2xl font-bold text-slate-100 m-0">
            {currentAlerte ? "Modifier l'alerte" : "Créer une nouvelle alerte"}
          </h2>
        </header>

        {saveMutation.isError && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl flex items-center gap-3 text-sm mb-6" role="alert">
            <AlertCircle size={20} aria-hidden /> Erreur lors de la sauvegarde.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="flex flex-col gap-2 mb-6">
            <label className="text-sm font-bold text-slate-200 text-lg">Titre de l'alerte (Accrocheur) *</label>
            <input
              className={`${inputClass} text-lg`}
              name="titre"
              value={formData.titre}
              onChange={handleChange}
              required
              placeholder="Ex: Nouveau taux de TVA applicable..."
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-6">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300" htmlFor="veille-texte-juridique">
                Texte juridique lié *
              </label>
              {textesLoading ? (
                <div className={`${inputClass} flex items-center gap-2 text-slate-400`}>
                  <Loader2 size={18} className="animate-spin shrink-0" aria-hidden />
                  Chargement des textes…
                </div>
              ) : (
                <select
                  id="veille-texte-juridique"
                  className={inputClass}
                  name="texteJuridiqueId"
                  value={formData.texteJuridiqueId}
                  onChange={handleTexteJuridiqueChange}
                  required
                >
                  <option value="">— Choisir un texte —</option>
                  {textesJuridiques.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.titre}
                      {t.referenceOfficielle ? ` — ${t.referenceOfficielle}` : ""}
                    </option>
                  ))}
                </select>
              )}
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300" htmlFor="veille-texte-type">
                Type de texte juridique
              </label>
              <select
                id="veille-texte-type"
                className={inputClass}
                name="texteType"
                value={formData.texteType}
                onChange={handleChange}
              >
                <option value="">— Non renseigné (tous types pour l’abonnement) —</option>
                {JURIDIQUE_TYPE_TEXTES.map(({ value, label }) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
              <p className="text-xs text-slate-500 m-0">
                Renseignez-le pour cibler les abonnés qui ont filtré par type (Loi, Décret, etc.).
              </p>
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Niveau d&apos;urgence *</label>
              <select className={inputClass} name="urgence" value={formData.urgence} onChange={handleChange} required>
                <option value="BASSE">Basse (Information)</option>
                <option value="MOYENNE">Moyenne (Impact modéré)</option>
                <option value="ELEVEE">Élevée (Action immédiate)</option>
              </select>
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Type d&apos;événement</label>
              <select className={inputClass} name="eventType" value={formData.eventType} onChange={handleChange}>
                <option value="NEW_TEXT">Nouveau texte publié</option>
                <option value="UPDATE">Mise à jour d&apos;un texte</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-400">Domaines ciblés (Ctrl + clic — mêmes codes que l&apos;abonnement client)</label>
              <select className={`${inputClass} h-[180px]`} name="domainesCibles" multiple value={formData.domainesCibles} onChange={handleMultiSelectChange}>
                {JURIDIQUE_LEGAL_DOMAINS.map((d) => (
                  <option key={d.value} value={d.value}>
                    {d.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-400">Secteurs Impactés (Maintenez Ctrl pour choix multiples)</label>
              <select className={`${inputClass} h-[120px]`} name="secteursImpactes" multiple value={formData.secteursImpactes} onChange={handleMultiSelectChange}>
                <option value="BANQUE">Banque / Finance</option>
                <option value="MINES">Mines / Énergie</option>
                <option value="COMMERCE">Commerce</option>
                <option value="TELECOM">Télécommunications</option>
              </select>
            </div>
          </div>

          <div className="flex flex-col gap-2 mb-6">
            <label className="text-sm font-bold text-slate-200">Résumé clarifié *</label>
            <textarea className={inputClass} name="resumeClarifie" value={formData.resumeClarifie} onChange={handleChange} required rows="4" placeholder="Résumé simple et compréhensible de la nouveauté..." />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Impacts Pratiques</label>
              <textarea className={inputClass} name="impactsPratiques" value={formData.impactsPratiques} onChange={handleChange} rows="4" placeholder="Qu'est-ce que cela change concrètement ?" />
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Conseils de Conformité</label>
              <textarea className={inputClass} name="conseilsConformite" value={formData.conseilsConformite} onChange={handleChange} rows="4" placeholder="Quelles actions les entreprises doivent-elles prendre ?" />
            </div>
          </div>

          <div className="flex justify-end gap-4 border-t border-white/10 pt-6">
            <button
              type="button"
              className="px-6 py-3 rounded-xl font-bold transition-all bg-white/5 border border-white/10 text-slate-200 hover:bg-white/10"
              onClick={() => setView("list")}
            >
              Annuler
            </button>
            <button
              type="submit"
              className="px-8 py-3 rounded-xl font-bold transition-all bg-red-500 text-white hover:bg-red-400 disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center gap-2"
              disabled={saveMutation.isPending}
            >
              {saveMutation.isPending ? (
                <>
                  <Loader2 size={18} className="animate-spin" aria-hidden /> Enregistrement...
                </>
              ) : (
                <>
                  <Save size={18} aria-hidden /> Sauvegarder l'alerte
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    );
  }

  // ==========================================
  // RENDU : VUE LISTE AVEC MODALE
  // ==========================================
  return (
    <div className="max-w-7xl mx-auto fade-in">

      {/* 🌟 LA MODALE SÉCURISÉE */}
      {modalConfig.isOpen && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex justify-center items-center z-[9999] p-4 animate-in fade-in duration-200" onClick={closeModal}>
          <div className="bg-slate-900 border border-white/10 p-8 max-w-md w-full rounded-2xl flex flex-col items-center text-center shadow-2xl animate-in zoom-in-95 duration-200" onClick={(e) => e.stopPropagation()}>
            <div className="w-16 h-16 rounded-full flex justify-center items-center mb-6" style={{ background: `${modalConfig.confirmColor}20`, color: modalConfig.confirmColor }}>
              <modalConfig.icon size={36} />
            </div>
            <h3 className="text-xl font-bold text-slate-100 mb-2">{modalConfig.title}</h3>
            <p className="text-slate-400 mb-8 whitespace-pre-wrap leading-relaxed">{modalConfig.message}</p>
            <div className="flex gap-4 w-full">
              <button className="flex-1 py-3 rounded-xl font-bold transition-all bg-white/5 border border-white/10 text-slate-200 hover:bg-white/10" onClick={closeModal} type="button">
                Annuler
              </button>
              <button className="flex-1 py-3 rounded-xl font-bold transition-all text-white hover:opacity-95" style={{ backgroundColor: modalConfig.confirmColor }} onClick={modalConfig.onConfirm} type="button">
                {modalConfig.confirmText}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* HEADER RECHERCHE */}
      <header className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 mb-8 flex flex-col md:flex-row justify-between items-center gap-6">
        <div className="flex items-center gap-3 bg-black/30 border border-white/10 rounded-lg px-4 py-3 transition-all focus-within:border-red-500 focus-within:ring-1 focus-within:ring-red-500 w-full md:max-w-lg">
          <Search size={20} className="text-slate-500" aria-hidden />
          <input
            type="text"
            className="w-full bg-transparent border-none outline-none text-slate-200 placeholder-slate-500"
            placeholder="Rechercher une alerte par titre ou mot clé..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <button
          className="inline-flex items-center justify-center gap-2 bg-red-500 text-white font-bold px-6 py-3 rounded-xl transition-all hover:bg-red-400 w-full md:w-auto"
          onClick={() => handleOpenForm()}
          type="button"
        >
          <Plus size={18} aria-hidden /> Nouvelle Alerte
        </button>
      </header>

      {/* TABLEAU */}
      <div className="overflow-x-auto bg-slate-900/50 border border-white/10 rounded-xl">
        {isLoading ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement des alertes...</p>
          </div>
        ) : isError ? (
          <div className="p-10 text-center">
            <AlertCircle size={40} className="mx-auto text-red-400 mb-4" aria-hidden />
            <p className="text-red-400">Erreur de chargement. Veuillez rafraîchir.</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse min-w-[900px]">
            <thead>
              <tr>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap w-[45%]">
                  Alerte & Résumé
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Urgence
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Statut de Diffusion
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap text-right">
                  Actions & Workflow
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredAlertes.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-center p-8 text-slate-400">
                    <Database size={40} className="mx-auto mb-3 opacity-30" aria-hidden /> Aucune alerte trouvée.
                  </td>
                </tr>
              ) : (
                filteredAlertes.map((alerte) => {
                  const currentStatus = WORKFLOW[alerte.statut] || WORKFLOW.DRAFT;
                  const StatusIcon = currentStatus.icon;
                  const urgenceInfo = URGENCE_COLORS[alerte.urgence] || URGENCE_COLORS.BASSE;

                  return (
                    <tr key={alerte.id} className="transition-colors hover:bg-white/5">

                      <td>
                        <div className="p-4 border-b border-white/5 align-middle">
                          <div className="font-semibold text-slate-100 text-base mb-1" title={alerte.titre}>
                            {alerte.titre}
                          </div>
                          <div className="text-sm text-slate-400 line-clamp-2">
                            {alerte.resumeClarifie}
                          </div>
                        </div>
                      </td>

                      <td>
                        <div className="p-4 border-b border-white/5 align-middle">
                          <div className="flex items-center gap-2 font-medium" style={{ color: urgenceInfo.color }}>
                            <span className="w-2 h-2 rounded-full" style={{ background: urgenceInfo.color }} />
                          {urgenceInfo.label}
                        </div>
                        </div>
                      </td>

                      <td>
                        <div className="p-4 border-b border-white/5 align-middle">
                          <div className="inline-flex items-center gap-2 text-xs px-3 py-1.5 rounded-full font-bold tracking-wide" style={{ background: currentStatus.bg, color: currentStatus.color }}>
                            <StatusIcon size={14} aria-hidden /> {currentStatus.label}
                          </div>
                        </div>
                      </td>

                      <td>
                        <div className="p-4 border-b border-white/5 align-middle">
                          <div className="flex justify-end items-center gap-2">

                          {/* Actions de Workflow conditionnelles */}
                          <div className="flex gap-1 mr-4 pr-4 border-r border-white/10">
                            {currentStatus.nextActions.map((action, idx) => {
                              const ActionIcon = action.icon;
                              return (
                                <button
                                  key={idx}
                                  className="p-2 rounded-lg flex items-center justify-center transition-all hover:bg-white/10 hover:scale-110 disabled:opacity-30 disabled:hover:scale-100 disabled:cursor-not-allowed"
                                  style={{ color: action.color }}
                                  onClick={() => handleStatusChange(alerte, action)}
                                  title={action.label}
                                  disabled={statusMutation.isPending}
                                  type="button"
                                >
                                  <ActionIcon size={20} aria-hidden />
                                </button>
                              );
                            })}
                          </div>

                          {/* Actions basiques (Editer / Supprimer) */}
                          <button
                            className="p-2 rounded-lg flex items-center justify-center transition-all hover:bg-white/10 hover:scale-110 disabled:opacity-30 disabled:hover:scale-100 disabled:cursor-not-allowed text-slate-400 hover:text-slate-200"
                            onClick={() => handleOpenForm(alerte)}
                            title="Modifier"
                            disabled={alerte.statut === "PUBLISHED"}
                            type="button"
                          >
                            <Edit size={18} aria-hidden />
                          </button>
                          <button
                            className="p-2 rounded-lg flex items-center justify-center transition-all hover:bg-white/10 hover:scale-110 disabled:opacity-30 disabled:hover:scale-100 disabled:cursor-not-allowed text-slate-400 hover:text-red-400"
                            onClick={() => handleDelete(alerte.id, alerte.titre)}
                            title="Supprimer"
                            disabled={deleteMutation.isPending || alerte.statut === "PUBLISHED"}
                            type="button"
                          >
                            <Trash2 size={18} aria-hidden />
                          </button>

                          </div>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}