# Docker Desktop Setup for macOS

Docker Desktop is required for local Supabase development. Follow these steps to install and configure it.

## Installation

### Option 1: Homebrew (Recommended)

```bash
# Install Docker Desktop
brew install --cask docker

# Note: This may require sudo password for some operations
# If installation fails, use Option 2 below
```

### Option 2: Manual Download

1. **Download Docker Desktop**:
   - Visit: https://www.docker.com/products/docker-desktop/
   - Download for Mac (Apple Silicon or Intel)
   - Open the downloaded `.dmg` file
   - Drag Docker.app to Applications folder

2. **Launch Docker Desktop**:
   ```bash
   open -a Docker
   ```
   Or double-click Docker.app in Applications

3. **Complete Setup**:
   - Accept the license agreement
   - Enter your password when prompted (for privileged access)
   - Wait for Docker to start (whale icon in menu bar)

## Verification

After installation, verify Docker is running:

```bash
# Check Docker version
docker --version

# Check if Docker daemon is running
docker ps

# If you see "Cannot connect to the Docker daemon", Docker Desktop is not running
# Start it with: open -a Docker
```

## Starting Docker Desktop

Docker Desktop must be running before using Supabase CLI:

```bash
# Start Docker Desktop
open -a Docker

# Wait a few seconds for it to start, then verify:
docker ps
```

## Troubleshooting

### "Cannot connect to the Docker daemon"

**Solution**: Docker Desktop is not running
```bash
open -a Docker
# Wait 10-20 seconds, then try again
docker ps
```

### Docker Desktop won't start

1. Check System Preferences → Security & Privacy
2. Allow Docker Desktop if it's blocked
3. Restart your Mac if needed
4. Check Docker Desktop logs: `~/Library/Containers/com.docker.docker/Data/log/`

### Permission denied errors

Docker Desktop should handle permissions automatically. If you see permission errors:
1. Ensure Docker Desktop is running
2. Check Docker Desktop settings → Resources → File Sharing
3. Restart Docker Desktop

## Next Steps

Once Docker is running, you can:

1. **Start local Supabase**:
   ```bash
   cd /Users/CharlieCalver/git/electric-sheep
   supabase start
   ```

2. **Check Supabase status**:
   ```bash
   supabase status
   ```

3. **Apply migrations locally**:
   ```bash
   supabase db reset
   ```

## Resources

- [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)
- [Docker Desktop Documentation](https://docs.docker.com/desktop/)

