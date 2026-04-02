import React from "react";
import { Link } from "react-router-dom";
import { Scale, Mail, Phone, MapPin } from "lucide-react";

const produitLinks = [
  { to: "/textes", label: "Base Juridique" },
  { to: "/veille", label: "Veille & Alertes" },
  { to: "/annuaire", label: "Annuaire des experts" },
  { to: "/#tarifs", label: "Tarifs" },
];

const legalLinks = [
  { to: "/cgu", label: "Conditions Générales" },
  { to: "/confidentialite", label: "Politique de confidentialité" },
  { to: "/mentions-legales", label: "Mentions légales" },
];

export default function LandingFooter() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-grid">
          <div className="footer-col footer-col-brand">
            <div className="footer-brand">
              <Scale size={28} color="var(--accent)" aria-hidden />
              <strong>LexData</strong>
            </div>
            <p className="footer-desc">
              La première plateforme LegalTech centralisant le droit nigérien et de l&apos;espace UEMOA/CEDEAO.
            </p>
          </div>

          <nav className="footer-col footer-col-nav" aria-label="Navigation produit">
            <h4>Produit</h4>
            <ul className="footer-links">
              {produitLinks.map(({ to, label }) => (
                <li key={to + label}>
                  <Link to={to}>{label}</Link>
                </li>
              ))}
            </ul>
          </nav>

          <nav className="footer-col footer-col-nav" aria-label="Informations légales">
            <h4>Légal</h4>
            <ul className="footer-links">
              {legalLinks.map(({ to, label }) => (
                <li key={to}>
                  <Link to={to}>{label}</Link>
                </li>
              ))}
            </ul>
          </nav>

          <div className="footer-col footer-col-contact">
            <h4>Contact</h4>
            <ul className="footer-contact-list">
              <li className="contact-item">
                <MapPin size={16} aria-hidden />
                <span>Plateau, Niamey, Niger</span>
              </li>
              <li className="contact-item">
                <Phone size={16} aria-hidden />
                <span>+227 00 00 00 00</span>
              </li>
              <li className="contact-item">
                <Mail size={16} aria-hidden />
                <span>contact@lexdata.ne</span>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <p>© {new Date().getFullYear()} LexData. Tous droits réservés.</p>
          <div className="footer-socials" aria-label="Réseaux sociaux">
            <a href="https://linkedin.com/company/lexdata" target="_blank" rel="noopener noreferrer" aria-label="LinkedIn">
              IN
            </a>
            <a href="https://twitter.com/lexdata" target="_blank" rel="noopener noreferrer" aria-label="Twitter">
              TW
            </a>
            <a href="https://facebook.com/lexdata" target="_blank" rel="noopener noreferrer" aria-label="Facebook">
              FB
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
