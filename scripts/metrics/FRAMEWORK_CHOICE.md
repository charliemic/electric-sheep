# Dashboard Framework Choice: Fastify

## Why Fastify?

### ✅ Aligned with Project Principles

1. **Simplicity First** (like Astro choice)
   - Lightweight and fast
   - Minimal boilerplate
   - Easy to understand

2. **Future Options**
   - ✅ **WebSocket support** - Can add real-time updates later
   - ✅ **TypeScript support** - Can migrate from JS to TS
   - ✅ **Plugin ecosystem** - Easy to add features (auth, CORS, etc.)
   - ✅ **High performance** - 2x faster than Express
   - ✅ **JSON Schema validation** - Built-in request validation

3. **Already in Ecosystem**
   - Node.js already used in `html-viewer`
   - No new runtime dependencies
   - Consistent with existing tooling

## Framework Comparison

| Framework | Performance | Simplicity | Future Options | TypeScript | WebSocket |
|-----------|-------------|------------|----------------|------------|-----------|
| **Fastify** ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | ✅ |
| Express | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | ⚠️ Plugin |
| Koa | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ✅ | ⚠️ Plugin |
| Raw http | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ | ❌ | ❌ |

## Future Extensibility

### Easy to Add Later:

1. **Real-time Updates (WebSocket)**
   ```javascript
   import fastifyWebsocket from '@fastify/websocket';
   await fastify.register(fastifyWebsocket);
   ```

2. **TypeScript Migration**
   - Fastify has excellent TS support
   - Can gradually migrate from JS to TS

3. **Authentication** (if needed)
   ```javascript
   import fastifyJWT from '@fastify/jwt';
   await fastify.register(fastifyJWT);
   ```

4. **CORS** (if needed)
   ```javascript
   import fastifyCors from '@fastify/cors';
   await fastify.register(fastifyCors);
   ```

5. **Request Validation**
   ```javascript
   fastify.get('/api/metrics', {
     schema: {
       querystring: {
         type: 'object',
         properties: {
           category: { type: 'string' }
         }
       }
     }
   }, handler);
   ```

## Migration Path

### Current: Raw http module
- Simple, no dependencies
- Works for basic needs

### Recommended: Fastify
- One dependency (`fastify`)
- Better structure for growth
- Easy migration path

### Future: TypeScript + Fastify
- Add TypeScript when needed
- Fastify has excellent TS support
- Type-safe APIs

## Installation

```bash
cd scripts/metrics
npm install fastify
```

## Usage

```bash
# Use Fastify version
node scripts/metrics/dashboard-server-fastify.js

# Or use simple version (no dependencies)
node scripts/metrics/dashboard-server.js
```

## Decision

**Recommendation: Use Fastify**

- ✅ Lightweight (one dependency)
- ✅ Better future options (WebSocket, TypeScript, plugins)
- ✅ Aligned with project principles (simplicity, extensibility)
- ✅ Easy migration from raw http module
- ✅ Can start simple, add features as needed

