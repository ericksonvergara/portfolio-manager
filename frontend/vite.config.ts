import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // In development the API is reached through nginx (docker compose) on the same origin,
    // so the HttpOnly refresh cookie works exactly as in production.
    proxy: {
      '/api': 'http://localhost:8081',
    },
  },
})
