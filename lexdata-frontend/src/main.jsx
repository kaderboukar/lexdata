import React, { StrictMode, useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import { HelmetProvider } from "react-helmet-async";
import "./index.css";
/* Landing : CSS chargé dès l’entrée (évite tout décalage si chunk lazy) + visible après rebuild Docker */
import "./pages/public/LandingPage.css";
import App from "./App.jsx";

class GlobalErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Non-obvious choice: keep logging minimal here to avoid leaking sensitive data in production.
    // You can wire this to Sentry/Datadog later.
    // eslint-disable-next-line no-console
    console.error("GlobalErrorBoundary:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            minHeight: "100vh",
            display: "grid",
            placeItems: "center",
            background: "var(--bg-body)",
            color: "var(--text-primary)",
            padding: "2rem",
            textAlign: "center",
          }}
        >
          <div className="lexdata-card lex-card" style={{ maxWidth: 520, width: "100%" }}>
            <h1 style={{ fontSize: "1.25rem", fontWeight: 800, marginBottom: "0.75rem" }}>
              Une erreur est survenue
            </h1>
            <p style={{ color: "var(--text-secondary)", lineHeight: 1.7 }}>
              Rechargez la page. Si le problème persiste, contactez le support LexData.
            </p>
            <div style={{ height: 16 }} />
            <button
              className="lexdata-btn-primary lex-btn-primary"
              type="button"
              onClick={() => window.location.reload()}
            >
              Recharger
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

function LoadingSplash() {
  // Minimal, no layout shift: fixed viewport layer.
  return (
    <div
      aria-busy="true"
      aria-live="polite"
      style={{
        position: "fixed",
        inset: 0,
        display: "grid",
        placeItems: "center",
        background: "var(--bg-body)",
        color: "var(--text-primary)",
      }}
    >
      <div style={{ display: "grid", gap: 14, placeItems: "center" }}>
        <div className="spinner" aria-hidden="true" style={{ width: 40, height: 40, borderWidth: 4 }} />
        <div style={{ fontSize: 12, letterSpacing: "0.08em", color: "var(--text-secondary)" }}>
          LEXDATA
        </div>
      </div>
    </div>
  );
}

function Root() {
  const [fontsReady, setFontsReady] = useState(false);

  useEffect(() => {
    let cancelled = false;

    // Font loading gate to prevent FOUT/CLS (especially on legal reading surfaces).
    const ready = async () => {
      try {
        if (document?.fonts?.ready) {
          await document.fonts.ready;
        }
      } finally {
        if (!cancelled) setFontsReady(true);
      }
    };

    void ready();
    return () => {
      cancelled = true;
    };
  }, []);

  if (!fontsReady) return <LoadingSplash />;

  return (
    <GlobalErrorBoundary>
      <HelmetProvider>
        <App />
      </HelmetProvider>
    </GlobalErrorBoundary>
  );
}

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <Root />
  </StrictMode>,
);

// ⚡ NEXT STEP: Refactor `src/App.jsx` to add route-level motion transitions + layouts.
