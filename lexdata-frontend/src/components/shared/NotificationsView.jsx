import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Bell, Settings, CheckCircle, Clock, Mail, Smartphone, MessageSquare, Loader2, Info, Trash2, Undo2, Filter } from "lucide-react";
import toast from "react-hot-toast";
import notificationService from "../../api/notificationService";
import { getApiErrorMessage } from "../../utils/getApiErrorMessage";

const PAGE_SIZE = 15;
const NOTIFICATION_TYPES = [
  { value: "", label: "Tous les types" },
  { value: "VEILLE", label: "Veille" },
  { value: "SYNTHESE", label: "Synthèse" },
  { value: "CALENDRIER", label: "Calendrier" },
  { value: "CONTRAT", label: "Contrat" },
  { value: "CONSULTATION", label: "Consultation" },
  { value: "PAIEMENT", label: "Paiement" },
  { value: "TRIBUNE", label: "Tribune" },
  { value: "SYSTEME", label: "Système" },
];

const DEFAULT_PREFS = {
  emailEnabled: true, smsEnabled: false, pushEnabled: true, inAppEnabled: true,
  digestFrequency: "IMMEDIATE", veilleAlerts: true, syntheseAlerts: true,
  calendrierAlerts: true, contratAlerts: true, consultationAlerts: true,
  paiementAlerts: true, tribuneAlerts: false,
};

function toIsoLocalDateTime(dtLocal) {
  if (!dtLocal) return undefined;
  return dtLocal.length === 16 ? `${dtLocal}:00` : dtLocal;
}

export default function NotificationsView() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState("list");
  const [page, setPage] = useState(0);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [typeFilter, setTypeFilter] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  const fromParam = toIsoLocalDateTime(from);
  const toParam = toIsoLocalDateTime(to);
  const typeParam = typeFilter || undefined;

  // 1. DATA FETCHING
  const { data: inboxPage, isLoading: loadingNotifs } = useQuery({
    queryKey: ["notifications", "inbox", page, PAGE_SIZE, unreadOnly, typeFilter, from, to],
    queryFn: () => notificationService.getNotificationsPage({ page, size: PAGE_SIZE, unreadOnly: unreadOnly ? true : undefined, type: typeParam, from: fromParam, to: toParam }),
  });

  const { data: unreadTotal = 0 } = useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: notificationService.getUnreadCount,
    staleTime: 15000,
  });

  const { data: preferences, isLoading: loadingPrefs } = useQuery({
    queryKey: ["preferences", "notifications"],
    queryFn: notificationService.getPreferences,
    retry: 1,
  });

  const notifications = inboxPage?.content ?? [];
  const totalPages = inboxPage?.totalPages ?? 0;
  const totalElements = inboxPage?.totalElements ?? 0;

  const [prefsForm, setPrefsForm] = useState({ ...DEFAULT_PREFS });

  useEffect(() => {
    if (preferences) setPrefsForm((prev) => ({ ...DEFAULT_PREFS, ...prev, ...preferences }));
  }, [preferences]);

  // 2. MUTATIONS
  const invalidateNotifications = () => queryClient.invalidateQueries({ queryKey: ["notifications"] });

  const markAsReadMutation = useMutation({ mutationFn: (id) => notificationService.markAsRead(id), onSuccess: invalidateNotifications });
  const markAsUnreadMutation = useMutation({ mutationFn: (id) => notificationService.markAsUnread(id), onSuccess: invalidateNotifications });

  const deleteMutation = useMutation({
    mutationFn: (id) => notificationService.deleteNotification(id),
    onSuccess: () => { invalidateNotifications(); toast.success("Notification supprimée."); },
    onError: (err) => { if (!err.response) toast.error(getApiErrorMessage(err, "Impossible de supprimer la notification.")); },
  });

  const savePreferencesMutation = useMutation({
    mutationFn: (newPrefs) => notificationService.updatePreferences(newPrefs),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["preferences", "notifications"] });
      toast.success("Préférences enregistrées.");
    },
    onError: (err) => { if (!err.response) toast.error(getApiErrorMessage(err, "Échec de l’enregistrement.")); },
  });

  // 3. HANDLERS
  const handleMarkAsRead = (id, e) => { e.stopPropagation(); markAsReadMutation.mutate(id); };
  const handleMarkAsUnread = (id, e) => { e.stopPropagation(); markAsUnreadMutation.mutate(id); };
  const handleDelete = (id, e) => { e.stopPropagation(); if (window.confirm("Supprimer cette notification ?")) deleteMutation.mutate(id); };

  const handlePrefChange = (e) => {
    const { name, checked, type, value } = e.target;
    setPrefsForm((prev) => ({ ...prev, [name]: type === "checkbox" ? checked : value }));
  };

  const handleSavePrefs = (e) => {
    e.preventDefault();
    savePreferencesMutation.mutate({
      emailEnabled: !!prefsForm.emailEnabled, smsEnabled: !!prefsForm.smsEnabled,
      pushEnabled: !!prefsForm.pushEnabled, inAppEnabled: !!prefsForm.inAppEnabled,
      digestFrequency: prefsForm.digestFrequency, veilleAlerts: !!prefsForm.veilleAlerts,
      syntheseAlerts: !!prefsForm.syntheseAlerts, calendrierAlerts: !!prefsForm.calendrierAlerts,
      contratAlerts: !!prefsForm.contratAlerts, consultationAlerts: !!prefsForm.consultationAlerts,
      paiementAlerts: !!prefsForm.paiementAlerts, tribuneAlerts: !!prefsForm.tribuneAlerts,
    });
  };

  return (
    <div className="max-w-4xl mx-auto fade-in">

      {/* TABS HEADER */}
      <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl flex gap-4 p-4 mb-6">
        <button
          className={[
            "flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-xl transition-all",
            activeTab === "list" ? "bg-amber-500 text-slate-950 font-bold" : "text-slate-400 hover:bg-white/5",
          ].join(" ")}
          onClick={() => setActiveTab("list")}
          type="button"
        >
          <Bell size={18} aria-hidden /> Mes Notifications
          {unreadTotal > 0 && (
            <span className="ml-2 inline-flex items-center justify-center bg-red-500 text-white text-xs font-extrabold min-w-[22px] h-[22px] px-1 rounded-full border-2 border-slate-950">
              {unreadTotal > 99 ? "99+" : unreadTotal}
            </span>
          )}
        </button>
        <button
          className={[
            "flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-xl transition-all",
            activeTab === "preferences" ? "bg-amber-500 text-slate-950 font-bold" : "text-slate-400 hover:bg-white/5",
          ].join(" ")}
          onClick={() => setActiveTab("preferences")}
          type="button"
        >
          <Settings size={18} aria-hidden /> Préférences
        </button>
      </div>

      {/* TAB 1: NOTIFICATIONS LIST */}
      {activeTab === "list" && (
        <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl overflow-hidden">

          {/* Filters */}
          <div className="p-5 border-b border-white/10 flex flex-wrap gap-5 items-end bg-black/10">
            <div className="flex items-center gap-2 text-slate-500 mr-2">
              <Filter size={16} aria-hidden /> <span className="text-sm">Filtres</span>
            </div>

            <label className="flex items-center gap-3 cursor-pointer p-2 rounded-lg hover:bg-white/5 transition-colors text-sm text-slate-300 mb-1">
              <input
                type="checkbox"
                className="w-5 h-5 accent-amber-500 cursor-pointer rounded border-white/20 bg-black/30 shrink-0"
                checked={unreadOnly}
                onChange={(e) => { setUnreadOnly(e.target.checked); setPage(0); }}
              />
              Non lues
            </label>

            <select
              className="bg-black/30 border border-white/10 rounded-lg p-2.5 text-sm text-slate-200 focus:border-amber-500 outline-none min-w-[160px]"
              value={typeFilter}
              onChange={(e) => { setTypeFilter(e.target.value); setPage(0); }}
            >
              {NOTIFICATION_TYPES.map((t) => <option key={t.value || "_all"} value={t.value}>{t.label}</option>)}
            </select>

            <div className="flex items-center gap-2">
              <input
                type="datetime-local"
                className="bg-black/30 border border-white/10 rounded-lg p-2.5 text-sm text-slate-200 focus:border-amber-500 outline-none"
                value={from}
                onChange={(e) => { setFrom(e.target.value); setPage(0); }}
              />
              <span className="text-slate-500">→</span>
              <input
                type="datetime-local"
                className="bg-black/30 border border-white/10 rounded-lg p-2.5 text-sm text-slate-200 focus:border-amber-500 outline-none"
                value={to}
                onChange={(e) => { setTo(e.target.value); setPage(0); }}
              />
            </div>
          </div>

          {/* List */}
          {loadingNotifs ? (
            <div className="p-10 text-center">
              <Loader2 size={32} className="animate-spin mx-auto text-amber-500" aria-hidden />
            </div>
          ) : notifications.length === 0 ? (
            <div className="p-12 text-center text-slate-400">
              <Bell size={48} className="opacity-30 mx-auto mb-4" aria-hidden />
              <p className="text-sm">Aucune notification pour ces critères.</p>
            </div>
          ) : (
            <div className="flex flex-col">
              {notifications.map((notif) => (
                <div
                  key={notif.id}
                  className={[
                    "p-6 border-b border-white/5 flex gap-5 items-start transition-all border-l-4 border-transparent hover:bg-white/5 group",
                    !notif.read ? "bg-blue-500/5 border-l-blue-500" : "",
                  ].join(" ")}
                >
                  <div
                    className={[
                      "p-3 rounded-full shrink-0 bg-white/5 text-slate-400",
                      !notif.read ? "bg-blue-500/10 text-blue-500" : "",
                    ].join(" ")}
                  >
                    <Info size={20} aria-hidden />
                  </div>

                  <div className="min-w-0 flex-1">
                    <h4 className={`text-lg text-slate-100 mb-2 ${notif.read ? "font-medium" : "font-bold"}`}>
                      {notif.link ? (
                        <a href={notif.link} target="_blank" rel="noopener noreferrer" className="hover:text-amber-400 transition-colors">
                          {notif.titre || "Notification"}
                        </a>
                      ) : (
                        notif.titre || "Notification"
                      )}
                    </h4>
                    <p className="text-slate-400 text-sm leading-relaxed mb-3">
                      {notif.message || notif.contenu}
                    </p>

                    <div className="flex flex-wrap gap-3 text-xs text-slate-500">
                      <span className="flex items-center gap-2">
                        <Clock size={14} aria-hidden />{" "}
                        {notif.dateCreation ? new Date(notif.dateCreation).toLocaleString("fr-FR") : "—"}
                      </span>
                      {notif.type && (
                        <span className="bg-white/5 border border-white/10 text-slate-300 px-2 py-0.5 rounded-full text-xs font-semibold">
                          {notif.type}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-1">
                    {!notif.read ? (
                      <button
                        className="p-2 rounded-md text-emerald-400 transition-colors duration-200 hover:bg-emerald-500/10 disabled:opacity-50"
                        onClick={(e) => handleMarkAsRead(notif.id, e)}
                        title="Marquer comme lue"
                        disabled={markAsReadMutation.isPending}
                        type="button"
                      >
                        <CheckCircle size={20} aria-hidden />
                      </button>
                    ) : (
                      <button
                        className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:bg-white/5 hover:text-slate-200 disabled:opacity-50"
                        onClick={(e) => handleMarkAsUnread(notif.id, e)}
                        title="Marquer comme non lue"
                        disabled={markAsUnreadMutation.isPending}
                        type="button"
                      >
                        <Undo2 size={20} aria-hidden />
                      </button>
                    )}
                    <button
                      className="p-2 rounded-md text-slate-500 transition-colors duration-200 hover:bg-red-500/10 hover:text-red-400 disabled:opacity-50"
                      onClick={(e) => handleDelete(notif.id, e)}
                      title="Supprimer"
                      disabled={deleteMutation.isPending}
                      type="button"
                    >
                      <Trash2 size={20} aria-hidden />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="p-4 border-t border-white/10 flex justify-between items-center text-sm text-slate-400 bg-black/10">
              <button
                className="px-4 py-2 rounded-lg text-sm font-medium text-slate-300 hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={page <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                type="button"
              >
                Précédent
              </button>
              <span>
                Page <strong>{page + 1}</strong> sur {Math.max(1, totalPages)} ({totalElements} total)
              </span>
              <button
                className="px-4 py-2 rounded-lg text-sm font-medium text-slate-300 hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((p) => p + 1)}
                type="button"
              >
                Suivant
              </button>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: PREFERENCES */}
      {activeTab === "preferences" && (
        <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6">
          <h3 className="text-xl text-slate-100 font-bold mb-4 border-b border-white/10 pb-3">Canaux et types d’alertes</h3>

          {loadingPrefs ? (
            <div className="p-10 text-center">
              <Loader2 className="animate-spin mx-auto text-amber-500" size={32} aria-hidden />
            </div>
          ) : (
            <form onSubmit={handleSavePrefs}>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
                {[
                  ["inAppEnabled", "Notifications in-app", Smartphone],
                  ["emailEnabled", "Notifications Email", Mail],
                  ["smsEnabled", "Alertes SMS (Premium)", MessageSquare],
                  ["pushEnabled", "Push Navigateur", Smartphone],
                ].map(([name, label, Icon]) => (
                  <label
                    key={name}
                    className="flex items-center gap-4 cursor-pointer bg-black/20 p-5 rounded-xl border border-white/10 transition-all hover:border-white/20 hover:bg-white/5"
                  >
                    <input
                      type="checkbox"
                      name={name}
                      className="w-5 h-5 accent-amber-500 cursor-pointer rounded border-white/20 bg-black/30 shrink-0"
                      checked={!!prefsForm[name]}
                      onChange={handlePrefChange}
                    />
                    <div className="flex items-center gap-2 font-medium text-slate-100">
                      <Icon size={18} className="text-amber-500" aria-hidden /> {label}
                    </div>
                  </label>
                ))}
              </div>

              <div className="mb-6 bg-black/20 p-4 rounded-lg border border-white/5">
                <label className="block text-sm text-slate-500 uppercase tracking-wider font-bold mb-2">Fréquence de regroupement (Digest)</label>
                <select
                  className="bg-black/30 border border-white/10 rounded-lg p-2.5 text-sm text-slate-200 focus:border-amber-500 outline-none max-w-xs w-full"
                  name="digestFrequency"
                  value={prefsForm.digestFrequency}
                  onChange={handlePrefChange}
                >
                  <option value="IMMEDIATE">Immédiat (Dès qu'un événement survient)</option>
                  <option value="DAILY">Quotidien (Un seul résumé par jour)</option>
                </select>
              </div>

              <h4 className="text-lg text-slate-100 font-bold mb-4">Alertes par domaine</h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 mb-8">
                {[
                  ["veilleAlerts", "Veille juridique"], ["syntheseAlerts", "Synthèses / fiches"],
                  ["calendrierAlerts", "Calendrier"], ["contratAlerts", "Contrats"],
                  ["consultationAlerts", "Consultations"], ["paiementAlerts", "Paiements"],
                  ["tribuneAlerts", "Tribune"],
                ].map(([name, label]) => (
                  <label
                    key={name}
                    className="flex items-center gap-3 cursor-pointer p-3 rounded-xl border border-white/10 bg-black/20 hover:bg-white/5 transition-colors text-sm text-slate-300"
                  >
                    <input
                      type="checkbox"
                      name={name}
                      className="w-5 h-5 accent-amber-500 cursor-pointer rounded border-white/20 bg-black/30 shrink-0"
                      checked={!!prefsForm[name]}
                      onChange={handlePrefChange}
                    />
                    {label}
                  </label>
                ))}
              </div>

              <div className="flex justify-end pt-4 border-t border-white/10">
                <button
                  type="submit"
                  className="flex items-center justify-center gap-2 bg-amber-500 text-slate-950 font-bold px-6 py-3 rounded-xl transition-all hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed w-fit"
                  disabled={savePreferencesMutation.isPending}
                >
                  {savePreferencesMutation.isPending && <Loader2 size={18} className="animate-spin" aria-hidden />}
                  {savePreferencesMutation.isPending ? "Enregistrement..." : "Sauvegarder les préférences"}
                </button>
              </div>
            </form>
          )}
        </div>
      )}
    </div>
  );
}