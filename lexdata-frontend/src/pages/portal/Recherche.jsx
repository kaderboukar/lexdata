import { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Search, Filter, BookOpen, AlertCircle, ChevronRight,
  FileText, Library, Zap, Database
} from "lucide-react";
import { Link } from "react-router-dom";
import juridiqueService from "../../api/juridiqueService";
import useDebounce from "../../hooks/useDebounce";
import { JURIDIQUE_LEGAL_DOMAINS, JURIDIQUE_TYPE_TEXTES } from "../../constants/juridiqueEnums";

export default function Recherche() {
  const [searchTerm, setSearchTerm] = useState("");
  const [domaine, setDomaine] = useState("");
  const [typeTexte, setTypeTexte] = useState("");
  const [searchMode, setSearchMode] = useState("catalogue");
  const [page, setPage] = useState(0);

  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  const elasticReady = searchMode === "elastic" && debouncedSearchTerm.trim().length >= 2;
  const catalogueReady = searchMode === "catalogue";

  useEffect(() => {
    setPage(0);
  }, [debouncedSearchTerm, domaine, typeTexte, searchMode]);

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["textes-search", searchMode, debouncedSearchTerm, domaine, typeTexte, page],
    queryFn: () =>
      searchMode === "elastic"
        ? juridiqueService.advancedSearch(debouncedSearchTerm.trim())
        : juridiqueService.searchTextes({
          q: debouncedSearchTerm || undefined,
          domaine: domaine || undefined,
          type: typeTexte || undefined,
          page,
          size: 10,
        }),
    enabled: catalogueReady || elasticReady,
    staleTime: 60000,
  });

  const isElastic = searchMode === "elastic";
  const textes = isElastic ? (Array.isArray(data) ? data : []) : data?.content ?? [];
  const totalElements = isElastic ? textes.length : data?.totalElements ?? textes.length;
  const totalPages = isElastic ? 1 : data?.totalPages ?? 1;
  const currentPage = isElastic ? 0 : data?.number ?? page;
  const showElasticHint = searchMode === "elastic" && debouncedSearchTerm.trim().length < 2;

  return (
    <div>

      {/* 1. EN-TÊTE DE RECHERCHE */}
      <section className="bg-white/5 border border-white/10 rounded-2xl p-6 mb-8 backdrop-blur-sm">

        {/* Barre principale */}
        <div className="flex items-center gap-3 bg-slate-900/60 border border-white/10 rounded-xl px-4 py-3 transition-all duration-300 focus-within:border-amber-500 focus-within:ring-1 focus-within:ring-amber-500 focus-within:bg-slate-900/90 mb-6">
          <Search size={22} className="text-amber-500" aria-hidden />
          <input
            type="text"
            className="flex-1 bg-transparent border-none outline-none text-slate-100 placeholder-slate-500 text-lg"
            placeholder="Rechercher une loi, un article, un mot-clé (ex: Cybercriminalité)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            autoFocus
          />
          {searchTerm && (
            <button
              className="text-slate-500 hover:text-red-500 transition-colors duration-200 p-1 rounded-full"
              onClick={() => setSearchTerm("")}
              type="button"
              aria-label="Effacer la recherche"
            >
              ✕
            </button>
          )}
        </div>

        {/* Filtres et Modes */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">

          {/* Toggle Catalogue / Elastic */}
          <div className="flex bg-black/30 p-1 rounded-lg border border-white/10">
            <button
              type="button"
              className={[
                "flex items-center gap-2 px-4 py-2 rounded-md text-sm font-semibold transition-all duration-200 text-slate-400 hover:text-slate-200",
                searchMode === "catalogue" ? "bg-amber-500/20 text-amber-500 shadow-md" : "",
              ].join(" ")}
              onClick={() => setSearchMode("catalogue")}
            >
              <Library size={16} /> Catalogue
            </button>
            <button
              type="button"
              className={[
                "flex items-center gap-2 px-4 py-2 rounded-md text-sm font-semibold transition-all duration-200 text-slate-400 hover:text-slate-200",
                searchMode === "elastic" ? "bg-amber-500/20 text-amber-500 shadow-md" : "",
              ].join(" ")}
              onClick={() => setSearchMode("elastic")}
            >
              <Zap size={16} /> Plein texte
            </button>
          </div>

          {/* Filtres (Désactivés si Elastic) */}
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex items-center gap-2">
              <Filter size={16} className="text-slate-500" aria-hidden />
              <select
                className="bg-slate-900/50 border border-white/10 rounded-lg px-4 py-2 text-sm text-slate-300 min-w-[180px] disabled:opacity-50 disabled:cursor-not-allowed focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
                value={domaine}
                onChange={(e) => setDomaine(e.target.value)}
                disabled={isElastic}
              >
                <option value="">Tous les domaines</option>
                {JURIDIQUE_LEGAL_DOMAINS.map((d) => (
                  <option key={d.value} value={d.value}>{d.label}</option>
                ))}
              </select>
            </div>

            <div className="flex items-center gap-2">
              <select
                className="bg-slate-900/50 border border-white/10 rounded-lg px-4 py-2 text-sm text-slate-300 min-w-[180px] disabled:opacity-50 disabled:cursor-not-allowed focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none"
                value={typeTexte}
                onChange={(e) => setTypeTexte(e.target.value)}
                disabled={isElastic}
              >
                <option value="">Tous les types</option>
                {JURIDIQUE_TYPE_TEXTES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Indication Elastic */}
        {isElastic && (
          <div className="mt-3 flex items-start gap-2 text-sm text-slate-400">
            <Zap size={14} className="mt-0.5 text-amber-500" aria-hidden />
            <span>
              Mode plein texte activé (Elasticsearch). Minimum 2 caractères. Les filtres de type/domaine sont ignorés.
            </span>
          </div>
        )}
      </section>

      {/* 2. ZONE DE RÉSULTATS */}
      <section>

        {/* Indice minimum Elastic */}
        {showElasticHint && (
          <div className="mt-4 rounded-xl border border-sky-500/20 bg-sky-500/10 px-5 py-4 text-sky-200 backdrop-blur-sm">
            <div className="flex items-start gap-3">
              <Database size={20} className="mt-0.5 shrink-0" aria-hidden />
              <div className="text-sm leading-relaxed">
                Saisissez au moins <strong>2 caractères</strong> pour lancer la recherche plein texte experte.
              </div>
            </div>
          </div>
        )}

        {/* Chargement (Skeleton Sémantique) */}
        {isLoading && !showElasticHint && (
          <div className="mt-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="animate-pulse bg-white/5 border border-white/10 rounded-xl p-6 mb-4 flex flex-col gap-4">
                <div className="h-6 w-3/4 bg-white/10 rounded" />
                <div className="h-4 w-full bg-white/5 rounded" />
                <div className="h-4 w-5/6 bg-white/5 rounded" />
              </div>
            ))}
          </div>
        )}

        {/* Erreur */}
        {isError && (
          <div className="mt-4 rounded-xl border border-red-500/20 bg-red-500/10 px-5 py-4 text-red-200 backdrop-blur-sm">
            <div className="flex items-start gap-3">
              <AlertCircle size={20} className="mt-0.5 shrink-0" aria-hidden />
              <div className="text-sm leading-relaxed">
                {error?.response?.data?.message || error?.message || "Une erreur est survenue lors de la recherche."}
              </div>
            </div>
          </div>
        )}

        {/* Liste des Résultats */}
        {!isLoading && !isError && !showElasticHint && textes.length > 0 && (
          <div className="mt-4">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
              <span className="text-sm font-semibold text-slate-100">{totalElements} résultat(s) trouvé(s)</span>
              <span className="text-xs text-slate-400">
                {isElastic ? "Tri par pertinence (Elastic)" : "Tri par date de signature"}
              </span>
            </div>

            <div className="flex flex-col gap-4 mt-6">
              {textes.map((texte) => (
                <Link to={`/textes/${texte.id}`} key={texte.id} className="block">
                  <div className="group flex flex-col md:flex-row justify-between items-start md:items-center p-6 bg-white/5 border border-white/10 rounded-xl transition-all duration-300 hover:-translate-y-1 hover:border-amber-500/50 hover:bg-amber-500/5 hover:shadow-xl">
                    <div className="min-w-0">
                      <div className="mb-3 flex flex-wrap gap-2">
                        <span className="rounded-full border border-white/10 bg-black/30 px-3 py-1 text-xs font-semibold text-slate-300">
                          {texte.type || "NON DÉFINI"}
                        </span>
                        <span className="rounded-full border border-white/10 bg-black/30 px-3 py-1 text-xs font-semibold text-slate-300">
                          {texte.domaine || "GÉNÉRAL"}
                        </span>
                      </div>

                      <h3 className="text-lg font-bold text-slate-100 mb-2 flex items-start gap-3 leading-tight">
                        <FileText size={18} className="mt-0.5 shrink-0 text-amber-500" aria-hidden />
                        <span className="min-w-0">{texte.titre}</span>
                      </h3>

                      {texte.referenceOfficielle && (
                        <p className="text-xs text-slate-400 pl-8 mb-2">Réf. {texte.referenceOfficielle}</p>
                      )}

                      <p className="text-sm text-slate-400 pl-8 line-clamp-2 leading-relaxed">
                        {isElastic && Array.isArray(texte.highlights) && texte.highlights.length > 0
                          ? texte.highlights.map((hl, i) => (
                              <span key={i} dangerouslySetInnerHTML={{ __html: hl }} />
                            ))
                          : texte.resume ||
                            (texte.contenu ? `${texte.contenu.substring(0, 180)}…` : "Aucun extrait disponible.")}
                      </p>
                    </div>

                    <ChevronRight
                      size={24}
                      className="text-slate-600 transition-colors duration-300 group-hover:text-amber-500 shrink-0 mt-4 md:mt-0"
                      aria-hidden
                    />
                  </div>
                </Link>
              ))}
            </div>

            {/* Pagination */}
            {!isElastic && totalPages > 1 && (
              <div className="flex items-center justify-center gap-6 pt-8">
                <button
                  className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm font-medium text-slate-300 hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={currentPage <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  type="button"
                >
                  Précédent
                </button>
                <span className="text-sm text-slate-400">
                  Page <strong>{currentPage + 1}</strong> sur {totalPages}
                </span>
                <button
                  className="px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm font-medium text-slate-300 hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                  type="button"
                >
                  Suivant
                </button>
              </div>
            )}
          </div>
        )}

        {/* Aucun résultat */}
        {!isLoading && !isError && !showElasticHint && textes.length === 0 && (
          <div className="mt-4 bg-white/5 border border-white/10 rounded-2xl p-8 backdrop-blur-sm text-center">
            <BookOpen size={48} className="mx-auto mb-3 text-slate-500" aria-hidden />
            <h3 className="text-lg font-bold text-slate-100">Aucun texte trouvé</h3>
            <p className="mt-2 text-sm text-slate-400">Il n&apos;y a aucun texte correspondant à vos critères de recherche actuels.</p>
            <button
              className="mt-5 px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-sm font-medium text-slate-300 hover:bg-white/10"
              onClick={() => { setSearchTerm(""); setDomaine(""); setTypeTexte(""); }}
              type="button"
            >
              Réinitialiser les filtres
            </button>
          </div>
        )}

      </section>
    </div>
  );
}