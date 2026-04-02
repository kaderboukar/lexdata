/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  // Le reset global vit dans index.css (design system LexData) — évite les doublons avec Preflight.
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      keyframes: {
        shake: {
          "0%, 100%": { transform: "translateX(0)" },
          "20%, 60%": { transform: "translateX(-5px)" },
          "40%, 80%": { transform: "translateX(5px)" },
        },
      },
      animation: {
        shake: "shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both",
      },
    },
  },
  plugins: [],
};
