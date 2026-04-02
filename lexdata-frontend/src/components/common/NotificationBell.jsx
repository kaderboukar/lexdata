import { useState, useRef, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { BellRing, CheckCircle2, Info, AlertTriangle, Clock } from "lucide-react";
import notificationService from "../../api/notificationService";

export default function NotificationBell({ onViewAll }) {
  const queryClient = useQueryClient();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const { data: bellPage } = useQuery({
    queryKey: ["notifications", "bell"],
    queryFn: () => notificationService.getNotificationsPage({ page: 0, size: 10 }),
    refetchInterval: 60000,
  });

  const notifications = bellPage?.content ?? [];

  const { data: unreadCount = 0 } = useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: notificationService.getUnreadCount,
    refetchInterval: 60000,
    staleTime: 15000,
  });

  const markAsReadMutation = useMutation({
    mutationFn: (id) => notificationService.markAsRead(id),
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: ["notifications"] });
      const previousBell = queryClient.getQueryData(["notifications", "bell"]);
      queryClient.setQueryData(["notifications", "bell"], (old) => {
        if (!old?.content) return old;
        return { ...old, content: old.content.map((n) => n.id === id ? { ...n, read: true } : n) };
      });
      return { previousBell };
    },
    onError: (err, id, context) => {
      if (context?.previousBell != null) queryClient.setQueryData(["notifications", "bell"], context.previousBell);
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ["notifications"] }),
  });

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) setIsOpen(false);
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const getIcon = (type) => {
    switch (type) {
      case "VEILLE":
      case "TRIBUNE":
        return <AlertTriangle size={18} className="text-red-400" aria-hidden />;
      case "SYSTEME":
        return <Info size={18} className="text-amber-500" aria-hidden />;
      default:
        return <BellRing size={18} className="text-slate-400" aria-hidden />;
    }
  };

  return (
    <div className="relative" ref={dropdownRef} onClick={(e) => e.stopPropagation()}>
      <button
        type="button"
        className="relative flex items-center justify-center w-[42px] h-[42px] rounded-full bg-white/5 border border-white/10 text-slate-400 hover:bg-white/10 hover:text-slate-200 hover:scale-105 transition-all"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Notifications"
      >
        <BellRing size={20} aria-hidden />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-extrabold min-w-[20px] h-[20px] px-1 rounded-full flex items-center justify-center border-2 border-slate-950 shadow-sm">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute top-[calc(100%+12px)] right-0 w-[380px] rounded-xl bg-slate-900/95 backdrop-blur-xl border border-white/10 shadow-2xl z-50 overflow-hidden origin-top-right">
          <header className="p-4 border-b border-white/10 flex justify-between items-center bg-black/20">
            <h4 className="text-slate-100 m-0 font-semibold">Notifications</h4>
            {unreadCount > 0 && <span className="text-slate-400 text-sm">{unreadCount} non lue(s)</span>}
          </header>

          <div className="max-h-[400px] overflow-y-auto scrollbar-thin scrollbar-thumb-white/10">
            {notifications.length === 0 ? (
              <div className="text-center p-5 text-slate-400 text-sm">Aucune notification récente.</div>
            ) : (
              notifications.map((notif) => (
                <div
                  key={notif.id}
                  className={[
                    "p-4 border-b border-white/5 flex gap-4 transition-colors hover:bg-white/5",
                    !notif.read ? "bg-amber-500/5" : "",
                  ].join(" ")}
                >
                  <div className="mt-1">{getIcon(notif.type)}</div>
                  <div className="flex-1 min-w-0">
                    <div className={`text-sm mb-1 ${!notif.read ? "text-slate-100 font-semibold" : "text-slate-300/80"}`}>
                      {notif.link ? (
                        <a
                          href={notif.link}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="hover:text-amber-400 transition-colors"
                          onClick={(e) => e.stopPropagation()}
                        >
                          {notif.titre}
                        </a>
                      ) : (
                        notif.titre
                      )}
                    </div>
                    <div className="text-xs text-slate-400 mb-2 line-clamp-2">{notif.message}</div>
                    <div className="text-xs text-slate-500 flex items-center gap-2">
                      <Clock size={12} aria-hidden />{" "}
                      {notif.dateCreation ? new Date(notif.dateCreation).toLocaleString("fr-FR") : "—"}
                    </div>
                  </div>
                  {!notif.read && (
                    <button
                      type="button"
                      className="p-2 rounded-md text-amber-500 transition-colors hover:bg-amber-500/10 disabled:opacity-50"
                      onClick={() => markAsReadMutation.mutate(notif.id)}
                      title="Marquer comme lue"
                      disabled={markAsReadMutation.isPending}
                    >
                      <CheckCircle2 size={18} aria-hidden />
                    </button>
                  )}
                </div>
              ))
            )}
          </div>

          <footer className="p-3 text-center border-t border-white/10 bg-black/20">
            {onViewAll ? (
              <button
                type="button"
                className="text-sm text-slate-400 hover:text-slate-100 transition-colors underline"
                onClick={() => {
                  setIsOpen(false);
                  onViewAll();
                }}
              >
                Voir toutes les notifications
              </button>
            ) : (
              <span className="text-slate-400 text-sm">Notifications récentes</span>
            )}
          </footer>
        </div>
      )}
    </div>
  );
}