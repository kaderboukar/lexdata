import React from 'react';
import { AlertTriangle, RefreshCcw } from 'lucide-react';

export class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        // Met à jour l'état pour afficher l'UI de repli au prochain rendu.
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        // Tu peux aussi enregistrer l'erreur dans un service comme Sentry ici
        console.error("Erreur critique interceptée :", error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            // UI de secours (Stylisée avec notre Glassmorphism)
            return (
                <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', padding: '1rem', backgroundColor: 'var(--bg-body)' }}>
                    <div className="glass-card fade-in" style={{ padding: '3rem 2rem', maxWidth: '500px', textAlign: 'center' }}>
                        <div style={{ background: 'rgba(239, 68, 68, 0.1)', width: '80px', height: '80px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.5rem auto' }}>
                            <AlertTriangle size={40} className="text-danger" />
                        </div>

                        <h2 className="text-primary" style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Oups, un incident est survenu</h2>
                        <p className="text-muted" style={{ marginBottom: '1.5rem' }}>
                            Notre système a rencontré un problème inattendu. Pas de panique, l'erreur a été isolée.
                        </p>

                        {/* Affichage technique de l'erreur pour aider au debug */}
                        <div style={{ background: 'rgba(0,0,0,0.3)', padding: '1rem', borderRadius: '8px', marginBottom: '2rem', textAlign: 'left', overflowX: 'auto' }}>
                            <code style={{ color: '#f87171', fontSize: '0.8rem' }}>
                                {this.state.error?.toString()}
                            </code>
                        </div>

                        <button
                            className="btn btn-primary btn-full"
                            onClick={() => window.location.reload()}
                        >
                            <RefreshCcw size={18} /> Recharger l'application
                        </button>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}