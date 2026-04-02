import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import toast from "react-hot-toast";
import {
  BellRing, Sparkles, Clock, CheckCircle2, ArrowRight, Filter,
  Settings, FileText, AlertTriangle, Info, Loader2, Power, Plus, Trash2, ListChecks,
} from "lucide-react";
import veilleService from "../../api/veilleService";
import { JURIDIQUE_LEGAL_DOMAINS, JURIDIQUE_TYPE_TEXTES } from "../../constants/juridiqueEnums";

const URGENCE_CONFIG = {
  BASSE: { color: "#10b981", bg: "rgba(16, 185, 129, 0.1)", icon: Info, label: "Info" },
  MOYENNE: { color: "#f59e0b", bg: "rgba(245, 158, 11, 0.1)", icon: BellRing, label: "Important" },
  ELEVEE: { color: "#ef4444", bg: "rgba(239, 68, 68, 0.1)", icon: AlertTriangle, label: "Urgent" },
};

function normalizeDomaines(domaines) {
  if (!domaines) return [];
  return Array.isArray(domaines) ? domaines : [...domaines];
}

function urgencyKeyFromAlert(a) {
  if (a.eventType === "UPDATE") return "MOYENNE";
  return "BASSE";
}

export default function Veille() {
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState("ALL");
  const [showSubForm, setShowSubForm] = useState(false);
  const [subFormDomains, setSubFormDomains] = useState(() => new Set());
  const [subFormTypes, setSubFormTypes] = useState(() => new Set());

  const alertsQueryKey = ["veille-my-alerts", filter];

  // GET ALERTS
  const { data: alertsPage, isLoading, isError, error } = useQuery({
    queryKey: alertsQueryKey,
    queryFn: () => veilleService.getMyAlerts({
      unreadOnly: filter === "UNREAD" ? true : undefined,
      page: 0,
      size: 20,
    }),
    staleTime: 60000,
    refetchInterval: 120000, // Rafraîchissement automatique toutes les 2 min
  });

  const alertes = alertsPage?.content ?? [];

  // GET SUBSCRIPTIONS
  const { data: subs = [], isLoading: subsLoading } = useQuery({
    queryKey: ["veille-subscriptions-me"],
    queryFn: () => veilleService.getMySubscriptions(),
    staleTime: 60000,
  });

  // MUTATIONS
  const markAsReadMutation = useMutation({
    mutationFn: (userAlertId) => veilleService.markAlertRead(userAlertId, true),
    onMutate: async (userAlertId) => {
      await queryClient.cancelQueries({ queryKey: alertsQueryKey });
      const previous = queryClient.getQueryData(alertsQueryKey);
      queryClient.setQueryData(alertsQueryKey, (old) => {
        if (!old?.content) return old;
        return { ...old, content: old.content.map((a) => a.id === userAlertId ? { ...a, isRead: true } : a) };
      });
      return { previous };
    },
    onError: (_err, _id, context) => {
      if (context?.previous) queryClient.setQueryData(alertsQueryKey, context.previous);
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ["veille-my-alerts"] }),
  });

  const dismissMutation = useMutation({
    mutationFn: (userAlertId) => veilleService.deleteMyAlert(userAlertId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["veille-my-alerts"] }),
  });

  const toggleSubMutation = useMutation({
    mutationFn: ({ id, active }) => veilleService.toggleSubscriptionActive(id, active),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["veille-subscriptions-me"] }),
  });

  const createSubMutation = useMutation({
    mutationFn: (body) => veilleService.createSubscription(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["veille-subscriptions-me"] });
      setShowSubForm(false);
      setSubFormDomains(new Set());
      setSubFormTypes(new Set());
      toast.success("Alerte de veille créée : vous serez notifié selon ces critères.");
    },
    onError: (err) => {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err?.message ||
        "Impossible d'enregistrer l'abonnement.";
      toast.error(typeof msg === "string" ? msg : "Erreur lors de la création.");
    },
  });

  // HANDLERS
  const handleMarkAsRead = (userAlertId, e) => {
    e.preventDefault();
    markAsReadMutation.mutate(userAlertId);
  };

  const handleDismiss = (userAlertId) => {
    if (window.confirm("Retirer cette alerte de votre liste ?")) {
      dismissMutation.mutate(userAlertId);
    }
  };

  const submitNewSubscription = () => {
    if (subFormDomains.size === 0 || subFormTypes.size === 0) {
      window.alert("Choisissez au moins un domaine et un type de texte.");
      return;
    }
    createSubMutation.mutate({
      domaines: [...subFormDomains],
      textTypes: [...subFormTypes],
      active: true,
    });
  };

  const allTrackedDomains = [...new Set(subs.flatMap((s) => (s.active ? normalizeDomaines(s.domaines) : [])))];

  return (
    <div className="veille-page container mx-auto px-4 py-8 pb-16 flex flex-col lg:flex-row gap-8 items-start fade-in">

      {/* ========================================== */}
      {/* COLONNE GAUCHE : FEED D'ALERTES              */}
      {/* ========================================== */}
      <main className="feed-container w-full lg:w-[65%] min-w-0 flex flex-col gap-6">

        <header className="feed-header flex flex-wrap justify-between items-center gap-4 border-b border-white/10 pb-6 mb-2">
          <div className="feed-tabs flex flex-wrap items-center gap-2">
            <button
              className={
                filter === "ALL"
                  ? "bg-amber-500 text-slate-950 font-bold px-4 py-2 rounded-lg"
                  : "text-slate-400 hover:bg-white/5 hover:text-slate-200 px-4 py-2 rounded-lg font-medium transition-colors"
              }
              onClick={() => setFilter("ALL")}
              type="button"
            >
              Toutes les alertes
            </button>
            <button
              className={
                filter === "UNREAD"
                  ? "bg-amber-500 text-slate-950 font-bold px-4 py-2 rounded-lg"
                  : "text-slate-400 hover:bg-white/5 hover:text-slate-200 px-4 py-2 rounded-lg font-medium transition-colors"
              }
              onClick={() => setFilter("UNREAD")}
              type="button"
            >
              <BellRing size={16} className="inline-block mr-2 -mt-[2px]" aria-hidden /> Non lues
            </button>
          </div>
        </header>

        <aside
          className="rounded-2xl border border-amber-500/25 bg-amber-500/[0.07] px-4 py-3 text-sm text-slate-200 flex flex-col sm:flex-row sm:items-start gap-3 mb-2"
          aria-label="Comment créer votre veille"
        >
          <ListChecks className="shrink-0 text-amber-400 mt-0.5" size={22} aria-hidden />
          <div className="space-y-1 min-w-0">
            <p className="font-semibold text-slate-100 m-0">Créer votre veille en 2 étapes</p>
            <ol className="list-decimal list-inside text-slate-300/90 space-y-1 m-0 pl-0">
              <li>
                À droite, cliquez sur <strong className="text-slate-100">Créer une alerte</strong>, puis choisissez les{" "}
                <strong className="text-slate-100">domaines</strong> et <strong className="text-slate-100">types de texte</strong> à suivre.
              </li>
              <li>
                Les nouveautés publiées par l&apos;équipe apparaissent ici ; vous pouvez aussi recevoir une notification (selon vos préférences profil).
              </li>
            </ol>
          </div>
        </aside>

        {/* LOADING STATE */}
        {isLoading && (
          <div>
            {[1, 2, 3].map((i) => (
              <div key={i} className="animate-pulse bg-white/5 border border-white/10 rounded-xl p-6 mb-4">
                <div className="h-5 w-32 bg-white/10 rounded mb-4" />
                <div className="h-6 w-3/4 bg-white/10 rounded mb-3" />
                <div className="h-4 w-full bg-white/5 rounded mb-2" />
                <div className="h-4 w-5/6 bg-white/5 rounded" />
              </div>
            ))}
          </div>
        )}

        {/* ERROR STATE */}
        {isError && (
          <div className="bg-red-500/10 border border-red-500/20 rounded-2xl p-5 backdrop-blur-sm">
            <div className="flex items-start gap-3 text-red-200">
              <AlertTriangle size={20} className="mt-0.5 shrink-0" aria-hidden />
              <div className="text-sm leading-relaxed">
                {error?.response?.data?.message || error?.message || "Impossible de charger vos alertes."}
              </div>
            </div>
          </div>
        )}

        {/* EMPTY STATE */}
        {!isLoading && !isError && alertes.length === 0 && (
          <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-12 text-center flex flex-col items-center justify-center">
            <CheckCircle2 size={48} className="text-emerald-400/70 mb-4" aria-hidden />
            <h3 className="text-xl font-bold text-slate-200 mb-2">Vous êtes à jour</h3>
            <p className="text-slate-400 max-w-md">
              Aucune alerte pour ce filtre. Ajustez vos abonnements dans le panneau de droite.
            </p>
          </div>
        )}

        {/* FEED LIST */}
        <div className="feed-list flex flex-col gap-4">
          {alertes.map((a) => {
            const uk = urgencyKeyFromAlert(a);
            const UrgenceIcon = URGENCE_CONFIG[uk]?.icon || Info;
            const urgenceColor = URGENCE_CONFIG[uk]?.color;
            const urgenceBg = URGENCE_CONFIG[uk]?.bg;
            const domains = normalizeDomaines(a.domaines);
            const dateLabel = a.alertDate || a.createdAt ? new Date(a.alertDate || a.createdAt).toLocaleString() : "Récemment";

            return (
              <article
                key={a.id}
                className={[
                  "feed-card bg-white/5 backdrop-blur-sm border border-white/10 border-l-4 rounded-xl p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl",
                  a.isRead ? "opacity-70 grayscale-[20%]" : "",
                ].join(" ")}
                style={{ borderLeftColor: !a.isRead ? urgenceColor : undefined }} // Seul style inline conservé (dynamique)
              >
                <div className="flex items-start justify-between gap-4">

                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span
                        className="px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1.5"
                        style={{ background: urgenceBg, color: urgenceColor }}
                      >
                        <UrgenceIcon size={12} aria-hidden /> {URGENCE_CONFIG[uk]?.label}
                      </span>
                      {a.eventType && (
                        <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                          {a.eventType}
                        </span>
                      )}
                      <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                        {domains.length > 0 ? domains[0] : "GÉNÉRAL"}
                      </span>
                      <span className="text-slate-500 text-sm flex items-center gap-2 ml-2">
                        <Clock size={12} aria-hidden /> {dateLabel}
                      </span>
                    </div>
                  </div>

                  <div className="flex gap-1">
                    {!a.isRead && (
                      <button
                        className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:text-emerald-400 hover:bg-emerald-500/10 disabled:opacity-50"
                        onClick={(e) => handleMarkAsRead(a.id, e)}
                        title="Marquer comme lu"
                        disabled={markAsReadMutation.isPending}
                        type="button"
                      >
                        <CheckCircle2 size={18} aria-hidden />
                      </button>
                    )}
                    <button
                      className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:text-red-400 hover:bg-red-500/10 disabled:opacity-50"
                      onClick={() => handleDismiss(a.id)}
                      title="Masquer"
                      disabled={dismissMutation.isPending}
                      type="button"
                    >
                      <Trash2 size={18} aria-hidden />
                    </button>
                  </div>
                </div>

                <h3 className="mt-4 text-lg font-bold text-slate-100 leading-tight">
                  {a.title}
                </h3>

                {a.summary && (
                  <div className="ai-summary bg-blue-500/5 border border-blue-500/20 p-4 rounded-xl my-4">
                    <div className="text-blue-400 font-bold text-xs uppercase tracking-wider mb-2 flex items-center gap-2">
                      <Sparkles size={14} aria-hidden /> L’essentiel
                    </div>
                    <p className="text-sm text-slate-300/80 leading-relaxed whitespace-pre-wrap">
                      {a.summary}
                    </p>
                  </div>
                )}

                {a.legalTextId && (
                  <Link to={`/textes/${a.legalTextId}`} className="feed-card-footer inline-flex items-center gap-2 text-amber-500 font-semibold hover:text-amber-400 transition-colors mt-2 text-sm">
                    <FileText size={16} aria-hidden /> Consulter le texte officiel <ArrowRight size={16} aria-hidden />
                  </Link>
                )}
              </article>
            );
          })}
        </div>
      </main>

      {/* ========================================== */}
      {/* COLONNE DROITE : ABONNEMENTS                 */}
      {/* ========================================== */}
      <aside className="feed-sidebar w-full lg:w-[35%] lg:sticky lg:top-28 flex flex-col gap-6">
        <div className="subscription-panel bg-slate-900/60 backdrop-blur-md border border-white/10 rounded-2xl p-6">

          <header className="flex items-center gap-3 border-b border-white/10 pb-4 mb-6 text-lg font-bold text-slate-100">
            <Filter size={20} className="text-amber-500" aria-hidden />
            <h3 className="m-0">Abonnements veille</h3>
          </header>

          {subsLoading ? (
            <div className="flex justify-center p-4">
              <Loader2 className="animate-spin text-amber-500" size={24} aria-hidden />
            </div>
          ) : subs.length === 0 ? (
            <p className="text-slate-400 text-sm mb-4">
              Aucune souscription active. Créez-en une pour recevoir des alertes ciblées.
            </p>
          ) : (
            <ul className="subscription-list">
              {subs.map((s) => (
                <li key={s.id} className="subscription-item bg-black/20 border border-white/5 rounded-xl p-4 mb-4 transition-colors hover:border-white/10">
                  <div className="flex items-center justify-between gap-3 mb-3">
                    <span className="text-slate-400 text-sm font-medium">Flux #{s.id}</span>
                    <button
                      className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:bg-white/5 hover:text-slate-200 disabled:opacity-50"
                      title={s.active ? "Mettre en pause" : "Réactiver"}
                      onClick={() => toggleSubMutation.mutate({ id: s.id, active: !s.active })}
                      disabled={toggleSubMutation.isPending}
                      type="button"
                    >
                      <Power size={16} className={s.active ? "text-emerald-400" : "text-slate-500"} aria-hidden />
                    </button>
                  </div>

                  <div className="flex flex-wrap gap-2 mb-2">
                    {normalizeDomaines(s.domaines).map((d) => (
                      <span key={d} className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                        {d}
                      </span>
                    ))}
                  </div>

                  <div className="flex flex-wrap gap-2">
                    {(s.textTypes ? [...s.textTypes] : []).map((t) => (
                      <span key={t} className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
                        {t}
                      </span>
                    ))}
                  </div>
                </li>
              ))}
            </ul>
          )}

          {/* Formulaire de création */}
          {!showSubForm ? (
            <button
              className="w-full inline-flex items-center justify-center gap-2 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10"
              onClick={() => setShowSubForm(true)}
              type="button"
            >
              <Plus size={16} aria-hidden /> Créer une alerte
            </button>
          ) : (
            <div className="sub-form-container bg-black/30 border border-white/10 rounded-xl p-5 mt-4">
              <p className="text-slate-100 font-semibold mb-2 text-sm">Configurer l'alerte :</p>

              <div className="sub-form-section max-h-[140px] overflow-y-auto bg-white/5 rounded-lg p-2 mb-4 flex flex-col gap-1">
                {JURIDIQUE_LEGAL_DOMAINS.map((d) => (
                  <label key={d.value} className="sub-checkbox-label flex items-center gap-3 cursor-pointer p-2 rounded-md hover:bg-white/5 transition-colors text-sm text-slate-300">
                    <input
                      type="checkbox"
                      className="accent-amber-500 w-4 h-4 cursor-pointer rounded"
                      checked={subFormDomains.has(d.value)}
                      onChange={() => {
                        setSubFormDomains((prev) => {
                          const n = new Set(prev);
                          n.has(d.value) ? n.delete(d.value) : n.add(d.value);
                          return n;
                        });
                      }}
                    />
                    {d.label}
                  </label>
                ))}
              </div>

              <div className="sub-form-section max-h-[140px] overflow-y-auto bg-white/5 rounded-lg p-2 mb-4 flex flex-col gap-1">
                {JURIDIQUE_TYPE_TEXTES.map((t) => (
                  <label key={t.value} className="sub-checkbox-label flex items-center gap-3 cursor-pointer p-2 rounded-md hover:bg-white/5 transition-colors text-sm text-slate-300">
                    <input
                      type="checkbox"
                      className="accent-amber-500 w-4 h-4 cursor-pointer rounded"
                      checked={subFormTypes.has(t.value)}
                      onChange={() => {
                        setSubFormTypes((prev) => {
                          const n = new Set(prev);
                          n.has(t.value) ? n.delete(t.value) : n.add(t.value);
                          return n;
                        });
                      }}
                    />
                    {t.label}
                  </label>
                ))}
              </div>

              <div className="flex gap-2 mt-3">
                <button
                  className="flex-1 inline-flex items-center justify-center rounded-lg bg-amber-500 px-4 py-2.5 text-sm font-extrabold text-slate-950 transition-all duration-300 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed"
                  onClick={submitNewSubscription}
                  disabled={createSubMutation.isPending}
                  type="button"
                >
                  {createSubMutation.isPending ? "Création..." : "Enregistrer"}
                </button>
                <button
                  className="inline-flex items-center justify-center rounded-lg border border-white/10 bg-white/5 px-4 py-2.5 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10"
                  onClick={() => { setShowSubForm(false); setSubFormDomains(new Set()); setSubFormTypes(new Set()); }}
                  type="button"
                >
                  Annuler
                </button>
              </div>
            </div>
          )}

          <Link to="/dashboard" className="mt-4 w-full inline-flex items-center justify-center gap-2 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10">
            <Settings size={16} aria-hidden /> Gérer mon profil
          </Link>

        </div>
      </aside>
    </div>
  );
}