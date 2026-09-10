import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        sidebar: {
          DEFAULT: "#1e2733",
          light: "#2b3644"
        }
      }
    }
  },
  plugins: []
} satisfies Config;
