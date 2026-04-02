import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft, Bookmark, BookmarkCheck, Share2, Download, Clock,
  ShieldAlert, PenTool, Plus, Trash2, Pencil, Check, X,
  FileText, BookOpen, CheckCircle2, AlertTriangle, Lightbulb, Target,
  Loader2,
} from "lucide-react";
import juridiqueService, { ANNOTATION_NOTE_MAX_LENGTH } from "../../api/juridiqueService";
import favoriteService from "../../api/favoriteService";
import syntheseService from "../../api/syntheseService";
import ConfirmDialog from "../../components/common/ConfirmDialog";

export default function TexteDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [newNote, setNewNote] = useState("");
  const [editingNoteId, setEditingNoteId] = useState(null);
  const [editDraft, setEditDraft] = useState("");
  const [activeTab, setActiveTab] = useState("texte"); // 'texte' ou 'fiche'
  const [deleteTargetId, setDeleteTargetId] = useState(null);

  const canAnnotate = true;

  // 1. CHARGEMENT TEXTE
  const { data: texte, isLoading: isTexteLoading, isError } = useQuery({
    queryKey: ["texte", id],
    queryFn: () => juridiqueService.getTexteById(id),
    staleTime: 300000,
  });

  // 2. CHARGEMENT FICHE
  const { data: fiche } = useQuery({
    queryKey: ["fiches-by-texte", id],
    queryFn: () => syntheseService.getPublishedFicheByTexteId(id),
    staleTime: 60000,
  });

  // FAVORIS
  const { data: myFavorites = [] } = useQuery({
    queryKey: ["my-favorites"],
    queryFn: () => favoriteService.getMyFavorites(),
    staleTime: 30000,
  });

  const isFavorite = myFavorites.some((f) => Number(f.legalTextId) === Number(id));

  const toggleFavoriteMutation = useMutation({
    mutationFn: async () => {
      const textId = Number(id);
      if (isFavorite) await favoriteService.removeFavorite(textId);
      else await favoriteService.addFavorite(textId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-favorites"] });
    },
  });

  const handleToggleFavorite = () => {
    if (texte?.estPublie === false) return;
    toggleFavoriteMutation.mutate();
  };

  // 3. ANNOTATIONS
  const { data: textAnnotations = [] } = useQuery({
    queryKey: ["texte-annotations", id],
    queryFn: () => juridiqueService.getAnnotationsForText(id),
    enabled: Boolean(id) && canAnnotate,
  });

  const addNoteMutation = useMutation({
    mutationFn: (noteContent) => juridiqueService.addAnnotation(id, noteContent),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["texte-annotations", id] });
      queryClient.invalidateQueries({ queryKey: ["my-annotations"] });
      setNewNote("");
    },
  });

  const deleteNoteMutation = useMutation({
    mutationFn: (noteId) => juridiqueService.deleteAnnotation(noteId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["texte-annotations", id] });
      queryClient.invalidateQueries({ queryKey: ["my-annotations"] });
    },
  });

  const updateNoteMutation = useMutation({
    mutationFn: ({ noteId, text }) => juridiqueService.updateAnnotation(noteId, text),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["texte-annotations", id] });
      queryClient.invalidateQueries({ queryKey: ["my-annotations"] });
      setEditingNoteId(null);
      setEditDraft("");
    },
  });

  const handleStartEdit = (note) => {
    setEditingNoteId(note.id);
    setEditDraft(note.note ?? note.contenu ?? "");
  };

  const handleCancelEdit = () => {
    setEditingNoteId(null);
    setEditDraft("");
  };

  const handleSaveEdit = (noteId) => {
    const t = editDraft.trim();
    if (!t) return;
    updateNoteMutation.mutate({ noteId, text: t });
  };

  const handleAddNote = (e) => {
    e.preventDefault();
    if (newNote.trim()) {
      addNoteMutation.mutate(newNote);
    }
  };

  const handleDeleteNote = (noteId) => {
    setDeleteTargetId(noteId);
  };

  // 4. PDF
  const handleDownloadPdf = async () => {
    if (!fiche) return;
    try {
      const blob = await syntheseService.downloadPdf(fiche.id);
      const url = window.URL.createObjectURL(new Blob([blob], { type: "application/pdf" }));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `Fiche_Pratique_${fiche.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (error) {
      console.error("Erreur de téléchargement", error);
      alert("Erreur lors de la génération du PDF.");
    }
  };

  // --- RENDUS DE CHARGEMENT & ERREUR ---
  if (isTexteLoading) {
    return (
      <div className="container mx-auto px-4 py-14 text-center">
        <Loader2 size={44} className="mx-auto mb-4 animate-spin text-amber-500" aria-hidden />
        <p className="text-sm text-slate-400">Ouverture des archives juridiques...</p>
      </div>
    );
  }

  if (isError || !texte) {
    return (
      <div className="container mx-auto px-4 py-14 text-center">
        <ShieldAlert size={64} className="mx-auto mb-4 text-red-500" aria-hidden />
        <h2 className="text-2xl font-extrabold text-slate-100">Texte introuvable ou accès refusé</h2>
        <p className="mt-2 text-sm text-slate-400">
          Vérifiez le lien ou vos droits d'accès, puis réessayez.
        </p>
        <button
          className="mt-6 inline-flex items-center justify-center rounded-xl border border-white/10 bg-white/5 px-5 py-3 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10"
          onClick={() => navigate(-1)}
          type="button"
        >
          Retourner à la recherche
        </button>
      </div>
    );
  }

  return (
    <div className="texte-detail-page flex flex-col lg:flex-row gap-8 pt-6 pb-20 items-start container mx-auto px-4">
      <ConfirmDialog
        open={deleteTargetId != null}
        title="Supprimer cette note ?"
        description="Cette action est irréversible. La note sera supprimée définitivement."
        confirmText="Supprimer"
        cancelText="Annuler"
        danger
        loading={deleteNoteMutation.isPending}
        onCancel={() => setDeleteTargetId(null)}
        onConfirm={() => {
          if (deleteTargetId == null) return;
          deleteNoteMutation.mutate(deleteTargetId, {
            onSettled: () => setDeleteTargetId(null),
          });
        }}
      />

      {/* ========================================== */}
      {/* COLONNE GAUCHE : LECTURE                     */}
      {/* ========================================== */}
      <main className="flex-1 w-full min-w-0 lg:w-[65%]">

        <div className="actions-bar flex flex-wrap justify-between items-center gap-4 mb-8">
          <button
            className="flex items-center gap-2 text-slate-400 hover:text-slate-200 transition-colors font-medium"
            onClick={() => navigate(-1)}
            type="button"
          >
            <ArrowLeft size={18} aria-hidden /> Retour
          </button>

          <div className="flex flex-wrap gap-3">
            <button
              className="inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-4 py-2.5 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10"
              title="Partager (Bientôt)"
              type="button"
            >
              <Share2 size={16} aria-hidden /> Partager
            </button>
            <button
              className={[
                "inline-flex items-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-semibold transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed",
                isFavorite
                  ? "border-amber-500/30 bg-amber-500/15 text-amber-400 hover:bg-amber-500/20"
                  : "border-white/10 bg-white/5 text-slate-200 hover:bg-white/10",
              ].join(" ")}
              onClick={handleToggleFavorite}
              disabled={toggleFavoriteMutation.isPending || texte?.estPublie === false}
              type="button"
            >
              {isFavorite ? (
                <>
                  <BookmarkCheck size={16} aria-hidden /> Sauvegardé
                </>
              ) : (
                <>
                  <Bookmark size={16} aria-hidden /> Sauvegarder
                </>
              )}
            </button>
            {fiche && (
              <button
                className="inline-flex items-center gap-2 rounded-xl bg-amber-500 px-4 py-2.5 text-sm font-bold text-slate-950 shadow-lg shadow-amber-500/20 transition-all duration-300 hover:brightness-110"
                onClick={handleDownloadPdf}
                type="button"
              >
                <Download size={16} aria-hidden /> Fiche PDF
              </button>
            )}
          </div>
        </div>

        <header className="border-b border-white/10 pb-8 mb-8">
          <div className="texte-badges flex flex-wrap gap-2 mb-5">
            <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
              {texte.type || "TYPE NON DÉFINI"}
            </span>
            <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
              {texte.domaine || "Général"}
            </span>
            {texte.statut && (
              <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                {texte.statut}
              </span>
            )}
            {texte.estPremium && (
              <span className="bg-amber-500/15 border border-amber-500/25 text-amber-400 px-3 py-1 rounded-full text-xs font-semibold">
                Premium
              </span>
            )}
            <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold inline-flex items-center gap-2">
              <Clock size={12} aria-hidden /> {texte.dateSignature || "—"}
            </span>
          </div>

          <h1 className="text-3xl md:text-4xl font-extrabold text-slate-100 mb-4 leading-tight tracking-tight">
            {texte.titre}
          </h1>

          <div className="texte-meta flex flex-wrap items-center gap-4 text-sm text-slate-400">
            <span className="inline-flex items-center gap-2">
              <BookOpen size={16} aria-hidden />
              {texte.referenceOfficielle ? `Réf: ${texte.referenceOfficielle}` : "Référence en cours"}
            </span>
            {texte.journalOfficielRef && <span>• J.O. {texte.journalOfficielRef}</span>}
            {texte.sourceOfficielle && <span>• {texte.sourceOfficielle}</span>}
          </div>
        </header>

        {/* ONGLETS */}
        <div className="content-tabs flex flex-wrap gap-4 border-b border-white/10 pb-6 mb-8">
          <button
            className={[
              "tab-btn flex items-center gap-2 px-6 py-2.5 rounded-full font-semibold border transition-all relative",
              activeTab === "texte"
                ? "bg-amber-500 text-slate-950 border-amber-500"
                : "border-white/10 text-slate-400 hover:bg-white/5",
            ].join(" ")}
            onClick={() => setActiveTab("texte")}
            type="button"
          >
            <FileText size={18} aria-hidden /> Texte Officiel
          </button>
          <button
            className={[
              "tab-btn flex items-center gap-2 px-6 py-2.5 rounded-full font-semibold border transition-all relative",
              activeTab === "fiche"
                ? "bg-amber-500 text-slate-950 border-amber-500"
                : "border-white/10 text-slate-400 hover:bg-white/5",
            ].join(" ")}
            onClick={() => setActiveTab("fiche")}
            type="button"
          >
            <BookOpen size={18} aria-hidden /> Fiche Pratique
            {fiche && (
              <span className="absolute -top-1 -right-1 w-3 h-3 bg-emerald-500 rounded-full border-2 border-slate-900" />
            )}
          </button>
        </div>

        {/* CONTENU : TEXTE OFFICIEL */}
        {activeTab === "texte" && (
          <article className="texte-body bg-slate-900/50 backdrop-blur-sm border border-white/10 p-6 md:p-12 rounded-2xl text-slate-300 text-lg leading-[1.9] whitespace-pre-wrap shadow-xl">
            {texte.contenu || "Le contenu brut de ce texte n'a pas encore été numérisé."}
          </article>
        )}

        {/* CONTENU : FICHE PRATIQUE */}
        {activeTab === "fiche" && (
          <div className="fiche-pratique-container bg-slate-900/50 backdrop-blur-sm border border-white/10 p-6 md:p-10 rounded-2xl">
            {!fiche ? (
              <div className="text-center">
                <Lightbulb size={48} className="mx-auto mb-3 text-slate-500" aria-hidden />
                <h3 className="text-lg font-bold text-slate-100 mb-2">En cours de rédaction</h3>
                <p className="text-sm text-slate-400">
                  Nos experts juridiques rédigent actuellement la fiche pratique pour ce texte.
                </p>
              </div>
            ) : (
              <div>
                <header className="border-b border-white/10 pb-6 mb-8">
                  <h2 className="text-2xl font-extrabold text-amber-500 mb-2">{fiche.titre}</h2>
                  {(fiche.status || fiche.version != null) && (
                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400">
                      {fiche.status && (
                        <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                          {fiche.status}
                        </span>
                      )}
                      {fiche.version != null && <span>V{fiche.version}</span>}
                      {fiche.dateModification && (
                        <span>• Maj. {new Date(fiche.dateModification).toLocaleDateString()}</span>
                      )}
                    </div>
                  )}
                </header>

                <section className="mb-8">
                  <h4 className="flex items-center gap-2 text-slate-100 text-lg font-bold mb-3">
                    <Target size={20} className="text-amber-500" aria-hidden /> Objectif Principal
                  </h4>
                  <div className="fiche-content-box bg-black/20 p-6 rounded-xl border border-white/10 text-slate-300 text-base leading-relaxed whitespace-pre-wrap">
                    {fiche.objectifPrincipal}
                  </div>
                </section>

                {fiche.changementsCles && (
                  <section className="mb-8">
                    <h4 className="flex items-center gap-2 text-slate-100 text-lg font-bold mb-3">
                      <Lightbulb size={20} className="text-amber-400" aria-hidden /> Ce qui change
                    </h4>
                    <div className="fiche-content-box bg-black/20 p-6 rounded-xl border border-white/10 text-slate-300 text-base leading-relaxed whitespace-pre-wrap">
                      {fiche.changementsCles}
                    </div>
                  </section>
                )}

                <div className="fiche-grid grid grid-cols-1 md:grid-cols-2 gap-6 my-8">
                  {fiche.obligations && (
                    <div className="bg-emerald-500/5 border border-emerald-500/20 p-6 rounded-xl">
                      <h4 className="flex items-center gap-2 text-emerald-500 text-lg font-bold mb-3">
                        <CheckCircle2 size={18} aria-hidden /> Nouvelles Obligations
                      </h4>
                      <p className="text-slate-300 text-base leading-relaxed whitespace-pre-wrap">
                        {fiche.obligations}
                      </p>
                    </div>
                  )}

                  {fiche.sanctions && (
                    <div className="bg-red-500/5 border border-red-500/20 p-6 rounded-xl">
                      <h4 className="flex items-center gap-2 text-red-500 text-lg font-bold mb-3">
                        <AlertTriangle size={18} aria-hidden /> Sanctions Prévues
                      </h4>
                      <p className="text-slate-300 text-base leading-relaxed whitespace-pre-wrap">
                        {fiche.sanctions}
                      </p>
                    </div>
                  )}
                </div>

                {fiche.conseilsPratiques && (
                  <section className="mb-2">
                    <h4 className="flex items-center gap-2 text-slate-100 text-lg font-bold mb-3">
                      <BookOpen size={20} className="text-amber-500" aria-hidden /> Conseils & Exemples
                    </h4>
                    <div className="border-l-4 border-amber-500 pl-6">
                      <p className="text-slate-300 text-base leading-relaxed whitespace-pre-wrap">
                        {fiche.conseilsPratiques}
                      </p>
                      {fiche.exemplesConcrets && (
                        <div className="mt-4 bg-white/5 border border-white/10 rounded-xl p-5 text-sm text-slate-300/80 italic whitespace-pre-wrap">
                          <strong className="not-italic text-slate-100 block mb-2">Exemple d&apos;application :</strong>
                          {fiche.exemplesConcrets}
                        </div>
                      )}
                    </div>
                  </section>
                )}
              </div>
            )}
          </div>
        )}
      </main>

      {/* ========================================== */}
      {/* COLONNE DROITE : ANNOTATIONS (Sticky)        */}
      {/* ========================================== */}
      <aside className="w-full lg:w-[35%] lg:sticky lg:top-28 lg:max-h-[calc(100vh-120px)] flex flex-col">
        <div className="annotations-panel bg-slate-900/60 backdrop-blur-md border border-white/10 rounded-2xl p-6 flex flex-col h-full">

          <header className="flex items-center gap-3 pb-4 border-b border-white/10 mb-6">
            <PenTool size={22} className="text-amber-500" aria-hidden />
            <h3 className="text-lg font-extrabold text-slate-100 m-0">Notes Privées</h3>
          </header>

          <form onSubmit={handleAddNote} className="mb-6">
            <textarea
              className="annotation-textarea w-full bg-black/30 border border-white/10 rounded-lg p-3 text-sm text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none resize-y min-h-[80px]"
              placeholder="Saisissez une annotation personnelle liée à ce texte..."
              value={newNote}
              maxLength={ANNOTATION_NOTE_MAX_LENGTH}
              onChange={(e) => setNewNote(e.target.value)}
              disabled={addNoteMutation.isPending}
            />
            <div className="mt-2 text-right text-xs text-slate-400">
              {newNote.length}/{ANNOTATION_NOTE_MAX_LENGTH}
            </div>

            <button
              type="submit"
              className="mt-3 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-amber-500 px-4 py-3 text-sm font-extrabold text-slate-950 shadow-lg shadow-amber-500/20 transition-all duration-300 hover:brightness-110 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={!newNote.trim() || addNoteMutation.isPending}
            >
              {addNoteMutation.isPending ? (
                "Sauvegarde..."
              ) : (
                <>
                  <Plus size={16} aria-hidden /> Ajouter
                </>
              )}
            </button>
          </form>

          <div className="notes-list flex-1 overflow-y-auto flex flex-col gap-4 pr-2 mt-2">
            {textAnnotations.length === 0 ? (
              <div className="rounded-xl border border-white/10 bg-black/20 p-4">
                <p className="text-slate-400 text-sm italic m-0">
                  Vous n&apos;avez pas encore de note pour ce texte.
                </p>
              </div>
            ) : (
              textAnnotations.map((note) => (
                <article key={note.id} className="note-card bg-black/30 p-5 rounded-xl border-l-4 border-amber-500 transition-transform hover:translate-x-1 group">

                  {editingNoteId === note.id ? (
                    <div>
                      <textarea
                        className="annotation-textarea w-full bg-black/30 border border-white/10 rounded-lg p-3 text-sm text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none resize-y min-h-[80px]"
                        value={editDraft}
                        maxLength={ANNOTATION_NOTE_MAX_LENGTH}
                        onChange={(e) => setEditDraft(e.target.value)}
                        disabled={updateNoteMutation.isPending}
                      />
                      <div className="mt-2 text-right text-xs text-slate-400">
                        {editDraft.length}/{ANNOTATION_NOTE_MAX_LENGTH}
                      </div>
                      <div className="flex gap-2 justify-end">
                        <button
                          type="button"
                          className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-xs font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10 disabled:opacity-50"
                          onClick={handleCancelEdit}
                          disabled={updateNoteMutation.isPending}
                        >
                          <X size={14} aria-hidden /> Annuler
                        </button>
                        <button
                          type="button"
                          className="inline-flex items-center gap-2 rounded-lg bg-amber-500 px-3 py-2 text-xs font-extrabold text-slate-950 transition-all duration-300 hover:brightness-110 disabled:opacity-50"
                          onClick={() => handleSaveEdit(note.id)}
                          disabled={!editDraft.trim() || updateNoteMutation.isPending}
                        >
                          <Check size={14} aria-hidden /> Enregistrer
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="whitespace-pre-wrap text-sm text-slate-200/85 leading-relaxed mb-4">
                        {note.note ?? note.contenu}
                      </div>
                      <footer className="flex justify-between items-end border-t border-dashed border-white/10 pt-4">
                        <div className="flex flex-col gap-1 text-xs text-slate-400">
                          <span>{note.createdAt ? new Date(note.createdAt).toLocaleDateString() : "À l'instant"}</span>
                          {note.updatedAt && String(note.updatedAt) !== String(note.createdAt) && (
                            <span className="italic opacity-70">Modifié</span>
                          )}
                        </div>
                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => handleStartEdit(note)}
                            className="rounded-md p-2 text-slate-500 transition-colors duration-200 hover:bg-white/5 hover:text-amber-500"
                            title="Modifier"
                          >
                            <Pencil size={14} aria-hidden />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDeleteNote(note.id)}
                            className="rounded-md p-2 text-slate-500 transition-colors duration-200 hover:bg-white/5 hover:text-red-500"
                            title="Supprimer"
                          >
                            <Trash2 size={14} aria-hidden />
                          </button>
                        </div>
                      </footer>
                    </>
                  )}
                </article>
              ))
            )}
          </div>

        </div>
      </aside>
    </div>
  );
}