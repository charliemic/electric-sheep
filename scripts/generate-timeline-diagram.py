#!/usr/bin/env python3
"""
Generate a professional visual timeline diagram for the learning progression.
Uses Electric Sheep app color scheme with modern design principles.
"""

try:
    from PIL import Image, ImageDraw, ImageFont
    import os
except ImportError:
    print("PIL (Pillow) not available. Install with: pip3 install Pillow")
    exit(1)

# Electric Sheep app color scheme (from Color.kt)
PRIMARY = '#4A7C7E'  # Deep teal-blue
PRIMARY_CONTAINER = '#B8E0E2'  # Light teal
ON_PRIMARY = '#FFFFFF'  # White text on primary

TERTIARY = '#6B8E6B'  # Muted sage green
TERTIARY_CONTAINER = '#D4E6D4'  # Light sage

ERROR = '#C85A4A'  # Warm coral-red
ERROR_CONTAINER = '#FFE5E2'  # Light coral

TEXT_PRIMARY = '#1A1A1A'  # Near black
TEXT_SECONDARY = '#4A4A4A'  # Medium gray

# Create image with transparent background - VERTICAL PROGRESS BAR layout
width, height = 200, 1200  # Progress bar with labels on right
img = Image.new('RGBA', (width, height), color=(255, 255, 255, 0))
draw = ImageDraw.Draw(img)

# Timeline events - mapped to document sections (non-linear scale)
# Positions are based on where sections appear in document, not actual time
events = [
    {
        'position': 0.0,  # Top - Introduction
        'label': 'Started'
    },
    {
        'position': 0.15,  # Early - Branch Problem section
        'label': 'Branch Rule'
    },
    {
        'position': 0.35,  # Mid-early - Worktrees section
        'label': 'Worktrees'
    },
    {
        'position': 0.50,  # Mid - Coordination section
        'label': 'Coordination'
    },
    {
        'position': 0.65,  # Mid-late - Rules section
        'label': 'Rules'
    },
    {
        'position': 0.85,  # Late - Outcomes section
        'label': 'Outcomes'
    },
    {
        'position': 1.0,  # Bottom - Final Thoughts
        'label': 'Complete'
    }
]

# Progress bar layout
bar_x = width // 2  # Center of bar
bar_start = 80
bar_end = height - 80
bar_length = bar_end - bar_start
bar_width = 8  # Thickness of progress bar

# Load fonts
try:
    font_label = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 14)
except:
    font_label = ImageFont.load_default()

# Convert hex colors to RGB tuples
def hex_to_rgb(hex_color):
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

primary_rgb = hex_to_rgb(PRIMARY)
primary_container_rgb = hex_to_rgb(PRIMARY_CONTAINER)

# Draw progress bar background (light)
bar_bg_rect = [bar_x - bar_width // 2, bar_start, bar_x + bar_width // 2, bar_end]
draw.rounded_rectangle(bar_bg_rect, radius=bar_width // 2, fill=primary_container_rgb + (255,), outline=None)

# Draw progress bar fill (solid primary color)
bar_fill_rect = [bar_x - bar_width // 2, bar_start, bar_x + bar_width // 2, bar_end]
draw.rounded_rectangle(bar_fill_rect, radius=bar_width // 2, fill=primary_rgb + (255,), outline=None)

# Draw event markers
for event in events:
    y = bar_start + (event['position'] * bar_length)
    
    # Draw marker circle
    marker_size = 14
    # Shadow
    draw.ellipse([bar_x - marker_size + 1, y - marker_size + 1, 
                  bar_x + marker_size + 1, y + marker_size + 1], 
                 fill=(0, 0, 0, 30), outline=None)
    # Outer ring
    on_primary_rgb = hex_to_rgb(ON_PRIMARY)
    draw.ellipse([bar_x - marker_size, y - marker_size, 
                  bar_x + marker_size, y + marker_size], 
                 fill=on_primary_rgb + (255,), outline=primary_rgb + (255,), width=3)
    # Inner dot
    inner_size = marker_size - 6
    draw.ellipse([bar_x - inner_size, y - inner_size, 
                  bar_x + inner_size, y + inner_size], 
                 fill=primary_rgb + (255,), outline=None)
    
    # Label to the right
    bbox = draw.textbbox((0, 0), event['label'], font=font_label)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    # Label background
    padding_x, padding_y = 6, 4
    label_x = bar_x + marker_size + 8
    label_bg = [label_x, y - text_height // 2 - padding_y,
                label_x + text_width + padding_x * 2, y + text_height // 2 + padding_y]
    draw.rounded_rectangle(label_bg, radius=4, fill=(255, 255, 255, 240), outline=primary_rgb + (255,), width=1)
    draw.text((label_x + padding_x, y - text_height // 2), event['label'], 
              fill=primary_rgb + (255,), font=font_label)

# No title needed - it's a progress bar, minimal design

# Save
output_path = 'docs/learning/timeline-diagram.png'
img.save(output_path)
print(f"✓ Timeline diagram created: {output_path}")
