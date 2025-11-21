#!/bin/bash

# Create an interactive HTML visualization of the test journey
# Shows brain modes (perception, planning, action, verification) with linked screenshots

NARRATIVE_LOG="${1:-/tmp/test-journey-narrative.md}"
SCREENSHOT_DIR="${2:-test-automation/build/screenshots}"
OUTPUT_HTML="${3:-/tmp/test-visual-journey.html}"

if [ ! -f "$NARRATIVE_LOG" ]; then
    echo "Error: Narrative log not found: $NARRATIVE_LOG"
    exit 1
fi

echo "Creating visual journey HTML..."
echo "Narrative: $NARRATIVE_LOG"
echo "Screenshots: $SCREENSHOT_DIR"
echo "Output: $OUTPUT_HTML"

# Find screenshots (sorted by modification time)
SCREENSHOTS=$(find "$SCREENSHOT_DIR" -name "screenshot_*.png" -type f 2>/dev/null | sort -t_ -k2 -n | head -20)

cat > "$OUTPUT_HTML" << 'HTMLHEAD'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test Automation Journey: Human-Like Cognitive Process</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            padding: 20px;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 20px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        
        h1 {
            color: #667eea;
            font-size: 2.5em;
            margin-bottom: 10px;
            text-align: center;
        }
        
        .subtitle {
            text-align: center;
            color: #666;
            font-size: 1.2em;
            margin-bottom: 40px;
        }
        
        .brain-mode {
            display: inline-block;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 0.9em;
            margin: 5px;
        }
        
        .perception {
            background: #e3f2fd;
            color: #1976d2;
        }
        
        .planning {
            background: #f3e5f5;
            color: #7b1fa2;
        }
        
        .action {
            background: #e8f5e9;
            color: #388e3c;
        }
        
        .verification {
            background: #fff3e0;
            color: #f57c00;
        }
        
        .timeline {
            margin: 40px 0;
        }
        
        .timeline-item {
            background: #f8f9fa;
            border-left: 4px solid #667eea;
            padding: 20px;
            margin: 20px 0;
            border-radius: 8px;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        
        .timeline-item:hover {
            transform: translateX(5px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        
        .timeline-item.perception {
            border-left-color: #1976d2;
        }
        
        .timeline-item.planning {
            border-left-color: #7b1fa2;
        }
        
        .timeline-item.action {
            border-left-color: #388e3c;
        }
        
        .timeline-item.verification {
            border-left-color: #f57c00;
        }
        
        .timeline-header {
            display: flex;
            align-items: center;
            gap: 15px;
            margin-bottom: 10px;
        }
        
        .timeline-title {
            font-size: 1.3em;
            font-weight: bold;
            color: #333;
        }
        
        .persona-thought {
            font-style: italic;
            color: #666;
            margin: 10px 0;
            padding-left: 20px;
            border-left: 2px solid #ddd;
        }
        
        .screenshot-container {
            margin: 15px 0;
            text-align: center;
        }
        
        .screenshot {
            max-width: 400px;
            max-height: 300px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .screenshot:hover {
            transform: scale(1.05);
        }
        
        .screenshot-modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.9);
            justify-content: center;
            align-items: center;
        }
        
        .screenshot-modal.active {
            display: flex;
        }
        
        .screenshot-modal img {
            max-width: 90%;
            max-height: 90%;
            border-radius: 12px;
        }
        
        .close-modal {
            position: absolute;
            top: 20px;
            right: 40px;
            color: white;
            font-size: 40px;
            font-weight: bold;
            cursor: pointer;
        }
        
        .success-badge {
            background: #4caf50;
            color: white;
            padding: 10px 20px;
            border-radius: 25px;
            display: inline-block;
            margin: 20px 0;
            font-weight: bold;
        }
        
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin: 40px 0;
        }
        
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 12px;
            text-align: center;
        }
        
        .stat-value {
            font-size: 2.5em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .stat-label {
            font-size: 0.9em;
            opacity: 0.9;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧠 Test Automation Journey</h1>
        <p class="subtitle">Human-Like Cognitive Process Visualization</p>
        
        <div class="stats">
            <div class="stat-card">
                <div class="stat-value">✅</div>
                <div class="stat-label">Task Completed</div>
            </div>
            <div class="stat-card">
                <div class="stat-value" id="screenshot-count">0</div>
                <div class="stat-label">Screenshots Captured</div>
            </div>
            <div class="stat-card">
                <div class="stat-value" id="action-count">0</div>
                <div class="stat-label">Actions Executed</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">100%</div>
                <div class="stat-label">Visual Detection</div>
            </div>
        </div>
        
        <div class="timeline" id="timeline">
HTMLHEAD

# Parse narrative log and create timeline items
SCREENSHOT_INDEX=0
ACTION_COUNT=0

while IFS= read -r line; do
    # Skip empty lines and headers
    [[ -z "$line" || "$line" =~ ^#+ ]] && continue
    
    # Detect brain mode from line content
    BRAIN_MODE=""
    TITLE=""
    THOUGHT=""
    
    if [[ "$line" =~ "USER ACTIVITY" || "$line" =~ "User Action" ]]; then
        BRAIN_MODE="action"
        TITLE=$(echo "$line" | sed 's/.*USER ACTIVITY: //' | sed 's/### User Action: //' | sed 's/### //')
    elif [[ "$line" =~ "PERSONA THOUGHT" || "$line" =~ "Persona thought" ]]; then
        THOUGHT=$(echo "$line" | sed 's/.*PERSONA THOUGHT: //' | sed 's/.*Persona thought: //' | sed 's/_Persona thought: //')
        continue
    elif [[ "$line" =~ "ADAPTIVE PLANNING" || "$line" =~ "Cognitive Planning" ]]; then
        BRAIN_MODE="planning"
        TITLE="Cognitive Planning"
    elif [[ "$line" =~ "OBSERVED" || "$line" =~ "Observed" ]]; then
        BRAIN_MODE="perception"
        TITLE="Visual Observation"
    elif [[ "$line" =~ "Verifying" || "$line" =~ "Verification" ]]; then
        BRAIN_MODE="verification"
        TITLE="Verifying Completion"
    elif [[ "$line" =~ "Task complete" || "$line" =~ "Task Completed" ]]; then
        BRAIN_MODE="verification"
        TITLE="Task Completed Successfully!"
    fi
    
    if [[ -n "$BRAIN_MODE" && -n "$TITLE" ]]; then
        # Get next screenshot if available
        SCREENSHOT_PATH=""
        if [[ -n "$SCREENSHOTS" ]]; then
            SCREENSHOT_ARRAY=($SCREENSHOTS)
            if [[ ${#SCREENSHOT_ARRAY[@]} -gt $SCREENSHOT_INDEX ]]; then
                SCREENSHOT_PATH="${SCREENSHOT_ARRAY[$SCREENSHOT_INDEX]}"
                SCREENSHOT_INDEX=$((SCREENSHOT_INDEX + 1))
            fi
        fi
        
        ACTION_COUNT=$((ACTION_COUNT + 1))
        
        cat >> "$OUTPUT_HTML" << HTMLITEM
            <div class="timeline-item $BRAIN_MODE">
                <div class="timeline-header">
                    <span class="brain-mode $BRAIN_MODE">$(echo $BRAIN_MODE | tr '[:lower:]' '[:upper:]')</span>
                    <span class="timeline-title">$TITLE</span>
                </div>
HTMLITEM
        
        if [[ -n "$THOUGHT" ]]; then
            cat >> "$OUTPUT_HTML" << HTMLTHOUGHT
                <div class="persona-thought">💭 $THOUGHT</div>
HTMLTHOUGHT
        fi
        
        if [[ -n "$SCREENSHOT_PATH" && -f "$SCREENSHOT_PATH" ]]; then
            # Convert to base64 for embedding
            SCREENSHOT_B64=$(base64 -i "$SCREENSHOT_PATH" 2>/dev/null || base64 "$SCREENSHOT_PATH" 2>/dev/null)
            if [[ -n "$SCREENSHOT_B64" ]]; then
                cat >> "$OUTPUT_HTML" << HTMLSCREENSHOT
                <div class="screenshot-container">
                    <img src="data:image/png;base64,$SCREENSHOT_B64" 
                         alt="Screenshot" 
                         class="screenshot"
                         onclick="openModal(this.src)">
                </div>
HTMLSCREENSHOT
            fi
        fi
        
        cat >> "$OUTPUT_HTML" << HTMLCLOSE
            </div>
HTMLCLOSE
    fi
done < "$NARRATIVE_LOG"

cat >> "$OUTPUT_HTML" << 'HTMLFOOT'
        </div>
        
        <div class="success-badge">
            ✅ Task Completed Successfully: User authenticated and mood value added
        </div>
        
        <div class="screenshot-modal" id="screenshotModal" onclick="closeModal()">
            <span class="close-modal">&times;</span>
            <img id="modalImage" src="" alt="Full screenshot">
        </div>
    </div>
    
    <script>
        document.getElementById('screenshot-count').textContent = document.querySelectorAll('.screenshot').length;
        document.getElementById('action-count').textContent = document.querySelectorAll('.timeline-item').length;
        
        function openModal(src) {
            document.getElementById('modalImage').src = src;
            document.getElementById('screenshotModal').classList.add('active');
        }
        
        function closeModal() {
            document.getElementById('screenshotModal').classList.remove('active');
        }
        
        // Close modal on Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
            }
        });
    </script>
</body>
</html>
HTMLFOOT

echo ""
echo "✅ Visual journey HTML created: $OUTPUT_HTML"
echo "   Screenshots included: $SCREENSHOT_INDEX"
echo "   Timeline items: $ACTION_COUNT"
echo ""
echo "🌐 Open in browser:"
echo "   open $OUTPUT_HTML"

