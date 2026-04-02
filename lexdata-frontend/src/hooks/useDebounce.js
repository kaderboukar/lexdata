import { useState, useEffect } from "react";

/**
 * Hook qui retarde la mise à jour d'une valeur.
 * Parfait pour les barres de recherche pour éviter de faire un appel API à chaque lettre tapée.
 */
export default function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    // Met en place un timer
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Si l'utilisateur re-tape avant la fin du délai, on annule le timer précédent
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}
