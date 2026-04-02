import { useState, useEffect, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  Scale, Users, Database, Settings, BookOpen, LogOut, Menu, X,
  ShieldAlert, CheckCircle2, LayoutDashboard, BellRing, BadgeCheck, Megaphone
} from "lucide-react";
import useAuthStore from "../../store/useAuthStore";

// Import de tous nos modules d'administration
import TextesManager from "./TextesManager";
import UsersManager from "./UsersManager";
import SynthesesManager from "./SynthesesManager";
import VeilleManager from "./VeilleManager";
import NotificationsView from "../../components/shared/NotificationsView";
import RoleChangeRequestsManager from "./RoleChangeRequestsManager";
import ProfilesManager from "./ProfilesManager";
import AdminBroadcastPanel from "./AdminBroadcastPanel";

export default function AdminDashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useAuthStore((state) => state.logout);

  const [activeTab, setActiveTab] = useState("overview");
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  /** Préremplissage du formulaire veille depuis Textes juridiques (location.state.openVeilleForm) */
  const [veillePrefill, setVeillePrefill] = useState(null);
  const clearVeillePrefill = useCallback(() => setVeillePrefill(null), []);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const switchTab = (tabId) => {
    setActiveTab(tabId);
    setMobileMenuOpen(false);
  };

  // Fermer le menu mobile si on change de route/onglet
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname, activeTab]);

  // Ouvrir l'onglet Veille avec brouillon si on arrive depuis Textes juridiques
  useEffect(() => {
    const draft = location.state?.openVeilleForm;
    if (!draft?.texteJuridiqueId) return;
    setActiveTab("veille");
    setVeillePrefill(draft);
    navigate("/admin", { replace: true, state: {} });
  }, [location.state, navigate]);

  // Bloquer le scroll du body quand le menu mobile est ouvert
  useEffect(() => {
    document.body.style.overflow = mobileMenuOpen ? 'hidden' : 'unset';
    return () => { document.body.style.overflow = 'unset'; };
  }, [mobileMenuOpen]);

  const menuItems = [
    { id: "overview", label: "Vue d'ensemble", icon: LayoutDashboard },
    { id: "textes", label: "Textes Juridiques", icon: BookOpen },
    { id: "veille", label: "Veille & Alertes", icon: BellRing },
    { id: "syntheses", label: "Fiches Pratiques", icon: Database },
    { id: "users", label: "Utilisateurs", icon: Users },
    { id: "profiles", label: "Profils (KYC)", icon: BadgeCheck },
    { id: "roleRequests", label: "Demandes de rôle", icon: ShieldAlert },
    { id: "broadcast", label: "Campagne de diffusion", icon: Megaphone },
    { id: "notifications", label: "Notifications Système", icon: BellRing },
    { id: "settings", label: "Paramètres", icon: Settings },
  ];

  return (
    <div className="flex h-screen w-full overflow-hidden bg-slate-950 text-slate-200">

      {/* BOUTON FLOTTANT MOBILE */}
      <button
        className="fixed bottom-5 right-5 z-50 p-3 rounded-full bg-red-500 text-white shadow-lg lg:hidden"
        onClick={() => setMobileMenuOpen(true)}
        type="button"
        aria-label="Ouvrir le menu administrateur"
      >
        <Menu size={24} aria-hidden />
      </button>

      {/* OVERLAY MOBILE */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* ========================================================= */}
      {/* BARRE LATÉRALE (SIDEBAR)                                  */}
      {/* ========================================================= */}
      <aside
        className={[
          "w-72 flex-shrink-0 flex flex-col bg-slate-900 border-r border-white/10 z-50",
          "fixed inset-y-0 left-0 transition-transform duration-300",
          mobileMenuOpen ? "translate-x-0" : "-translate-x-full",
          "lg:relative lg:translate-x-0",
        ].join(" ")}
      >

        <header className="h-20 shrink-0 flex items-center justify-between px-6 border-b border-white/10">
          <div className="flex items-center gap-3 min-w-0">
            <Scale size={28} className="text-red-500 shrink-0" aria-hidden />
            <span className="text-slate-100 font-semibold truncate">
              Lex<strong>Data</strong>
            </span>
            <span className="bg-red-500/20 text-red-500 border border-red-500/30 rounded-full px-2.5 py-1 text-xs font-bold shrink-0">
              ADMIN
            </span>
          </div>
          <button
            className="lg:hidden p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 transition-colors"
            onClick={() => setMobileMenuOpen(false)}
            type="button"
            aria-label="Fermer le menu"
          >
            <X size={24} aria-hidden />
          </button>
        </header>

        <nav className="flex-1 overflow-y-auto px-4 pb-6 flex flex-col gap-1 mt-4">
          <div className="px-3 pb-2 text-xs font-bold uppercase tracking-wider text-slate-500">
            Gestion Plateforme
          </div>

          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;

            return (
              <button
                key={item.id}
                onClick={() => switchTab(item.id)}
                className={
                  isActive
                    ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-bold text-red-500 bg-red-500/10 transition-all duration-200 text-left border-l-4 border-red-500 rounded-l-none"
                    : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
                }
                type="button"
              >
                <Icon size={18} aria-hidden />
                {item.label}
              </button>
            );
          })}
        </nav>

        <footer className="p-4 border-t border-white/10">
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-slate-200 hover:bg-white/10 transition-colors"
            type="button"
          >
            <LogOut size={18} aria-hidden /> Déconnexion
          </button>
        </footer>
      </aside>

      {/* ========================================================= */}
      {/* ZONE DE CONTENU PRINCIPALE (MAIN)                           */}
      {/* ========================================================= */}
      <main className="flex-1 flex flex-col min-w-0 h-screen bg-slate-950">

        <header className="h-20 shrink-0 sticky top-0 z-10 px-8 flex justify-between items-center border-b border-white/10 bg-slate-950/80 backdrop-blur-md">
          <div className="min-w-0">
            <h2 className="text-xl md:text-2xl font-bold text-slate-100 truncate">
              {menuItems.find((item) => item.id === activeTab)?.label || "Administration"}
            </h2>
            <p className="text-sm text-slate-400 truncate">
              Supervision et gestion de la plateforme LexData.
            </p>
          </div>

          <span className="hidden md:flex items-center gap-2 bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 px-4 py-2 rounded-full text-sm font-semibold">
            <CheckCircle2 size={16} aria-hidden /> Système Opérationnel
          </span>
        </header>

        <div className="flex-1 overflow-y-auto p-6 md:p-8">

          {/* ONGLET : VUE D'ENSEMBLE */}
          {activeTab === "overview" && (
            <div className="fade-in">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">

                <div
                  className="bg-white/5 border border-white/10 rounded-xl p-6 flex items-center gap-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20"
                  onClick={() => switchTab("textes")}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") switchTab("textes");
                  }}
                >
                  <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0 bg-blue-500/10 text-blue-500">
                    <Database size={28} aria-hidden />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-2xl font-extrabold text-slate-100">12,450</h3>
                    <p className="text-slate-400 text-sm">Textes en base</p>
                  </div>
                </div>

                <div
                  className="bg-white/5 border border-white/10 rounded-xl p-6 flex items-center gap-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20"
                  onClick={() => switchTab("syntheses")}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") switchTab("syntheses");
                  }}
                >
                  <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0 bg-emerald-500/10 text-emerald-500">
                    <BookOpen size={28} aria-hidden />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-2xl font-extrabold text-slate-100">3,890</h3>
                    <p className="text-slate-400 text-sm">Fiches Publiées</p>
                  </div>
                </div>

                <div
                  className="bg-white/5 border border-white/10 rounded-xl p-6 flex items-center gap-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20"
                  onClick={() => switchTab("users")}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") switchTab("users");
                  }}
                >
                  <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0 bg-amber-500/10 text-amber-500">
                    <Users size={28} aria-hidden />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-2xl font-extrabold text-slate-100">342</h3>
                    <p className="text-slate-400 text-sm">Abonnés Pro</p>
                  </div>
                </div>
              </div>

              <div className="bg-white/5 border border-white/10 rounded-xl p-6">
                <h3 className="text-lg font-bold text-slate-100 border-b border-white/10 pb-3">
                  Activité récente de l'équipe
                </h3>
                <div className="mt-4">
                  <p className="text-slate-400 m-0">
                    Le flux d'activité des agents s'affichera ici prochainement.
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* CHARGEMENT DES SOUS-COMPOSANTS SELON L'ONGLET */}
          {activeTab === "textes" && <div className="fade-in"><TextesManager /></div>}
          {activeTab === "syntheses" && <div className="fade-in"><SynthesesManager /></div>}
          {activeTab === "veille" && (
            <div className="fade-in">
              <VeilleManager prefillFromTexte={veillePrefill} onPrefillConsumed={clearVeillePrefill} />
            </div>
          )}
          {activeTab === "users" && <div className="fade-in"><UsersManager /></div>}
          {activeTab === "profiles" && <div className="fade-in"><ProfilesManager /></div>}
          {activeTab === "roleRequests" && <div className="fade-in"><RoleChangeRequestsManager /></div>}
          {activeTab === "broadcast" && <div className="fade-in"><AdminBroadcastPanel /></div>}
          {activeTab === "notifications" && <div className="fade-in"><NotificationsView /></div>}

          {activeTab === "settings" && (
            <div className="fade-in bg-white/5 border border-white/10 rounded-xl p-10 text-center max-w-2xl mx-auto mt-10">
              <Settings size={64} className="text-slate-500 mx-auto mb-6 opacity-60" aria-hidden />
              <h3 className="text-2xl font-bold text-slate-100 mb-2">Paramètres de la plateforme</h3>
              <p className="text-slate-400">
                Configuration globale de LexData (Maintenance, logs, synchronisation ElasticSearch, etc.).
                <br /> Module en cours de développement.
              </p>
            </div>
          )}

        </div>
      </main>
    </div>
  );
}