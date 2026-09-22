import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

const backendHost = process.env.BACKEND_HOST ?? 'localhost'
const backendPort = process.env.BACKEND_PORT ?? '8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Mirrors the nginx setup of the docker image, so frontend and api share one origin
    proxy: {
      '/api': `http://${backendHost}:${backendPort}`,
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
})
