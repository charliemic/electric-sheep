# Dashboard Hosting Options - Free Tier Analysis

**Date**: 2025-01-20  
**Purpose**: Research free hosting options for Fastify-based metrics dashboard

## Requirements

- **Node.js server** (Fastify, not static site)
- **Always-on or near always-on** (for dashboard availability)
- **Free tier** (or very low cost)
- **HTTPS included**
- **Easy deployment**

## Free Hosting Options Comparison

### 1. Render ⭐ **BEST FREE OPTION**

**Free Tier:**
- ✅ **750 hours/month** (enough for always-on)
- ✅ **512 MB RAM**
- ✅ **0.5 CPU**
- ✅ **HTTPS included**
- ✅ **Auto-deploy from Git**
- ✅ **Custom domain support**

**Limitations:**
- ⚠️ Spins down after 15 minutes of inactivity (free tier)
- ⚠️ Cold start takes ~30 seconds after spin-down
- ⚠️ No persistent storage (use external storage)

**Cost:** $0/month (free tier) or $7/month (always-on)

**Setup:**
1. Connect GitHub repository
2. Select "Web Service"
3. Build command: `cd scripts/metrics && npm install`
4. Start command: `cd scripts/metrics && npm start`
5. Environment: Node.js
6. Deploy!

**URL:** `https://<service-name>.onrender.com`

**Best For:** Free tier with enough hours for always-on (if you can accept cold starts)

---

### 2. Railway ⭐ **GOOD FOR QUICK SETUP**

**Free Tier:**
- ✅ **$5 credit/month** (included)
- ✅ **0.5 GB RAM, 1 vCPU**
- ✅ **1 project, 3 services**
- ✅ **HTTPS included**
- ✅ **Auto-deploy from Git**
- ✅ **No cold starts**

**Limitations:**
- ⚠️ **May not be enough for always-on** (credit runs out)
- ⚠️ Need to monitor usage
- ⚠️ May need to upgrade to Hobby plan ($5/month) for reliability

**Cost:** $0-5/month (free tier) or $5/month (Hobby plan recommended)

**Setup:**
1. Connect GitHub repository
2. Railway auto-detects Node.js
3. Set root directory: `scripts/metrics`
4. Deploy!

**URL:** `https://<project-name>.railway.app`

**Best For:** Quick setup, but may need paid plan for always-on

---

### 3. Fly.io ⭐ **GOOD ALTERNATIVE**

**Free Tier:**
- ✅ **3 shared VMs**
- ✅ **3 GB storage**
- ✅ **160 GB outbound data transfer**
- ✅ **HTTPS included**
- ✅ **Global edge network**
- ✅ **No cold starts**

**Limitations:**
- ⚠️ Shared resources (may be slower)
- ⚠️ CLI-based setup (more complex)
- ⚠️ Need to manage scaling

**Cost:** $0/month (free tier) or ~$5-10/month (dedicated instance)

**Setup:**
```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Launch app
cd scripts/metrics
fly launch

# Deploy
fly deploy
```

**URL:** `https://<app-name>.fly.dev`

**Best For:** Free tier with no cold starts, but CLI setup required

---

### 4. Vercel (Serverless Functions)

**Free Tier:**
- ✅ **100 GB bandwidth**
- ✅ **HTTPS included**
- ✅ **Auto-deploy from Git**
- ✅ **Edge network**

**Limitations:**
- ⚠️ **Serverless functions** (not ideal for always-on Fastify server)
- ⚠️ Cold starts on inactivity
- ⚠️ Function timeout limits (10s on free tier)
- ⚠️ Would need to restructure as serverless functions

**Cost:** $0/month (free tier)

**Best For:** Not ideal for Fastify server (designed for serverless)

---

### 5. Netlify (Serverless Functions)

**Free Tier:**
- ✅ **100 GB bandwidth**
- ✅ **HTTPS included**
- ✅ **Auto-deploy from Git**

**Limitations:**
- ⚠️ **Serverless functions** (not ideal for always-on Fastify server)
- ⚠️ Cold starts
- ⚠️ Function timeout limits
- ⚠️ Would need to restructure as serverless functions

**Cost:** $0/month (free tier)

**Best For:** Not ideal for Fastify server (designed for serverless)

---

### 6. GitHub Codespaces (Development Only)

**Free Tier:**
- ✅ **60 hours/month** (free for personal accounts)
- ✅ **2-core machine**
- ✅ **HTTPS via port forwarding**

**Limitations:**
- ⚠️ **Development only** (not for production)
- ⚠️ Limited hours
- ⚠️ Not always-on

**Cost:** $0/month (free tier)

**Best For:** Development/testing only, not production hosting

---

### 7. Cloudflare Workers (Not Suitable)

**Limitations:**
- ❌ **Not suitable for Fastify** (designed for edge functions)
- ❌ Would require complete rewrite
- ❌ No file system access

**Best For:** Not suitable for our use case

---

## Recommendation: **Render (Free Tier)**

### Why Render?

1. ✅ **750 hours/month** - Enough for always-on (if you can accept cold starts)
2. ✅ **Free tier** - No cost
3. ✅ **Easy setup** - Connect GitHub, deploy
4. ✅ **HTTPS included** - Automatic SSL
5. ✅ **Auto-deploy** - Push to deploy
6. ✅ **Custom domain** - Can add your domain

### Trade-offs

**Cold Starts:**
- Free tier spins down after 15 minutes of inactivity
- Cold start takes ~30 seconds
- **Solution**: Use a "keep-alive" ping service (free options available)
- Or upgrade to $7/month for always-on

**Storage:**
- No persistent storage on free tier
- **Solution**: Store pages in Git or external storage (Supabase, etc.)

### Setup Steps

1. **Create Render Account**
   - Sign up at https://render.com
   - Connect GitHub account

2. **Create Web Service**
   - New → Web Service
   - Connect repository
   - Settings:
     - **Name**: `electric-sheep-dashboard`
     - **Environment**: Node
     - **Build Command**: `cd scripts/metrics && npm install`
     - **Start Command**: `cd scripts/metrics && npm start`
     - **Root Directory**: `scripts/metrics`

3. **Environment Variables** (if needed)
   - Add any required env vars

4. **Deploy**
   - Click "Create Web Service"
   - Render builds and deploys automatically

5. **Keep-Alive (Optional)**
   - Use free service like https://uptimerobot.com
   - Ping your dashboard every 10 minutes
   - Prevents spin-down

### Alternative: Railway Hobby Plan ($5/month)

If you need always-on without cold starts:
- **Railway Hobby Plan**: $5/month
- No cold starts
- More reliable
- Still very affordable

## Cost Comparison

| Service | Free Tier | Always-On Free | Paid Always-On |
|---------|-----------|----------------|----------------|
| **Render** | ✅ 750 hrs/month | ⚠️ Cold starts | $7/month |
| **Railway** | ⚠️ $5 credit | ❌ No | $5/month |
| **Fly.io** | ✅ 3 VMs | ✅ Yes | $5-10/month |
| **Vercel** | ✅ Serverless | ⚠️ Cold starts | N/A |
| **Netlify** | ✅ Serverless | ⚠️ Cold starts | N/A |

## Final Recommendation

**For Free Hosting:**
1. **Render** (750 hours/month, cold starts acceptable)
2. **Fly.io** (3 VMs, no cold starts, CLI setup)

**For Always-On (Low Cost):**
1. **Railway Hobby** ($5/month, easiest setup)
2. **Render** ($7/month, more resources)

**Best Overall:**
- **Free**: Render (with keep-alive ping)
- **Paid**: Railway Hobby ($5/month)

## Next Steps

1. Choose hosting option
2. Set up deployment
3. Configure environment variables
4. Test deployment
5. Set up keep-alive (if using Render free tier)
6. Update documentation with deployment URL

