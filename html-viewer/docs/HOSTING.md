# Hosting Guide

This document outlines hosting options for the HTML Viewer & Converter tool.

## Hosting Options

### 1. GitHub Pages (Recommended for Free Static Hosting)

**Pros:**
- ✅ Free
- ✅ Easy setup
- ✅ Automatic deployment from GitHub
- ✅ Custom domain support
- ✅ HTTPS included

**Setup:**
1. Build the project: `npm run build`
2. Push to GitHub
3. Go to repository Settings → Pages
4. Select source branch (e.g., `main`) and folder (`dist`)
5. Done! Site available at `https://<username>.github.io/html-viewer`

**Deployment:**
```bash
npm run build
# Push dist/ folder to gh-pages branch or use GitHub Actions
```

### 2. Railway

**Pros:**
- ✅ Already in ecosystem (mentioned in docs)
- ✅ Simple deployment
- ✅ Free tier available ($5 credit/month)
- ✅ Auto-deploy from Git
- ✅ HTTPS included

**Setup:**
1. Connect GitHub repository to Railway
2. Railway auto-detects Astro project
3. Deploy automatically
4. Get URL: `https://<project-name>.railway.app`

**Configuration:**
- Build command: `npm run build`
- Output directory: `dist`
- Start command: Not needed (static site)

### 3. Netlify

**Pros:**
- ✅ Free tier
- ✅ One-click deployment
- ✅ Automatic builds from Git
- ✅ Form handling (if needed)
- ✅ Edge functions

**Setup:**
1. Connect GitHub repository
2. Build settings:
   - Build command: `npm run build`
   - Publish directory: `dist`
3. Deploy!

**URL:** `https://<project-name>.netlify.app`

### 4. Vercel

**Pros:**
- ✅ Free tier
- ✅ Excellent performance
- ✅ Automatic deployments
- ✅ Edge network

**Setup:**
1. Install Vercel CLI: `npm i -g vercel`
2. Run: `vercel`
3. Follow prompts

**URL:** `https://<project-name>.vercel.app`

### 5. Supabase (If Static Hosting Available)

**Pros:**
- ✅ Already in ecosystem
- ✅ Integrated with existing infrastructure

**Note:** Check if Supabase offers static hosting. If not, use one of the above options.

## Deployment Steps

### Build for Production

```bash
# Install dependencies
npm install

# Build static site
npm run build

# Preview locally
npm run preview
```

### Environment Variables

No environment variables needed for basic functionality. All dependencies are loaded via CDN in the generated HTML.

### Custom Domain

All hosting options support custom domains:
- GitHub Pages: Settings → Pages → Custom domain
- Railway: Project Settings → Domains
- Netlify: Site Settings → Domain Management
- Vercel: Project Settings → Domains

## CI/CD Integration

### GitHub Actions (for GitHub Pages)

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to GitHub Pages

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: npm install
      - run: npm run build
      - uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./dist
```

## Recommended Setup

For this project, I recommend:

1. **Development/Testing**: GitHub Pages (free, easy)
2. **Production**: Railway (if already using it) or Netlify (best free tier)

Both are simple to set up and maintain.

