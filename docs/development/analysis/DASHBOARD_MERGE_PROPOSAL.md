# Dashboard Merge Proposal - Final

**Date**: 2025-01-20  
**Issue**: Two separate dashboards with different toolsets  
**Goal**: Merge into single unified dashboard with integrated content authoring tool

## Understanding the Real Purpose

### HTML Viewer's Actual Purpose
The HTML Viewer is a **communication and storytelling tool**:
- **Purpose**: Create information-rich pages that convey a narrative about development/testing outputs
- **Use Cases**:
  - Craft stories from test results (what happened, why it matters)
  - Present log analysis with context and explanations
  - Create documentation pages that tell a story
  - Build information-rich pages from metrics data
  - Share technical findings with narrative structure
- **Key Characteristics**:
  - **Ad-hoc**: Created manually when needed, not automated
  - **Narrative-driven**: Tailored to convey a specific story
  - **Information-rich**: Combines data, context, analysis, explanations
  - **Editorial control**: Full control over content structure and flow

### Current State

#### 1. Metrics Dashboard (`scripts/metrics/`)
- **Technology**: Node.js + Fastify
- **Purpose**: Real-time development metrics visualization
- **Features**:
  - Complexity metrics (files, lines, classes, functions)
  - Test metrics (passed, failed, execution time)
  - Prompt tracking
  - Active agent detection (worktrees, branches)
  - Auto-refresh every 5 seconds
- **Status**: ✅ **Working** (implemented and tested)
- **Port**: 8080

#### 2. HTML Viewer (`html-viewer/`)
- **Technology**: Astro + TypeScript + Tailwind CSS
- **Purpose**: Create information-rich pages for communicating dev/test outputs
- **Features**:
  - Markdown to HTML conversion
  - Log file formatting with syntax highlighting
  - Plain text conversion
  - Web interface with live preview
  - Customization (themes, TOC, image embedding)
  - Download standalone HTML files
- **Status**: ✅ **Implemented** (separate project)

## Revised Recommendation: **Integrated Content Authoring Tool**

### Concept: Dashboard + Content Authoring

**Unified Dashboard** with two main functions:
1. **Metrics Visualization** (existing) - Real-time development metrics
2. **Content Authoring** (new) - Create information-rich narrative pages

### Why This Approach?

1. **Unified Experience**: One dashboard for both monitoring and communication
2. **Data Integration**: Authoring tool can pull from live metrics/logs/tests
3. **Editorial Control**: Manual, ad-hoc creation with full narrative control
4. **Rich Content**: Combine data, context, analysis, explanations
5. **Simpler Architecture**: One server, one technology stack

### Implementation Plan

#### Phase 1: Add Content Authoring to Fastify Dashboard

**New Routes:**
- `GET /author` - Content authoring interface
- `GET /author/new` - Create new page
- `GET /author/edit/:id` - Edit existing page
- `POST /api/author/save` - Save page content
- `GET /api/author/pages` - List all pages
- `GET /pages/:id` - View published page
- `GET /api/author/data-sources` - Available data sources (metrics, logs, tests)

**Content Authoring Features:**
1. **Rich Editor**
   - Markdown editor with live preview
   - Insert data blocks (metrics, logs, test results)
   - Add code blocks with syntax highlighting
   - Insert images and diagrams
   - Custom styling options

2. **Data Integration**
   - Pull metrics data into pages
   - Insert log snippets with context
   - Include test results with analysis
   - Link to live dashboard data

3. **Narrative Structure**
   - Section-based organization
   - Custom headings and structure
   - Table of contents generation
   - Flow control (introduction, analysis, conclusions)

4. **Publishing & Hosting**
   - Pages hosted directly in dashboard
   - Accessible via `/pages/:id` routes
   - Rich JavaScript interactivity (Chart.js, interactive elements)
   - Shareable links to hosted pages
   - Version history (optional)

**Workflow:**
1. User opens `/author/new`
2. Creates page with markdown editor
3. Inserts data blocks from metrics/logs/tests
4. Adds context, analysis, narrative
5. Saves page
6. Shares via link or downloads HTML

#### Phase 2: Enhanced Authoring Features

**Advanced Features:**
- Templates for common page types
- Data visualization blocks (charts from metrics)
- Custom styling/themes
- Collaboration features (optional)
- Export to various formats

### Architecture

```
┌─────────────────────────────────────┐
│   Fastify Dashboard Server          │
│   (Port 8080)                        │
├─────────────────────────────────────┤
│   Metrics Routes (existing)         │
│   - GET /                            │
│   - GET /api/metrics                │
│   - GET /api/agents                  │
├─────────────────────────────────────┤
│   Content Authoring Routes (new)    │
│   - GET /author                      │
│   - GET /author/new                  │
│   - POST /api/author/save            │
│   - GET /pages/:id                   │
│   - GET /api/author/data-sources     │
├─────────────────────────────────────┤
│   Shared Utilities                  │
│   - markdownToHTML()                 │
│   - formatLogContent()               │
│   - insertDataBlock()                │
│   - generatePageHTML()               │
└─────────────────────────────────────┘
```

### Implementation Steps

#### Step 1: Add Dependencies

```bash
cd scripts/metrics
npm install marked prismjs
```

#### Step 2: Create Content Authoring Module

Create `scripts/metrics/content-author.js`:

```javascript
import { marked } from 'marked';
import { readFileSync, writeFileSync, existsSync, mkdirSync } from 'fs';
import { join } from 'path';

const PAGES_DIR = join(__dirname, '../../development-metrics/pages');

// Ensure pages directory exists
if (!existsSync(PAGES_DIR)) {
  mkdirSync(PAGES_DIR, { recursive: true });
}

/**
 * Save authored page
 */
export function savePage(pageId, content, metadata = {}) {
  const pageData = {
    id: pageId,
    content,
    metadata: {
      title: metadata.title || 'Untitled Page',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      ...metadata
    }
  };
  
  const filePath = join(PAGES_DIR, `${pageId}.json`);
  writeFileSync(filePath, JSON.stringify(pageData, null, 2), 'utf8');
  
  return pageData;
}

/**
 * Load authored page
 */
export function loadPage(pageId) {
  const filePath = join(PAGES_DIR, `${pageId}.json`);
  
  if (!existsSync(filePath)) {
    return null;
  }
  
  const content = readFileSync(filePath, 'utf8');
  return JSON.parse(content);
}

/**
 * List all pages
 */
export function listPages() {
  if (!existsSync(PAGES_DIR)) {
    return [];
  }
  
  const files = readdirSync(PAGES_DIR)
    .filter(f => f.endsWith('.json'))
    .map(f => {
      const content = readFileSync(join(PAGES_DIR, f), 'utf8');
      const page = JSON.parse(content);
      return {
        id: page.id,
        title: page.metadata.title,
        createdAt: page.metadata.createdAt,
        updatedAt: page.metadata.updatedAt
      };
    })
    .sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
  
  return files;
}

/**
 * Generate HTML from authored page (hosted in dashboard)
 */
export function generatePageHTML(pageData, options = {}) {
  const { content, metadata } = pageData;
  const { theme = 'light', includeTOC = true } = options;
  
  // Process content - replace data blocks with actual data
  const processedContent = processDataBlocks(content);
  
  // Convert markdown to HTML
  marked.setOptions({ gfm: true, breaks: true, headerIds: true });
  const htmlContent = marked.parse(processedContent);
  
  // Generate TOC if requested
  const toc = includeTOC ? generateTOC(htmlContent) : '';
  
  // Build complete HTML document with rich JavaScript
  return buildPageHTML(htmlContent, {
    title: metadata.title,
    theme,
    toc,
    metadata,
    pageId: pageData.id
  });
}

/**
 * Process data blocks in content (e.g., {{metrics:latest}})
 */
function processDataBlocks(content) {
  // Replace data block placeholders with actual data
  // Example: {{metrics:latest}} → formatted metrics HTML
  // Example: {{logs:recent}} → formatted log HTML
  
  let processed = content;
  
  // Metrics data block
  processed = processed.replace(
    /\{\{metrics:latest\}\}/g,
    () => formatMetricsBlock(getLatestMetrics())
  );
  
  // Test results data block
  processed = processed.replace(
    /\{\{tests:latest\}\}/g,
    () => formatTestResultsBlock(getLatestTestResults())
  );
  
  // Log data block (with optional file path)
  processed = processed.replace(
    /\{\{logs:(.+?)\}\}/g,
    (match, logPath) => formatLogBlock(logPath)
  );
  
  return processed;
}

/**
 * Build complete HTML page document with rich JavaScript
 */
function buildPageHTML(content, options) {
  const { title, theme, toc, metadata, pageId } = options;
  
  return `<!DOCTYPE html>
<html lang="en" class="${theme === 'dark' ? 'dark' : ''}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${escapeHtml(title)}</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
  <!-- Chart.js for interactive charts -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
  <!-- Prism.js for syntax highlighting -->
  <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-core.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
  <link href="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/themes/prism-tomorrow.css" rel="stylesheet" />
  <style>
    body {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem;
      line-height: 1.7;
    }
    .page-header {
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 1rem;
      margin-bottom: 2rem;
    }
    .page-meta {
      color: #6b7280;
      font-size: 0.9em;
      margin-top: 0.5rem;
    }
    .data-block {
      background: #f9fafb;
      border-left: 4px solid #3b82f6;
      padding: 1rem;
      margin: 1.5rem 0;
      border-radius: 0.25rem;
    }
    .data-block-title {
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 0.5rem;
    }
    .chart-container {
      position: relative;
      height: 400px;
      margin: 1.5rem 0;
    }
    .interactive-element {
      margin: 1.5rem 0;
    }
    pre {
      background: #1f2937;
      padding: 1rem;
      border-radius: 0.5rem;
      overflow-x: auto;
    }
    code {
      background: #f3f4f6;
      padding: 0.2rem 0.4rem;
      border-radius: 0.25rem;
    }
    pre code {
      background: transparent;
    }
  </style>
</head>
<body>
  <div class="page-header">
    <h1>${escapeHtml(title)}</h1>
    <div class="page-meta">
      Created: ${new Date(metadata.createdAt).toLocaleString()}
      ${metadata.updatedAt !== metadata.createdAt ? ` | Updated: ${new Date(metadata.updatedAt).toLocaleString()}` : ''}
    </div>
  </div>
  ${toc}
  ${content}
  
  <script>
    // Page-specific JavaScript for interactivity
    const pageId = '${pageId}';
    
    // Initialize charts from data blocks
    document.querySelectorAll('.chart-container').forEach((container, index) => {
      const chartData = container.dataset.chartData;
      if (chartData) {
        try {
          const config = JSON.parse(chartData);
          new Chart(container, config);
        } catch (e) {
          console.error('Failed to initialize chart:', e);
        }
      }
    });
    
    // Load live data for data blocks
    async function loadLiveData() {
      const dataBlocks = document.querySelectorAll('[data-source]');
      for (const block of dataBlocks) {
        const source = block.dataset.source;
        try {
          const response = await fetch(\`/api/author/data/\${source}\`);
          const data = await response.json();
          updateDataBlock(block, data);
        } catch (e) {
          console.error(\`Failed to load data from \${source}:\`, e);
        }
      }
    }
    
    // Update data block with live data
    function updateDataBlock(block, data) {
      // Update block content based on data source type
      if (block.dataset.source.startsWith('metrics:')) {
        block.innerHTML = formatMetricsData(data);
      } else if (block.dataset.source.startsWith('tests:')) {
        block.innerHTML = formatTestData(data);
      }
    }
    
    // Initialize on page load
    document.addEventListener('DOMContentLoaded', () => {
      loadLiveData();
      // Refresh live data every 30 seconds if page has live data blocks
      if (document.querySelectorAll('[data-source]').length > 0) {
        setInterval(loadLiveData, 30000);
      }
    });
  </script>
</body>
</html>`;
}

// Helper functions
function formatMetricsBlock(metrics) { /* Format metrics as HTML block */ }
function formatTestResultsBlock(tests) { /* Format test results as HTML block */ }
function formatLogBlock(logPath) { /* Format log as HTML block */ }
function generateTOC(html) { /* Generate table of contents */ }
function getLatestMetrics() { /* Get latest metrics data */ }
function getLatestTestResults() { /* Get latest test results */ }
function escapeHtml(text) { /* Escape HTML */ }
```

#### Step 3: Add Routes to Fastify

Add to `dashboard-server-fastify.js`:

```javascript
import { savePage, loadPage, listPages, generatePageHTML } from './content-author.js';

// Content authoring routes
fastify.get('/author', async (request, reply) => {
  reply.type('text/html');
  return getAuthoringInterfaceHTML();
});

fastify.get('/author/new', async (request, reply) => {
  reply.type('text/html');
  return getNewPageEditorHTML();
});

fastify.get('/author/edit/:id', async (request, reply) => {
  const { id } = request.params;
  const page = loadPage(id);
  
  if (!page) {
    reply.code(404);
    return { error: 'Page not found' };
  }
  
  reply.type('text/html');
  return getEditPageEditorHTML(page);
});

fastify.post('/api/author/save', async (request, reply) => {
  const { id, content, metadata } = request.body;
  
  try {
    const page = savePage(id || `page-${Date.now()}`, content, metadata);
    return { success: true, page };
  } catch (error) {
    reply.code(400);
    return { success: false, error: error.message };
  }
});

fastify.get('/api/author/pages', async (request, reply) => {
  return { pages: listPages() };
});

fastify.get('/pages/:id', async (request, reply) => {
  const { id } = request.params;
  const page = loadPage(id);
  
  if (!page) {
    reply.code(404);
    return { error: 'Page not found' };
  }
  
  const html = generatePageHTML(page);
  reply.type('text/html');
  return html;
});

// API endpoint for live data in pages
fastify.get('/api/author/data/:source', async (request, reply) => {
  const { source } = request.params;
  
  try {
    let data;
    if (source === 'metrics:latest') {
      data = getAllMetrics();
    } else if (source === 'tests:latest') {
      data = getLatestTestResults();
    } else if (source.startsWith('logs:')) {
      const logPath = source.substring(5);
      data = getLogData(logPath);
    } else {
      reply.code(404);
      return { error: 'Unknown data source' };
    }
    
    return { success: true, data };
  } catch (error) {
    reply.code(500);
    return { success: false, error: error.message };
  }
});

fastify.get('/api/author/data-sources', async (request, reply) => {
  return {
    dataSources: [
      { id: 'metrics:latest', name: 'Latest Metrics', description: 'Current development metrics' },
      { id: 'tests:latest', name: 'Latest Test Results', description: 'Most recent test run results' },
      { id: 'logs:recent', name: 'Recent Logs', description: 'Recent log entries' }
    ]
  };
});
```

#### Step 4: Create Authoring Interface

The authoring interface should include:
- Markdown editor with live preview
- Data source insertion buttons
- Save/load functionality
- Page list/sidebar
- Export options

#### Step 5: Update Navigation

Add authoring link to main dashboard:

```javascript
function getDashboardHTML() {
  return `
    <nav>
      <a href="/">Metrics</a>
      <a href="/author">Author</a>
      <a href="/agents">Agents</a>
    </nav>
    ...
  `;
}
```

### Benefits of This Approach

1. **Unified Dashboard**
   - One URL: `http://localhost:8080`
   - Easy navigation between metrics and authoring
   - Consistent styling and UX
   - Pages hosted directly in dashboard

2. **Rich Interactivity**
   - Chart.js for interactive visualizations
   - Live data updates (optional)
   - Interactive elements and widgets
   - JavaScript-powered features

3. **Data Integration**
   - Pull live data from metrics/logs/tests
   - Insert data blocks into pages
   - Keep data current or snapshot at creation time
   - API endpoints for live data updates

4. **Editorial Control**
   - Manual, ad-hoc creation
   - Full control over narrative structure
   - Add context, analysis, explanations
   - Craft information-rich pages

5. **Narrative-Driven**
   - Structure content to tell a story
   - Combine data with context
   - Create compelling presentations
   - Share via hosted links

6. **Simpler Architecture**
   - One server, one technology stack
   - Shared utilities and styling
   - Easier maintenance
   - No export/import workflow needed

### Content Authoring Workflow

**Example: Creating a Test Results Narrative Page**

1. Open `/author/new`
2. Write introduction: "Here's what we found in our latest test run..."
3. Insert data block: `{{tests:latest}}` (pulls live test data)
4. Add analysis: "The failures are concentrated in the authentication module..."
5. Insert interactive chart: `{{chart:test-trends}}` (Chart.js visualization)
6. Insert log snippet: `{{logs:auth-test.log}}`
7. Add conclusion: "We need to refactor the auth flow..."
8. Save page
9. Share via `/pages/page-id` (hosted in dashboard with rich interactivity)

**Example: Creating a Metrics Analysis Page**

1. Open `/author/new`
2. Write narrative: "Our codebase complexity has increased..."
3. Insert metrics: `{{metrics:latest}}`
4. Add context: "This is expected given the new features..."
5. Insert specific metrics with analysis
6. Add recommendations
7. Save and share

### Data Block Syntax

**Available Data Blocks:**
- `{{metrics:latest}}` - Latest metrics snapshot (live or snapshot)
- `{{tests:latest}}` - Latest test results (live or snapshot)
- `{{logs:path/to/log.log}}` - Specific log file
- `{{logs:recent}}` - Recent log entries
- `{{chart:complexity-trend}}` - Interactive Chart.js visualization
- `{{chart:test-results}}` - Test results as interactive chart
- `{{table:metrics}}` - Format data as interactive table
- `{{code:file.js}}` - Include code file snippet with syntax highlighting

**Live vs. Snapshot Data:**
- Pages can use live data (refreshes automatically)
- Or snapshot data (frozen at creation time)
- Configure per data block: `{{metrics:latest:live}}` or `{{metrics:latest:snapshot}}`

### Migration Steps

1. **Add dependencies** (`marked`, `prismjs`)
2. **Create content authoring module** (`content-author.js`)
3. **Add Fastify routes** for authoring interface
4. **Create authoring UI** (markdown editor, data blocks)
5. **Test thoroughly**
6. **Archive or remove** `html-viewer/` directory
7. **Update documentation**

### Timeline

- **Phase 1 (Core Authoring)**: 4-5 hours
  - Add dependencies
  - Create content authoring module
  - Add routes
  - Basic markdown editor interface

- **Phase 2 (Data Integration)**: 2-3 hours
  - Data block processing
  - Integration with metrics/logs/tests
  - Data source selection UI

- **Phase 3 (Enhancement)**: 2-3 hours (optional)
  - Advanced editor features
  - Templates
  - Export options
  - Version history

## Decision

**Recommended**: ✅ **Keep Fastify, add integrated Content Authoring Tool**

**Rationale**:
- Metrics Dashboard is working and needs real-time server capabilities
- Content Authoring serves specific purpose: creating narrative-driven pages
- Unified experience for monitoring and communication
- Data integration allows pulling from live metrics
- Editorial control for ad-hoc, information-rich pages
- Simpler architecture (one stack)

## Next Steps

1. Review this final proposal
2. Approve approach
3. Implement Phase 1 (core authoring)
4. Test content creation workflow
5. Archive or remove `html-viewer/` directory
6. Update documentation
