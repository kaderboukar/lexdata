import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Loader2, ShieldAlert, AlertTriangle, CheckCircle,
  XCircle, ShieldQuestion, Mail, Database
} from "lucide-react";
import roleChangeRequestService from "../../api/roleChangeRequestService";
import toast from "react-hot-toast";

// Utilitaires de rendu pour les statuts
const STATUS_UI = {
  PENDING: { label: "En attente", color: "#f59e0b", bg: "rgba(245, 158, 11, 0.15)", icon: ShieldQuestion },
  APPROVED: { label: "Approuvée", color: "#10b981", bg: "rgba(16, 185, 129, 0.15)", icon: CheckCircle },
  REJECTED: { label: "Rejetée", color: "#ef4444", bg: "rgba(239, 68, 68, 0.15)", icon: XCircle },
};

export default function RoleChangeRequestsManager() {
  const queryClient = useQueryClient();

  // État de la modale de rejet personnalisé
  const [rejectModal, setRejectModal] = useState({ isOpen: false, requestId: null, reason: "" });

  // 1. CHARGEMENT
  const { data: requests = [], isLoading, isError } = useQuery({
    queryKey: ["role-requests-admin"],
    queryFn: () => roleChangeRequestService.listAllAdmin(),
  });

  // 2. MUTATIONS
  const approveMutation = useMutation({
    mutationFn: (id) => roleChangeRequestService.approveAdmin(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["role-requests-admin"] });
      toast.success("Demande approuvée avec succès.");
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || err.response?.data?.error || "Erreur lors de l’approbation.");
    },
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }) => roleChangeRequestService.rejectAdmin(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["role-requests-admin"] });
      toast.success("Demande rejetée.");
      closeRejectModal();
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || err.response?.data?.error || "Erreur lors du rejet.");
    },
  });

  // 3. HANDLERS
  const handleApprove = (id, userName) => {
    if (window.confirm(`Voulez-vous vraiment approuver la demande de ${userName} ?`)) {
      approveMutation.mutate(id);
    }
  };

  const openRejectModal = (id) => {
    setRejectModal({ isOpen: true, requestId: id, reason: "" });
  };

  const closeRejectModal = () => {
    setRejectModal({ isOpen: false, requestId: null, reason: "" });
  };

  const confirmReject = (e) => {
    e.preventDefault();
    if (!rejectModal.reason.trim()) return toast.error("Le motif est obligatoire.");
    rejectMutation.mutate({ id: rejectModal.requestId, reason: rejectModal.reason });
  };

  const pendingCount = requests.filter(r => r.status === "PENDING").length;

  return (
    <div className="max-w-7xl mx-auto fade-in">

      {/* 🌟 MODALE DE REJET PERSONNALISÉE (Remplace window.prompt) */}
      {rejectModal.isOpen && (
        <div
          className="fixed inset-0 bg-black/80 backdrop-blur-sm flex justify-center items-center z-[9999] p-4 animate-in fade-in duration-200"
          onClick={closeRejectModal}
        >
          <div
            className="bg-slate-900 border border-white/10 p-8 max-w-md w-full rounded-2xl flex flex-col items-center text-center shadow-2xl animate-in zoom-in-95 duration-200"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="w-16 h-16 rounded-full flex justify-center items-center mb-6 bg-red-500/10 text-red-400">
              <XCircle size={36} aria-hidden />
            </div>
            <h3 className="text-xl font-bold text-slate-100 mb-2">Rejeter la demande</h3>
            <p className="text-slate-400 text-sm mb-6">
              Veuillez indiquer le motif du rejet. Ce message sera conservé dans le dossier de l'utilisateur.
            </p>

            <form onSubmit={confirmReject} className="w-full flex flex-col gap-6">
              <textarea
                className="w-full bg-black/30 border border-white/10 rounded-lg p-3 text-sm text-slate-200 placeholder-slate-500 focus:border-red-500 focus:ring-1 focus:ring-red-500 outline-none resize-y"
                rows="3"
                placeholder="Ex: Numéro de toque invalide, document manquant..."
                value={rejectModal.reason}
                onChange={(e) => setRejectModal({ ...rejectModal, reason: e.target.value })}
                required
                autoFocus
              />
              <div className="flex gap-4 w-full">
                <button
                  type="button"
                  className="flex-1 py-3 rounded-xl font-bold transition-all bg-white/5 border border-white/10 text-slate-200 hover:bg-white/10"
                  onClick={closeRejectModal}
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  className="flex-1 py-3 rounded-xl font-bold transition-all bg-red-500 text-white hover:bg-red-400 disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center gap-2"
                  disabled={rejectMutation.isPending || !rejectModal.reason.trim()}
                >
                  {rejectMutation.isPending && <Loader2 size={18} className="animate-spin" aria-hidden />}
                  {rejectMutation.isPending ? "Traitement..." : "Confirmer le rejet"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* HEADER DE LA PAGE */}
      <header className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-6 mb-8 flex flex-col md:flex-row justify-between items-center gap-6">
        <div className="flex items-center gap-3">
          <ShieldAlert size={24} className="text-red-500" aria-hidden />
          <h2 className="text-xl font-bold text-slate-100 m-0">Accréditations & Rôles</h2>
        </div>
        <div className="flex gap-3">
          <div className="bg-white/5 border border-white/10 text-slate-300 px-4 py-2 rounded-lg text-sm font-medium">
            {requests.length} Demande(s) au total
          </div>
          {pendingCount > 0 && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-500 px-4 py-2 rounded-lg text-sm font-bold">
              {pendingCount} En attente
            </div>
          )}
        </div>
      </header>

      {/* TABLEAU */}
      <div className="overflow-x-auto bg-slate-900/50 border border-white/10 rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement des requêtes...</p>
          </div>
        ) : isError ? (
          <div className="p-10 text-center">
            <AlertTriangle size={40} className="mx-auto text-red-400 mb-4" aria-hidden />
            <p className="text-red-400">Impossible de charger les demandes. Vérifiez vos droits.</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse min-w-[800px]">
            <thead>
              <tr>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 w-[35%]">
                  Utilisateur
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Rôle Demandé
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Statut
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap text-right">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {requests.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-center p-8 text-slate-400">
                    <Database size={40} className="mx-auto mb-3 opacity-30" aria-hidden /> Aucune demande de changement de rôle.
                  </td>
                </tr>
              ) : (
                requests.map((r) => {
                  const isPending = r.status === "PENDING";
                  const statusInfo = STATUS_UI[r.status] || STATUS_UI.PENDING;
                  const StatusIcon = statusInfo.icon;
                  const displayName = r.firstName ? `${r.firstName} ${r.lastName}` : r.username;

                  return (
                    <tr key={r.id} className="hover:bg-white/5 transition-colors">
                      <td className="p-4 border-b border-white/5 align-middle">
                        <div className="flex items-center gap-4">
                          <div className="w-10 h-10 rounded-full bg-red-500/15 text-red-500 flex items-center justify-center font-bold text-lg shrink-0">
                            {r.firstName?.charAt(0) || r.username?.charAt(0) || "U"}
                          </div>
                          <div className="min-w-0">
                            <div className="font-bold text-slate-100 text-base mb-0.5">{displayName}</div>
                            <div className="text-xs text-slate-400">
                              @{r.username} • ID: {r.userId}
                            </div>
                          </div>
                        </div>
                      </td>

                      <td className="p-4 border-b border-white/5 align-middle">
                        <span className="inline-block bg-white/10 border border-white/10 text-slate-300 px-2 py-1 rounded text-xs font-mono">
                          {r.requestedRole}
                        </span>
                      </td>

                      <td className="p-4 border-b border-white/5 align-middle">
                        <div className="flex flex-col items-start gap-1">
                          <span className="inline-flex items-center gap-2 text-xs px-3 py-1.5 rounded-full font-bold tracking-wide" style={{ background: statusInfo.bg, color: statusInfo.color }}>
                            <StatusIcon size={14} aria-hidden /> {statusInfo.label}
                          </span>
                          {r.status === "REJECTED" && r.rejectionReason && (
                            <div className="text-xs text-slate-400 mt-1 max-w-xs truncate" title={r.rejectionReason}>
                              Motif: {r.rejectionReason}
                            </div>
                          )}
                        </div>
                      </td>

                      <td className="p-4 border-b border-white/5 align-middle text-right">
                        {isPending ? (
                          <div className="flex gap-2 justify-end">
                            <button
                              className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg border border-red-500/30 text-red-500 hover:bg-red-500/10 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                              disabled={approveMutation.isPending || rejectMutation.isPending}
                              onClick={() => openRejectModal(r.id)}
                              type="button"
                            >
                              <XCircle size={16} aria-hidden /> Rejeter
                            </button>
                            <button
                              className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-red-500 text-white hover:bg-red-400 transition-colors text-sm font-medium ml-2 disabled:opacity-50 disabled:cursor-not-allowed"
                              disabled={approveMutation.isPending || rejectMutation.isPending}
                              onClick={() => handleApprove(r.id, displayName)}
                              type="button"
                            >
                              <CheckCircle size={16} aria-hidden /> Approuver
                            </button>
                          </div>
                        ) : (
                          <span className="text-slate-400 text-sm italic">Traitée</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}