import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import {
  PenTool, Trash2, ArrowRight, FileText, Search, Pencil, Check, X,
} from "lucide-react";
import juridiqueService, { ANNOTATION_NOTE_MAX_LENGTH } from "../../api/juridiqueService";
import ConfirmDialog from "../../components/common/ConfirmDialog";

export default function MesNotes() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [editingId, setEditingId] = useState(null);
  const [editText, setEditText] = useState("");
  const [deleteTargetId, setDeleteTargetId] = useState(null);

  const canViewNotes = true;

  // 1. CHARGEMENT
  const { data: notes, isLoading } = useQuery({
    queryKey: ["my-annotations"],
    queryFn: () => juridiqueService.getMyAnnotations(),
    enabled: canViewNotes,
  });

  // 2. MUTATIONS
  const deleteNoteMutation = useMutation({
    mutationFn: (noteId) => juridiqueService.deleteAnnotation(noteId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-annotations"] });
      queryClient.invalidateQueries({ queryKey: ["texte-annotations"] });
    },
  });

  const updateNoteMutation = useMutation({
    mutationFn: ({ noteId, text }) => juridiqueService.updateAnnotation(noteId, text),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-annotations"] });
      queryClient.invalidateQueries({ queryKey: ["texte-annotations"] });
      setEditingId(null);
      setEditText("");
    },
  });

  // 3. HANDLERS
  const handleStartEdit = (note) => {
    setEditingId(note.id);
    setEditText(note.note ?? note.contenu ?? "");
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditText("");
  };

  const handleSaveEdit = (noteId) => {
    const t = editText.trim();
    if (!t) return;
    updateNoteMutation.mutate({ noteId, text: t });
  };

  const handleDelete = (noteId) => {
    setDeleteTargetId(noteId);
  };

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-14 text-center">
        <div className="mx-auto mb-4 h-10 w-10 animate-spin rounded-full border-4 border-white/10 border-t-amber-500" aria-hidden />
        <p className="text-sm text-slate-400">Chargement de votre carnet de notes...</p>
      </div>
    );
  }

  const annotationsList = notes || [];

  return (
    <div className="mes-notes-page container mx-auto px-4 py-8 pb-16 fade-in">
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

      {/* EN-TÊTE DE PAGE */}
      <header className="notes-header bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 mb-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div className="min-w-0">
          <h2 className="text-2xl font-bold flex items-center gap-3 text-slate-100 mb-2">
            <PenTool size={26} className="text-amber-500" aria-hidden />
            Mon Carnet Privé
          </h2>
          <p className="text-slate-400 m-0">
            Retrouvez toutes vos réflexions et mémos liés aux textes juridiques.
          </p>
          <div className="mt-3 text-xs text-slate-500">
            Édition / Suppression : <code className="rounded bg-black/30 px-2 py-1 text-slate-300">AnnotationController</code>
          </div>
        </div>

        <div className="bg-amber-500/10 text-amber-500 border border-amber-500/30 rounded-full px-5 py-2.5 font-semibold text-lg shrink-0">
          {annotationsList.length} Note{annotationsList.length > 1 ? "s" : ""}
        </div>
      </header>

      {/* ÉTAT VIDE */}
      {annotationsList.length === 0 ? (
        <div className="empty-state bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-12 mt-8 text-center flex flex-col items-center justify-center">
          <PenTool size={48} className="text-slate-600 mb-4" aria-hidden />
          <h3 className="text-xl font-bold text-slate-200 mb-2">Votre carnet est vide</h3>
          <p className="text-slate-400 mb-8 max-w-md">
            Commencez par rechercher un texte de loi pour y ajouter vos premières annotations.
          </p>
          {/* Navigation propre avec React Router */}
          <button
            className="flex items-center gap-2 bg-amber-500 text-slate-950 font-bold px-6 py-3 rounded-xl transition-all hover:bg-amber-400"
            onClick={() => navigate("/textes")}
            type="button"
          >
            <Search size={18} aria-hidden /> Explorer la base juridique
          </button>
        </div>
      ) : (

        /* GRILLE DE NOTES */
        <div className="notes-grid grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 mt-8">
          {annotationsList.map((note) => {
            const textId = note.legalTextId ?? note.texte?.id ?? note.texteId;
            const textTitle = note.texte?.titre || note.texteTitre || `Texte Juridique #${textId}`;

            return (
              <article
                key={note.id}
                className="note-card-full flex flex-col h-full bg-white/5 backdrop-blur-sm border border-white/10 border-t-4 border-t-amber-500 rounded-xl p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-amber-500/5 group"
              >

                {/* MODE ÉDITION VS LECTURE */}
                {editingId === note.id ? (
                  <div className="mb-4 flex-1">
                    <textarea
                      className="annotation-textarea w-full bg-black/30 border border-white/10 rounded-lg p-3 text-sm text-slate-200 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none resize-y"
                      rows={5}
                      value={editText}
                      maxLength={ANNOTATION_NOTE_MAX_LENGTH}
                      onChange={(e) => setEditText(e.target.value)}
                      disabled={updateNoteMutation.isPending}
                      autoFocus
                    />
                    <div className="mt-2 text-right text-xs text-slate-500">
                      {editText.length}/{ANNOTATION_NOTE_MAX_LENGTH}
                    </div>

                    <div className="flex gap-2 justify-end mt-2">
                      <button
                        className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-white/5 text-slate-300 hover:bg-white/10 transition-colors text-sm"
                        onClick={handleCancelEdit}
                        disabled={updateNoteMutation.isPending}
                        type="button"
                      >
                        <X size={14} aria-hidden /> Annuler
                      </button>
                      <button
                        className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-amber-500 text-slate-950 font-semibold hover:bg-amber-400 transition-colors text-sm disabled:opacity-50"
                        onClick={() => handleSaveEdit(note.id)}
                        disabled={!editText.trim() || updateNoteMutation.isPending}
                        type="button"
                      >
                        <Check size={14} aria-hidden /> Enregistrer
                      </button>
                    </div>
                  </div>
                ) : (
                  <p className="note-card-content text-slate-300 text-base leading-relaxed whitespace-pre-wrap flex-1 mb-6 italic">
                    &quot;{note.note || note.contenu}&quot;
                  </p>
                )}

                {/* CONTEXTE (Lien vers la loi) */}
                <div className="note-context-box bg-black/20 p-4 rounded-lg border border-white/5 mb-6">
                  <div className="note-context-label flex items-center gap-2 text-xs text-slate-500 mb-1">
                    <FileText size={14} aria-hidden /> Sur le texte :
                  </div>
                  <div className="font-medium text-amber-500 truncate text-sm" title={textTitle}>
                    {textTitle}
                  </div>
                </div>

                {/* FOOTER (Dates et Actions) */}
                <footer className="note-card-footer flex flex-wrap justify-between items-end pt-5 border-t border-white/10 gap-4 mt-auto">
                  <div className="text-xs text-slate-500 flex flex-col gap-1">
                    <span>
                      {note.createdAt ? `Créé le ${new Date(note.createdAt).toLocaleDateString()}` : "Récemment"}
                    </span>
                    {note.updatedAt && String(note.updatedAt) !== String(note.createdAt) && editingId !== note.id && (
                      <span className="italic opacity-80">
                        Modifié le {new Date(note.updatedAt).toLocaleDateString()}
                      </span>
                    )}
                  </div>

                  <div className="flex items-center flex-wrap gap-2">
                    {editingId !== note.id && (
                      <button
                        className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:text-amber-500 hover:bg-amber-500/10"
                        onClick={() => handleStartEdit(note)}
                        disabled={deleteNoteMutation.isPending || updateNoteMutation.isPending}
                        title="Modifier la note"
                        type="button"
                      >
                        <Pencil size={18} aria-hidden />
                      </button>
                    )}
                    <button
                      className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:text-red-500 hover:bg-red-500/10"
                      onClick={() => handleDelete(note.id)}
                      disabled={deleteNoteMutation.isPending || updateNoteMutation.isPending || editingId === note.id}
                      title="Supprimer la note"
                      type="button"
                    >
                      <Trash2 size={18} aria-hidden />
                    </button>

                    <Link
                      to={`/textes/${textId}`}
                      className="flex items-center gap-2 px-3 py-1.5 border border-white/10 rounded-lg text-slate-300 hover:bg-white/10 hover:text-white text-sm transition-colors ml-2"
                    >
                      Ouvrir <ArrowRight size={14} aria-hidden />
                    </Link>
                  </div>
                </footer>

              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}