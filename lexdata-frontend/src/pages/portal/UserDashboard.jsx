import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  Scale, BookOpen, PenTool, User as UserIcon, LogOut, Shield,
  Library, Globe, Users, Menu, X, BellRing, Bookmark,
} from "lucide-react";

import Recherche from "./Recherche";
import Veille from "./Veille";
import MesNotes from "./MesNotes";
import MesFavoris from "./MesFavoris";
import UserProfile from "./UserProfile";
import RoleChangeRequest from "./RoleChangeRequest";
import useAuthStore from "../../store/useAuthStore";
import favoriteService from "../../api/favoriteService";
import NotificationBell from "../../components/common/NotificationBell";
import NotificationsView from "../../components/shared/NotificationsView";

// ============================================================================
// COMPOSANT : Premium Lock
// ============================================================================
const PremiumLock = ({ title, description }) => {
  const navigate = useNavigate();
  return (
    <div className="mx-auto my-8 w-full max-w-2xl">
      <div className="rounded-2xl border border-white/10 bg-white/5 p-8 text-center shadow-2xl backdrop-blur-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl sm:p-10">
        <div className="mx-auto mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-amber-500/10 shadow-[0_0_24px_rgba(245,158,11,0.18)]">
          <Shield size={40} className="text-amber-400" aria-hidden />
        </div>
        <h3 className="mb-3 text-2xl font-bold text-slate-100">{title}</h3>
        <p className="mx-auto mb-6 max-w-lg text-sm leading-relaxed text-slate-300/80">{description}</p>
        <button
          onClick={() => navigate("/tarifs")}
          className="inline-flex items-center justify-center rounded-xl bg-amber-500 px-5 py-3 text-sm font-bold text-black shadow-lg shadow-amber-500/20 transition-all duration-300 hover:brightness-110 hover:shadow-amber-500/30 focus:outline-none focus:ring-2 focus:ring-amber-500/30"
          type="button"
        >
          Découvrir l&apos;offre Premium
        </button>
      </div>
    </div>
  );
};

// ============================================================================
// COMPOSANT PRINCIPAL : DASHBOARD
// ============================================================================
export default function UserDashboard() {
  const navigate = useNavigate();

  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const isPremium = useAuthStore((state) => state.isPremium());

  const [activeTab, setActiveTab] = useState("overview");
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const { data: favoritesList = [] } = useQuery({
    queryKey: ["my-favorites"],
    queryFn: () => favoriteService.getMyFavorites(),
    staleTime: 60000,
  });

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const switchTab = (tabId) => {
    setActiveTab(tabId);
    setMobileMenuOpen(false);
  };

  return (
    <div className="flex h-screen w-full bg-slate-950 overflow-hidden text-slate-200">

      {/* BOUTON FLOTTANT MOBILE */}
      <button
        className="fixed bottom-5 right-5 z-50 rounded-full bg-amber-500 p-3 text-black shadow-lg shadow-amber-500/25 transition-all duration-200 hover:brightness-110 active:scale-95 lg:hidden"
        onClick={() => setMobileMenuOpen(true)}
        type="button"
        aria-label="Ouvrir le menu"
      >
        <Menu size={24} aria-hidden />
      </button>

      {/* OVERLAY MOBILE */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={() => setMobileMenuOpen(false)}
          aria-hidden
        />
      )}

      {/* ========================================================= */}
      {/* BARRE LATÉRALE (SIDEBAR)                                  */}
      {/* ========================================================= */}
      <aside
        className={[
          "w-[280px] flex-shrink-0 flex flex-col bg-[#0f1827] border-r border-white/10 z-50",
          "fixed inset-y-0 left-0 transition-transform duration-300",
          "lg:relative lg:translate-x-0",
          mobileMenuOpen ? "translate-x-0" : "-translate-x-full",
        ].join(" ")}
        aria-label="Navigation latérale"
      >

        <header className="h-[80px] shrink-0 flex items-center justify-between gap-3 px-6 border-b border-white/10">
          <div className="flex items-center gap-3 text-xl font-semibold text-slate-100">
            <Scale size={28} className="text-amber-400" aria-hidden />
            <span>
              Lex<strong className="font-extrabold">Data</strong>
            </span>
          </div>
          <button
            className="rounded-lg p-2 text-slate-400 transition-all duration-200 hover:bg-white/5 hover:text-slate-200 lg:hidden"
            onClick={() => setMobileMenuOpen(false)}
            type="button"
            aria-label="Fermer le menu"
          >
            <X size={22} aria-hidden />
          </button>
        </header>

        {/* Profil Rapide */}
        <div className="mx-4 my-6 p-4 flex items-center gap-4 bg-black/20 border border-white/10 rounded-xl">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-amber-400 to-amber-600 text-sm font-extrabold uppercase text-black shadow-[0_10px_22px_rgba(245,158,11,0.18)]">
            {user?.firstName?.charAt(0) || user?.username?.charAt(0) || "U"}
          </div>
          <div className="min-w-0">
            <span
              className="block truncate text-sm font-semibold text-slate-100"
              title={`${user?.firstName} ${user?.lastName}`}
            >
              {user?.firstName} {user?.lastName}
            </span>
            <span
              className={
                "mt-1 inline-flex w-fit items-center rounded-full border px-2 py-0.5 text-[11px] font-semibold " +
                (isPremium
                  ? "border-amber-500/30 bg-amber-500/10 text-amber-300"
                  : "border-white/10 bg-white/5 text-slate-300/70")
              }
            >
              {isPremium ? "Membre Premium" : "Compte Standard"}
            </span>
          </div>
        </div>

        <nav className="flex-1 overflow-y-auto px-4 pb-6 flex flex-col gap-1">

          <div className="text-xs font-bold uppercase tracking-wider text-slate-500 px-3 mt-4 mb-2">
            Mon Espace
          </div>
          <button
            className={
              activeTab === "overview"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("overview")}
            type="button"
          >
            <BookOpen size={18} /> Vue d'ensemble
          </button>
          <button
            className={
              activeTab === "notifications"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("notifications")}
            type="button"
          >
            <BellRing size={18} /> Mes Alertes
          </button>
          <button
            className={
              activeTab === "annotations"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("annotations")}
            type="button"
          >
            <PenTool size={18} /> Mes Annotations
          </button>
          <button
            className={
              activeTab === "favorites"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("favorites")}
            type="button"
          >
            <Bookmark size={18} /> Mes favoris
          </button>
          <button
            className={
              activeTab === "profile"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("profile")}
            type="button"
          >
            <UserIcon size={18} /> Mon Profil
          </button>
          <button
            className={
              activeTab === "roleRequests"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("roleRequests")}
            type="button"
          >
            <Shield size={18} /> Demandes de rôle
          </button>

          <div className="mb-2 mt-6 px-2 text-[11px] font-extrabold uppercase tracking-wider text-slate-300/60">
            Base &amp; Outils
          </div>
          <button
            className={
              activeTab === "textes"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("textes")}
            type="button"
          >
            <Library size={18} /> Base Juridique
          </button>
          <button
            className={
              activeTab === "veille"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => switchTab("veille")}
            type="button"
          >
            <Globe size={18} /> Veille & Actualités
          </button>
          <button
            className={
              activeTab === "annuaire"
                ? "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium bg-transparent transition-all duration-200 text-left bg-amber-500/10 text-amber-500 font-bold border-l-4 border-amber-500 rounded-l-none"
                : "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-slate-400 bg-transparent transition-all duration-200 text-left hover:bg-white/5 hover:text-slate-200 hover:translate-x-1"
            }
            onClick={() => navigate("/annuaire")}
            type="button"
          >
            <Users size={18} /> Annuaire Public
          </button>
        </nav>

        <footer className="border-t border-white/10 bg-slate-950/60 p-5">
          <button
            onClick={handleLogout}
            className="flex w-full items-center justify-center gap-2 rounded-xl border border-red-500/25 bg-red-500/10 px-4 py-3 text-sm font-semibold text-red-200 transition-all duration-200 hover:bg-red-500 hover:text-white"
            type="button"
          >
            <LogOut size={18} /> Déconnexion
          </button>
        </footer>
      </aside>

      {/* ========================================================= */}
      {/* ZONE DE CONTENU PRINCIPALE (MAIN)                           */}
      {/* ========================================================= */}
      <main className="flex-1 flex flex-col min-w-0 h-screen bg-slate-950">

        <header className="h-[80px] shrink-0 sticky top-0 z-10 px-8 flex justify-between items-center border-b border-white/10 bg-slate-950/80 backdrop-blur-md">
          <div className="min-w-0">
            <h2 className="truncate text-xl font-bold tracking-tight text-slate-100 sm:text-2xl">
              {activeTab === "overview" && `Bonjour, ${user?.firstName || "Maître"}.`}
              {activeTab === "textes" && "Recherche Juridique"}
              {activeTab === "annotations" && "Mon Carnet de Notes"}
              {activeTab === "favorites" && "Bibliothèque des Favoris"}
              {activeTab === "profile" && "Paramètres du Profil"}
              {activeTab === "roleRequests" && "Demandes de Rôle"}
              {activeTab === "veille" && "Veille Juridique"}
              {activeTab === "notifications" && "Centre de Notifications"}
            </h2>
            <p className="mt-1 truncate text-sm text-slate-300/70">
              {activeTab === "overview" && "Voici un résumé de votre activité sur LexData."}
            </p>
          </div>

          <div className="shrink-0">
            <NotificationBell onViewAll={() => switchTab("notifications")} />
          </div>
        </header>

        <div className="flex-1 overflow-y-auto p-6 lg:p-10">

          {/* ONGLET : VUE D'ENSEMBLE */}
          {activeTab === "overview" && (
            <div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">

                <div
                  className="bg-white/5 border border-white/10 rounded-xl p-6 flex items-center gap-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20"
                  onClick={() => switchTab("favorites")}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => (e.key === "Enter" || e.key === " " ? switchTab("favorites") : null)}
                >
                  <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0 bg-amber-500/10 text-amber-500">
                      <Bookmark size={28} aria-hidden />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-3xl font-extrabold leading-none text-slate-100">{favoritesList?.length || 0}</h3>
                    <p className="mt-1 text-sm text-slate-300/70">Textes en favoris</p>
                  </div>
                </div>

                <div
                  className="bg-white/5 border border-white/10 rounded-xl p-6 flex items-center gap-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20"
                  onClick={() => switchTab("notifications")}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => (e.key === "Enter" || e.key === " " ? switchTab("notifications") : null)}
                >
                  <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0 bg-red-500/10 text-red-500">
                      <BellRing size={28} aria-hidden />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-3xl font-extrabold leading-none text-slate-100">3</h3>
                    <p className="mt-1 text-sm text-slate-300/70">Alertes non lues</p>
                  </div>
                </div>

                <div
                  className="bg-white/5 border border-white/10 rounded-xl p-6 flex items-center gap-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20"
                  onClick={() => switchTab("annotations")}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => (e.key === "Enter" || e.key === " " ? switchTab("annotations") : null)}
                >
                  <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0 bg-emerald-500/10 text-emerald-500">
                      <PenTool size={28} aria-hidden />
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-3xl font-extrabold leading-none text-slate-100">5</h3>
                    <p className="mt-1 text-sm text-slate-300/70">Annotations créées</p>
                  </div>
                </div>
              </div>

              <div className="bg-white/5 border border-white/10 rounded-xl backdrop-blur-sm p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/20">
                <h3 className="mb-4 text-lg font-bold text-slate-100">Activité Récente</h3>
                <div className="rounded-xl border border-dashed border-white/10 bg-black/20 p-10 text-center">
                  <p className="m-0 text-sm text-slate-300/70">
                    Votre historique d&apos;activité apparaîtra ici prochainement.
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* CHARGEMENT DES SOUS-COMPOSANTS SELON L'ONGLET */}
          {activeTab === "notifications" && (
            <div>
              <NotificationsView />
            </div>
          )}
          {activeTab === "textes" && (
            <div>
              <Recherche />
            </div>
          )}
          {activeTab === "annotations" && (
            <div>
              <MesNotes />
            </div>
          )}
          {activeTab === "favorites" && (
            <div>
              <MesFavoris />
            </div>
          )}
          {activeTab === "veille" && (
            <div>
              <Veille />
            </div>
          )}
          {activeTab === "profile" && (
            <div>
              <UserProfile />
            </div>
          )}
          {activeTab === "roleRequests" && (
            <div>
              <RoleChangeRequest />
            </div>
          )}

        </div>
      </main>
    </div>
  );
}