import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import useAuthStore from "../store/useAuthStore";

const AuthContext = createContext(null);

/**
 * Pont React : expose la même API qu'avant pour les guards, mais la source de vérité
 * est useAuthStore (persist lexdata-auth). LoginPage met à jour le store, pas ce contexte local.
 */
export function AuthProvider({ children }) {
  const token = useAuthStore((s) => s.token);
  const user = useAuthStore((s) => s.user);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const storeLogout = useAuthStore((s) => s.logout);

  const [hasHydrated, setHasHydrated] = useState(() => useAuthStore.persist.hasHydrated());

  useEffect(() => {
    const unsub = useAuthStore.persist.onFinishHydration(() => {
      setHasHydrated(true);
    });
    if (useAuthStore.persist.hasHydrated()) {
      setHasHydrated(true);
    }
    return unsub;
  }, []);

  const isLoading = !hasHydrated;

  const login = useCallback(async () => {
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.warn(
        "[AuthContext] login() est obsolète : appelez authService.login puis useAuthStore.getState().login(user, token, refresh).",
      );
    }
    return false;
  }, []);

  const logout = useCallback(async () => {
    await storeLogout();
  }, [storeLogout]);

  const refreshToken = useCallback(async () => {
    return Boolean(useAuthStore.getState().token);
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      isLoading,
      isAuthenticated,
      login,
      logout,
      refreshToken,
    }),
    [user, token, isLoading, isAuthenticated, login, logout, refreshToken],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within <AuthProvider />");
  return ctx;
}
