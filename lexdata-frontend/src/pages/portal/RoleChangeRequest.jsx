import { useMemo, useState } from "react";
import { ShieldAlert, Loader2, AlertCircle } from "lucide-react";
import useAuthStore from "../../store/useAuthStore";
import roleChangeRequestService from "../../api/roleChangeRequestService";

const REQUESTABLE_ROLES = [
  { value: "ROLE_JURISTE", label: "Juriste" },
  { value: "ROLE_AVOCAT", label: "Avocat" },
];

export default function RoleChangeRequest() {
  const user = useAuthStore((s) => s.user);
  const [requestedRole, setRequestedRole] = useState("ROLE_JURISTE");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [lastResult, setLastResult] = useState(null);

  const currentRoles = useMemo(() => {
    return (user?.roles || []).map((r) => (r?.startsWith("ROLE_") ? r : `ROLE_${r}`));
  }, [user?.roles]);

  const isAlready = currentRoles.includes(requestedRole);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLastResult(null);

    if (isAlready) {
      setError("Vous possédez déjà ce rôle.");
      return;
    }

    setLoading(true);
    try {
      const data = await roleChangeRequestService.createRequest(requestedRole);
      setLastResult(data);
      setSuccess(
        data?.status === "APPROVED"
          ? "Votre demande a été approuvée."
          : "Votre demande a bien été envoyée (en attente de traitement).",
      );
    } catch (err) {
      if (!err.response) {
        setError("Erreur de connexion au serveur. Vérifiez votre réseau.");
      } else {
        setError(
          err.response?.data?.message ||
            err.response?.data?.error ||
            "Impossible d'envoyer votre demande."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl fade-in">
      <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 md:p-8">
        <div className="flex items-center gap-3 mb-2">
          <ShieldAlert size={22} className="text-amber-500" aria-hidden />
          <h2 className="text-2xl font-bold text-slate-100 m-0">Demander un changement de rôle</h2>
        </div>

        <p className="text-slate-400 text-sm mb-8 mt-2">
          Les rôles avancés sont validés par l’administrateur via une demande.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="flex flex-col gap-2 mb-6 max-w-md">
            <label className="text-sm font-medium text-slate-300">Rôle demandé</label>
            <select
              className="w-full bg-black/30 border border-white/10 rounded-xl p-3 text-slate-200 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none transition-all"
              value={requestedRole}
              onChange={(e) => setRequestedRole(e.target.value)}
              disabled={loading}
            >
              {REQUESTABLE_ROLES.map((r) => (
                <option key={r.value} value={r.value}>
                  {r.label}
                </option>
              ))}
            </select>
          </div>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl flex items-center gap-3 text-sm mb-6" role="alert">
              <AlertCircle size={18} aria-hidden />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 p-4 rounded-xl flex items-center gap-3 text-sm mb-6" role="status" aria-live="polite">
              <CheckCircle2 size={18} aria-hidden />
              <span>{success}</span>
            </div>
          )}

          {lastResult?.id && (
            <div className="bg-blue-500/10 border border-blue-500/20 text-blue-400 p-4 rounded-xl flex items-center gap-3 text-sm mb-6" role="status" aria-live="polite">
              <Info size={18} aria-hidden />
              <span>
                Demande #{lastResult.id} • Statut: {lastResult.status}
              </span>
            </div>
          )}

          <button
            className="flex items-center justify-center gap-2 bg-amber-500 text-slate-950 font-bold px-6 py-3 rounded-xl transition-all hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed w-fit"
            disabled={loading || isAlready}
            type="submit"
          >
            {loading ? (
              <>
                <Loader2 size={18} className="animate-spin" aria-hidden />
                Envoi...
              </>
            ) : (
              "Envoyer la demande"
            )}
          </button>
        </form>
      </div>
    </div>
  );
}

