#!/usr/bin/env python3
"""
Convert Markdown to HTML for sharing.

NOTE: This script embeds images (screenshots, diagrams) directly in the HTML as base64.
When updating this script, DO NOT remove or modify the image embedding logic
in the convert_image_to_base64 function - the embedded images section should
be preserved to avoid breaking image display in the HTML output.

SECURITY NOTE: This script must NEVER automatically prompt, send, or trigger emails
to other users. Such functionality would be intrusive and is explicitly prohibited.
"""

import sys
import re
import os
import base64
from pathlib import Path

def convert_image_to_base64(image_path, md_file_dir):
    """
    Convert image to base64 data URI.
    
    NOTE: This function embeds images directly in HTML. Do not remove or modify
    this logic - it's critical for standalone HTML files that need to work
    without external image dependencies.
    """
    # Handle relative paths from markdown file location
    if not os.path.isabs(image_path):
        image_path = os.path.join(md_file_dir, image_path)
    
    if not os.path.exists(image_path):
        print(f"Warning: Image not found: {image_path}")
        return None
    
    # Determine MIME type from extension
    ext = os.path.splitext(image_path)[1].lower()
    mime_types = {
        '.png': 'image/png',
        '.jpg': 'image/jpeg',
        '.jpeg': 'image/jpeg',
        '.gif': 'image/gif',
        '.svg': 'image/svg+xml',
        '.webp': 'image/webp'
    }
    mime_type = mime_types.get(ext, 'image/png')
    
    # Read and encode image
    with open(image_path, 'rb') as img_file:
        img_data = base64.b64encode(img_file.read()).decode('utf-8')
    
    return f"data:{mime_type};base64,{img_data}"

def markdown_to_html(md_file, html_file, title):
    """Convert markdown to HTML."""
    md_file_path = Path(md_file)
    md_file_dir = str(md_file_path.parent.absolute())
    
    with open(md_file, 'r', encoding='utf-8') as f:
        md_content = f.read()
    
    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
    <script>
        // Section to timeline position mapping (non-linear, based on document position)
        var sectionToPosition = {{
            'the-learning-timeline': 0.0,  // Top - Introduction
            'the-branch-problem': 0.15,  // Early
            'parallel-work-and-worktrees': 0.35,  // Mid-early
            'coordination': 0.50,  // Mid
            'rules-that-actually-work': 0.65,  // Mid-late
            'does-it-actually-work': 0.85,  // Late
            'what-i-learned': 0.90,  // Late
            'final-thoughts': 1.0  // Bottom
        }};
        
        // Timeline dimensions in diagram
        var timelineStart = 80; // Top padding in diagram
        var timelineEnd = 1120; // Bottom padding (height - 80)
        var timelineLength = timelineEnd - timelineStart;
        
        // Timeline sidebar is hidden for now
    </script>
    <style>
        body {{
            max-width: 900px;
            margin: 0 auto;
            padding: 0;
            line-height: 1.7;
            font-size: 16px;
        }}
        .content-wrapper {{
            padding: 20px;
            padding-top: 24px;
        }}
        .timeline-sidebar {{
            display: none;
        }}
        .screenshot-container {{
            float: right;
            width: 180px;
            margin: 0 0 20px 24px;
            z-index: 10;
        }}
        .main-content {{
            flex: 1;
            max-width: 100%;
        }}
        h1 {{
            margin-top: 0;
            margin-bottom: 16px;
        }}
        h2 {{
            margin-top: 32px;
            margin-bottom: 16px;
        }}
        h3 {{
            margin-top: 24px;
            margin-bottom: 12px;
        }}
        p {{
            margin: 0 0 16px 0;
        }}
        code {{
            background: #f4f4f4;
            padding: 2px 6px;
            border-radius: 3px;
        }}
        pre code {{
            background: #f4f4f4;
            padding: 10px;
            display: block;
            border-radius: 5px;
            overflow-x: auto;
        }}
        img {{
            border-radius: 5px;
        }}
        img.screenshot {{
            max-width: 180px;
            width: 180px;
            height: auto;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15), 0 2px 8px rgba(0, 0, 0, 0.1);
            border: 2px solid rgba(255, 255, 255, 0.8);
        }}
        img.diagram {{
            max-width: 100%;
            height: auto;
            margin: 20px 0;
        }}
        .image-caption {{
            font-style: italic;
            color: #666;
            text-align: center;
            margin-top: -15px;
            margin-bottom: 20px;
            clear: both;
        }}
    </style>
</head>
<body>
    <div class="content-wrapper">
"""
    
    lines = md_content.split('\n')
    in_code_block = False
    in_list = False
    prev_was_image = False
    screenshot_inserted = False
    screenshot_data = None
    screenshot_caption = None
    
    # First pass: find screenshot
    for line in lines:
        image_match = re.match(r'!\[([^\]]*)\]\(([^)]+)\)', line.strip())
        if image_match and 'screenshot' in image_match.group(2).lower():
            screenshot_path = image_match.group(2)
            screenshot_data = convert_image_to_base64(screenshot_path, md_file_dir)
            screenshot_caption = image_match.group(1)
            break
        # Also check for caption
        if line.strip().startswith('*') and line.strip().endswith('*'):
            if screenshot_caption is None:
                screenshot_caption = line.strip('*').strip()
    
    for i, line in enumerate(lines):
        stripped = line.strip()
        
        # Handle image references: ![alt](path)
        # NOTE: This image embedding logic must be preserved - do not remove
        image_match = re.match(r'!\[([^\]]*)\]\(([^)]+)\)', stripped)
        if image_match:
            alt_text = image_match.group(1)
            image_path = image_match.group(2)
            
            # Convert image to base64
            base64_data = convert_image_to_base64(image_path, md_file_dir)
            
            if base64_data:
                if in_list:
                    html += '</ul>\n'
                    in_list = False
                # Determine image class based on filename
                if 'screenshot' in image_path.lower():
                    # Skip screenshot here - we'll insert it in sidebar later
                    # Don't set screenshot_inserted = True here, we'll do it when we actually insert
                    continue
                elif 'diagram' in image_path.lower() or 'timeline' in image_path.lower():
                    # Timeline diagram is hidden for now
                    continue
                else:
                    img_class = 'diagram'  # default
                    html += f'<img src="{base64_data}" alt="{alt_text}" class="{img_class}" />\n'
                    prev_was_image = True
            else:
                # Fallback to regular img tag if conversion failed
                img_class = 'screenshot' if 'screenshot' in image_path.lower() else 'diagram'
                html += f'<img src="{image_path}" alt="{alt_text}" class="{img_class}" />\n'
                prev_was_image = True
            continue
        
        # Handle italic caption lines (often follow images)
        if prev_was_image and stripped.startswith('*') and stripped.endswith('*'):
            caption = stripped.strip('*')
            html += f'<p class="image-caption">{caption}</p>\n'
            prev_was_image = False
            continue
        
        # Insert screenshot in fixed position (never moves)
        if not screenshot_inserted and screenshot_data and i >= 10 and i <= 12 and stripped and not stripped.startswith('#') and not stripped.startswith('!') and not stripped.startswith('*') and not stripped.startswith('```') and ('Turns out' in stripped or 'tools themselves' in stripped):
            # Insert screenshot in fixed container (never scrolls)
            html += '<div class="screenshot-container">'
            caption_text = screenshot_caption if screenshot_caption else 'Screenshot'
            html += f'<img src="{screenshot_data}" alt="{caption_text}" class="screenshot" />'
            html += '</div>'
            screenshot_inserted = True
        
        prev_was_image = False
        
        if stripped.startswith('```'):
            if in_code_block:
                html += '</pre></code>\n'
                in_code_block = False
            else:
                code_lang = stripped[3:].strip()
                html += f'<pre><code class="language-{code_lang}">\n'
                in_code_block = True
        elif in_code_block:
            # Preserve original line content including indentation
            # Escape HTML entities to prevent breaking the code block
            escaped_line = line.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            html += escaped_line + '\n'
        elif stripped.startswith('# '):
            if in_list:
                html += '</ul>\n' if not stripped.startswith('-') else ''
                in_list = False
            html += f'<h1>{stripped[2:]}</h1>\n'
        elif stripped.startswith('## '):
            if in_list:
                html += '</ul>\n'
                in_list = False
            # Add ID to h2 for section tracking
            section_id = stripped[3:].lower().replace(' ', '-').replace(':', '').replace('?', '')
            html += f'<h2 id="{section_id}">{stripped[3:]}</h2>\n'
        elif stripped.startswith('### '):
            if in_list:
                html += '</ul>\n'
                in_list = False
            html += f'<h3>{stripped[4:]}</h3>\n'
        elif stripped.startswith('- ') or stripped.startswith('* '):
            if not in_list:
                html += '<ul>\n'
                in_list = True
            content = stripped[2:]
            # Handle inline formatting
            content = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', content)
            content = re.sub(r'\*(.*?)\*', r'<em>\1</em>', content)
            content = re.sub(r'`(.*?)`', r'<code>\1</code>', content)
            html += f'<li>{content}</li>\n'
        elif re.match(r'^\d+\.\s+', stripped):
            if not in_list:
                html += '<ol>\n'
                in_list = True
            content = re.sub(r'^\d+\.\s+', '', stripped)
            content = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', content)
            content = re.sub(r'\*(.*?)\*', r'<em>\1</em>', content)
            content = re.sub(r'`(.*?)`', r'<code>\1</code>', content)
            html += f'<li>{content}</li>\n'
        elif stripped == '':
            if in_list:
                html += '</ul>\n' if not any(l.strip().startswith(('-', '*')) or re.match(r'^\d+\.', l.strip()) for l in lines[i+1:i+3] if l.strip()) else ''
                in_list = False
            html += '<p></p>\n'
        else:
            if in_list:
                html += '</ul>\n'
                in_list = False
            # Handle inline formatting
            text = stripped
            text = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', text)
            text = re.sub(r'\*(.*?)\*', r'<em>\1</em>', text)
            text = re.sub(r'`(.*?)`', r'<code>\1</code>', text)
            html += f'<p>{text}</p>\n'
    
    if in_list:
        html += '</ul>\n'
    
    html += """
    </div>
</body>
</html>
"""
    
    with open(html_file, 'w', encoding='utf-8') as f:
        f.write(html)
    
    print("✓ HTML file created")

if __name__ == '__main__':
    if len(sys.argv) < 4:
        print("Usage: python scripts/md-to-html.py <input.md> <output.html> <title>")
        sys.exit(1)
    
    markdown_to_html(sys.argv[1], sys.argv[2], sys.argv[3])

