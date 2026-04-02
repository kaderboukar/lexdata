import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Loader2, Megaphone, Send, Info, Users, ShieldAlert, CheckSquare } from "lucide-react";
import toast from "react-hot-toast";
import notificationService from "../../api/notificationService";
import { getApiErrorMessage } from "../../utils/getApiErrorMessage";

const TARGET_TYPES = [
  { value: "ALL", label: "Tous les utilisateurs de la plateforme", icon: Users },
  { value: "ROLE", label: "Utilisateurs ayant un rôle spécifique", icon: ShieldAlert },
  { value: "USERS", label: "Liste d’identifiants spécifiques (IDs)", icon: CheckSquare },
];

const NOTIFICATION_TYPES = [
  "VEILLE", "SYNTHESE", "CALENDRIER", "CONTRAT",
  "CONSULTATION", "PAIEMENT", "TRIBUNE", "SYSTEME",
];

const CHANNELS = [
  { value: "IN_APP", label: "Application (In-App)" },
  { value: "EMAIL", label: "Email" },
  { value: "PUSH", label: "Push Notification" },
  { value: "SMS", label: "SMS" },
];

export default function AdminBroadcastPanel() {
  const [targetType, setTargetType] = useState("ALL");
  const [role, setRole] = useState("");
  const [userIdsText, setUserIdsText] = useState("");
  const [title, setTitle] = useState("");
  const [message, setMessage] = useState("");
  const [link, setLink] = useState("");
  const [notifType, setNotifType] = useState("SYSTEME");

  const [forceChannels, setForceChannels] = useState(false);
  const [selectedChannels, setSelectedChannels] = useState({
    EMAIL: false, SMS: false, PUSH: false, IN_APP: true,
  });

  const broadcastMutation = useMutation({
    mutationFn: (body) => notificationService.sendAdminBroadcast(body),
    onSuccess: () => {
      toast.success("Diffusion envoyée avec succès dans la file d'attente.");
      // Optionnel : Réinitialiser le formulaire après envoi
      setTitle(""); setMessage(""); setLink("");
    },
    onError: (err) => {
      if (!err.response) {
        toast.error(getApiErrorMessage(err, "Échec de l’envoi de la diffusion."));
      }
    },
  });

  const toggleChannel = (key) => {
    setSelectedChannels((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const buildPayload = () => {
    const payload = {
      targetType,
      title: title.trim(),
      message: message.trim(),
      type: notifType,
    };

    const linkTrim = link.trim();
    if (linkTrim) payload.link = linkTrim;

    if (targetType === "ROLE") {
      payload.role = role.trim();
    } else if (targetType === "USERS") {
      payload.userIds = userIdsText
        .split(/[\s,;]+/)
        .map((s) => s.trim())
        .filter(Boolean)
        .map(Number)
        .filter((n) => Number.isFinite(n) && n > 0);
    }

    if (forceChannels) {
      const channels = Object.entries(selectedChannels)
        .filter(([, isOn]) => isOn)
        .map(([c]) => c);
      if (channels.length > 0) payload.channels = channels;
    }

    return payload;
  };

  const validate = (payload) => {
    if (!payload.title || !payload.message) {
      toast.error("Le titre et le message sont obligatoires.");
      return false;
    }
    if (payload.targetType === "ROLE" && !payload.role) {
      toast.error("Veuillez indiquer un rôle valide (ex. ROLE_JURISTE).");
      return false;
    }
    if (payload.targetType === "USERS" && (!payload.userIds || payload.userIds.length === 0)) {
      toast.error("Veuillez indiquer au moins un identifiant utilisateur valide.");
      return false;
    }
    if (forceChannels && (!payload.channels || payload.channels.length === 0)) {
      toast.error("Veuillez sélectionner au moins un canal de diffusion.");
      return false;
    }
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = buildPayload();

    if (!validate(payload)) return;

    if (window.confirm(`Confirmez-vous l'envoi de ce message à la cible sélectionnée ?\n\nTitre: ${payload.title}`)) {
      broadcastMutation.mutate(payload);
    }
  };

  return (
    <div className="max-w-3xl mx-auto pb-10 animate-in fade-in duration-300">

      {/* EN-TÊTE */}
      <header className="bg-white/5 backdrop-blur-sm border border-white/10 border-l-4 border-l-red-500 rounded-2xl p-6 mb-8 flex items-start gap-5">
        <div className="p-4 bg-red-500/10 rounded-full text-red-500 shrink-0">
          <Megaphone size={32} aria-hidden />
        </div>
        <div>
          <h2 className="text-2xl font-bold text-slate-100 mb-2">Campagne de Diffusion</h2>
          <p className="text-slate-400 text-sm leading-relaxed m-0">
            Cet outil vous permet d'envoyer des notifications de masse.
            Par défaut, le système respecte les préférences de chaque utilisateur (Email, Push, etc.).
            Vous pouvez forcer l'utilisation d'un canal spécifique pour les communications critiques.
          </p>
        </div>
      </header>

      {/* FORMULAIRE PRINCIPAL */}
      <form className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8" onSubmit={handleSubmit}>

        {/* SECTION 1 : CIBLAGE */}
        <section className="mb-8">
          <h3 className="text-lg font-bold text-slate-100 mb-6 flex items-center gap-3 border-b border-white/10 pb-3">
            <Users size={18} className="text-red-500" aria-hidden /> 1. Audience cible
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            {TARGET_TYPES.map((t) => {
              const Icon = t.icon;
              const isSelected = targetType === t.value;
              return (
                <label
                  key={t.value}
                  className={
                    isSelected
                      ? "flex flex-col items-center gap-3 p-5 rounded-xl border border-red-500 bg-red-500/10 text-red-500 cursor-pointer transition-all shadow-lg"
                      : "flex flex-col items-center gap-3 p-5 rounded-xl border border-white/5 bg-black/20 text-slate-400 cursor-pointer hover:border-white/20 transition-all"
                  }
                >
                  <input type="radio" className="hidden" name="targetType" value={t.value} checked={isSelected} onChange={(e) => setTargetType(e.target.value)} />
                  <Icon size={24} aria-hidden />
                  <span className="text-sm font-medium text-center">{t.label}</span>
                </label>
              );
            })}
          </div>

          {targetType === "ROLE" && (
            <div className="bg-black/20 p-5 rounded-xl border border-white/5 mt-4 animate-in fade-in duration-200">
              <div className="mb-6">
                <label className="block text-sm font-bold text-slate-300 mb-2 text-amber-500">Rôle ciblé *</label>
                <input
                  className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all font-mono text-sm"
                  placeholder="ex: ROLE_AVOCAT, ROLE_JURISTE..."
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  required
                />
                <span className="text-xs text-slate-400 mt-2 block">
                  <Info size={12} className="inline mr-1" aria-hidden /> Respectez la casse et le format exact du rôle en base de données.
                </span>
              </div>
            </div>
          )}

          {targetType === "USERS" && (
            <div className="bg-black/20 p-5 rounded-xl border border-white/5 mt-4 animate-in fade-in duration-200">
              <div className="mb-6">
                <label className="block text-sm font-bold text-slate-300 mb-2 text-amber-500">Identifiants (IDs) des utilisateurs *</label>
                <textarea
                  className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all font-mono text-sm"
                  rows="3"
                  placeholder="Ex: 12, 45, 102 (Séparés par des virgules)"
                  value={userIdsText}
                  onChange={(e) => setUserIdsText(e.target.value)}
                  required
                />
              </div>
            </div>
          )}
        </section>

        {/* SECTION 2 : CONTENU DU MESSAGE */}
        <section className="mb-8">
          <h3 className="text-lg font-bold text-slate-100 mb-6 flex items-center gap-3 border-b border-white/10 pb-3">
            <Megaphone size={18} className="text-red-500" aria-hidden /> 2. Contenu du message
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
            <div className="mb-6">
              <label className="block text-sm font-bold text-slate-300 mb-2">Titre de la notification *</label>
              <input
                className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all"
                placeholder="Titre accrocheur..."
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            <div className="mb-6">
              <label className="block text-sm font-bold text-slate-300 mb-2">Catégorie (Type)</label>
              <select
                className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all"
                value={notifType}
                onChange={(e) => setNotifType(e.target.value)}
              >
                {NOTIFICATION_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
          </div>

          <div className="mb-6">
            <label className="block text-sm font-bold text-slate-300 mb-2">Corps du message *</label>
            <textarea
              className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all"
              rows="4"
              placeholder="Rédigez votre message ici..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              required
            />
          </div>

          <div className="mb-6">
            <label className="block text-sm font-bold text-slate-300 mb-2">Lien de redirection (Optionnel)</label>
            <input
              className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none transition-all"
              type="url"
              placeholder="https://lexdata.ne/..."
              value={link}
              onChange={(e) => setLink(e.target.value)}
            />
            <span className="text-xs text-slate-400 mt-1 block">Les utilisateurs seront redirigés vers ce lien au clic sur la notification.</span>
          </div>
        </section>

        {/* SECTION 3 : CANAUX DE DIFFUSION */}
        <section className="mb-8">
          <h3 className="text-lg font-bold text-slate-100 mb-6 flex items-center gap-3 border-b border-white/10 pb-3">
            <Send size={18} className="text-red-500" aria-hidden /> 3. Canaux de distribution
          </h3>

          <div className="bg-black/20 p-6 rounded-xl border border-white/5">
            <label className="flex items-center gap-3 cursor-pointer text-sm text-slate-300 hover:text-slate-100 transition-colors mb-4 font-bold text-amber-500">
              <input
                type="checkbox"
                className="w-5 h-5 accent-red-500 cursor-pointer rounded border-white/20 bg-black/30 shrink-0"
                checked={forceChannels}
                onChange={(e) => setForceChannels(e.target.checked)}
              />
              Forcer les canaux de diffusion
            </label>

            {forceChannels ? (
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 fade-in">
                {CHANNELS.map((c) => (
                  <label key={c.value} className="flex items-center gap-3 cursor-pointer text-sm text-slate-300 hover:text-slate-100 transition-colors">
                    <input
                      type="checkbox"
                      className="w-5 h-5 accent-red-500 cursor-pointer rounded border-white/20 bg-black/30 shrink-0"
                      checked={selectedChannels[c.value]}
                      onChange={() => toggleChannel(c.value)}
                    />
                    {c.label}
                  </label>
                ))}
              </div>
            ) : (
              <p className="text-sm text-slate-400 m-0 italic flex items-center gap-2">
                <Info size={16} aria-hidden /> Le système utilisera les canaux choisis par chaque utilisateur dans ses préférences (Email, Push, in-app).
              </p>
            )}
          </div>
        </section>

        {/* ACTIONS */}
        <div className="flex justify-end border-t border-white/10 pt-8 mt-4">
          <button
            type="submit"
            className="flex items-center justify-center gap-2 bg-red-500 text-white font-bold px-8 py-3 rounded-xl transition-all hover:bg-red-400 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-red-500/25"
            disabled={broadcastMutation.isPending}
          >
            {broadcastMutation.isPending ? (
              <>
                <Loader2 size={20} className="animate-spin" aria-hidden /> Génération en cours...
              </>
            ) : (
              <>
                <Send size={20} aria-hidden /> Lancer la campagne
              </>
            )}
          </button>
        </div>
      </form>

    </div>
  );
}