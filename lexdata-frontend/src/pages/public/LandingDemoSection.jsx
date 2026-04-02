import React from "react";
import { Search, BellRing, FolderLock, CheckCircle } from "lucide-react";

const DEMO_TABS = [
  { id: "recherche", label: "Recherche Avancée", icon: Search },
  { id: "veille", label: "Veille & Alertes", icon: BellRing },
  { id: "dossiers", label: "Espace de Travail", icon: FolderLock },
];

export default function LandingDemoSection({ activeDemoTab, setActiveDemoTab }) {
  return (
    <section className="demo-section container reveal">
      <div className="section-header">
        <h2>Découvrez LEXDATA de l&apos;intérieur</h2>
        <p>Une interface moderne et épurée pour dompter la complexité juridique</p>
      </div>
      <div className="demo-tabs" role="tablist" aria-label="Démonstration produit">
        {DEMO_TABS.map((tab) => {
          const TabIcon = tab.icon;
          const selected = activeDemoTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              id={`demo-tab-${tab.id}`}
              aria-selected={selected}
              className={`demo-tab ${selected ? "active" : ""}`}
              onClick={() => setActiveDemoTab(tab.id)}
            >
              <TabIcon size={18} /> {tab.label}
            </button>
          );
        })}
      </div>
      <div className="demo-window">
        <div className="mockup-header">
          <span className="dot dot-red" /><span className="dot dot-yellow" /><span className="dot dot-green" />
        </div>
        <div className="demo-content">
          {activeDemoTab === "recherche" && (
            <div className="demo-pane" role="tabpanel" aria-labelledby="demo-tab-recherche">
              <div className="demo-search-bar">
                <Search size={16} color="var(--text-muted)" />
                <span className="text-primary">Loi de finances 2026 | UEMOA</span>
              </div>
              <div className="demo-result-card">
                <div className="badge badge-success mb-2">Loi validée</div>
                <h4 className="mb-2">Loi N°2026-01 portant budget de l&apos;État</h4>
                <p className="text-muted">Promulguée le 15 Janvier 2026 • Ministère des Finances</p>
              </div>
              <div className="demo-result-card">
                <div className="badge badge-premium mb-2">Directive UEMOA</div>
                <h4 className="mb-2">Directive N°02/2026/CM/UEMOA</h4>
                <p className="text-muted">Relative à la transparence des relations financières</p>
              </div>
            </div>
          )}
          {activeDemoTab === "veille" && (
            <div className="demo-pane" role="tabpanel" aria-labelledby="demo-tab-veille">
              <div className="demo-alert-header">
                <h3>Vos alertes récentes</h3>
                <span className="demo-badge-new">3 Nouvelles</span>
              </div>
              <div className="demo-alert-item unread">
                <BellRing size={20} color="var(--accent)" className="mt-1" />
                <div>
                  <strong className="text-primary block mb-1">Droit du Travail</strong>
                  <span className="text-muted text-sm">Nouveau décret sur le télétravail publié ce matin.</span>
                </div>
                <span className="demo-time">Il y a 2h</span>
              </div>
              <div className="demo-alert-item">
                <CheckCircle size={20} color="var(--text-muted)" className="mt-1" />
                <div>
                  <strong className="text-secondary block mb-1">Droit des Affaires</strong>
                  <span className="text-muted text-sm">Mise à jour de l&apos;Acte Uniforme OHADA.</span>
                </div>
                <span className="demo-time">Hier</span>
              </div>
            </div>
          )}
          {activeDemoTab === "dossiers" && (
            <div className="demo-pane" role="tabpanel" aria-labelledby="demo-tab-dossiers">
              <div className="demo-folders-grid">
                <div className="demo-folder">
                  <FolderLock size={32} color="var(--accent)" />
                  <strong className="text-primary mt-2">Affaire S.G vs K.</strong>
                  <span className="text-muted text-sm">12 Textes annotés</span>
                </div>
                <div className="demo-folder">
                  <FolderLock size={32} color="var(--accent)" />
                  <strong className="text-primary mt-2">Audit Fiscal</strong>
                  <span className="text-muted text-sm">5 Lois de référence</span>
                </div>
                <div className="demo-folder new-folder">
                  <span className="plus-icon">+</span>
                  <strong className="text-primary mt-2">Nouveau Dossier</strong>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
