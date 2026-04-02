import React from "react";
import { Link } from "react-router-dom";
import { Check, ChevronDown } from "lucide-react";
import { pricingPlans } from "../../data/landingData";

export default function LandingPricingFaqCta({ faqs, openFaq, setOpenFaq }) {
  return (
    <>
      <section className="pricing-section container reveal" id="tarifs">
        <div className="section-header"><h2>Des plans adaptés à votre pratique</h2></div>
        <div className="pricing-grid">
          {pricingPlans.map((plan) => (
            <div key={plan.id} className={`pricing-card glass-card ${plan.featured ? "pricing-featured" : ""}`}>
              {plan.badge ? <div className="featured-badge">{plan.badge}</div> : null}
              <h3>{plan.name}</h3>
              <div className="price">
                {plan.priceType === "free" || plan.priceType === "quote" ? (
                  <span className="amount">{plan.priceLabel}</span>
                ) : (
                  <>
                    <span className="amount">{plan.amount}</span>
                    <span className="currency">{plan.currency}</span>
                    <span className="period">{plan.period}</span>
                  </>
                )}
              </div>
              <p className="pricing-desc">{plan.desc}</p>
              <ul className="pricing-features-list">
                {plan.features.map((item) => (
                  <li key={item.id} className={item.muted ? "text-muted" : ""}>
                    <Check size={18} className="check-icon" />
                    {item.bold ? <strong>{item.text}</strong> : item.text}
                  </li>
                ))}
              </ul>
              <Link to={plan.cta.to} className={`btn btn-full ${plan.cta.variant === "primary" ? "btn-primary" : "btn-secondary"}`}>
                {plan.cta.label}
              </Link>
            </div>
          ))}
        </div>
      </section>

      <section className="cta-section reveal">
        <div className="container">
          <div className="cta-card glass-card">
            <h2>Prêt à transformer votre pratique juridique ?</h2>
            <p>Rejoignez +500 professionnels du droit qui font confiance à LexData.</p>
            <div className="cta-actions">
              <Link to="/register" className="btn btn-primary">Démarrer gratuitement</Link>
              <Link to="/textes" className="btn btn-secondary">Explorer la base</Link>
            </div>
          </div>
        </div>
      </section>

      <section className="faq-section container reveal">
        <div className="section-header"><h2>Questions Fréquentes</h2></div>
        <div className="faq-accordion">
          {faqs.map((faq) => {
            const expanded = openFaq === faq.id;
            return (
              <article key={faq.id} className={`faq-item glass-card ${expanded ? "active" : ""}`} aria-expanded={expanded}>
                <button type="button" className="faq-item-trigger" onClick={() => setOpenFaq(expanded ? null : faq.id)} aria-controls={`faq-answer-${faq.id}`} id={`faq-question-${faq.id}`}>
                  <div className="faq-question">
                    <h3>{faq.q}</h3>
                    <ChevronDown size={20} className={`faq-icon ${expanded ? "rotated" : ""}`} aria-hidden="true" />
                  </div>
                </button>
                <div className="faq-answer" id={`faq-answer-${faq.id}`} role="region" aria-labelledby={`faq-question-${faq.id}`}>
                  <p>{faq.a}</p>
                </div>
              </article>
            );
          })}
        </div>
      </section>
    </>
  );
}
