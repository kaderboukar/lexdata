import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Search, ArrowLeft, Loader2, ShieldAlert, BadgeCheck, User,
  MapPin, Briefcase, Phone, Building2, Shield, Info, CheckCircle2, XCircle
} from "lucide-react";
import toast from "react-hot-toast";
import userProfileService from "../../api/userProfileService";
import { getApiErrorMessage } from "../../utils/getApiErrorMessage";

const KYC_STATUSES = [
  { value: "PENDING", label: "En attente d'examen", icon: Info, color: "text-warning" },
  { value: "VERIFIED", label: "Profil Vérifié (Approuvé)", icon: CheckCircle2, color: "text-success" },
  { value: "REJECTED", label: "Profil Rejeté", icon: XCircle, color: "text-danger" },
];

export default function ProfilesManager() {
  const queryClient = useQueryClient();
  const [view, setView] = useState("list");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedUsername, setSelectedUsername] = useState(null);

  const [kycStatus, setKycStatus] = useState("VERIFIED");
  const [kycComment, setKycComment] = useState("");

  // 1. DATA FETCHING (Liste)
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin-profiles"],
    queryFn: () => userProfileService.getAllProfiles({ page: 0, size: 100 }),
  });

  const profiles = data?.content ?? data ?? [];
  const filtered = profiles.filter((p) => {
    const q = searchTerm.toLowerCase();
    return (
      p.username?.toLowerCase().includes(q) ||
      p.fullName?.toLowerCase().includes(q) ||
      p.city?.toLowerCase().includes(q) ||
      p.professionalTitle?.toLowerCase().includes(q)
    );
  });

  // 2. DATA FETCHING (Détail Profil)
  const detailQuery = useQuery({
    queryKey: ["admin-profile", selectedUsername],
    queryFn: () => userProfileService.getProfileByUsername(selectedUsername),
    enabled: !!selectedUsername && view === "detail",
  });

  const profile = detailQuery.data;

  // 3. MUTATION (Validation KYC)
  const verifyMutation = useMutation({
    mutationFn: ({ username, body }) => userProfileService.verifyProfile(username, body),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["admin-profiles"] });
      queryClient.invalidateQueries({ queryKey: ["admin-profile", variables.username] });
      toast.success("Statut KYC mis à jour avec succès.");
      setView("list");
    },
    onError: (err) => {
      toast.error(getApiErrorMessage(err, "Erreur lors de la mise à jour KYC."));
    }
  });

  // 4. HANDLERS
  const openDetail = (username) => {
    setSelectedUsername(username);
    setKycStatus("VERIFIED");
    setKycComment("");
    setView("detail");
  };

  const handleVerifySubmit = (e) => {
    e.preventDefault();
    if (!selectedUsername) return;

    if (kycStatus === "REJECTED" && !kycComment.trim()) {
      toast.error("Un commentaire est obligatoire en cas de rejet.");
      return;
    }

    verifyMutation.mutate({
      username: selectedUsername,
      body: { status: kycStatus, comment: kycComment.trim() || undefined },
    });
  };

  // ==========================================
  // RENDU : VUE DÉTAIL (Fiche Utilisateur & KYC)
  // ==========================================
  if (view === "detail" && selectedUsername) {
    return (
      <div className="max-w-7xl mx-auto fade-in">

        {/* HEADER DE LA VUE DÉTAIL */}
        <header className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 mb-8 flex items-center gap-4">
          <button
            type="button"
            className="p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 transition-colors"
            onClick={() => { setView("list"); setSelectedUsername(null); }}
            aria-label="Retour à la liste"
          >
            <ArrowLeft size={24} aria-hidden />
          </button>
          <div>
            <h2 className="text-xl font-bold text-slate-100 m-0">Examen du profil</h2>
            <p className="text-slate-400 m-0 text-sm">Utilisateur : @{selectedUsername}</p>
          </div>
        </header>

        {detailQuery.isLoading ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement du dossier...</p>
          </div>
        ) : detailQuery.isError || !profile ? (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl flex gap-3 items-center text-sm" role="alert">
            <ShieldAlert size={20} aria-hidden /> Impossible de charger ce profil. L'utilisateur a peut-être été supprimé.
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

            {/* COLONNE GAUCHE : INFOS UTILISATEUR */}
            <div className="lg:col-span-2 flex flex-col gap-6">

              {/* Carte d'identité principale */}
              <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8 border-t-4 border-t-red-500">
                <div className="flex items-start gap-4 mb-6">
                  <div className="w-20 h-20 rounded-full bg-red-500/20 text-red-500 flex items-center justify-center text-3xl font-bold shrink-0">
                    {profile.fullName?.charAt(0) || profile.username?.charAt(0) || "U"}
                  </div>
                  <div className="min-w-0">
                    <h3 className="text-2xl font-bold text-slate-100 m-0 mb-1">{profile.fullName || "Nom non renseigné"}</h3>
                    <p className="text-slate-400 m-0 flex items-center gap-2 flex-wrap">
                      @{profile.username}
                      {profile.verificationStatus && (
                        <span className={`badge ${profile.verificationStatus === 'VERIFIED' ? 'badge-publie' : 'badge-brouillon'} text-xs`}>
                          KYC: {profile.verificationStatus}
                        </span>
                      )}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-y-4 gap-x-6">
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider flex items-center gap-1.5">
                      <Briefcase size={12} aria-hidden /> Titre Pro
                    </span>
                    <span className="text-slate-200 font-medium mt-1">{profile.professionalTitle || "—"}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider flex items-center gap-1.5">
                      <MapPin size={12} aria-hidden /> Ville
                    </span>
                    <span className="text-slate-200 font-medium mt-1">{profile.city || "—"}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider flex items-center gap-1.5">
                      <Phone size={12} aria-hidden /> Téléphone
                    </span>
                    <span className="text-slate-200 font-medium mt-1">{profile.phoneNumber || "—"}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider flex items-center gap-1.5">
                      <User size={12} aria-hidden /> Spécialités
                    </span>
                    <span className="text-slate-200 font-medium mt-1">
                      {Array.isArray(profile.specialties) ? profile.specialties.join(", ") : profile.specialties || "—"}
                    </span>
                  </div>
                </div>

                {profile.bio && (
                  <div className="mt-6 pt-4 border-t border-white/10">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider mb-2 block">Biographie</span>
                    <p className="text-slate-300 italic text-sm m-0">"{profile.bio}"</p>
                  </div>
                )}
              </div>

              {/* Informations Entreprise / Cabinet */}
              <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">
                <h4 className="text-lg font-bold text-slate-100 flex items-center gap-2 mb-4 border-b border-white/10 pb-3">
                  <Building2 size={20} className="text-red-500" aria-hidden /> Informations Cabinet / Entreprise
                </h4>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-y-4 gap-x-6">
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider">Raison Sociale</span>
                    <span className="text-slate-200 font-medium mt-1">{profile.companyName || "—"}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider">NIF</span>
                    <span className="text-slate-200 font-medium mt-1">{profile.nif || "—"}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider">Barreau</span>
                    <span className="text-slate-200 font-medium mt-1">{profile.barreau || "—"}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    <span className="text-slate-500 text-xs uppercase font-bold tracking-wider">N° de Toque</span>
                    <span className="text-slate-200 font-medium mt-1">{profile.numeroToque || "—"}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* COLONNE DROITE : ACTION KYC */}
            <div className="flex flex-col gap-6">
              <div className="bg-black/20 border border-white/10 border-t-4 border-t-amber-500 rounded-2xl p-6">
                <h4 className="text-lg font-bold text-slate-100 flex items-center gap-2 mb-4 border-b border-white/10 pb-3">
                  <BadgeCheck size={20} className="text-amber-400" aria-hidden /> Validation KYC
                </h4>
                <p className="text-sm text-slate-400 mb-4">Vérifiez les informations ci-contre avant d'accorder l'accréditation au professionnel.</p>

                <form onSubmit={handleVerifySubmit}>
                  <div className="form-group mb-4">
                    <label className="text-sm font-bold text-slate-200">Statut de vérification</label>
                    <div className="flex flex-col gap-2 mt-2">
                      {KYC_STATUSES.map((s) => {
                        const StatusIcon = s.icon;
                        const isActive = kycStatus === s.value;
                        const colorClass =
                          s.color === "text-warning"
                            ? "text-amber-400"
                            : s.color === "text-success"
                              ? "text-emerald-400"
                              : "text-red-400";

                        return (
                          <label
                            key={s.value}
                            className={
                              isActive
                                ? "flex items-center gap-3 p-3 rounded-xl border border-red-500 bg-white/5 transition-all cursor-pointer"
                                : "flex items-center gap-3 p-3 rounded-xl border border-white/10 bg-black/20 hover:border-white/20 transition-all cursor-pointer"
                            }
                          >
                            <input
                              type="radio"
                              name="kycStatus"
                              value={s.value}
                              checked={isActive}
                              onChange={(e) => setKycStatus(e.target.value)}
                              className="scale-125 accent-red-500 cursor-pointer"
                            />
                            <span className={`flex items-center gap-2 font-medium ${colorClass}`}>
                              <StatusIcon size={16} aria-hidden /> {s.label}
                            </span>
                          </label>
                        );
                      })}
                    </div>
                  </div>

                  <div className="form-group mb-6">
                    <label className="text-sm font-bold text-slate-200 flex justify-between">
                      Commentaire <span className="text-xs font-normal text-slate-500">(Obligatoire si rejet)</span>
                    </label>
                    <textarea
                      className="w-full bg-black/40 border border-white/10 rounded-lg p-3 text-sm text-slate-200 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none resize-y mt-2"
                      rows={4}
                      value={kycComment}
                      onChange={(e) => setKycComment(e.target.value)}
                      placeholder="Raison du rejet ou note interne..."
                      required={kycStatus === "REJECTED"}
                    />
                  </div>

                  <button
                    type="submit"
                    className="w-full flex justify-center items-center gap-2 bg-red-500 text-white font-bold px-6 py-3 rounded-xl transition-all hover:bg-red-400 disabled:opacity-50 mt-6"
                    disabled={verifyMutation.isPending}
                  >
                    {verifyMutation.isPending && <Loader2 size={18} className="animate-spin" aria-hidden />}
                    {verifyMutation.isPending ? "Traitement..." : "Appliquer la décision"}
                  </button>
                </form>
              </div>

              <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6">
                <h4 className="text-sm font-bold text-slate-500 uppercase tracking-wider mb-3 flex items-center gap-2">
                  <Shield size={14} aria-hidden /> Accès Plateforme
                </h4>
                <div className="flex flex-col gap-1 text-sm">
                  <div className="flex justify-between py-2 border-b border-white/5">
                    <span className="text-slate-500">Abonnement</span>
                    <span className="font-bold text-slate-100">{profile.subscriptionType || "STANDARD"}</span>
                  </div>
                  <div className="flex justify-between py-2">
                    <span className="text-slate-500">Rôle système</span>
                    <span className="font-bold text-slate-100">{profile.roles?.[0] || "USER"}</span>
                  </div>
                </div>
              </div>
            </div>

          </div>
        )}
      </div>
    );
  }

  // ==========================================
  // RENDU : VUE LISTE (Tableau)
  // ==========================================
  return (
    <div className="max-w-7xl mx-auto fade-in">
      <header className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 mb-8 flex flex-col md:flex-row justify-between items-center gap-6">
        <div className="flex items-center gap-3 bg-black/30 border border-white/10 rounded-lg px-4 py-3 transition-all focus-within:border-red-500 focus-within:ring-1 focus-within:ring-red-500 w-full md:max-w-lg">
          <Search size={20} className="text-slate-500" aria-hidden />
          <input
            type="text"
            className="w-full bg-transparent border-none outline-none text-slate-200 placeholder-slate-500"
            placeholder="Rechercher par @username, nom, ville…"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="badge badge-premium px-4 py-2 text-sm">
          {filtered.length} Profil(s)
        </div>
      </header>

      <div className="overflow-x-auto bg-slate-900/50 border border-white/10 rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement des profils...</p>
          </div>
        ) : isError ? (
          <div className="p-10 text-center">
            <ShieldAlert size={40} className="mx-auto text-red-400 mb-4" aria-hidden />
            <p className="text-red-400">Impossible de charger les profils. Vérifiez vos droits.</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse min-w-[900px]">
            <thead>
              <tr>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 w-[35%]">
                  Identité
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Activité & Localisation
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Statut KYC
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap text-right">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-center p-8 text-slate-400">
                    Aucun profil ne correspond à votre recherche.
                  </td>
                </tr>
              ) : (
                filtered.map((p) => {
                  // Style dynamique du badge KYC
                  let kycBadgeClass = "badge-brouillon";
                  if (p.verificationStatus === "VERIFIED") kycBadgeClass = "badge-publie";
                  if (p.verificationStatus === "REJECTED") kycBadgeClass = "badge-danger";

                  return (
                    <tr key={p.username} className="hover:bg-white/5 transition-colors">
                      <td className="p-4 border-b border-white/5 align-middle">
                        <div className="flex items-center gap-4">
                          <div className="w-10 h-10 rounded-full bg-red-500/15 text-red-500 flex items-center justify-center font-bold text-lg shrink-0">
                            {p.fullName?.charAt(0) || p.username?.charAt(0) || "U"}
                          </div>
                          <div className="min-w-0">
                            <div className="font-bold text-slate-100 text-base mb-0.5">
                              {p.fullName || <span className="text-slate-500 italic">Non renseigné</span>}
                            </div>
                            <div className="text-xs text-slate-400">@{p.username}</div>
                          </div>
                        </div>
                      </td>
                      <td className="p-4 border-b border-white/5 align-middle">
                        <div className="text-slate-100 font-medium text-sm mb-1">{p.professionalTitle || "—"}</div>
                        <div className="text-slate-400 text-xs flex items-center gap-1">
                          <MapPin size={12} aria-hidden /> {p.city || "—"}
                        </div>
                      </td>
                      <td className="p-4 border-b border-white/5 align-middle">
                        <span className={`badge ${kycBadgeClass} text-xs font-mono px-2 py-1`}>
                          {p.verificationStatus || "NON SOUMIS"}
                        </span>
                      </td>
                      <td className="p-4 border-b border-white/5 align-middle text-right">
                        <button
                          className="flex items-center justify-end gap-2 px-3 py-1.5 bg-white/5 border border-white/10 rounded-lg text-sm text-slate-300 hover:bg-white/10 hover:text-white transition-colors inline-flex"
                          onClick={() => openDetail(p.username)}
                          type="button"
                        >
                          <BadgeCheck size={16} aria-hidden /> Dossier KYC
                        </button>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}