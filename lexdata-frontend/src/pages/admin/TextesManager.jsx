import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Plus, Search, Edit, Trash2, ArrowLeft, Save, FileText,
  AlertCircle, Loader2, CheckCircle, XCircle, Clock, Globe,
  Send, AlertTriangle, Database, Bell
} from "lucide-react";
import adminService from "../../api/adminService";
import { JURIDIQUE_LEGAL_DOMAINS, JURIDIQUE_TYPE_TEXTES } from "../../constants/juridiqueEnums";

function toTexteRequestPayload(formData) {
  return {
    titre: formData.titre.trim(),
    referenceOfficielle: formData.referenceOfficielle.trim(),
    type: formData.type,
    domaine: formData.domaine,
    dateSignature: formData.dateSignature || null,
    datePublicationJO: formData.datePublicationJO || null,
    dateEntreeEnVigueur: formData.dateEntreeEnVigueur || null,
    journalOfficielRef: formData.journalOfficielRef?.trim() || null,
    sourceOfficielle: formData.sourceOfficielle?.trim() || null,
    contenu: formData.contenu,
    estPremium: !!formData.estPremium,
  };
}

const WORKFLOW = {
  BROUILLON: { label: "Brouillon", color: "#9ca3af", bg: "rgba(156, 163, 175, 0.15)", icon: FileText, nextActions: [{ status: "EN_RELECTURE", label: "Soumettre pour relecture", icon: Send, color: "#3b82f6" }] },
  EN_RELECTURE: { label: "En Relecture", color: "#f59e0b", bg: "rgba(245, 158, 11, 0.15)", icon: Clock, nextActions: [{ status: "VALIDE", label: "Approuver", icon: CheckCircle, color: "#10b981" }, { status: "REJETE", label: "Rejeter", icon: XCircle, color: "#ef4444" }] },
  VALIDE: { label: "Validé", color: "#3b82f6", bg: "rgba(59, 130, 246, 0.15)", icon: CheckCircle, nextActions: [{ status: "PUBLIE", label: "Publier", icon: Globe, color: "#10b981" }, { status: "BROUILLON", label: "Annuler", icon: ArrowLeft, color: "#9ca3af" }] },
  REJETE: { label: "Rejeté", color: "#ef4444", bg: "rgba(239, 68, 68, 0.15)", icon: XCircle, nextActions: [{ status: "BROUILLON", label: "Remettre en brouillon", icon: Edit, color: "#9ca3af" }] },
  PUBLIE: { label: "Publié", color: "#10b981", bg: "rgba(16, 185, 129, 0.15)", icon: Globe, nextActions: [{ status: "BROUILLON", label: "Dépublier", icon: Trash2, color: "#ef4444" }] },
};

export default function TextesManager() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [view, setView] = useState("list");
  const [searchTerm, setSearchTerm] = useState("");
  const [currentTexte, setCurrentTexte] = useState(null);

  const [modalConfig, setModalConfig] = useState({ isOpen: false, title: "", message: "", confirmText: "Confirmer", confirmColor: "var(--primary)", icon: AlertCircle, onConfirm: () => { } });

  const initialFormState = { titre: "", referenceOfficielle: "", type: "", domaine: "", dateSignature: "", datePublicationJO: "", dateEntreeEnVigueur: "", journalOfficielRef: "", sourceOfficielle: "", estPremium: false, contenu: "" };
  const [formData, setFormData] = useState(initialFormState);

  const handleChange = (e) => setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  const closeModal = () => setModalConfig((prev) => ({ ...prev, isOpen: false }));

  // 1. DATA FETCHING
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin-textes"],
    queryFn: () => adminService.getAllTextes({ size: 100 }),
  });

  const textes = data?.content || data || [];
  const filteredTextes = textes.filter((t) => t.titre?.toLowerCase().includes(searchTerm.toLowerCase()) || t.referenceOfficielle?.toLowerCase().includes(searchTerm.toLowerCase()));

  // 2. MUTATIONS
  const saveMutation = useMutation({
    mutationFn: (dataToSave) => currentTexte ? adminService.updateTexte(currentTexte.id, dataToSave) : adminService.createTexte(dataToSave),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ["admin-textes"] }); setView("list"); setCurrentTexte(null); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => adminService.deleteTexte(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-textes"] }),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }) => adminService.updateTexteStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-textes"] }),
  });

  // 3. HANDLERS
  const handleOpenForm = (texte = null) => {
    if (texte) {
      setCurrentTexte(texte);
      setFormData({ ...initialFormState, ...texte, estPremium: !!texte.estPremium });
    } else {
      setCurrentTexte(null);
      setFormData(initialFormState);
    }
    setView("form");
  };

  const handleSubmit = (e) => { e.preventDefault(); saveMutation.mutate(toTexteRequestPayload(formData)); };

  const handleDelete = (id, titre) => {
    setModalConfig({
      isOpen: true,
      title: "Suppression définitive",
      message: `Êtes-vous sûr de vouloir supprimer le texte :\n"${titre}" ?\n\nCette action est irréversible.`,
      confirmText: "Oui, supprimer",
      confirmColor: "#ef4444",
      icon: AlertTriangle,
      onConfirm: () => { deleteMutation.mutate(id); closeModal(); },
    });
  };

  const openVeilleFormForTexte = (texte) => {
    navigate("/admin", {
      state: {
        openVeilleForm: {
          texteJuridiqueId: texte.id,
          titre: texte.titre || "",
          datePublicationJO: texte.datePublicationJO || "",
          dateEntreeEnVigueur: texte.dateEntreeEnVigueur || "",
          domaine: texte.domaine || "",
          type: texte.type || "",
        },
      },
    });
  };

  const handleStatusChange = (texte, nextStatusInfo) => {
    setModalConfig({
      isOpen: true,
      title: "Changement de statut",
      message: `Passer le texte à l'état "${nextStatusInfo.label}" ?`,
      confirmText: "Confirmer",
      confirmColor: nextStatusInfo.color,
      icon: nextStatusInfo.icon,
      onConfirm: () => { statusMutation.mutate({ id: texte.id, status: nextStatusInfo.status }); closeModal(); },
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
            {currentTexte ? "Modifier le texte" : "Saisir un nouveau texte"}
          </h2>
        </header>

        {saveMutation.isError && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl flex items-center gap-3 text-sm mb-6" role="alert">
            <AlertCircle size={20} aria-hidden />
            Erreur lors de la sauvegarde. Veuillez vérifier les champs requis.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="flex flex-col gap-2 mb-6">
            <label className="text-sm font-bold text-slate-200">Titre complet de la loi / du décret *</label>
            <input
              className={`${inputClass} text-lg`}
              name="titre"
              value={formData.titre}
              onChange={handleChange}
              required
              placeholder="Ex: Loi N°2026-01 portant code pénal..."
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Référence officielle *</label>
              <input
                className={inputClass}
                name="referenceOfficielle"
                value={formData.referenceOfficielle}
                onChange={handleChange}
                required
                placeholder="Ex: J.O N°12"
              />
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Domaine *</label>
              <select className={inputClass} name="domaine" value={formData.domaine} onChange={handleChange} required>
                <option value="">Sélectionnez un domaine</option>
                {JURIDIQUE_LEGAL_DOMAINS.map((d) => (
                  <option key={d.value} value={d.value}>
                    {d.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Type de texte *</label>
              <select className={inputClass} name="type" value={formData.type} onChange={handleChange} required>
                <option value="">Sélectionnez un type</option>
                {JURIDIQUE_TYPE_TEXTES.map((t) => (
                  <option key={t.value} value={t.value}>
                    {t.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Date de signature *</label>
              <input className={inputClass} type="date" name="dateSignature" value={formData.dateSignature} onChange={handleChange} required />
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Date de publication au J.O.</label>
              <input className={inputClass} type="date" name="datePublicationJO" value={formData.datePublicationJO} onChange={handleChange} />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Date d’entrée en vigueur</label>
              <input className={inputClass} type="date" name="dateEntreeEnVigueur" value={formData.dateEntreeEnVigueur} onChange={handleChange} />
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-slate-300">Référence J.O.</label>
              <input className={inputClass} name="journalOfficielRef" value={formData.journalOfficielRef} onChange={handleChange} placeholder="Ex. n° 12 du …" />
            </div>
          </div>

          <div className="flex flex-col gap-2 mb-6">
            <label className="text-sm font-medium text-slate-300">Source officielle (URL ou libellé)</label>
            <input className={inputClass} name="sourceOfficielle" value={formData.sourceOfficielle} onChange={handleChange} placeholder="https://…" />
          </div>

          <div className="mb-6 bg-black/20 p-4 rounded-lg border border-white/5">
            <label className="flex items-center gap-3 cursor-pointer text-slate-200 font-semibold">
              <input
                type="checkbox"
                className="w-5 h-5 accent-red-500 cursor-pointer rounded border-white/20 bg-black/30"
                name="estPremium"
                checked={formData.estPremium}
                onChange={(e) => setFormData((prev) => ({ ...prev, estPremium: e.target.checked }))}
              />
              Contenu réservé aux abonnés premium
            </label>
          </div>

          <div className="flex flex-col gap-2 mb-8">
            <label className="text-sm font-bold text-slate-200">Contenu intégral du texte *</label>
            <textarea
              className={`${inputClass} font-mono`}
              name="contenu"
              value={formData.contenu}
              onChange={handleChange}
              required
              rows="20"
              placeholder="Collez le texte brut ici. Les sauts de ligne seront respectés."
            />
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
                  <Save size={18} aria-hidden /> Sauvegarder le texte
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    );
  }

  // ==========================================
  // RENDU : VUE LISTE AVEC WORKFLOW ET MODALE
  // ==========================================
  return (
    <div className="max-w-7xl mx-auto fade-in">

      {/* 🌟 LA MODALE (Réparée et placée correctement) */}
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
            placeholder="Rechercher par titre ou référence..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <button
          className="inline-flex items-center justify-center gap-2 bg-red-500 text-white font-bold px-6 py-3 rounded-xl transition-all hover:bg-red-400 w-full md:w-auto"
          onClick={() => handleOpenForm()}
          type="button"
        >
          <Plus size={18} aria-hidden /> Ajouter un texte
        </button>
      </header>

      {/* TABLEAU DES TEXTES */}
      <div className="overflow-x-auto bg-slate-900/50 border border-white/10 rounded-xl">
        {isLoading ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement de la base de données...</p>
          </div>
        ) : isError ? (
          <div className="p-10 text-center">
            <AlertCircle size={40} className="mx-auto text-red-400 mb-4" aria-hidden />
            <p className="text-red-400">Erreur de chargement. Veuillez rafraîchir.</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse min-w-[800px]">
            <thead>
              <tr>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap w-[40%]">
                  Titre & Référence
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Classification
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Statut
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap text-right">
                  Workflow & Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredTextes.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-center p-8 text-slate-400">
                    <Database size={40} className="mx-auto mb-3 opacity-30" aria-hidden /> Aucun texte trouvé.
                  </td>
                </tr>
              ) : (
                filteredTextes.map((texte) => {
                  const currentStatus = WORKFLOW[texte.statut] || WORKFLOW.BROUILLON;
                  const StatusIcon = currentStatus.icon;

                  return (
                    <tr key={texte.id} className="transition-colors hover:bg-white/5">

                      <td>
                        <div className="p-4 border-b border-white/5 align-middle">
                          <div className="font-semibold text-slate-100 text-base mb-1 line-clamp-2 leading-snug" title={texte.titre}>
                            {texte.titre}
                          </div>
                          <div className="text-sm text-slate-400">
                            {texte.referenceOfficielle} • {texte.dateSignature}
                          </div>
                        </div>
                      </td>

                      <td>
                        <div className="p-4 border-b border-white/5 align-middle">
                          <div className="flex flex-col gap-2 items-start">
                            <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                              {texte.type}
                            </span>
                            <span className="text-xs text-slate-400 font-medium">{texte.domaine}</span>
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
                                  onClick={() => handleStatusChange(texte, action)}
                                  title={action.label}
                                  disabled={statusMutation.isPending}
                                  type="button"
                                >
                                  <ActionIcon size={20} aria-hidden />
                                </button>
                              );
                            })}
                          </div>

                          {/* Créer une alerte veille (texte validé ou publié) */}
                          {(texte.statut === "VALIDE" || texte.statut === "PUBLIE") && (
                            <button
                              className="p-2 rounded-lg flex items-center justify-center transition-all hover:bg-white/10 hover:scale-110 text-amber-400 hover:text-amber-300 mr-1"
                              onClick={() => openVeilleFormForTexte(texte)}
                              title="Créer une alerte de veille liée à ce texte"
                              type="button"
                            >
                              <Bell size={18} aria-hidden />
                            </button>
                          )}

                          {/* Actions de base (Éditer / Supprimer) */}
                          <button
                            className="p-2 rounded-lg flex items-center justify-center transition-all hover:bg-white/10 hover:scale-110 disabled:opacity-30 disabled:hover:scale-100 disabled:cursor-not-allowed text-slate-400 hover:text-slate-200"
                            onClick={() => handleOpenForm(texte)}
                            title="Modifier"
                            disabled={texte.statut === "PUBLIE"}
                            type="button"
                          >
                            <Edit size={18} aria-hidden />
                          </button>

                          <button
                            className="p-2 rounded-lg flex items-center justify-center transition-all hover:bg-white/10 hover:scale-110 disabled:opacity-30 disabled:hover:scale-100 disabled:cursor-not-allowed text-slate-400 hover:text-red-400"
                            onClick={() => handleDelete(texte.id, texte.titre)}
                            title="Supprimer"
                            disabled={deleteMutation.isPending || texte.statut === "PUBLIE"}
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