import { useEffect } from "react";

/**
 * Révèle les éléments `.reveal` à l’intérieur d’un conteneur (refs React, pas document global).
 */
export function useScrollReveal(containerRef) {
  useEffect(() => {
    if (!containerRef?.current) return;
    const observer = new IntersectionObserver(
      (entries) =>
        entries.forEach((e) => {
          if (e.isIntersecting) e.target.classList.add("active");
        }),
      { threshold: 0.1, rootMargin: "0px 0px -50px 0px" },
    );
    const elements = containerRef.current.querySelectorAll(".reveal");
    elements.forEach((el) => observer.observe(el));
    return () => elements.forEach((el) => observer.unobserve(el));
  }, [containerRef]);
}
