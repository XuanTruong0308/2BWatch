import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { fileURLToPath } from "node:url";
import path from "node:path";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    outDir: path.resolve(__dirname, "../target/frontend-dist"),
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/ws": {
        target: "http://localhost:8080",
        changeOrigin: false,
        ws: true,
      },
      "/perform-login": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/logout": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/oauth2": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/login": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/payment": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/uploads": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
      "/assets": {
        target: "http://localhost:8080",
        changeOrigin: false,
      },
    },
  },
});
