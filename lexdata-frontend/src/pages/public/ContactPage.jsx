import React from "react";
import { Link } from "react-router-dom";
import "./ContactPage.css";

/** Page contact minimale — CTA Entreprise (Landing). */
export default function ContactPage() {
  return (
    <main className="container main-content">
      <div className="glass-card contact-page-card">
        <h1 className="text-primary">Contact</h1>
        <p className="text-secondary contact-page-lead">
          Écrivez-nous pour un devis Entreprise, une intégration API ou une démonstration.
        </p>
        <p className="text-muted contact-page-mail">
          <a href="mailto:contact@lexdata.ne" className="text-accent">contact@lexdata.ne</a>
        </p>
        <Link to="/" className="btn btn-secondary">Retour à l’accueil</Link>
      </div>
    </main>
  );
}
