import React from "react";
import { Helmet } from "react-helmet-async";
import Veille from "./Veille";

export default function VeillePage() {
  return (
    <>
      <Helmet>
        <title>Veille &amp; alertes — LexData</title>
        <meta
          name="description"
          content="Abonnez-vous aux domaines et types de textes juridiques. Consultez vos alertes et ouvrez les textes officiels."
        />
      </Helmet>
      <Veille />
    </>
  );
}

