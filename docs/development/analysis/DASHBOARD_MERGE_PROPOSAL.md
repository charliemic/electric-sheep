# Dashboard Merge Proposal - Revised

**Date**: 2025-01-20  
**Issue**: Two separate dashboards with different toolsets  
**Goal**: Merge into single unified dashboard with integrated report generation

## Understanding the Real Purpose

### HTML Viewer's Actual Purpose
The HTML Viewer isn't just a converter - it's a **communication tool**:
- **Purpose**: Take outputs from development or testing and communicate these effectively to an audience
- **Use Cases**:
  - Convert test results to shareable HTML reports
  - Format log files for presentation
  - Convert markdown documentation for sharing
  - Generate reports from metrics data
  - Create presentation-ready HTML from technical content

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
- **Purpose**: Convert dev/test outputs to shareable HTML for communication
- **Features**:
  - Markdown to HTML conversion
  - Log file formatting with syntax highlighting
  - Plain text conversion
  - Web interface with live preview
  - Customization (themes, TOC, image embedding)
  - Download standalone HTML files
- **Status**: ✅ **Implemented** (separate project)

## Revised Recommendation: **Integrated Report Generator**

### Concept: Dashboard + Report Generator

**Unified Dashboard** with two main functions:
1. **Metrics Visualization** (existing) - Real-time development metrics
2. **Report Generator** (new) - Convert dev/test outputs to shareable HTML

### Why This Approach?

1. **Unified Experience**: One dashboard for both monitoring and communication
2. **Context-Aware**: Reports can pull from live metrics data
3. **Workflow Integration**: Generate reports directly from test results, logs, metrics
4. **Custom & Tailored**: Built specifically for communicating dev/test outputs
5. **Simpler Architecture**: One server, one technology stack

### Implementation Plan

#### Phase 1: Add Report Generator to Fastify Dashboard

**New Routes:**
- `GET /reports` - Report generator interface
- `GET /reports/new` - Create new report form
- `POST /api/reports/generate` - Generate HTML report
- `GET /api/reports/templates` - Available report templates
- `GET /reports/:id` - View generated report

**Report Types:**
1. **Test Results Report**
   - Pull from latest test metrics
   - Format test output as HTML
   - Include charts/graphs from test data
   - Shareable link for stakeholders

2. **Log Analysis Report**
   - Upload or paste log files
   - Format with syntax highlighting
   - Add context/analysis
   - Generate shareable HTML

3. **Metrics Summary Report**
   - Pull from current metrics dashboard data
   - Create snapshot report
   - Include charts and visualizations
   - Export as standalone HTML

4. **Documentation Report**
   - Convert markdown docs to HTML
   - Add branding/styling
   - Generate shareable version

**Integration Points:**
- Reports can pull live data from metrics API
- Test results automatically available for report generation
- Logs can be uploaded or linked from recent runs
- Metrics snapshots can be included in reports

#### Phase 2: Enhanced Report Features

**Customization:**
- Report templates (test results, log analysis, metrics summary, etc.)
- Custom branding/styling
- Add context/analysis sections
- Include charts/graphs from metrics
- Export options (HTML, PDF via print)

**Workflow Integration:**
- "Generate Report" button on metrics pages
- "Share as Report" from test results
- "Create Report" from log viewer
- Recent reports list in dashboard

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
│   Report Generator Routes (new)     │
│   - GET /reports                     │
│   - POST /api/reports/generate       │
│   - GET /reports/:id                 │
├─────────────────────────────────────┤
│   Shared Utilities                  │
│   - markdownToHTML()                 │
│   - formatLogContent()               │
│   - generateReportHTML()             │
└─────────────────────────────────────┘
```

### Implementation Steps

#### Step 1: Add Dependencies

```bash
cd scripts/metrics
npm install marked prismjs
```

#### Step 2: Create Report Generator Module

Create `scripts/metrics/report-generator.js`:

```javascript
import { marked } from 'marked';

/**
 * Generate HTML report from various sources
 */
export function generateReport(type, data, options = {}) {
  switch (type) {
    case 'test-results':
      return generateTestResultsReport(data, options);
    case 'log-analysis':
      return generateLogAnalysisReport(data, options);
    case 'metrics-summary':
      return generateMetricsSummaryReport(data, options);
    case 'documentation':
      return generateDocumentationReport(data, options);
    default:
      throw new Error(`Unknown report type: ${type}`);
  }
}

/**
 * Generate test results report
 */
function generateTestResultsReport(testData, options) {
  const { title = 'Test Results Report', theme = 'light' } = options;
  
  // Format test data as HTML
  const testSummary = `
    <div class="test-summary">
      <h2>Test Summary</h2>
      <p>Total Tests: ${testData.total}</p>
      <p>Passed: ${testData.passed}</p>
      <p>Failed: ${testData.failed}</p>
      <p>Execution Time: ${testData.executionTime}s</p>
    </div>
  `;
  
  // Generate full HTML document
  return buildReportHTML(testSummary, { title, theme });
}

/**
 * Generate log analysis report
 */
function generateLogAnalysisReport(logContent, options) {
  const { title = 'Log Analysis Report', theme = 'light' } = options;
  
  // Format log with syntax highlighting
  const formattedLog = formatLogContent(logContent);
  
  return buildReportHTML(formattedLog, { title, theme });
}

/**
 * Generate metrics summary report
 */
function generateMetricsSummaryReport(metricsData, options) {
  const { title = 'Metrics Summary Report', theme = 'light' } = options;
  
  // Format metrics as HTML
  const metricsHTML = formatMetricsAsHTML(metricsData);
  
  return buildReportHTML(metricsHTML, { title, theme });
}

/**
 * Generate documentation report
 */
function generateDocumentationReport(markdown, options) {
  const { title = 'Documentation', theme = 'light', includeTOC = true } = options;
  
  // Convert markdown to HTML
  marked.setOptions({ gfm: true, breaks: true, headerIds: true });
  const htmlContent = marked.parse(markdown);
  
  // Generate TOC if requested
  const toc = includeTOC ? generateTOC(htmlContent) : '';
  
  return buildReportHTML(toc + htmlContent, { title, theme });
}

/**
 * Build complete HTML report document
 */
function buildReportHTML(content, options) {
  const { title, theme } = options;
  
  return `<!DOCTYPE html>
<html lang="en" class="${theme === 'dark' ? 'dark' : ''}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${escapeHtml(title)}</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
  <style>
    body {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem;
      line-height: 1.7;
    }
    .report-header {
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 1rem;
      margin-bottom: 2rem;
    }
    .report-meta {
      color: #6b7280;
      font-size: 0.9em;
      margin-top: 0.5rem;
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
  <div class="report-header">
    <h1>${escapeHtml(title)}</h1>
    <div class="report-meta">
      Generated: ${new Date().toLocaleString()}
    </div>
  </div>
  ${content}
</body>
</html>`;
}

// Helper functions (from html-viewer)
function formatLogContent(log) { /* ... */ }
function generateTOC(html) { /* ... */ }
function formatMetricsAsHTML(metrics) { /* ... */ }
function escapeHtml(text) { /* ... */ }
```

#### Step 3: Add Routes to Fastify

Add to `dashboard-server-fastify.js`:

```javascript
import { generateReport } from './report-generator.js';

// Report generator routes
fastify.get('/reports', async (request, reply) => {
  reply.type('text/html');
  return getReportsPageHTML();
});

fastify.get('/reports/new', async (request, reply) => {
  reply.type('text/html');
  return getNewReportPageHTML();
});

fastify.post('/api/reports/generate', async (request, reply) => {
  const { type, data, options } = request.body;
  
  try {
    const html = generateReport(type, data, options);
    return { 
      success: true, 
      html,
      downloadUrl: `/api/reports/download/${Date.now()}.html`
    };
  } catch (error) {
    reply.code(400);
    return { success: false, error: error.message };
  }
});

fastify.get('/api/reports/templates', async (request, reply) => {
  return {
    templates: [
      { id: 'test-results', name: 'Test Results Report', description: 'Format test results for sharing' },
      { id: 'log-analysis', name: 'Log Analysis Report', description: 'Format log files with syntax highlighting' },
      { id: 'metrics-summary', name: 'Metrics Summary', description: 'Create snapshot of current metrics' },
      { id: 'documentation', name: 'Documentation Report', description: 'Convert markdown docs to HTML' }
    ]
  };
});
```

#### Step 4: Update Navigation

Add reports link to main dashboard:

```javascript
function getDashboardHTML() {
  return `
    <nav>
      <a href="/">Metrics</a>
      <a href="/reports">Reports</a>
      <a href="/agents">Agents</a>
    </nav>
    ...
  `;
}
```

#### Step 5: Add "Generate Report" Actions

Add report generation buttons to relevant pages:

```javascript
// On test results page
function getTestsPageHTML() {
  return `
    ...
    <button onclick="generateReport('test-results')">
      Generate Report
    </button>
    ...
  `;
}

// On metrics page
function getDashboardHTML() {
  return `
    ...
    <button onclick="generateReport('metrics-summary')">
      Create Metrics Report
    </button>
    ...
  `;
}
```

### Benefits of This Approach

1. **Unified Dashboard**
   - One URL: `http://localhost:8080`
   - Easy navigation between metrics and reports
   - Consistent styling and UX

2. **Context-Aware Reports**
   - Reports can pull live data from metrics
   - Test results automatically available
   - Metrics snapshots included in reports

3. **Workflow Integration**
   - Generate reports directly from dashboard
   - "Share as Report" buttons on relevant pages
   - Recent reports list

4. **Custom & Tailored**
   - Built specifically for communicating dev/test outputs
   - Report templates for common use cases
   - Easy to extend with new report types

5. **Simpler Architecture**
   - One server, one technology stack
   - Shared utilities and styling
   - Easier maintenance

### Report Templates

**Test Results Report:**
- Pull from latest test metrics
- Format test output as HTML
- Include pass/fail statistics
- Add execution time and trends
- Shareable link for stakeholders

**Log Analysis Report:**
- Upload or paste log files
- Format with syntax highlighting
- Add context/analysis sections
- Highlight errors/warnings
- Generate shareable HTML

**Metrics Summary Report:**
- Pull from current metrics dashboard
- Create snapshot of complexity, tests, prompts
- Include charts/graphs
- Add timestamp and context
- Export as standalone HTML

**Documentation Report:**
- Convert markdown docs to HTML
- Add branding/styling
- Include table of contents
- Generate shareable version

### Migration Steps

1. **Add dependencies** (`marked`, `prismjs`)
2. **Create report generator module** (`report-generator.js`)
3. **Add Fastify routes** for report generation
4. **Update navigation** to include reports
5. **Add report generation buttons** to relevant pages
6. **Test thoroughly**
7. **Archive or remove** `html-viewer/` directory
8. **Update documentation**

### Timeline

- **Phase 1 (Core Integration)**: 3-4 hours
  - Add dependencies
  - Create report generator module
  - Add routes
  - Basic report generation

- **Phase 2 (Enhancement)**: 2-3 hours (optional)
  - Report templates
  - Integration with metrics pages
  - Recent reports list
  - Export options

## Decision

**Recommended**: ✅ **Keep Fastify, add integrated Report Generator**

**Rationale**:
- Metrics Dashboard is working and needs real-time server capabilities
- Report Generator serves specific purpose: communicating dev/test outputs
- Unified experience for monitoring and communication
- Context-aware reports can pull from live metrics
- Simpler architecture (one stack)

## Next Steps

1. Review this revised proposal
2. Approve approach
3. Implement Phase 1 (core integration)
4. Test report generation from various sources
5. Archive or remove `html-viewer/` directory
6. Update documentation
