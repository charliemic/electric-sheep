import { defineConfig } from 'astro/config';
import tailwind from '@astrojs/tailwind';

// https://astro.build/config
// Base path for versioned deployments (set by CI/CD)
// Empty string for root/latest deployment, or '/v1.0.0' for versioned
const base = process.env.ASTRO_BASE || '';

export default defineConfig({
  base: base,
  integrations: [tailwind()],
  output: 'static',
  build: {
    assets: 'assets'
  }
});

