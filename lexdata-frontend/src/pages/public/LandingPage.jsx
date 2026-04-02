import React, { useMemo, useRef, useState } from "react";
import { Helmet } from "react-helmet-async";
import { Link, useNavigate } from "react-router-dom";
import { Search, BellRing, Scale, Scroll, Landmark, GraduationCap, Library, Target, Users, Sparkles, FolderLock } from "lucide-react";
import { useScrollReveal } from "../../hooks/useScrollReveal";
import { features as featuresData, faqs as faqsData } from "../../data/landingData";
import LandingFooter from "./LandingFooter";
import LandingDemoSection from "./LandingDemoSection";
import LandingPricingFaqCta from "./LandingPricingFaqCta";

const FEATURE_ICONS = {
  library: Library,
  target: Target,
  users: Users,
  sparkles: Sparkles,
  bellRing: BellRing,
  folderLock: FolderLock,
};

export default function LandingPage() {
  const pageRef = useRef(null);
  useScrollReveal(pageRef);
  const features = useMemo(() => featuresData, []);
  const faqs = useMemo(() => faqsData, []);
  const [searchQuery, setSearchQuery] = useState("");
  const [activeDemoTab, setActiveDemoTab] = useState("recherche");
  const [openFaq, setOpenFaq] = useState(null);
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) navigate(`/textes?q=${encodeURIComponent(searchQuery)}`);
  };

  return (
    <>
      <Helmet>
        <title>LexData — Le Droit UEMOA à Portée de Main</title>
        <meta name="description" content="LexData centralise le corpus juridique nigérien et UEMOA. Recherche avancée, veille automatisée, synthèses IA pour avocats et juristes." />
        <meta property="og:title" content="LexData — LegalTech UEMOA" />
        <meta property="og:description" content="La référence juridique de l'espace UEMOA/CEDEAO." />
      </Helmet>
      <div className="landing" ref={pageRef}>
        <section className="hero">
          <div className="hero-bg">
            <div className="hero-orb hero-orb-1" />
            <div className="hero-orb hero-orb-2" />
          </div>
          <div className="container hero-container">
            <div className="hero-content reveal">
              <div className="hero-badge">
                <span className="badge badge-premium">✨ La référence juridique UEMOA</span>
              </div>
              <h1 className="hero-title">
                Le Droit Africain à<br />
                <span className="hero-title-accent">Portée de Main</span>
              </h1>
              <p className="hero-subtitle">
                LexData centralise, analyse et met à jour l&apos;ensemble du corpus juridique. Gagnez des heures de recherche et soyez alerté des nouvelles lois avant tout le monde.
              </p>
              <form className="hero-search-form" onSubmit={handleSearch}>
                <div className="search-input-group">
                  <Search className="search-icon" size={20} />
                  <input type="text" placeholder="Rechercher une loi, un décret..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} aria-label="Recherche juridique" />
                  <button type="submit" className="btn btn-primary" disabled={!searchQuery.trim()}>Chercher</button>
                </div>
              </form>
              <div className="hero-trust">
                <div className="trust-item"><strong>10 000+</strong> Textes validés</div>
                <div className="trust-divider" />
                <div className="trust-item"><strong>Mise à jour</strong> Quotidienne</div>
                <div className="trust-divider" />
                <div className="trust-item"><strong>100%</strong> Sécurisé</div>
              </div>
            </div>
            <div className="hero-visual reveal" style={{ "--delay": "0.2s" }}>
              <div className="mockup-container">
                <div className="css-mockup">
                  <div className="mockup-header">
                    <span className="dot dot-red" />
                    <span className="dot dot-yellow" />
                    <span className="dot dot-green" />
                  </div>
                  <div className="mockup-body">
                    <div className="mockup-sidebar" />
                    <div className="mockup-content">
                      <div className="mockup-line skeleton-title" />
                      <div className="mockup-line skeleton-text" />
                      <div className="mockup-line skeleton-text short" />
                      <div className="mockup-cards">
                        <div className="mockup-card" />
                        <div className="mockup-card" />
                      </div>
                    </div>
                  </div>
                </div>
                <div className="mockup-floating-badge glass-card">
                  <BellRing size={18} color="var(--accent)" />
                  <span>Nouvelle loi promulguée</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="social-proof-section container reveal">
          <p className="social-proof-title">Approuvé par les institutions de référence</p>
          <div className="logos-grid">
            <span><Scale size={24} color="var(--text-muted)" /> Barreau du Niger</span>
            <span><Scroll size={24} color="var(--text-muted)" /> Chambre des Notaires</span>
            <span><Landmark size={24} color="var(--text-muted)" /> Ministère de la Justice</span>
            <span><GraduationCap size={24} color="var(--text-muted)" /> Universités Partenaires</span>
          </div>
        </section>

        <section className="features-section container reveal">
          <div className="section-header">
            <h2>Pourquoi les professionnels nous choisissent ?</h2>
            <p>Une suite d&apos;outils pensée pour booster votre productivité au quotidien</p>
          </div>
          <div className="features-grid">
            {features.map((f, i) => {
              const Icon = FEATURE_ICONS[f.iconKey];
              return (
                <article
                  key={f.id}
                  className="feature-card glass-card"
                  style={{ "--stagger": `${i * 0.06}s` }}
                >
                  <div className="feature-header">
                    <div className="feature-icon" aria-hidden>
                      {Icon ? <Icon size={28} strokeWidth={1.75} /> : null}
                    </div>
                    {f.premium ? <span className="badge badge-premium">PRO</span> : null}
                  </div>
                  <h3>{f.title}</h3>
                  <p>{f.desc}</p>
                </article>
              );
            })}
          </div>
        </section>

        <LandingDemoSection activeDemoTab={activeDemoTab} setActiveDemoTab={setActiveDemoTab} />
        <LandingPricingFaqCta faqs={faqs} openFaq={openFaq} setOpenFaq={setOpenFaq} />
        <LandingFooter />
      </div>
    </>
  );
}

// ⚡ NEXT STEPS:
// 1. Enrich ContactPage (form + CRM) — route /contact déjà branchée
// 2. Replace CSS mockup with a real product screenshot
// 3. Add Google Analytics / Plausible on hero search submit
// 4. i18n FR/EN pour la meta Helmet si besoin export
