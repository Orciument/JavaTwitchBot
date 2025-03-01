import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from "path"

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {target: "esnext"},
  resolve: {
    alias: {
      "@shadcn": path.resolve(__dirname, "./@shadcn"),
      "/@shadcn": path.resolve(__dirname, "./@shadcn"),
    },
  },
})
