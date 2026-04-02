import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Search, Edit, ArrowLeft, Save, FileText,
  AlertCircle, Loader2, BookOpen, Send, Database
} from "lucide-react";
import adminService from "../../api/adminService";
import syntheseService, { buildSyntheseRequestBody } from "../../api/syntheseService";

export default function SynthesesManager() {
  const queryClient = useQueryClient();

  const [view, setView] = useState("list");
  const [searchTerm, setSearchTerm] = useState("");
  const [currentTexte, setCurrentTexte] = useState(null);
  const [existingFicheId, setExistingFicheId] = useState(null);
  const [ficheVersion, setFicheVersion] = useState(1);

  const initialFormState = {
    titre: "", objectifPrincipal: "", changementsCles: "",
    obligations: "", sanctions: "", conseilsPratiques: "",
    exemplesConcrets: "", commentaireVersion: "Création / Mise à jour manuelle",
  };
  const [formData, setFormData] = useState(initialFormState);

  // 1. DATA FETCHING (Textes)
  const { data: textesData, isLoading: isLoadingTextes, isError } = useQuery({
    queryKey: ["admin-textes-syntheses"],
    queryFn: () => adminService.getAllTextes({ size: 100 }),
  });

  const textes = textesData?.content || textesData || [];
  const filteredTextes = textes.filter((t) =>
    t.titre?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.referenceOfficielle?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 2. MUTATION (Sauvegarde)
  const saveFicheMutation = useMutation({
    mutationFn: async ({ mode, version }) => {
      const status = mode === "publish" ? "PUBLISHED" : "DRAFT";
      const payload = buildSyntheseRequestBody(formData, currentTexte.id, version, status);

      if (existingFicheId) {
        return syntheseService.updateFiche(existingFicheId, payload);
      }
      return syntheseService.createFiche(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fiches-by-texte", String(currentTexte?.id)] });
      setView("list");
      setCurrentTexte(null);
      setExistingFicheId(null);
      setFicheVersion(1);
    },
  });

  // 3. HANDLERS
  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleOpenEditor = async (texte) => {
    setCurrentTexte(texte);
    setView("loading-fiche");

    try {
      const fiche = await syntheseService.getPublishedFicheByTexteId(texte.id);
      if (fiche) {
        setExistingFicheId(fiche.id);
        setFicheVersion(fiche.version ?? 1);
        setFormData({
          titre: fiche.titre || `Synthèse : ${texte.titre.substring(0, 50)}...`,
          objectifPrincipal: fiche.objectifPrincipal || "",
          changementsCles: fiche.changementsCles || "",
          obligations: fiche.obligations || "",
          sanctions: fiche.sanctions || "",
          conseilsPratiques: fiche.conseilsPratiques || "",
          exemplesConcrets: fiche.exemplesConcrets || "",
          commentaireVersion: "",
        });
      } else {
        setExistingFicheId(null);
        setFicheVersion(1);
        setFormData({ ...initialFormState, titre: `Synthèse : ${texte.titre.substring(0, 50)}...` });
      }
      setView("edit");
    } catch (error) {
      console.error("Erreur lors de la récupération de la fiche:", error);
      setView("list");
    }
  };

  // ==========================================
  // RENDU : VUE ÉDITEUR (Split Screen)
  // ==========================================
  if (view === "edit" && currentTexte) {
    const inputBase =
      "w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 outline-none transition-all mb-6";

    return (
      <div className="w-full max-w-[1600px] mx-auto fade-in h-[calc(100vh-6rem)] flex flex-col">

        {/* HEADER DE L'ÉDITEUR */}
        <header className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-4 md:p-6 mb-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 shrink-0">
          <div className="flex items-center gap-4 min-w-0">
            <button
              className="p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 transition-colors"
              onClick={() => setView("list")}
              type="button"
              aria-label="Retour à la liste"
            >
              <ArrowLeft size={24} aria-hidden />
            </button>
            <div>
              <h2 className="text-xl font-bold text-slate-100 m-0">Rédiger une Fiche Synthétique</h2>
              <p className="text-slate-400 text-sm m-0 mt-1 max-w-lg truncate" title={currentTexte.titre}>
                Pour : {currentTexte.titre}
              </p>
            </div>
          </div>

          <div className="flex gap-2">
            <button
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl font-bold transition-all bg-white/5 border border-white/10 text-slate-200 hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={() => saveFicheMutation.mutate({ mode: "draft", version: ficheVersion })}
              disabled={saveFicheMutation.isPending}
              type="button"
            >
              <Save size={16} aria-hidden /> Brouillon
            </button>
            <button
              className="inline-flex items-center gap-2 px-5 py-2 rounded-xl transition-all bg-red-500 hover:bg-red-400 text-white font-bold disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={() => saveFicheMutation.mutate({ mode: "publish", version: ficheVersion })}
              disabled={saveFicheMutation.isPending || !formData.objectifPrincipal?.trim()}
              type="button"
            >
              {saveFicheMutation.isPending ? (
                <>
                  <Loader2 size={16} className="animate-spin" aria-hidden /> Publication...
                </>
              ) : (
                <>
                  <Send size={16} aria-hidden /> Publier la fiche
                </>
              )}
            </button>
          </div>
        </header>

        {saveFicheMutation.isError && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl flex items-center gap-3 text-sm mb-4" role="alert">
            <AlertCircle size={20} aria-hidden /> Erreur lors de la sauvegarde de la fiche.
          </div>
        )}

        {/* WORKSPACE : DUAL PANE */}
        <div className="flex-1 grid grid-cols-1 lg:grid-cols-12 gap-6 min-h-0">

          {/* PANNEAU GAUCHE : TEXTE ORIGINAL */}
          <article className="flex flex-col bg-slate-900/60 backdrop-blur-sm border border-white/10 rounded-2xl overflow-hidden lg:col-span-5">
            <header className="p-4 bg-black/40 border-b border-white/10 flex items-center gap-3 font-bold text-slate-100 shrink-0">
              <FileText size={20} className="text-slate-400" aria-hidden /> Texte Juridique Brut
            </header>
            <div className="flex-1 overflow-y-auto p-6 scrollbar-thin scrollbar-thumb-white/10">
              <div className="font-serif text-slate-300 leading-relaxed whitespace-pre-wrap bg-black/20 p-6 rounded-xl border border-white/5">
                {currentTexte.contenu || "⚠️ Aucun contenu brut disponible pour ce texte. Veuillez vérifier la base de données."}
              </div>
            </div>
          </article>

          {/* PANNEAU DROIT : FORMULAIRE */}
          <aside className="flex flex-col bg-slate-900/60 backdrop-blur-sm border border-white/10 rounded-2xl overflow-hidden lg:col-span-7 border-t-4 border-t-red-500">
            <header className="p-4 bg-black/40 border-b border-white/10 flex items-center gap-3 font-bold text-slate-100 shrink-0">
              <BookOpen size={20} className="text-red-400" aria-hidden /> Structure de la Fiche
            </header>
            <div className="flex-1 overflow-y-auto p-6 scrollbar-thin scrollbar-thumb-white/10">

              <label className="text-sm font-bold text-slate-300 mb-2 block">Titre de la fiche *</label>
              <input className={`${inputBase} focus:border-red-500`} name="titre" value={formData.titre} onChange={handleChange} required />

              <label className="text-sm font-bold text-slate-300 mb-2 block">Objectif Principal *</label>
              <textarea
                className={`${inputBase} focus:border-red-500`}
                name="objectifPrincipal"
                value={formData.objectifPrincipal}
                onChange={handleChange}
                rows="4"
                required
                placeholder="Quel est le but de cette loi ?"
              />

              <label className="text-sm font-bold text-slate-300 mb-2 block">Changements Clés</label>
              <textarea
                className={`${inputBase} focus:border-red-500`}
                name="changementsCles"
                value={formData.changementsCles}
                onChange={handleChange}
                rows="4"
                placeholder="Qu'est-ce qui change par rapport à l'ancienne loi ?"
              />

              <label className="text-sm font-bold text-emerald-500 mb-2 block">Nouvelles Obligations</label>
              <textarea
                className={`${inputBase} focus:border-emerald-500`}
                name="obligations"
                value={formData.obligations}
                onChange={handleChange}
                rows="3"
              />

              <label className="text-sm font-bold text-rose-500 mb-2 block">Sanctions Prévues</label>
              <textarea
                className={`${inputBase} focus:border-rose-500`}
                name="sanctions"
                value={formData.sanctions}
                onChange={handleChange}
                rows="3"
              />

              <label className="text-sm font-bold text-slate-300 mb-2 block">Conseils Pratiques & Exemples</label>
              <textarea
                className={`${inputBase} focus:border-red-500`}
                name="conseilsPratiques"
                value={formData.conseilsPratiques}
                onChange={handleChange}
                rows="3"
                placeholder="Conseils de mise en conformité..."
              />
              <textarea
                className={`${inputBase} focus:border-red-500 bg-black/20`}
                name="exemplesConcrets"
                value={formData.exemplesConcrets}
                onChange={handleChange}
                rows="2"
                placeholder="Exemple concret d'application..."
              />

            </div>
          </aside>

        </div>
      </div>
    );
  }

  // Vue de chargement intermédiaire (Fetch de la fiche existante)
  if (view === "loading-fiche") {
    return (
      <div className="p-10 text-center fade-in">
        <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
        <p className="text-slate-400 text-lg">Recherche d'une fiche existante...</p>
      </div>
    );
  }

  // ==========================================
  // RENDU : VUE LISTE (Choix du texte)
  // ==========================================
  return (
    <div className="max-w-7xl mx-auto fade-in">
      <div className="flex items-center gap-3 bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-4 mb-8 focus-within:border-red-500 focus-within:ring-1 focus-within:ring-red-500 transition-all">
        <Search size={20} className="text-slate-500" aria-hidden />
        <input
          type="text"
          className="w-full bg-transparent border-none outline-none text-slate-200 placeholder-slate-500"
          placeholder="Rechercher un texte de loi à synthétiser..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="overflow-x-auto bg-slate-900/50 border border-white/10 rounded-xl">
        {isLoadingTextes ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement des textes de loi...</p>
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
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap w-[70%]">
                  Texte Juridique Original
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap text-right">
                  Action
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredTextes.length === 0 ? (
                <tr>
                  <td colSpan="2" className="text-center p-8 text-slate-400">
                    <Database size={40} className="mx-auto mb-3 opacity-30" aria-hidden /> Aucun texte ne correspond à votre recherche.
                  </td>
                </tr>
              ) : (
                filteredTextes.map((texte) => (
                  <tr key={texte.id} className="transition-colors hover:bg-white/5">
                    <td className="p-4 border-b border-white/5 align-middle">
                      <div className="font-semibold text-slate-100 text-base mb-1" title={texte.titre}>
                        {texte.titre}
                      </div>
                      <div className="text-sm text-slate-400">
                        {texte.referenceOfficielle || "Réf. inconnue"} • {texte.domaine}
                      </div>
                    </td>
                    <td className="p-4 border-b border-white/5 align-middle text-right">
                      <button
                        className="flex items-center justify-end gap-2 px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm text-slate-300 hover:bg-white/10 transition-colors inline-flex"
                        onClick={() => handleOpenEditor(texte)}
                        type="button"
                      >
                        <Edit size={16} aria-hidden /> Gérer la Fiche IA
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}