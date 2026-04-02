import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Search, Shield, Mail, ShieldAlert, Loader2, ArrowLeft, Save, CheckCircle2, Circle } from "lucide-react";
import adminService from "../../api/adminService";
import useAuthStore from "../../store/useAuthStore";

const AVAILABLE_ROLES = [
  { id: "ROLE_USER", label: "Citoyen / Étudiant", desc: "Accès basique en lecture", color: "badge-brouillon" },
  { id: "ROLE_JURISTE", label: "Juriste", desc: "Accès professionnel complet", color: "badge-brouillon" },
  { id: "ROLE_AVOCAT", label: "Avocat", desc: "Accès professionnel et annuaire", color: "badge-premium" },
  { id: "ROLE_AGENT_ADMIN", label: "Agent Administratif", desc: "Gestion des textes et publications", color: "badge-danger" },
  { id: "ROLE_SUPER_ADMIN", label: "Super Administrateur", desc: "Contrôle total sur le système", color: "badge-danger" },
];

export default function UsersManager() {
  const queryClient = useQueryClient();
  const currentUser = useAuthStore((state) => state.user);
  const isSuperAdmin = useAuthStore((state) => state.isSuperAdmin());

  const [view, setView] = useState("list");
  const [searchTerm, setSearchTerm] = useState("");
  const [editingUser, setEditingUser] = useState(null);
  const [selectedRole, setSelectedRole] = useState("");

  // 1. DATA FETCHING
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin-users"],
    queryFn: () => adminService.getAllUsers({ size: 100 }),
  });

  const users = data?.content || data || [];

  const filteredUsers = users.filter((u) =>
    u.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.username?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 2. MUTATION
  const updateRolesMutation = useMutation({
    mutationFn: ({ userId, role }) => adminService.updateUserRole(userId, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
      setView("list");
      setEditingUser(null);
    },
  });

  // 3. HANDLERS
  const handleEditClick = (user) => {
    setEditingUser(user);
    setSelectedRole(user.roles?.[0] || "ROLE_USER");
    setView("edit");
  };

  const handleSaveRoles = () => {
    if (!selectedRole) return alert("Veuillez sélectionner un rôle.");
    updateRolesMutation.mutate({ userId: editingUser.id, role: selectedRole });
  };

  // ==========================================
  // RENDU : VUE ÉDITION DE RÔLE
  // ==========================================
  if (view === "edit" && editingUser) {
    return (
      <div className="max-w-4xl mx-auto fade-in">
        <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-8">

          <header className="flex items-center gap-4 mb-8 border-b border-white/10 pb-6">
            <button
              className="p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 transition-colors"
              onClick={() => setView("list")}
              type="button"
              aria-label="Retour à la liste"
            >
              <ArrowLeft size={24} aria-hidden />
            </button>
            <h2 className="text-2xl font-bold text-slate-100 m-0">Gérer les accès de l'utilisateur</h2>
          </header>

          <div className="bg-black/20 border border-white/10 rounded-xl p-6 flex items-center gap-6 mb-8">
            <div className="w-16 h-16 rounded-full bg-gradient-to-br from-red-500 to-red-700 text-white flex shrink-0 items-center justify-center text-2xl font-extrabold shadow-lg">
              {editingUser.firstName?.charAt(0) || editingUser.username?.charAt(0) || "U"}
            </div>
            <div className="min-w-0">
              <h3 className="text-xl font-bold text-slate-100 m-0 mb-1">
                {editingUser.firstName} {editingUser.lastName}
              </h3>
              <p className="text-slate-400 m-0 text-sm">@{editingUser.username}</p>
              <div className="flex items-center gap-2 text-sm text-slate-300 mt-2">
                <Mail size={14} aria-hidden /> {editingUser.email}
              </div>
            </div>
          </div>

          <h4 className="text-slate-100 font-bold mb-4">Niveau d'accréditation :</h4>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
            {AVAILABLE_ROLES.map((role) => {
              const isSelected = selectedRole === role.id;
              // Sécurité : On empêche un Super Admin de se retirer ses propres droits par erreur
              const isEditingSelf = currentUser?.id === editingUser.id;
              const locked = isEditingSelf && isSuperAdmin;
              const canSelectRole = !locked || role.id === "ROLE_SUPER_ADMIN";

              const baseCard =
                "border border-white/10 rounded-xl bg-white/5 p-5 cursor-pointer flex items-start gap-4 transition-all hover:bg-white/10 hover:border-white/20";
              const lockedCard = "opacity-50 cursor-not-allowed hover:bg-white/5 hover:border-white/10";
              const selectedCard = "border-red-500 bg-red-500/10";

              return (
                <div
                  key={role.id}
                  className={[
                    baseCard,
                    isSelected ? selectedCard : "",
                    !canSelectRole ? lockedCard : "",
                  ].join(" ")}
                  onClick={() => { if (canSelectRole) setSelectedRole(role.id); }}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => {
                    if (!canSelectRole) return;
                    if (e.key === "Enter" || e.key === " ") setSelectedRole(role.id);
                  }}
                >
                  <div className={`mt-0.5 ${isSelected ? "text-red-500" : "text-slate-500"}`}>
                    {isSelected ? <CheckCircle2 size={22} aria-hidden /> : <Circle size={22} aria-hidden />}
                  </div>
                  <div>
                    <div className={`font-bold mb-1 ${isSelected ? "text-red-500" : "text-slate-100"}`}>
                      {role.label}
                    </div>
                    <div className="text-xs text-slate-400">{role.desc}</div>
                    <div className="text-[10px] text-slate-500 font-mono mt-2 opacity-70">{role.id}</div>
                  </div>
                </div>
              );
            })}
          </div>

          {currentUser?.id === editingUser.id && isSuperAdmin && (
            <div className="bg-blue-500/10 border border-blue-500/20 text-blue-400 p-4 rounded-xl text-sm mb-6" role="alert">
              Vous modifiez votre propre compte. Par mesure de sécurité, vous ne pouvez pas vous retirer le rôle Super Administrateur.
            </div>
          )}

          <div className="flex justify-end gap-4 border-t border-white/10 pt-6">
            <button
              className="px-6 py-3 rounded-xl font-bold transition-all bg-white/5 border border-white/10 text-slate-200 hover:bg-white/10"
              onClick={() => setView("list")}
              type="button"
            >
              Annuler
            </button>
            <button
              className="px-8 py-3 rounded-xl font-bold transition-all bg-red-500 text-white hover:bg-red-400 disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center gap-2"
              onClick={handleSaveRoles}
              disabled={!isSuperAdmin || updateRolesMutation.isPending}
              type="button"
            >
              {updateRolesMutation.isPending ? (
                <>
                  <Loader2 size={18} className="animate-spin" aria-hidden /> Application en cours...
                </>
              ) : (
                <>
                  <Save size={18} aria-hidden /> Sauvegarder les accès
                </>
              )}
            </button>
          </div>

        </div>
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
            placeholder="Rechercher par nom, username ou email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="badge badge-premium px-4 py-2 text-sm">
          {filteredUsers.length} Utilisateur(s)
        </div>
      </header>

      <div className="overflow-x-auto bg-slate-900/50 border border-white/10 rounded-xl">
        {isLoading ? (
          <div className="p-10 text-center">
            <Loader2 size={40} className="animate-spin mx-auto text-red-500 mb-4" aria-hidden />
            <p className="text-slate-400">Chargement des utilisateurs...</p>
          </div>
        ) : isError ? (
          <div className="p-10 text-center">
            <ShieldAlert size={40} className="mx-auto text-red-400 mb-4" aria-hidden />
            <p className="text-red-400">Impossible de charger les utilisateurs. Vérifiez vos droits.</p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse min-w-[800px]">
            <thead>
              <tr>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Utilisateur
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Contact
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap">
                  Rôles Actifs
                </th>
                <th className="bg-black/40 p-4 text-slate-400 text-xs uppercase tracking-wider font-bold border-b border-white/10 whitespace-nowrap text-right">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-center p-8 text-slate-400">
                    Aucun utilisateur trouvé.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((u) => (
                  <tr key={u.id} className="transition-colors hover:bg-white/5">
                    <td className="p-4 border-b border-white/5 align-middle">
                      <div className="flex items-center gap-4">
                        <div className="w-10 h-10 rounded-full bg-red-500/15 text-red-500 flex items-center justify-center font-bold text-lg shrink-0">
                          {u.firstName?.charAt(0) || u.username?.charAt(0) || "U"}
                        </div>
                        <div className="min-w-0">
                          <div className="font-bold text-slate-100 text-base mb-0.5">
                            {u.firstName} {u.lastName}
                          </div>
                          <div className="text-xs text-slate-400">@{u.username}</div>
                        </div>
                      </div>
                    </td>
                    <td className="p-4 border-b border-white/5 align-middle">
                      <div className="flex items-center gap-2 text-sm text-slate-300">
                        <Mail size={14} aria-hidden /> {u.email}
                      </div>
                    </td>
                    <td className="p-4 border-b border-white/5 align-middle">
                      <div className="flex gap-2 flex-wrap">
                        {u.roles?.map((role) => {
                          const roleDef = AVAILABLE_ROLES.find((r) => r.id === role);
                          return (
                            <span key={role} className={`badge ${roleDef ? roleDef.color : "badge-brouillon"} text-xs px-2 py-0.5`}>
                              {roleDef ? roleDef.label : role}
                            </span>
                          );
                        })}
                      </div>
                    </td>
                    <td className="p-4 border-b border-white/5 align-middle text-right">
                      <button
                        className="flex items-center justify-end gap-2 px-3 py-1.5 bg-white/5 border border-white/10 rounded-lg text-sm text-slate-300 hover:bg-white/10 hover:text-white transition-colors inline-flex"
                        onClick={() => handleEditClick(u)}
                        title="Gérer les accès"
                        type="button"
                      >
                        <Shield size={14} aria-hidden /> Gérer
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}