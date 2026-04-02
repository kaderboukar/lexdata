/** Données statiques landing (sans JSX) — icônes résolues côté page via `iconKey`. */
export const features = [
  {
    id: "corpus",
    iconKey: "library",
    title: "Corpus Exhaustif",
    desc: "Accédez à l'intégralité des lois, décrets et conventions (UEMOA/CEDEAO) centralisés en un seul endroit.",
    premium: false,
  },
  {
    id: "recherche",
    iconKey: "target",
    title: "Recherche Intelligente",
    desc: "Trouvez la jurisprudence ou l'article exact en quelques secondes grâce à notre moteur de recherche avancé.",
    premium: false,
  },
  {
    id: "reseau",
    iconKey: "users",
    title: "Réseau Professionnel",
    desc: "Développez votre clientèle et vos partenariats grâce à notre annuaire certifié d'experts juridiques.",
    premium: false,
  },
  {
    id: "ia",
    iconKey: "sparkles",
    title: "Synthèses par IA",
    desc: "Gagnez un temps précieux avec nos résumés simplifiés et fiches pratiques générés sur mesure.",
    premium: true,
  },
  {
    id: "veille",
    iconKey: "bellRing",
    title: "Veille Automatisée",
    desc: "Ne ratez aucune évolution légale. Recevez des alertes ciblées selon vos domaines de spécialité.",
    premium: true,
  },
  {
    id: "securise",
    iconKey: "folderLock",
    title: "Espace Sécurisé",
    desc: "Annotez les textes, sauvegardez vos dossiers et organisez votre travail en toute confidentialité.",
    premium: true,
  },
];

export const faqs = [
  {
    id: "official",
    q: "Les textes de loi sont-ils officiels et à jour ?",
    a: "Absolument. Nos équipes d'experts juridiques vérifient et mettent à jour la base de données quotidiennement à partir des journaux officiels.",
  },
  {
    id: "cancel",
    q: "Puis-je annuler mon abonnement Professionnel ?",
    a: "Oui, nos abonnements sont 100% sans engagement. Vous pouvez annuler, mettre en pause ou modifier votre forfait à tout moment.",
  },
  {
    id: "veille-how",
    q: "Comment fonctionne la veille automatisée ?",
    a: "Définissez vos mots-clés ou domaines de prédilection. Dès qu'un nouveau texte est publié, vous recevez une alerte immédiate.",
  },
  {
    id: "etudiants",
    q: "La plateforme est-elle adaptée aux étudiants ?",
    a: "Oui ! Le plan 'Découverte' est gratuit. Il permet aux étudiants d'accéder aux textes fondamentaux.",
  },
];

export const pricingPlans = [
  {
    id: "decouverte",
    name: "Découverte",
    priceType: "free",
    priceLabel: "Gratuit",
    desc: "Pour les étudiants et citoyens",
    features: [
      { id: "d1", text: "Textes fondamentaux" },
      { id: "d2", text: "Espace de travail annoté", muted: true },
    ],
    cta: { label: "Créer un compte", to: "/register", variant: "secondary" },
    featured: false,
    badge: null,
  },
  {
    id: "pro",
    name: "Professionnel",
    priceType: "monthly",
    amount: "15 000",
    currency: "FCFA",
    period: "/mois",
    desc: "Pour les avocats et juristes",
    features: [
      { id: "p1", text: "Accès illimité", bold: true },
      { id: "p2", text: "Veille juridique illimitée" },
    ],
    cta: { label: "Essai gratuit de 14 jours", to: "/register", variant: "primary" },
    featured: true,
    badge: "Le plus choisi",
  },
  {
    id: "entreprise",
    name: "Entreprise",
    priceType: "quote",
    priceLabel: "Sur devis",
    desc: "Pour cabinets et institutions",
    features: [
      { id: "e1", text: "Tout le plan Pro" },
      { id: "e2", text: "API dédiée" },
      { id: "e3", text: "Formation sur site" },
      { id: "e4", text: "SLA garanti" },
    ],
    cta: { label: "Nous contacter", to: "/contact", variant: "secondary" },
    featured: false,
    badge: null,
  },
];
