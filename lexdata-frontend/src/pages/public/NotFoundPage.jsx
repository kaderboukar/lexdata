import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function NotFoundPage() {
  const { isAuthenticated } = useAuth();
  const to = isAuthenticated ? "/dashboard" : "/";
  const label = isAuthenticated ? "Retour au dashboard" : "Retour à l'accueil";

  return (
    <div className="container text-center py-5 scale-in notfound">
      <div className="notfound__bg" aria-hidden="true" />
      <h1 className="notfound__title">404</h1>
      <h2 className="text-primary mb-3">Hors de notre juridiction</h2>
      <p className="text-muted mb-4">La page que vous cherchez n'existe pas ou a été déplacée.</p>
      <Link to={to} className="btn btn-primary">
        {label}
      </Link>
    </div>
  );
}

