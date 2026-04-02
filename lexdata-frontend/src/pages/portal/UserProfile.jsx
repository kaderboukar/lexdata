import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import {
  User, Mail, Phone, Shield, CreditCard, CheckCircle2,
  Bell, Loader2, Download, Trash2, AlertCircle, Building2, MapPin
} from "lucide-react";
import toast from "react-hot-toast";
import useAuthStore from "../../store/useAuthStore";
import userMeService from "../../api/userMeService";
import userPreferenceService, { buildPreferenceRequest } from "../../api/userPreferenceService";
import { LEGAL_DOMAINS } from "../../constants/legalDomains";

const defaultForm = () => ({
  fullName: "", phoneNumber: "", city: "", profilePictureUrl: "",
  preferredLanguage: "fr", bio: "", professionalTitle: "", availability: "",
  barreau: "", numeroToque: "", companyName: "", nif: "", employeeCount: "",
  specialties: [], followedTopics: [], alertKeywords: "",
  emailEnabled: true, pushEnabled: true, smsEnabled: false, timezone: "Europe/Paris",
});

function mapAggregateToForm(aggregate, authUser) {
  const p = aggregate?.profile || {};
  const pref = aggregate?.preference || {};
  const fromAuth = [authUser?.firstName, authUser?.lastName].filter(Boolean).join(" ").trim() || authUser?.username || "Utilisateur";
  return {
    ...defaultForm(),
    fullName: p.fullName || fromAuth,
    phoneNumber: p.phoneNumber || authUser?.telephone || "",
    city: p.city || "",
    profilePictureUrl: p.profilePictureUrl || "",
    preferredLanguage: p.preferredLanguage || "fr",
    bio: p.bio || "",
    professionalTitle: p.professionalTitle || "",
    availability: p.availability || "",
    barreau: p.barreau || "",
    numeroToque: p.numeroToque || "",
    companyName: p.companyName || "",
    nif: p.nif || "",
    employeeCount: p.employeeCount != null && p.employeeCount !== "" ? String(p.employeeCount) : "",
    specialties: Array.isArray(p.specialties) ? [...p.specialties] : p.specialties ? [...p.specialties] : [],
    followedTopics: Array.isArray(pref.followedTopics) ? [...pref.followedTopics] : pref.followedTopics ? [...pref.followedTopics] : [],
    alertKeywords: Array.isArray(pref.alertKeywords) ? pref.alertKeywords.join(", ") : "",
    emailEnabled: pref.emailEnabled ?? true,
    pushEnabled: pref.pushEnabled ?? true,
    smsEnabled: pref.smsEnabled ?? false,
    timezone: pref.timezone || "Europe/Paris",
  };
}

function buildPayload(form) {
  let employeeCount;
  if (form.employeeCount !== "" && form.employeeCount != null) {
    const n = parseInt(String(form.employeeCount), 10);
    employeeCount = Number.isNaN(n) ? undefined : n;
  }
  return {
    fullName: form.fullName.trim(),
    phoneNumber: form.phoneNumber?.trim() || undefined,
    city: form.city?.trim() || undefined,
    profilePictureUrl: form.profilePictureUrl?.trim() || undefined,
    preferredLanguage: form.preferredLanguage?.trim() || undefined,
    bio: form.bio?.trim() || undefined,
    professionalTitle: form.professionalTitle?.trim() || undefined,
    availability: form.availability?.trim() || undefined,
    barreau: form.barreau?.trim() || undefined,
    numeroToque: form.numeroToque?.trim() || undefined,
    companyName: form.companyName?.trim() || undefined,
    nif: form.nif?.trim() || undefined,
    employeeCount,
    specialties: form.specialties?.length > 0 ? form.specialties : undefined,
    ...buildPreferenceRequest(form),
  };
}

export default function UserProfile() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const authUser = useAuthStore((state) => state.user);
  const isPremium = useAuthStore((state) => state.isPremium());
  const logout = useAuthStore((state) => state.logout);

  const [form, setForm] = useState(defaultForm);
  const [verificationStatus, setVerificationStatus] = useState(null);
  const [subscriptionType, setSubscriptionType] = useState(null);

  // 1. CHARGEMENT DONNÉES
  const { data: aggregate, isLoading, isError, error: loadError } = useQuery({
    queryKey: ["user-me"],
    queryFn: () => userMeService.getMe(),
  });

  useEffect(() => {
    if (!aggregate) return;
    setForm(mapAggregateToForm(aggregate, authUser));
    setVerificationStatus(aggregate?.profile?.verificationStatus ?? null);
    setSubscriptionType(aggregate?.profile?.subscriptionType ?? null);
  }, [aggregate, authUser]);

  // 2. MUTATIONS
  const saveMutation = useMutation({
    mutationFn: (payload) => userMeService.updateMe(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-me"] });
      toast.success("Profil enregistré avec succès.");
    },
    onError: (err) => {
      const msg = err.response?.data?.message || (Array.isArray(err.response?.data?.errors) ? err.response.data.errors.join(", ") : null) || "Erreur lors de l'enregistrement.";
      toast.error(msg);
    },
  });

  const prefsMutation = useMutation({
    mutationFn: (payload) => userPreferenceService.updatePreferences(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-me"] });
      toast.success("Préférences de veille mises à jour.");
    },
    onError: (err) => {
      const msg = err.response?.data?.message || (Array.isArray(err.response?.data?.errors) ? err.response.data.errors.join(", ") : null) || "Erreur d'enregistrement des préférences.";
      toast.error(msg);
    },
  });

  // 3. HANDLERS
  const handleSavePreferencesOnly = () => prefsMutation.mutate(buildPreferenceRequest(form));

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((f) => ({ ...f, [name]: type === "checkbox" ? checked : value }));
  };

  const toggleDomain = (field, value) => {
    setForm((f) => {
      const set = new Set(f[field]);
      set.has(value) ? set.delete(value) : set.add(value);
      return { ...f, [field]: [...set] };
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.fullName?.trim()) return toast.error("Le nom complet est obligatoire.");
    saveMutation.mutate(buildPayload(form));
  };

  const handleExport = async () => {
    try {
      const res = await userMeService.exportMe();
      const blob = new Blob([res.data], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `lexdata_export_${authUser?.username || 'rgpd'}.json`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success("Archive téléchargée.");
    } catch {
      toast.error("Impossible de générer l’archive.");
    }
  };

  const handleAnonymize = async () => {
    if (!window.confirm("Anonymiser votre compte ? Cette action est irréversible (droit à l’oubli). Vos données personnelles seront écrasées et vous serez déconnecté.")) return;
    try {
      await userMeService.deleteMe();
      toast.success("Profil anonymisé avec succès.");
      await logout();
      navigate("/", { replace: true });
    } catch (err) {
      toast.error(err.response?.data?.message || "Impossible d’anonymiser le profil.");
    }
  };

  // --- RENDUS DE CHARGEMENT ET ERREUR ---
  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center mt-20 gap-3 text-slate-400">
        <Loader2 className="animate-spin text-amber-500" size={32} aria-hidden />
        <p className="text-sm">Récupération de vos données sécurisées...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center mt-20 gap-3 text-slate-400 px-4">
        <div className="w-full max-w-2xl bg-red-500/10 border border-red-500/20 rounded-2xl p-6 backdrop-blur-sm">
          <div className="flex gap-3 items-start">
            <AlertCircle size={24} className="mt-0.5 text-red-300" aria-hidden />
            <div>
              <strong className="text-slate-100">Erreur de chargement du profil</strong>
              <p className="text-slate-300/80 mb-0 mt-1 text-sm">
                {loadError?.response?.data?.message || loadError?.message || "Vérifiez votre connexion au serveur."}
              </p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const displayInitial = form.fullName?.charAt(0) || authUser?.username?.charAt(0) || "U";

  return (
    <div className="user-profile-page container mx-auto px-4 py-8 pb-16 fade-in">

      {/* ========================================== */}
      {/* EN-TÊTE : IDENTITÉ ET ACTIONS RGPD           */}
      {/* ========================================== */}
      <header className="profile-header-card bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8 mb-8 flex flex-col md:flex-row items-center md:items-start text-center md:text-left gap-6 md:gap-8">
        <div className="profile-avatar w-20 h-20 md:w-24 md:h-24 rounded-full bg-gradient-to-br from-amber-500 to-amber-700 flex items-center justify-center text-3xl font-extrabold text-slate-900 shadow-lg shrink-0">
          {displayInitial}
        </div>

        <div className="profile-identity flex-1 min-w-0">
          <h2 className="text-2xl md:text-3xl font-bold text-slate-100 mb-1 truncate">
            {form.fullName || authUser?.username}
          </h2>
          <p className="text-slate-400 text-sm md:text-base mb-4 truncate">
            @{authUser?.username || "utilisateur"} {authUser?.email ? ` · ${authUser.email}` : ""}
          </p>

          <div className="flex flex-wrap justify-center md:justify-start gap-2">
            <span className="bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold">
              {authUser?.roles?.[0]?.replace("ROLE_", "") || "CLIENT"}
            </span>
            {verificationStatus && (
              <span
                className={[
                  "bg-white/5 border border-white/10 text-slate-300 px-3 py-1 rounded-full text-xs font-semibold",
                  verificationStatus === "VERIFIED" ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-300" : "",
                ].join(" ")}
              >
                KYC : {verificationStatus}
              </span>
            )}
            {(subscriptionType || isPremium) && (
              <span className="bg-amber-500/10 border border-amber-500/30 text-amber-400 px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-2">
                <Shield size={12} aria-hidden /> {subscriptionType || (isPremium ? "Premium" : "")}
              </span>
            )}
          </div>
        </div>

        <div className="profile-actions flex flex-col sm:flex-row gap-3 mt-4 md:mt-0">
          <button
            type="button"
            className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/10 bg-white/5 px-4 py-2.5 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10"
            onClick={handleExport}
            title="Télécharger mes données (RGPD)"
          >
            <Download size={16} aria-hidden /> Export RGPD
          </button>
          <button
            type="button"
            className="inline-flex items-center justify-center gap-2 rounded-xl border border-red-500/25 bg-red-500/10 px-4 py-2.5 text-sm font-semibold text-red-200 transition-all duration-300 hover:bg-red-500 hover:text-white"
            onClick={handleAnonymize}
            title="Droit à l'oubli"
          >
            <Trash2 size={16} aria-hidden /> Supprimer le compte
          </button>
        </div>
      </header>

      {/* ========================================== */}
      {/* GRILLE DES FORMULAIRES                       */}
      {/* ========================================== */}
      <div className="profile-grid grid grid-cols-1 lg:grid-cols-12 gap-8">

        {/* COLONNE GAUCHE : LE PROFIL COMPLET */}
        <form onSubmit={handleSubmit} className="lg:col-span-7 xl:col-span-8 flex flex-col gap-8">
          <section className="profile-section bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">

            <h3 className="section-title flex items-center gap-3 text-xl font-bold text-slate-100 mb-2 pb-4 border-b border-white/10">
              <User size={20} className="text-amber-500" aria-hidden /> Informations Personnelles
            </h3>

            <div className="form-group flex flex-col gap-2 mb-5 mt-6">
              <label className="form-label text-sm font-medium text-slate-300">Nom complet *</label>
              <input
                className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-grid-2 grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Téléphone</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="phoneNumber"
                  value={form.phoneNumber}
                  onChange={handleChange}
                  placeholder="+227..."
                />
              </div>
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Ville</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="city"
                  value={form.city}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="form-group flex flex-col gap-2 mb-5">
              <label className="form-label text-sm font-medium text-slate-300">Bio / Présentation</label>
              <textarea
                className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                name="bio"
                value={form.bio}
                onChange={handleChange}
                rows={3}
              />
            </div>

          </section>

          <section className="profile-section bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">
            <h3 className="section-title flex items-center gap-3 text-xl font-bold text-slate-100 mb-2 pb-4 border-b border-white/10">
              <Building2 size={20} className="text-amber-500" aria-hidden /> Activité Professionnelle
            </h3>
            <p className="section-subtitle text-sm text-slate-400 mb-6 leading-relaxed">
              Toute modification du barreau nécessite une nouvelle validation KYC.
            </p>

            <div className="form-grid-2 grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Titre professionnel</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="professionalTitle"
                  value={form.professionalTitle}
                  onChange={handleChange}
                  placeholder="Ex: Avocat à la cour"
                />
              </div>
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Disponibilité</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="availability"
                  value={form.availability}
                  onChange={handleChange}
                  placeholder="Ex: Lundi-Vendredi"
                />
              </div>
            </div>

            <div className="form-grid-2 grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Barreau de rattachement</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="barreau"
                  value={form.barreau}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Numéro de toque</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="numeroToque"
                  value={form.numeroToque}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="form-group flex flex-col gap-2 mb-5">
              <label className="form-label text-sm font-medium text-slate-300">Spécialités juridiques</label>
              <div className="tags-container flex flex-wrap gap-2 max-h-[200px] overflow-y-auto pr-2">
                {LEGAL_DOMAINS.map((d) => {
                  const selected = form.specialties.includes(d.value);
                  return (
                    <button
                      key={d.value}
                      type="button"
                      className={
                        selected
                          ? "px-3 py-1.5 rounded-full border border-amber-500 bg-amber-500/10 text-amber-500 text-xs font-bold transition-colors"
                          : "px-3 py-1.5 rounded-full border border-white/10 bg-white/5 text-slate-400 text-xs font-medium hover:bg-white/10 hover:text-slate-200 transition-colors"
                      }
                      onClick={() => toggleDomain("specialties", d.value)}
                    >
                      {d.label}
                    </button>
                  );
                })}
              </div>
            </div>
          </section>

          <section className="profile-section bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">
            <h3 className="section-title flex items-center gap-3 text-xl font-bold text-slate-100 mb-2 pb-4 border-b border-white/10">
              <MapPin size={20} className="text-amber-500" aria-hidden /> Entreprise / Cabinet
            </h3>

            <div className="form-group flex flex-col gap-2 mb-5 mt-6">
              <label className="form-label text-sm font-medium text-slate-300">Raison sociale</label>
              <input
                className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                name="companyName"
                value={form.companyName}
                onChange={handleChange}
              />
            </div>

            <div className="form-grid-2 grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">NIF / Numéro d'identification</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  name="nif"
                  value={form.nif}
                  onChange={handleChange}
                  placeholder="5 à 30 caractères"
                />
              </div>
              <div className="form-group flex flex-col gap-2">
                <label className="form-label text-sm font-medium text-slate-300">Effectif</label>
                <input
                  className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                  type="number"
                  name="employeeCount"
                  value={form.employeeCount}
                  onChange={handleChange}
                  min={0}
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full inline-flex items-center justify-center rounded-xl bg-amber-500 px-5 py-3 text-sm font-extrabold text-slate-950 transition-all duration-300 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={saveMutation.isPending}
            >
              {saveMutation.isPending ? "Enregistrement..." : "Mettre à jour mon profil global"}
            </button>
          </section>
        </form>

        {/* COLONNE DROITE : PRÉFÉRENCES ET SÉCURITÉ */}
        <div className="lg:col-span-5 xl:col-span-4 flex flex-col gap-8">

          <section className="profile-section bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">
            <h3 className="section-title flex items-center gap-3 text-xl font-bold text-slate-100 mb-2 pb-4 border-b border-white/10">
              <Bell size={20} className="text-amber-500" aria-hidden /> Préférences de Veille
            </h3>
            <p className="section-subtitle text-sm text-slate-400 mb-6 leading-relaxed">
              Ces réglages définissent les actualités que vous recevrez par notification.
            </p>

            <div className="form-group flex flex-col gap-2 mb-5">
              <label className="form-label text-sm font-medium text-slate-300">Domaines d&apos;intérêt (Alertes)</label>
              <div className="tags-container flex flex-wrap gap-2 max-h-[200px] overflow-y-auto pr-2">
                {LEGAL_DOMAINS.map((d) => {
                  const selected = form.followedTopics.includes(d.value);
                  return (
                    <button
                      key={d.value}
                      type="button"
                      className={
                        selected
                          ? "px-3 py-1.5 rounded-full border border-amber-500 bg-amber-500/10 text-amber-500 text-xs font-bold transition-colors"
                          : "px-3 py-1.5 rounded-full border border-white/10 bg-white/5 text-slate-400 text-xs font-medium hover:bg-white/10 hover:text-slate-200 transition-colors"
                      }
                      onClick={() => toggleDomain("followedTopics", d.value)}
                    >
                      {d.label}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="form-group flex flex-col gap-2 mb-5">
              <label className="form-label text-sm font-medium text-slate-300">
                Mots-clés spécifiques (séparés par des virgules)
              </label>
              <input
                className="form-control w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
                name="alertKeywords"
                value={form.alertKeywords}
                onChange={handleChange}
                placeholder="ex: fiscalité, minier, ohada"
              />
            </div>

            <div className="mb-6">
              <label className="form-label text-sm font-medium text-slate-300 mb-2 block">Canaux de réception</label>
              <label className="preference-checkbox flex items-center gap-3 cursor-pointer p-2 rounded-lg hover:bg-white/5 transition-colors text-sm text-slate-300 mb-1">
                <input
                  className="w-4 h-4 accent-amber-500 cursor-pointer rounded border-white/20 bg-black/30"
                  type="checkbox"
                  name="emailEnabled"
                  checked={form.emailEnabled}
                  onChange={handleChange}
                />
                Recevoir les alertes par Email
              </label>
              <label className="preference-checkbox flex items-center gap-3 cursor-pointer p-2 rounded-lg hover:bg-white/5 transition-colors text-sm text-slate-300 mb-1">
                <input
                  className="w-4 h-4 accent-amber-500 cursor-pointer rounded border-white/20 bg-black/30"
                  type="checkbox"
                  name="pushEnabled"
                  checked={form.pushEnabled}
                  onChange={handleChange}
                />
                Notifications Push (Navigateur)
              </label>
              <label className="preference-checkbox flex items-center gap-3 cursor-pointer p-2 rounded-lg hover:bg-white/5 transition-colors text-sm text-slate-300 mb-1">
                <input
                  className="w-4 h-4 accent-amber-500 cursor-pointer rounded border-white/20 bg-black/30"
                  type="checkbox"
                  name="smsEnabled"
                  checked={form.smsEnabled}
                  onChange={handleChange}
                />
                Alertes SMS (Option Premium)
              </label>
            </div>

            <button
              type="button"
              className="w-full inline-flex items-center justify-center rounded-xl border border-white/10 bg-white/5 px-5 py-3 text-sm font-semibold text-slate-200 transition-all duration-300 hover:bg-white/10 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={prefsMutation.isPending}
              onClick={handleSavePreferencesOnly}
            >
              {prefsMutation.isPending ? "Enregistrement..." : "Sauvegarder les préférences"}
            </button>
          </section>

          <section className="profile-section bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">
            <h3 className="section-title flex items-center gap-3 text-xl font-bold text-slate-100 mb-2 pb-4 border-b border-white/10">
              <CreditCard size={20} className="text-amber-500" aria-hidden /> Abonnement & Sécurité
            </h3>

            <ul className="mt-6 space-y-4 text-sm text-slate-300/80">
              <li className="flex items-start gap-3">
                <CheckCircle2 size={16} className={isPremium ? "mt-0.5 text-emerald-400" : "mt-0.5 text-slate-500"} aria-hidden />
                <span>
                  Statut du compte :{" "}
                  <strong className="text-slate-100">
                    {subscriptionType || (isPremium ? "Premium" : "Découverte")}
                  </strong>
                </span>
              </li>
              <li className="flex items-start gap-3">
                <Mail size={16} className="mt-0.5 shrink-0 text-slate-500" aria-hidden />
                <span className="leading-relaxed">
                  L’email de connexion <strong>({authUser?.email})</strong> est verrouillé par le service d'authentification central.
                  En cas de changement, contactez le support.
                </span>
              </li>
            </ul>
          </section>

        </div>
      </div>
    </div>
  );
}