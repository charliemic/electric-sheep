import { marked } from 'marked';

export interface ConversionOptions {
  title?: string;
  theme?: 'light' | 'dark' | 'auto';
  embedImages?: boolean;
  includeTOC?: boolean;
  syntaxHighlight?: boolean;
}

export interface ImageInfo {
  path: string;
  alt: string;
  base64?: string;
}

/**
 * Convert markdown content to HTML
 */
export function markdownToHTML(
  markdown: string,
  options: ConversionOptions = {}
): string {
  const {
    title = 'Document',
    theme = 'light',
    embedImages = true,
    includeTOC = true,
  } = options;

  // Configure marked
  marked.setOptions({
    gfm: true,
    breaks: true,
    headerIds: true,
    mangle: false,
  });

  // Extract images if embedding
  const images: ImageInfo[] = [];
  if (embedImages) {
    const imageRegex = /!\[([^\]]*)\]\(([^)]+)\)/g;
    let match;
    while ((match = imageRegex.exec(markdown)) !== null) {
      images.push({
        alt: match[1],
        path: match[2],
      });
    }
  }

  // Convert markdown to HTML
  const htmlContent = marked.parse(markdown) as string;

  // Generate table of contents if requested
  const toc = includeTOC ? generateTOC(htmlContent) : '';

  // Build final HTML
  return buildHTMLDocument(htmlContent, {
    title,
    theme,
    toc,
  });
}

/**
 * Generate table of contents from HTML content
 */
function generateTOC(html: string): string {
  const headingRegex = /<h([2-3]) id="([^"]+)">([^<]+)<\/h[2-3]>/g;
  const headings: Array<{ level: number; id: string; text: string }> = [];
  let match;

  while ((match = headingRegex.exec(html)) !== null) {
    headings.push({
      level: parseInt(match[1]),
      id: match[2],
      text: match[3],
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
    toc += `${indent}<li><a href="#${heading.id}">${heading.text}</a></li>\n`;
  }

  toc += '</ul>\n';
  toc += '</nav>\n';

  return toc;
}

/**
 * Build complete HTML document with styling
 */
function buildHTMLDocument(
  content: string,
  options: {
    title: string;
    theme: 'light' | 'dark' | 'auto';
    toc: string;
  }
): string {
  const { title, theme, toc } = options;

  return `<!DOCTYPE html>
<html lang="en" class="${theme === 'dark' ? 'dark' : ''}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${escapeHtml(title)}</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-core.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/autoloader/prism-autoloader.min.js"></script>
  <link href="https://cdn.jsdelivr.net/npm/prismjs@1.29.0/themes/prism-tomorrow.css" rel="stylesheet" />
  <style>
    body {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem;
      line-height: 1.7;
      font-size: 16px;
    }
    .toc {
      background: #f3f4f6;
      padding: 1.5rem;
      border-radius: 0.5rem;
      margin-bottom: 2rem;
    }
    .dark .toc {
      background: #1f2937;
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
      font-size: 0.9em;
    }
    pre code {
      background: transparent;
      padding: 0;
    }
    img {
      max-width: 100%;
      height: auto;
      border-radius: 0.5rem;
      margin: 1rem 0;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 1rem 0;
    }
    th, td {
      border: 1px solid #e5e7eb;
      padding: 0.5rem;
    }
    th {
      background: #f9fafb;
      font-weight: 600;
    }
    @media (prefers-color-scheme: dark) {
      body {
        background: #111827;
        color: #f9fafb;
      }
      code {
        background: #374151;
      }
      th, td {
        border-color: #374151;
      }
      th {
        background: #1f2937;
      }
    }
  </style>
</head>
<body>
  <h1>${escapeHtml(title)}</h1>
  ${toc}
  ${content}
</body>
</html>`;
}

/**
 * Escape HTML special characters
 */
function escapeHtml(text: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
}

/**
 * Format log content with syntax highlighting
 */
export function formatLogContent(log: string): string {
  // Try to detect JSON logs
  if (log.trim().startsWith('{') || log.trim().startsWith('[')) {
    try {
      const parsed = JSON.parse(log);
      return `<pre><code class="language-json">${escapeHtml(JSON.stringify(parsed, null, 2))}</code></pre>`;
    } catch {
      // Not valid JSON, continue as plain text
    }
  }

  // Format as plain text with line numbers
  const lines = log.split('\n');
  const numbered = lines
    .map((line, i) => `<span class="line-number">${i + 1}</span> ${escapeHtml(line)}`)
    .join('\n');

  return `<pre><code class="language-text">${numbered}</code></pre>`;
}

