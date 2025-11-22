#!/usr/bin/env node
/**
 * Development Metrics Dashboard Server (Fastify)
 * 
 * Uses Fastify framework for:
 * - Better performance and future extensibility
 * - TypeScript support (can migrate later)
 * - WebSocket support (for real-time updates)
 * - Plugin ecosystem
 * - Better error handling and logging
 */

import Fastify from 'fastify';
import { readFileSync, existsSync, readdirSync, statSync } from 'fs';
import { execSync } from 'child_process';
import { join, resolve, dirname } from 'path';
import { fileURLToPath } from 'url';
import { savePage, loadPage, listPages, generatePageHTML } from './content-author.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const PROJECT_ROOT = resolve(__dirname, '../..');
const METRICS_DIR = join(PROJECT_ROOT, 'development-metrics');
const COORDINATION_DOC = join(PROJECT_ROOT, 'docs', 'development', 'workflow', 'AGENT_COORDINATION.md');
const PORT = 8080;

const fastify = Fastify({
    logger: true
});

// Helper functions
function getLatestMetric(category, pattern) {
    const categoryDir = join(METRICS_DIR, category);
    if (!existsSync(categoryDir)) {
        return {};
    }
    
    const files = readdirSync(categoryDir)
        .filter(f => f.match(new RegExp(pattern.replace('*', '.*'))))
        .map(f => ({
            name: f,
            path: join(categoryDir, f),
            mtime: statSync(join(categoryDir, f)).mtime
        }))
        .sort((a, b) => b.mtime - a.mtime);
    
    if (files.length === 0) {
        return {};
    }
    
    try {
        const content = readFileSync(files[0].path, 'utf8');
        return JSON.parse(content);
    } catch (e) {
        return {};
    }
}

function getAllMetrics() {
    return {
        complexity: getLatestMetric('complexity', 'complexity_.*\\.json'),
        tests: getLatestMetric('tests', 'test_.*\\.json'),
        prompts: getLatestMetric('prompts', 'prompt_.*\\.json'),
        timestamp: new Date().toISOString()
    };
}

function getActiveWorktrees() {
    try {
        const output = execSync('git worktree list --porcelain', {
            cwd: PROJECT_ROOT,
            encoding: 'utf8',
            timeout: 5000
        });
        
        const worktrees = [];
        let current = {};
        
        for (const line of output.split('\n')) {
            if (line.startsWith('worktree ')) {
                if (current.path) {
                    worktrees.push(current);
                }
                current = { path: line.substring(9) };
            } else if (line.startsWith('HEAD ')) {
                current.commit = line.substring(5);
            } else if (line.startsWith('branch ')) {
                let branch = line.substring(7);
                if (branch.startsWith('refs/heads/')) {
                    branch = branch.substring(11);
                }
                current.branch = branch;
            }
        }
        
        if (current.path) {
            worktrees.push(current);
        }
        
        return worktrees.filter(wt => wt.path !== PROJECT_ROOT);
    } catch (e) {
        fastify.log.error('Error getting worktrees:', e);
        return [];
    }
}

function getActiveBranches() {
    try {
        const output = execSync('git branch --format="%(refname:short)|%(upstream:short)|%(HEAD)"', {
            cwd: PROJECT_ROOT,
            encoding: 'utf8',
            timeout: 5000
        });
        
        return output.split('\n')
            .filter(line => line.trim())
            .map(line => {
                const parts = line.split('|');
                const name = parts[0].trim();
                const upstream = parts[1]?.trim() || '';
                const isCurrent = parts[2]?.trim() === '*';
                
                if (name.match(/^(feature|fix|refactor|docs|test)\//)) {
                    return { name, upstream, current: isCurrent };
                }
                return null;
            })
            .filter(b => b !== null);
    } catch (e) {
        fastify.log.error('Error getting branches:', e);
        return [];
    }
}

function parseCoordinationDoc() {
    if (!existsSync(COORDINATION_DOC)) {
        return [];
    }
    
    try {
        const content = readFileSync(COORDINATION_DOC, 'utf8');
        const activeWork = [];
        
        const taskPattern = /### Task: (.+?)(?=### Task:|$)/gs;
        let match;
        
        while ((match = taskPattern.exec(content)) !== null) {
            const taskContent = match[1];
            const taskName = taskContent.split('\n')[0].trim();
            
            // Skip example entries (they contain placeholder text like <task-name>)
            if (taskName.includes('<') || taskName.includes('Example') || taskName.toLowerCase().includes('example')) {
                continue;
            }
            
            const branchMatch = taskContent.match(/\*\*Branch\*\*: `([^`]+)`/);
            const worktreeMatch = taskContent.match(/\*\*Worktree\*\*: `([^`]+)`/);
            const statusMatch = taskContent.match(/\*\*Status\*\*: (.+)/);
            const filesMatch = taskContent.match(/\*\*Files Modified\*\*: (.+?)(?=\*\*|$)/s);
            
            // Skip if no branch or branch contains placeholders (example format)
            if (!branchMatch || !branchMatch[1] || branchMatch[1].includes('<') || branchMatch[1].includes('task-name')) {
                continue;
            }
            
            let files = [];
            if (filesMatch) {
                files = filesMatch[1]
                    .split('\n')
                    .map(f => f.trim().replace(/^-\s*/, '').replace(/`/g, ''))
                    .filter(f => f && !f.startsWith('**'));
            }
            
            const status = statusMatch ? statusMatch[1].trim() : 'Unknown';
            
            // Only include tasks that are explicitly "In Progress"
            // Don't include "Complete" or example statuses like "In Progress / Complete"
            if (status.includes('In Progress') && !status.includes('Complete') && !status.includes('/')) {
                activeWork.push({
                    task: taskName,
                    branch: branchMatch[1],
                    worktree: worktreeMatch ? worktreeMatch[1] : '',
                    status: status,
                    files: files.slice(0, 10)
                });
            }
        }
        
        return activeWork;
    } catch (e) {
        fastify.log.error('Error parsing coordination doc:', e);
        return [];
    }
}

function getActiveSessions() {
    const sessionsDir = join(METRICS_DIR, 'sessions');
    if (!existsSync(sessionsDir)) {
        return [];
    }
    
    const now = Date.now();
    const oneHourAgo = now - (60 * 60 * 1000); // 1 hour in milliseconds
    
    try {
        const files = readdirSync(sessionsDir)
            .filter(f => f.endsWith('.json'))
            .map(f => ({
                name: f,
                path: join(sessionsDir, f),
                mtime: statSync(join(sessionsDir, f)).mtime
            }))
            .sort((a, b) => b.mtime - a.mtime);
        
        const activeSessions = [];
        
        for (const file of files) {
            try {
                const content = readFileSync(file.path, 'utf8');
                const session = JSON.parse(content);
                
                // Check if session was updated recently (within last hour)
                const lastUpdate = session.lastUpdate ? new Date(session.lastUpdate).getTime() : file.mtime.getTime();
                
                if (lastUpdate > oneHourAgo) {
                    activeSessions.push({
                        sessionId: session.sessionId || file.name.replace('.json', ''),
                        task: session.originalTask || session.currentTask || 'Unknown task',
                        lastUpdate: session.lastUpdate || new Date(file.mtime).toISOString(),
                        startTime: session.startTime || new Date(file.mtime).toISOString(),
                        filesModified: session.filesModified || []
                    });
                }
            } catch (e) {
                // Skip invalid JSON files
                continue;
            }
        }
        
        return activeSessions;
    } catch (e) {
        fastify.log.error('Error getting active sessions:', e);
        return [];
    }
}

function getRecentPrompts() {
    const promptsDir = join(METRICS_DIR, 'prompts');
    if (!existsSync(promptsDir)) {
        return [];
    }
    
    const now = Date.now();
    const oneHourAgo = now - (60 * 60 * 1000); // 1 hour in milliseconds
    
    try {
        const files = readdirSync(promptsDir)
            .filter(f => f.endsWith('.json'))
            .map(f => ({
                name: f,
                path: join(promptsDir, f),
                mtime: statSync(join(promptsDir, f)).mtime
            }))
            .sort((a, b) => b.mtime - a.mtime);
        
        const recentPrompts = [];
        
        for (const file of files.slice(0, 20)) { // Check last 20 prompts
            try {
                const content = readFileSync(file.path, 'utf8');
                const prompt = JSON.parse(content);
                
                const timestamp = prompt.timestamp ? new Date(prompt.timestamp).getTime() : file.mtime.getTime();
                
                if (timestamp > oneHourAgo) {
                    recentPrompts.push({
                        sessionId: prompt.sessionId,
                        timestamp: prompt.timestamp || new Date(file.mtime).toISOString(),
                        task: prompt.prompt ? prompt.prompt.substring(0, 100) : 'Unknown'
                    });
                }
            } catch (e) {
                continue;
            }
        }
        
        return recentPrompts;
    } catch (e) {
        fastify.log.error('Error getting recent prompts:', e);
        return [];
    }
}

function getAgentStatus() {
    const worktrees = getActiveWorktrees();
    const coordinationWork = parseCoordinationDoc();
    const activeSessions = getActiveSessions();
    const recentPrompts = getRecentPrompts();
    
    const agents = [];
    const sessionIds = new Set();
    
    // First, add all worktrees (these are definitely active)
    for (const worktree of worktrees) {
        const branchName = worktree.branch || '';
        const coordEntry = coordinationWork.find(w => w.branch === branchName);
        
        agents.push({
            branch: branchName,
            worktree: worktree.path || '',
            commit: worktree.commit ? worktree.commit.substring(0, 8) : '',
            task: coordEntry ? coordEntry.task : branchName,
            status: coordEntry ? coordEntry.status : 'Active',
            files: coordEntry ? coordEntry.files : [],
            source: 'worktree'
        });
    }
    
    // Then, add coordination doc entries that are "In Progress" but don't have worktrees
    for (const coordEntry of coordinationWork) {
        if (coordEntry.status.includes('In Progress') && 
            !agents.find(a => a.branch === coordEntry.branch)) {
            agents.push({
                branch: coordEntry.branch,
                worktree: coordEntry.worktree || '',
                commit: '',
                task: coordEntry.task,
                status: coordEntry.status,
                files: coordEntry.files,
                source: 'coordination-doc'
            });
        }
    }
    
    // Add active sessions that don't have branches/worktrees
    for (const session of activeSessions) {
        // Check if this session is already represented by a worktree or coordination entry
        const existingAgent = agents.find(a => 
            a.task === session.task || 
            (a.files && a.files.length > 0 && session.filesModified && 
             a.files.some(f => session.filesModified.includes(f)))
        );
        
        if (!existingAgent) {
            agents.push({
                branch: '',
                worktree: '',
                commit: '',
                task: session.task,
                status: 'Active (Session)',
                files: session.filesModified || [],
                source: 'session',
                sessionId: session.sessionId,
                lastUpdate: session.lastUpdate
            });
            sessionIds.add(session.sessionId);
        }
    }
    
    // Add recent prompts from sessions not yet represented
    for (const prompt of recentPrompts) {
        if (prompt.sessionId && !sessionIds.has(prompt.sessionId)) {
            // Check if this session is already represented
            const existingAgent = agents.find(a => a.sessionId === prompt.sessionId);
            
            if (!existingAgent) {
                agents.push({
                    branch: '',
                    worktree: '',
                    commit: '',
                    task: prompt.task,
                    status: 'Active (Recent Prompt)',
                    files: [],
                    source: 'prompt',
                    sessionId: prompt.sessionId,
                    lastUpdate: prompt.timestamp
                });
                sessionIds.add(prompt.sessionId);
            }
        }
    }
    
    return agents;
}

// Main dashboard - compact, no-scroll design
function getDashboardHTML() {
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Electric Sheep Dashboard</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { height: 100vh; overflow: hidden; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            display: flex;
            flex-direction: column;
        }
        .header {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            flex-shrink: 0;
        }
        .header h1 {
            font-size: 1.5em;
            font-weight: 600;
            color: #667eea;
        }
        .header-nav {
            display: flex;
            gap: 16px;
        }
        .header-nav a {
            color: #667eea;
            text-decoration: none;
            font-size: 0.9em;
            padding: 4px 8px;
            border-radius: 4px;
            transition: background 0.2s;
        }
        .header-nav a:hover {
            background: rgba(102, 126, 234, 0.1);
        }
        .header-status {
            display: flex;
            gap: 20px;
            font-size: 0.85em;
            color: #666;
        }
        .refresh-indicator.active::before {
            content: "🔄";
            animation: spin 0.8s linear infinite;
            display: inline-block;
            margin-right: 4px;
        }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .main-content {
            flex: 1;
            padding: 16px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            grid-template-rows: repeat(3, 1fr);
            gap: 12px;
            height: 100%;
            flex: 1;
        }
        .card {
            background: white;
            border-radius: 12px;
            padding: 16px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            display: flex;
            flex-direction: column;
            cursor: pointer;
            transition: all 0.2s ease;
            overflow: hidden;
        }
        .card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }
        .card.clickable {
            cursor: pointer;
        }
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
        }
        .card-title {
            font-size: 0.75em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #888;
            font-weight: 600;
        }
        .card-icon {
            font-size: 1.2em;
        }
        .card-value {
            font-size: 2em;
            font-weight: 700;
            color: #667eea;
            line-height: 1;
            margin: 8px 0;
        }
        .card-label {
            font-size: 0.8em;
            color: #999;
            margin-top: auto;
        }
        .card-large {
            grid-column: span 2;
            grid-row: span 2;
        }
        .card-tall {
            grid-row: span 2;
        }
        .card-wide {
            grid-column: span 2;
        }
        .agent-summary {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }
        .agent-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px;
            background: #f8f9fa;
            border-radius: 6px;
            font-size: 0.85em;
        }
        .agent-name {
            font-weight: 500;
            color: #333;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .agent-status-badge {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 0.75em;
            font-weight: 600;
        }
        .status-active { background: #d1ecf1; color: #0c5460; }
        .status-in-progress { background: #fff3cd; color: #856404; }
        .view-all-link {
            margin-top: 8px;
            text-align: center;
            color: #667eea;
            font-size: 0.85em;
            font-weight: 500;
            text-decoration: none;
        }
        .view-all-link:hover { text-decoration: underline; }
        .test-status {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 1.1em;
            font-weight: 600;
        }
        .test-pass { color: #28a745; }
        .test-fail { color: #dc3545; }
        .test-none { color: #999; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🐑 Electric Sheep</h1>
        <nav class="header-nav">
            <a href="/">Metrics</a>
            <a href="/author">Author</a>
            <a href="/agents">Agents</a>
        </nav>
        <div class="header-status">
            <span class="refresh-indicator" id="refreshIndicator">
                Updated: <span id="lastUpdate">Loading...</span>
            </span>
            <span>Refresh: <span id="refreshCountdown">5</span>s</span>
        </div>
    </div>
    <div class="main-content">
        <div class="dashboard-grid" id="dashboardGrid">
            <div class="card">
                <div class="card-header">
                    <div class="card-title">Source Files</div>
                    <div class="card-icon">📄</div>
                </div>
                <div class="card-value" id="sourceFiles">-</div>
                <div class="card-label">Kotlin files</div>
            </div>
            <div class="card">
                <div class="card-header">
                    <div class="card-title">Test Files</div>
                    <div class="card-icon">🧪</div>
                </div>
                <div class="card-value" id="testFiles">-</div>
                <div class="card-label">Test files</div>
            </div>
            <div class="card clickable" onclick="window.location.href='/agents'">
                <div class="card-header">
                    <div class="card-title">Active Agents</div>
                    <div class="card-icon">🤖</div>
                </div>
                <div class="card-value" id="agentCount">-</div>
                <div class="card-label">Working now</div>
            </div>
            <div class="card">
                <div class="card-header">
                    <div class="card-title">Test Status</div>
                    <div class="card-icon">✓</div>
                </div>
                <div class="card-value test-status" id="testStatus">-</div>
                <div class="card-label" id="testLabel">Last run</div>
            </div>
            <div class="card card-large clickable" onclick="window.location.href='/agents'">
                <div class="card-header">
                    <div class="card-title">Active Agents</div>
                    <div class="card-icon">🤖</div>
                </div>
                <div class="agent-summary" id="agentSummary">
                    <div style="text-align: center; color: #999; padding: 20px;">Loading...</div>
                </div>
                <a href="/agents" class="view-all-link">View All →</a>
            </div>
            <div class="card card-tall clickable" onclick="window.location.href='/complexity'">
                <div class="card-header">
                    <div class="card-title">Code Complexity</div>
                    <div class="card-icon">📊</div>
                </div>
                <div class="card-value" id="totalLines">-</div>
                <div class="card-label">Total lines</div>
                <div style="margin-top: 12px; font-size: 0.9em; color: #666;">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                        <span>Classes:</span>
                        <strong id="classCount">-</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                        <span>Functions:</span>
                        <strong id="functionCount">-</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between;">
                        <span>Test Ratio:</span>
                        <strong id="testRatio">-</strong>
                    </div>
                </div>
                <a href="/complexity" class="view-all-link" style="margin-top: auto;">Details →</a>
            </div>
            <div class="card card-wide clickable" onclick="window.location.href='/tests'">
                <div class="card-header">
                    <div class="card-title">Test Metrics</div>
                    <div class="card-icon">🧪</div>
                </div>
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; align-items: center;">
                    <div>
                        <div style="font-size: 0.75em; color: #888; margin-bottom: 4px;">Passed</div>
                        <div style="font-size: 1.8em; font-weight: 700; color: #28a745;" id="testPassed">-</div>
                    </div>
                    <div>
                        <div style="font-size: 0.75em; color: #888; margin-bottom: 4px;">Failed</div>
                        <div style="font-size: 1.8em; font-weight: 700; color: #dc3545;" id="testFailed">-</div>
                    </div>
                    <div>
                        <div style="font-size: 0.75em; color: #888; margin-bottom: 4px;">Time</div>
                        <div style="font-size: 1.8em; font-weight: 700; color: #667eea;" id="testTime">-</div>
                    </div>
                </div>
                <a href="/tests" class="view-all-link" style="margin-top: 12px;">Details →</a>
            </div>
        </div>
    </div>
    <script>
        let refreshInterval, countdownInterval, countdown = 5;
        async function fetchData() {
            try {
                const response = await fetch('/api/status');
                const data = await response.json();
                updateDashboard(data);
                document.getElementById('lastUpdate').textContent = new Date().toLocaleTimeString();
                countdown = 5;
            } catch (error) { console.error('Error:', error); }
        }
        function updateDashboard(data) {
            const c = data.metrics.complexity || {};
            const t = data.tests || {};
            const agents = data.agents || [];
            
            document.getElementById('sourceFiles').textContent = c.sourceFiles || 0;
            document.getElementById('testFiles').textContent = c.testFiles || 0;
            document.getElementById('agentCount').textContent = agents.length;
            document.getElementById('totalLines').textContent = (c.totalLinesOfCode || 0).toLocaleString();
            document.getElementById('classCount').textContent = c.classCount || 0;
            document.getElementById('functionCount').textContent = c.functionCount || 0;
            document.getElementById('testRatio').textContent = (c.testToSourceRatio || 0).toFixed(2);
            document.getElementById('testPassed').textContent = t.passed || 0;
            document.getElementById('testFailed').textContent = t.failed || 0;
            document.getElementById('testTime').textContent = (t.executionTimeSeconds || 0) + 's';
            
            const testStatusEl = document.getElementById('testStatus');
            const testLabelEl = document.getElementById('testLabel');
            if (t.failed === 0 && t.passed > 0) {
                testStatusEl.textContent = '✓ Passing';
                testStatusEl.className = 'card-value test-status test-pass';
                testLabelEl.textContent = 'All tests pass';
            } else if (t.failed > 0) {
                testStatusEl.textContent = '✗ Failing';
                testStatusEl.className = 'card-value test-status test-fail';
                testLabelEl.textContent = t.failed + ' failed';
            } else {
                testStatusEl.textContent = '—';
                testStatusEl.className = 'card-value test-status test-none';
                testLabelEl.textContent = 'No test data';
            }
            
            const agentSummary = document.getElementById('agentSummary');
            if (agents.length === 0) {
                agentSummary.innerHTML = '<div style="text-align: center; color: #999; padding: 20px;">No active agents</div>';
            } else {
                agentSummary.innerHTML = agents.slice(0, 5).map(a => \`
                    <div class="agent-item">
                        <div class="agent-name" title="\${a.task || a.branch}">\${(a.task || a.branch).substring(0, 30)}\${(a.task || a.branch).length > 30 ? '...' : ''}</div>
                        <div class="agent-status-badge status-\${a.status.toLowerCase().replace(/ /g, '-')}">\${a.status}</div>
                    </div>
                \`).join('') + (agents.length > 5 ? \`<div style="text-align: center; color: #999; font-size: 0.85em; margin-top: 4px;">+ \${agents.length - 5} more</div>\` : '');
            }
        }
        function startCountdown() {
            countdownInterval = setInterval(() => {
                countdown--;
                document.getElementById('refreshCountdown').textContent = countdown;
                if (countdown <= 0) countdown = 5;
            }, 1000);
        }
        fetchData();
        startCountdown();
        refreshInterval = setInterval(fetchData, 5000);
        setInterval(() => {
            document.getElementById('refreshIndicator').classList.add('active');
            setTimeout(() => document.getElementById('refreshIndicator').classList.remove('active'), 800);
        }, 5000);
    </script>
</body>
</html>`;
}

// Detail pages - also no-scroll
function getAgentsPageHTML() {
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Active Agents - Electric Sheep</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { height: 100vh; overflow: hidden; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            display: flex;
            flex-direction: column;
        }
        .header {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .header h1 {
            font-size: 1.5em;
            font-weight: 600;
            color: #667eea;
        }
        .back-link {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }
        .back-link:hover { text-decoration: underline; }
        .main-content {
            flex: 1;
            padding: 16px;
            overflow-y: auto;
        }
        .agents-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 12px;
        }
        .agent-card {
            background: white;
            border-radius: 12px;
            padding: 16px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .agent-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            padding-bottom: 12px;
            border-bottom: 1px solid #eee;
        }
        .agent-name {
            font-weight: 600;
            font-size: 1.1em;
            color: #333;
        }
        .agent-status-badge {
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 0.8em;
            font-weight: 600;
        }
        .status-active { background: #d1ecf1; color: #0c5460; }
        .status-in-progress { background: #fff3cd; color: #856404; }
        .agent-details {
            font-size: 0.9em;
            color: #666;
        }
        .agent-details div {
            margin: 6px 0;
        }
        .agent-details code {
            background: #f8f9fa;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="header">
        <a href="/" class="back-link">← Back</a>
        <h1>🤖 Active Agents</h1>
        <div></div>
    </div>
    <div class="main-content">
        <div class="agents-grid" id="agentsGrid">
            <div style="text-align: center; color: #999; padding: 40px;">Loading...</div>
        </div>
    </div>
    <script>
        async function fetchData() {
            try {
                const response = await fetch('/api/agents');
                const data = await response.json();
                updateAgents(data.agents || []);
            } catch (error) { console.error('Error:', error); }
        }
        function updateAgents(agents) {
            const grid = document.getElementById('agentsGrid');
            if (agents.length === 0) {
                grid.innerHTML = '<div style="text-align: center; color: #999; padding: 40px; grid-column: 1/-1;">No active agents</div>';
            } else {
                grid.innerHTML = agents.map(a => \`
                    <div class="agent-card">
                        <div class="agent-header">
                            <div class="agent-name">\${a.task || a.branch}</div>
                            <div class="agent-status-badge status-\${a.status.toLowerCase().replace(/ /g, '-')}">\${a.status}</div>
                        </div>
                        <div class="agent-details">
                            <div><strong>Branch:</strong> <code>\${a.branch}</code></div>
                            \${a.worktree ? \`<div><strong>Worktree:</strong> <code>\${a.worktree}</code></div>\` : ''}
                            \${a.commit ? \`<div><strong>Commit:</strong> <code>\${a.commit}</code></div>\` : ''}
                            \${a.files && a.files.length > 0 ? \`
                                <div style="margin-top: 12px;">
                                    <strong>Files (\${a.files.length}):</strong>
                                    <div style="margin-top: 6px; font-size: 0.85em; color: #888;">
                                        \${a.files.slice(0, 8).map(f => \`<div>• \${f}</div>\`).join('')}
                                        \${a.files.length > 8 ? \`<div style="color: #999; font-style: italic;">... and \${a.files.length - 8} more</div>\` : ''}
                                    </div>
                                </div>
                            \` : ''}
                        </div>
                    </div>
                \`).join('');
            }
        }
        fetchData();
        setInterval(fetchData, 5000);
    </script>
</body>
</html>`;
}

function getComplexityPageHTML() {
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Code Complexity - Electric Sheep</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { height: 100vh; overflow: hidden; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            display: flex;
            flex-direction: column;
        }
        .header {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .header h1 {
            font-size: 1.5em;
            font-weight: 600;
            color: #667eea;
        }
        .back-link {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }
        .main-content {
            flex: 1;
            padding: 16px;
            overflow-y: auto;
        }
        .metrics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 12px;
        }
        .metric-card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .metric-label {
            font-size: 0.85em;
            color: #888;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }
        .metric-value {
            font-size: 2.5em;
            font-weight: 700;
            color: #667eea;
        }
    </style>
</head>
<body>
    <div class="header">
        <a href="/" class="back-link">← Back</a>
        <h1>📊 Code Complexity</h1>
        <div></div>
    </div>
    <div class="main-content">
        <div class="metrics-grid" id="metricsGrid">
            <div style="text-align: center; color: #999; padding: 40px;">Loading...</div>
        </div>
    </div>
    <script>
        async function fetchData() {
            try {
                const response = await fetch('/api/metrics');
                const data = await response.json();
                updateMetrics(data.complexity || {});
            } catch (error) { console.error('Error:', error); }
        }
        function updateMetrics(c) {
            document.getElementById('metricsGrid').innerHTML = \`
                <div class="metric-card">
                    <div class="metric-label">Source Files</div>
                    <div class="metric-value">\${c.sourceFiles || 0}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Test Files</div>
                    <div class="metric-value">\${c.testFiles || 0}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Total Lines</div>
                    <div class="metric-value">\${(c.totalLinesOfCode || 0).toLocaleString()}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Classes</div>
                    <div class="metric-value">\${c.classCount || 0}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Functions</div>
                    <div class="metric-value">\${c.functionCount || 0}</div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Test Ratio</div>
                    <div class="metric-value">\${(c.testToSourceRatio || 0).toFixed(2)}</div>
                </div>
            \`;
        }
        fetchData();
        setInterval(fetchData, 5000);
    </script>
</body>
</html>`;
}

function getTestsPageHTML() {
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test Metrics - Electric Sheep</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { height: 100vh; overflow: hidden; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #333;
            display: flex;
            flex-direction: column;
        }
        .header {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            padding: 12px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .header h1 {
            font-size: 1.5em;
            font-weight: 600;
            color: #667eea;
        }
        .back-link {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }
        .main-content {
            flex: 1;
            padding: 16px;
            overflow-y: auto;
        }
        .test-card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 12px;
        }
        .test-stat {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid #eee;
        }
        .test-stat:last-child { border-bottom: none; }
        .test-stat-label {
            font-size: 0.9em;
            color: #666;
        }
        .test-stat-value {
            font-size: 1.5em;
            font-weight: 700;
        }
        .test-pass { color: #28a745; }
        .test-fail { color: #dc3545; }
        .test-time { color: #667eea; }
    </style>
</head>
<body>
    <div class="header">
        <a href="/" class="back-link">← Back</a>
        <h1>🧪 Test Metrics</h1>
        <div></div>
    </div>
    <div class="main-content">
        <div class="test-card" id="testCard">
            <div style="text-align: center; color: #999; padding: 40px;">Loading...</div>
        </div>
    </div>
    <script>
        async function fetchData() {
            try {
                const response = await fetch('/api/metrics');
                const data = await response.json();
                updateTests(data.tests || {});
            } catch (error) { console.error('Error:', error); }
        }
        function updateTests(t) {
            const status = t.failed === 0 && t.passed > 0 ? '✓ All Passing' : 
                          t.failed > 0 ? \`✗ \${t.failed} Failed\` : 'No test data';
            const statusClass = t.failed === 0 && t.passed > 0 ? 'test-pass' : 
                               t.failed > 0 ? 'test-fail' : '';
            document.getElementById('testCard').innerHTML = \`
                <div class="test-stat">
                    <div class="test-stat-label">Status</div>
                    <div class="test-stat-value \${statusClass}">\${status}</div>
                </div>
                <div class="test-stat">
                    <div class="test-stat-label">Passed</div>
                    <div class="test-stat-value test-pass">\${t.passed || 0}</div>
                </div>
                <div class="test-stat">
                    <div class="test-stat-label">Failed</div>
                    <div class="test-stat-value test-fail">\${t.failed || 0}</div>
                </div>
                <div class="test-stat">
                    <div class="test-stat-label">Execution Time</div>
                    <div class="test-stat-value test-time">\${t.executionTimeSeconds || 0}s</div>
                </div>
                <div class="test-stat">
                    <div class="test-stat-label">Last Run</div>
                    <div class="test-stat-value" style="font-size: 1em;">\${t.timestamp || 'N/A'}</div>
                </div>
            \`;
        }
        fetchData();
        setInterval(fetchData, 5000);
    </script>
</body>
</html>`;
}

// Fastify routes
fastify.get('/', async (request, reply) => {
    reply.type('text/html');
    return getDashboardHTML();
});

fastify.get('/agents', async (request, reply) => {
    reply.type('text/html');
    return getAgentsPageHTML();
});

fastify.get('/complexity', async (request, reply) => {
    reply.type('text/html');
    return getComplexityPageHTML();
});

fastify.get('/tests', async (request, reply) => {
    reply.type('text/html');
    return getTestsPageHTML();
});

fastify.get('/api/metrics', async (request, reply) => {
    return getAllMetrics();
});

fastify.get('/api/agents', async (request, reply) => {
    return {
        agents: getAgentStatus(),
        timestamp: new Date().toISOString()
    };
});

fastify.get('/api/status', async (request, reply) => {
    return {
        metrics: getAllMetrics(),
        agents: getAgentStatus(),
        timestamp: new Date().toISOString()
    };
});

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
        const pageId = id || `page-${Date.now()}`;
        const page = savePage(pageId, content, metadata);
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
        reply.type('text/html');
        return '<h1>Page not found</h1><p>The requested page does not exist.</p>';
    }
    
    const html = generatePageHTML(page, {
        theme: page.metadata.theme || 'light',
        includeTOC: page.metadata.includeTOC !== false
    });
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
            const testMetric = getLatestMetric('tests', 'test_.*\\.json');
            data = testMetric;
        } else if (source.startsWith('logs:')) {
            // Log loading would be implemented here
            reply.code(501);
            return { success: false, error: 'Log loading not yet implemented' };
        } else {
            reply.code(404);
            return { success: false, error: 'Unknown data source' };
        }
        
        return { success: true, data };
    } catch (error) {
        reply.code(500);
        return { success: false, error: error.message };
    }
});

// API endpoint for chart data
fastify.get('/api/author/chart/:type', async (request, reply) => {
    const { type } = request.params;
    
    try {
        // Generate chart config based on type
        let config;
        if (type === 'complexity-trend') {
            // Would generate complexity trend chart config
            config = {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: 'Complexity',
                        data: []
                    }]
                }
            };
        } else {
            reply.code(404);
            return { success: false, error: 'Unknown chart type' };
        }
        
        return { success: true, config };
    } catch (error) {
        reply.code(500);
        return { success: false, error: error.message };
    }
});

// Authoring interface HTML functions
function getAuthoringInterfaceHTML() {
    const pages = listPages();
    
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Content Authoring - Electric Sheep Dashboard</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
    <style>
        body { max-width: 1200px; margin: 0 auto; padding: 2rem; }
        .page-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; margin: 2rem 0; }
        .page-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; background: white; }
        .page-card h3 { margin: 0 0 0.5rem 0; }
        .page-card-meta { color: #6b7280; font-size: 0.85em; margin-top: 0.5rem; }
        .actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
        .btn { padding: 0.5rem 1rem; border-radius: 4px; text-decoration: none; display: inline-block; }
        .btn-primary { background: #3b82f6; color: white; }
        .btn-secondary { background: #6b7280; color: white; }
    </style>
</head>
<body>
    <h1>Content Authoring</h1>
    <p>Create information-rich narrative pages with data integration.</p>
    
    <div class="actions">
        <a href="/author/new" class="btn btn-primary">Create New Page</a>
    </div>
    
    <h2>Your Pages</h2>
    ${pages.length === 0 ? '<p>No pages yet. Create your first page!</p>' : ''}
    <div class="page-list">
        ${pages.map(page => `
            <div class="page-card">
                <h3>${escapeHtml(page.title)}</h3>
                <div class="page-card-meta">
                    Updated: ${new Date(page.updatedAt).toLocaleString()}
                </div>
                <div class="actions">
                    <a href="/pages/${page.id}" class="btn btn-primary">View</a>
                    <a href="/author/edit/${page.id}" class="btn btn-secondary">Edit</a>
                </div>
            </div>
        `).join('')}
    </div>
</body>
</html>`;
}

function getNewPageEditorHTML() {
    return getPageEditorHTML(null);
}

function getEditPageEditorHTML(page) {
    return getPageEditorHTML(page);
}

function getPageEditorHTML(page) {
    const isEdit = page !== null;
    const content = page ? page.content : '';
    const title = page ? page.metadata.title : '';
    const theme = page ? page.metadata.theme : 'light';
    const includeTOC = page ? page.metadata.includeTOC : true;
    
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Edit' : 'New'} Page - Electric Sheep Dashboard</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css">
    <style>
        body { max-width: 1400px; margin: 0 auto; padding: 2rem; }
        .editor-container { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin: 2rem 0; }
        .editor-panel { border: 1px solid #e5e7eb; border-radius: 8px; padding: 1rem; }
        .editor-panel h3 { margin-top: 0; }
        textarea { width: 100%; min-height: 500px; font-family: monospace; font-size: 0.9em; }
        .preview-panel { background: #f9fafb; }
        .form-group { margin: 1rem 0; }
        .form-group label { display: block; margin-bottom: 0.5rem; font-weight: 600; }
        .form-group input, .form-group select { width: 100%; padding: 0.5rem; }
        .actions { display: flex; gap: 1rem; margin-top: 2rem; }
        .btn { padding: 0.75rem 1.5rem; border-radius: 4px; border: none; cursor: pointer; font-size: 1em; }
        .btn-primary { background: #3b82f6; color: white; }
        .btn-secondary { background: #6b7280; color: white; }
    </style>
</head>
<body>
    <h1>${isEdit ? 'Edit' : 'Create New'} Page</h1>
    
    <form id="page-form">
        <div class="form-group">
            <label for="page-title">Page Title</label>
            <input type="text" id="page-title" value="${escapeHtml(title)}" required>
        </div>
        
        <div class="form-group">
            <label for="page-theme">Theme</label>
            <select id="page-theme">
                <option value="light" ${theme === 'light' ? 'selected' : ''}>Light</option>
                <option value="dark" ${theme === 'dark' ? 'selected' : ''}>Dark</option>
            </select>
        </div>
        
        <div class="form-group">
            <label>
                <input type="checkbox" id="include-toc" ${includeTOC ? 'checked' : ''}>
                Include Table of Contents
            </label>
        </div>
        
        <div class="editor-container">
            <div class="editor-panel">
                <h3>Markdown Editor</h3>
                <textarea id="page-content" placeholder="Write your content in Markdown...&#10;&#10;You can use data blocks:&#10;{{metrics:latest}} - Latest metrics&#10;{{tests:latest}} - Latest test results&#10;{{logs:path/to/log.log}} - Log file&#10;{{chart:complexity-trend}} - Interactive chart">${escapeHtml(content)}</textarea>
            </div>
            <div class="editor-panel preview-panel">
                <h3>Preview</h3>
                <div id="preview"></div>
            </div>
        </div>
        
        <div class="actions">
            <button type="submit" class="btn btn-primary">${isEdit ? 'Update' : 'Create'} Page</button>
            <a href="/author" class="btn btn-secondary">Cancel</a>
        </div>
    </form>
    
    <script>
        const form = document.getElementById('page-form');
        const contentTextarea = document.getElementById('page-content');
        const preview = document.getElementById('preview');
        const pageId = '${page ? page.id : ''}';
        
        // Simple markdown preview (basic implementation)
        function updatePreview() {
            const content = contentTextarea.value;
            // Basic markdown rendering (would use marked library in production)
            preview.innerHTML = '<p>Preview will be available after saving</p>';
        }
        
        contentTextarea.addEventListener('input', updatePreview);
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const pageData = {
                ${isEdit ? `id: '${page.id}',` : ''}
                content: contentTextarea.value,
                metadata: {
                    title: document.getElementById('page-title').value,
                    theme: document.getElementById('page-theme').value,
                    includeTOC: document.getElementById('include-toc').checked
                }
            };
            
            try {
                const response = await fetch('/api/author/save', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(pageData)
                });
                
                const result = await response.json();
                
                if (result.success) {
                    window.location.href = '/pages/' + result.page.id;
                } else {
                    alert('Error: ' + result.error);
                }
            } catch (error) {
                alert('Error saving page: ' + error.message);
            }
        });
        
        updatePreview();
    </script>
</body>
</html>`;
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}

// Start server
const start = async () => {
    try {
        await fastify.listen({ port: PORT, host: '0.0.0.0' });
        console.log(`🚀 Dashboard server running on http://localhost:${PORT}`);
        console.log(`📊 Dashboard: http://localhost:${PORT}/`);
        console.log(`📝 Author: http://localhost:${PORT}/author`);
        console.log(`📡 API: http://localhost:${PORT}/api/status`);
        console.log('\nPress Ctrl+C to stop\n');
    } catch (err) {
        fastify.log.error(err);
        process.exit(1);
    }
};

start();
