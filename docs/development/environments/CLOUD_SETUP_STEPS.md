# Supabase Cloud Setup - Step by Step

Follow these steps to link your local project to your Supabase cloud project.

## Step 1: Get Your Project Reference ID

1. Go to: https://supabase.com/dashboard
2. Select your **electric-sheep** project (or whatever you named it)
3. Go to: **Settings** → **General**
4. Find **Reference ID** (looks like: `abcdefghijklmnop`)
5. Copy it - you'll need it in Step 3

## Step 2: Login to Supabase CLI

Run this in your terminal:

```bash
cd /Users/CharlieCalver/git/electric-sheep
supabase login
```

This will:
- Open your browser
- Ask you to authorize the CLI
- Complete authentication

## Step 3: Link Your Project

After logging in, run:

```bash
supabase link --project-ref YOUR_PROJECT_REF
```

Replace `YOUR_PROJECT_REF` with the Reference ID from Step 1.

Example:
```bash
supabase link --project-ref abcdefghijklmnop
```

## Step 4: Apply Migrations

Push your database migrations to the cloud:

```bash
supabase db push
```

This will apply the `moods` table migration we created earlier.

## Step 5: Get Your Credentials

1. In Supabase Dashboard, go to: **Settings** → **API**
2. Copy these values:
   - **Project URL** (API URL): `https://YOUR_PROJECT_REF.supabase.co`
   - **anon public** key: (starts with `eyJ...`)

## Step 6: Configure the App

We'll add these credentials to your app configuration next.

---

**Once you've completed Steps 1-5, let me know and I'll help configure the app!**

