#!/usr/bin/env node
/**
 * Content Authoring Module
 * 
 * Manages authored pages for the dashboard - allows creating
 * information-rich narrative pages with data integration.
 */

import { readFileSync, writeFileSync, existsSync, mkdirSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { marked } from 'marked';
import Prism from 'prismjs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const PROJECT_ROOT = join(__dirname, '../..');
const PAGES_DIR = join(PROJECT_ROOT, 'development-metrics', 'pages');

// Ensure pages directory exists
if (!existsSync(PAGES_DIR)) {
  mkdirSync(PAGES_DIR, { recursive: true });
}

/**
 * Save authored page (with user scoping)
 * 
 * @param {string} pageId - Page identifier
 * @param {string} content - Page content (markdown)
 * @param {object} metadata - Page metadata (title, theme, etc.)
 * @param {string} userId - User ID of the creator (required)
 * @returns {object} Saved page data
 */
export function savePage(pageId, content, metadata = {}, userId) {
  if (!userId) {
    throw new Error('User ID is required');
  }
  
  const pageData = {
    id: pageId,
    userId, // Add user ID for scoping
    content,
    metadata: {
      title: metadata.title || 'Untitled Page',
      createdAt: metadata.createdAt || new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      theme: metadata.theme || 'light',
      includeTOC: metadata.includeTOC !== false,
      isPublic: metadata.isPublic || false, // Public/private flag
      ...metadata
    }
  };
  
  // If updating existing page, preserve createdAt and verify ownership
  const existing = loadPage(pageId, userId);
  if (existing) {
    pageData.metadata.createdAt = existing.metadata.createdAt;
    // Verify ownership
    if (existing.userId !== userId) {
      throw new Error('Access denied: You can only edit your own pages');
    }
  }
  
  const filePath = join(PAGES_DIR, `${pageId}.json`);
  writeFileSync(filePath, JSON.stringify(pageData, null, 2), 'utf8');
  
  return pageData;
}

/**
 * Load authored page (with access control)
 * 
 * @param {string} pageId - Page identifier
 * @param {string} userId - User ID (optional, for access control)
 * @returns {object|null} Page data or null if not found/access denied
 */
export function loadPage(pageId, userId = null) {
  const filePath = join(PAGES_DIR, `${pageId}.json`);
  
  if (!existsSync(filePath)) {
    return null;
  }
  
  const content = readFileSync(filePath, 'utf8');
  const page = JSON.parse(content);
  
  // Check access permissions
  if (userId && page.userId !== userId && !page.metadata.isPublic) {
    return null; // User doesn't have access
  }
  
  return page;
}

/**
 * List pages (filtered by user and public pages)
 * 
 * @param {string} userId - User ID (optional, filters to user's pages + public pages)
 * @returns {array} List of pages
 */
export function listPages(userId = null) {
  if (!existsSync(PAGES_DIR)) {
    return [];
  }
  
  const files = readdirSync(PAGES_DIR)
    .filter(f => f.endsWith('.json'))
    .map(f => {
      try {
        const content = readFileSync(join(PAGES_DIR, f), 'utf8');
        const page = JSON.parse(content);
        return {
          id: page.id,
          userId: page.userId,
          title: page.metadata.title,
          createdAt: page.metadata.createdAt,
          updatedAt: page.metadata.updatedAt,
          isPublic: page.metadata.isPublic || false
        };
      } catch (e) {
        console.error(`Error reading page ${f}:`, e);
        return null;
      }
    })
    .filter(p => {
      if (p === null) return false;
      
      // Filter by user access
      if (userId) {
        // Return user's pages + public pages
        return p.userId === userId || p.isPublic;
      } else {
        // Return only public pages
        return p.isPublic;
      }
    })
    .sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
  
  return files;
}

/**
 * Generate HTML from authored page (hosted in dashboard)
 */
export function generatePageHTML(pageData, options = {}) {
  const { content, metadata } = pageData;
  const { theme = metadata.theme || 'light', includeTOC = metadata.includeTOC !== false } = options;
  
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
  let processed = content;
  
  // Metrics data block (snapshot at page generation)
  processed = processed.replace(
    /\{\{metrics:latest(?::snapshot)?\}\}/g,
    () => {
      const metrics = getLatestMetrics();
      return formatMetricsBlock(metrics);
    }
  );
  
  // Test results data block
  processed = processed.replace(
    /\{\{tests:latest(?::snapshot)?\}\}/g,
    () => {
      const tests = getLatestTestResults();
      return formatTestResultsBlock(tests);
    }
  );
  
  // Log data block (with optional file path)
  processed = processed.replace(
    /\{\{logs:(.+?)\}\}/g,
    (match, logPath) => {
      return formatLogBlock(logPath);
    }
  );
  
  // Chart placeholder (will be rendered by JavaScript)
  processed = processed.replace(
    /\{\{chart:(.+?)\}\}/g,
    (match, chartType) => {
      return `<div class="chart-container" data-chart-type="${chartType}"></div>`;
    }
  );
  
  return processed;
}

/**
 * Get latest metrics (will be passed from dashboard server)
 */
function getLatestMetrics() {
  // This will be populated by the dashboard server when processing data blocks
  // For now, return placeholder - actual data comes from API
  return {
    complexity: { files: 0, lines: 0, classes: 0, functions: 0 },
    timestamp: new Date().toISOString()
  };
}

/**
 * Get latest test results (will be passed from dashboard server)
 */
function getLatestTestResults() {
  // This will be populated by the dashboard server when processing data blocks
  // For now, return placeholder - actual data comes from API
  return {
    total: 0,
    passed: 0,
    failed: 0,
    executionTime: 0,
    timestamp: new Date().toISOString()
  };
}

/**
 * Format metrics as HTML block
 */
function formatMetricsBlock(metrics) {
  if (!metrics || !metrics.complexity) {
    return '<div class="data-block"><p>No metrics data available</p></div>';
  }
  
  const { complexity } = metrics;
  return `
    <div class="data-block" data-source="metrics:latest">
      <div class="data-block-title">Development Metrics</div>
      <p><strong>Files:</strong> ${complexity.files || 0}</p>
      <p><strong>Lines of Code:</strong> ${complexity.lines || 0}</p>
      <p><strong>Classes:</strong> ${complexity.classes || 0}</p>
      <p><strong>Functions:</strong> ${complexity.functions || 0}</p>
      <p class="data-block-meta">Last updated: ${new Date(metrics.timestamp).toLocaleString()}</p>
    </div>
  `;
}

/**
 * Format test results as HTML block
 */
function formatTestResultsBlock(tests) {
  if (!tests || tests.total === undefined) {
    return '<div class="data-block"><p>No test data available</p></div>';
  }
  
  const passRate = tests.total > 0 ? ((tests.passed / tests.total) * 100).toFixed(1) : 0;
  
  return `
    <div class="data-block" data-source="tests:latest">
      <div class="data-block-title">Test Results</div>
      <p><strong>Total Tests:</strong> ${tests.total}</p>
      <p><strong>Passed:</strong> <span style="color: #10b981;">${tests.passed}</span></p>
      <p><strong>Failed:</strong> <span style="color: #ef4444;">${tests.failed}</span></p>
      <p><strong>Pass Rate:</strong> ${passRate}%</p>
      <p><strong>Execution Time:</strong> ${tests.executionTime || 0}s</p>
      <p class="data-block-meta">Last run: ${new Date(tests.timestamp).toLocaleString()}</p>
    </div>
  `;
}

/**
 * Format log as HTML block
 */
function formatLogBlock(logPath) {
  // For now, return placeholder
  // In full implementation, would read and format log file
  return `
    <div class="data-block">
      <div class="data-block-title">Log: ${logPath}</div>
      <pre><code class="language-text">Log content would be loaded here...</code></pre>
    </div>
  `;
}

/**
 * Generate table of contents from HTML content
 */
function generateTOC(html) {
  const headingRegex = /<h([2-3]) id="([^"]+)">([^<]+)<\/h[2-3]>/g;
  const headings = [];
  let match;
  
  while ((match = headingRegex.exec(html)) !== null) {
    headings.push({
      level: parseInt(match[1]),
      id: match[2],
      text: match[3]
    });
  }
  
  if (headings.length === 0) {
    return '';
  }
  
  let toc = '<nav class="toc" aria-label="Table of Contents">\n';
  toc += '<h2>Table of Contents</h2>\n';
  toc += '<ul>\n';
  
  for (const heading of headings) {
    const indent = heading.level === 3 ? '  ' : '';
    toc += `${indent}<li><a href="#${heading.id}">${escapeHtml(heading.text)}</a></li>\n`;
  }
  
  toc += '</ul>\n';
  toc += '</nav>\n';
  
  return toc;
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
    .data-block-meta {
      color: #6b7280;
      font-size: 0.85em;
      margin-top: 0.5rem;
      font-style: italic;
    }
    .chart-container {
      position: relative;
      height: 400px;
      margin: 1.5rem 0;
    }
    .toc {
      background: #f3f4f6;
      padding: 1.5rem;
      border-radius: 0.5rem;
      margin-bottom: 2rem;
    }
    .toc ul {
      list-style: none;
      padding-left: 0;
    }
    .toc li {
      margin: 0.5rem 0;
    }
    .toc a {
      color: #3b82f6;
      text-decoration: none;
    }
    .toc a:hover {
      text-decoration: underline;
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
    @media (prefers-color-scheme: dark) {
      body {
        background: #111827;
        color: #f9fafb;
      }
      .data-block {
        background: #1f2937;
      }
      .toc {
        background: #1f2937;
      }
      code {
        background: #374151;
      }
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
      const chartType = container.dataset.chartType;
      if (chartType) {
        initializeChart(container, chartType);
      }
    });
    
    // Load live data for data blocks
    async function loadLiveData() {
      const dataBlocks = document.querySelectorAll('[data-source]');
      for (const block of dataBlocks) {
        const source = block.dataset.source;
        try {
          const response = await fetch(\`/api/author/data/\${source}\`);
          if (response.ok) {
            const result = await response.json();
            if (result.success) {
              updateDataBlock(block, result.data, source);
            }
          }
        } catch (e) {
          console.error(\`Failed to load data from \${source}:\`, e);
        }
      }
    }
    
    // Update data block with live data
    function updateDataBlock(block, data, source) {
      if (source.startsWith('metrics:')) {
        block.innerHTML = formatMetricsData(data);
      } else if (source.startsWith('tests:')) {
        block.innerHTML = formatTestData(data);
      }
    }
    
    // Format metrics data
    function formatMetricsData(data) {
      if (!data || !data.complexity) {
        return '<p>No metrics data available</p>';
      }
      const { complexity } = data;
      return \`
        <div class="data-block-title">Development Metrics</div>
        <p><strong>Files:</strong> \${complexity.files || 0}</p>
        <p><strong>Lines of Code:</strong> \${complexity.lines || 0}</p>
        <p><strong>Classes:</strong> \${complexity.classes || 0}</p>
        <p><strong>Functions:</strong> \${complexity.functions || 0}</p>
        <p class="data-block-meta">Last updated: \${new Date().toLocaleString()}</p>
      \`;
    }
    
    // Format test data
    function formatTestData(data) {
      if (!data || data.total === undefined) {
        return '<p>No test data available</p>';
      }
      const passRate = data.total > 0 ? ((data.passed / data.total) * 100).toFixed(1) : 0;
      return \`
        <div class="data-block-title">Test Results</div>
        <p><strong>Total Tests:</strong> \${data.total}</p>
        <p><strong>Passed:</strong> <span style="color: #10b981;">\${data.passed}</span></p>
        <p><strong>Failed:</strong> <span style="color: #ef4444;">\${data.failed}</span></p>
        <p><strong>Pass Rate:</strong> \${passRate}%</p>
        <p><strong>Execution Time:</strong> \${data.executionTime || 0}s</p>
        <p class="data-block-meta">Last run: \${new Date().toLocaleString()}</p>
      \`;
    }
    
    // Initialize chart
    function initializeChart(container, chartType) {
      // Fetch chart data from API
      fetch(\`/api/author/chart/\${chartType}\`)
        .then(response => response.json())
        .then(result => {
          if (result.success && result.config) {
            new Chart(container, result.config);
          }
        })
        .catch(e => {
          console.error('Failed to load chart data:', e);
          container.innerHTML = '<p>Chart data unavailable</p>';
        });
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

/**
 * Escape HTML special characters
 */
function escapeHtml(text) {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return String(text).replace(/[&<>"']/g, m => map[m]);
}

