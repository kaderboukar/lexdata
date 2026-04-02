import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import {
  Bookmark, Trash2, ArrowRight, FileText, Library, Loader2,
} from "lucide-react";
import favoriteService from "../../api/favoriteService";
import { useState } from "react";
import ConfirmDialog from "../../components/common/ConfirmDialog";

export default function MesFavoris() {
  const queryClient = useQueryClient();
  const [deleteTarget, setDeleteTarget] = useState(null);

  // CHARGEMENT DES FAVORIS
  const { data: favorites = [], isLoading, isError } = useQuery({
    queryKey: ["my-favorites"],
    queryFn: () => favoriteService.getMyFavorites(),
    staleTime: 30_000,
  });

  // MUTATION DE SUPPRESSION
  const removeMutation = useMutation({
    mutationFn: (textId) => favoriteService.removeFavorite(textId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-favorites"] });
    },
  });

  const handleRemove = (textId, titre) => {
    setDeleteTarget({ textId, titre });
  };

  // --- RENDUS DE CHARGEMENT ET ERREUR ---
  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center mt-20 gap-4 text-slate-400">
        <Loader2 size={36} className="animate-spin text-amber-500" aria-hidden />
        <p className="text-sm">Chargement de votre bibliothèque...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center mt-20 gap-4 text-slate-400 px-4">
        <div className="bg-red-500/10 border border-red-500/20 rounded-2xl p-6 backdrop-blur-sm max-w-2xl w-full text-center">
          <h3 className="text-lg font-bold text-red-200 mb-2">Impossible de charger vos favoris</h3>
          <p className="text-sm text-slate-300/80">
            Vérifiez votre connexion au serveur ou l'accès à la route{" "}
            <code className="rounded bg-black/30 px-2 py-1 text-slate-200">/api/juridique/favorites/me</code>.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="mes-favoris-page container mx-auto px-4 py-8 pb-16 fade-in">
      <ConfirmDialog
        open={deleteTarget != null}
        title="Retirer des favoris ?"
        description={
          deleteTarget?.titre
            ? `Retirer « ${deleteTarget.titre.slice(0, 80)} » de votre bibliothèque ?`
            : "Retirer ce texte de votre bibliothèque ?"
        }
        confirmText="Retirer"
        cancelText="Annuler"
        danger
        loading={removeMutation.isPending}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={() => {
          if (!deleteTarget) return;
          removeMutation.mutate(deleteTarget.textId, {
            onSettled: () => setDeleteTarget(null),
          });
        }}
      />

      {/* EN-TÊTE DE PAGE */}
      <header className="favorites-header bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 mb-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div className="min-w-0">
          <h2 className="text-2xl font-bold flex items-center gap-3 text-slate-100 mb-2">
            <Bookmark size={26} className="text-amber-500" aria-hidden />
            Mes Textes Favoris
          </h2>
          <div className="text-xs text-slate-500">
            API : <code className="rounded bg-black/30 px-2 py-1 text-slate-300">FavoriteController</code>
          </div>
        </div>

        <div className="bg-amber-500/10 text-amber-500 border border-amber-500/30 rounded-full px-5 py-2.5 font-semibold text-lg shrink-0">
          {favorites.length} favori{favorites.length !== 1 ? "s" : ""}
        </div>
      </header>

      {/* ÉTAT VIDE */}
      {favorites.length === 0 ? (
        <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-12 mt-8 text-center flex flex-col items-center justify-center">
          <Bookmark size={48} className="text-slate-600 mb-4" aria-hidden />
          <h3 className="text-xl font-bold text-slate-200 mb-2">Aucun favori pour l’instant</h3>
          <p className="text-slate-400 mb-8 max-w-md">
            Depuis la fiche d’un texte publié, utilisez le bouton "Sauvegarder" pour le retrouver ici.
          </p>
          <Link
            to="/textes"
            className="flex items-center gap-2 bg-amber-500 text-slate-950 font-bold px-6 py-3 rounded-xl transition-all hover:bg-amber-400"
          >
            <Library size={18} aria-hidden /> Parcourir la base juridique
          </Link>
        </div>
      ) : (

        /* GRILLE DES FAVORIS */
        <div className="favorites-grid grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 mt-8">
          {favorites.map((f) => {
            // UX Premium : On vérifie si CE composant précis est en cours de suppression
            const isDeletingThis = removeMutation.isPending && removeMutation.variables === f.legalTextId;

            return (
              <article
                key={f.id}
                className="favorite-card flex flex-col h-full bg-white/5 backdrop-blur-sm border border-white/10 border-t-4 border-t-amber-500 rounded-xl p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-amber-500/5 group"
              >

                {/* BADGES */}
                <div className="favorite-card-badges flex flex-wrap gap-2 mb-4">
                  <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                    {f.type || "NON DÉFINI"}
                  </span>
                  <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                    {f.domaine || "GÉNÉRAL"}
                  </span>
                </div>

                {/* TITRE & META */}
                <h3 className="favorite-card-title text-lg font-bold text-slate-100 mb-3 flex items-start gap-3 leading-tight line-clamp-3">
                  <FileText size={18} className="text-amber-500 shrink-0 mt-1" aria-hidden />
                  <span className="min-w-0">{f.titre}</span>
                </h3>

                {f.referenceOfficielle && (
                  <p className="favorite-card-meta text-sm text-slate-400 mb-1">Réf. {f.referenceOfficielle}</p>
                )}
                {f.createdAt && (
                  <p className="favorite-card-meta text-sm text-slate-400 mb-1 italic opacity-70">
                    Ajouté le {new Date(f.createdAt).toLocaleDateString()}
                  </p>
                )}

                {/* ACTIONS */}
                <footer className="favorite-card-footer flex justify-between items-center pt-5 mt-auto border-t border-white/10">
                  <button
                    type="button"
                    className="text-slate-500 hover:text-red-500 hover:bg-red-500/10 p-2 rounded-md transition-colors duration-200 disabled:opacity-50"
                    title="Retirer des favoris"
                    disabled={removeMutation.isPending}
                    onClick={() => handleRemove(f.legalTextId, f.titre)}
                  >
                    {isDeletingThis ? (
                      <Loader2 size={18} className="animate-spin" aria-hidden />
                    ) : (
                      <Trash2 size={18} aria-hidden />
                    )}
                  </button>

                  <Link
                    to={`/textes/${f.legalTextId}`}
                    className="flex items-center gap-2 px-3 py-1.5 border border-white/10 rounded-lg text-slate-300 hover:bg-white/10 hover:text-white text-sm transition-colors"
                  >
                    Ouvrir <ArrowRight size={14} aria-hidden />
                  </Link>
                </footer>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}