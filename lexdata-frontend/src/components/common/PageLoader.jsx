import React from "react";

export function PageLoader({ fullscreen = false, label = "Chargement..." }) {
  return (
    <div className={`page-loader${fullscreen ? " is-fullscreen" : ""}`} aria-busy="true" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <div className="page-loader__text">{label}</div>
    </div>
  );
}

export default PageLoader;
