# A Week with AI-Driven Coding: What I Learned

I spent this week trying to figure out how AI coding tools actually fit into a real workflow. AI is clearly good at generating code, but I wasn't sure how well it would work in practice, or whether it could handle more architectural or conceptual work.

I figured the best way to learn was to build something real. So I started building Electric Sheep—a bit of a personal playground for Android, with a mood tracker as a small feature to start with. Nothing revolutionary, but enough complexity to make the workflow challenges real.

![Electric Sheep app screenshot - showing the mood tracking interface with calm teal-blue design](app-screenshot.png)

*The Electric Sheep app - mood tracking with a calm, pastoral aesthetic*

Turns out the tools themselves are fine—the hard part is building the structure around them. Through exploring AI-driven development, especially when multiple agents (human or AI) are working on the same codebase, I began to notice patterns. Four areas kept coming up: workflow, collaboration, rules and guidelines, and outcomes. They're more interconnected than I expected.

*Note: Throughout this document, when I say "I," it often means I prompted the AI to do something, which then became a rule, and we enhanced the overall workflow together. The learning journey was collaborative—I identified problems, the AI suggested solutions, and together we codified them into persistent rules.*

## The Learning Timeline

Here's how the week unfolded—the problems I hit, when I hit them, and how I solved them:

![Learning Timeline Diagram](timeline-diagram.png)

Each day brought a new problem, which led to a new solution, which became a new rule. The pattern was consistent: notice problem → try prompt → doesn't stick → codify as rule.

## The Branch Problem

Early in the week I thought I was working on feature branches. I probably intended to—it's standard practice, after all. But when I checked what was actually happening, I found the AI was working directly on `main`. I hadn't been explicit enough in my instructions, or the AI hadn't stuck to them. Within hours I had conflicts, confusion, and a mess to clean up.

My first attempt was to be more explicit in prompts: "Make sure you're on a feature branch before making changes." That worked for that session. But the next time, the AI was back on `main` again. I'd corrected the same mistake three times. That's when I realised I needed something more permanent.

I created a cursor rule—a file that Cursor reads automatically and applies to every conversation. Now, every time I start a conversation, Cursor automatically enforces branch isolation. The AI checks the branch before making changes. If it's on `main`, it creates a feature branch first.

This pattern repeated throughout the week: notice problem → try prompt → doesn't stick → codify as rule. **Prompts are ephemeral; rules persist.**

## Parallel Work and Worktrees

When I started running multiple AI agents in parallel, I hit conflicts again. Even with feature branches, both agents were working in the same directory. Untracked files from one agent were causing problems for the other.

I asked Cursor to evaluate the problem and suggest options. It came back with three approaches: separate git clones (disk-heavy), git worktrees (file system isolation with shared history), or better coordination. I went with worktrees—separate working directories that share the same git history. Each agent has its own file system space but they're all working from the same codebase. It solved the untracked file problem completely.

The key insight: when AI is generating code rapidly, you need more structure, not less.

## Coordination

Even with worktrees providing file system isolation, I still needed a way to coordinate which files agents were working on. When multiple agents are modifying the same codebase, you need visibility into what's happening.

I created a central coordination document—a single source of truth for active work. It tracks which files are being modified, who's working on what, and status. This isn't just for AI agents; it's equally valuable for human developers. The difference is that AI agents can't "just ask" what someone else is working on—they need explicit documentation.

The most important pattern: **explicit over implicit**. Don't assume the other agent will figure it out.

## Rules That Actually Work

Rules for AI coding assistants must be actionable, enforceable, contextual, and prioritised. I categorised them into three levels: **Critical** (must follow), **Quality** (should follow), and **Style** (consistency). Rules are only as good as their enforcement—I set up pre-commit checks, CI/CD gates, and code review.

**The key insight:** Rules emerge from repeated mistakes. When the AI makes the same mistake three times, that's a rule waiting to be written.

## Does It Actually Work?

After a week, I can see reduced conflicts, faster iteration, better quality, and improved collaboration. The approach prevents working on main, uncoordinated changes, vague rules, and accumulated technical debt.

## What I Learned

Four things stand out: **Isolation enables parallel work** (feature branches and worktrees prevent conflicts), **Coordination prevents duplication** (a central doc tracks active work), **Rules provide guidance** (clear, actionable rules prevent mistakes), and **Outcomes validate approach** (measure success and iterate).

The goal is to create a predictable, scalable workflow that enables multiple agents (human or AI) to work effectively together while maintaining code quality and avoiding conflicts.

## Final Thoughts

This week taught me that AI-driven development isn't about replacing human judgment—it's about creating the right structure to enable effective collaboration between humans and AI. The tools are impressive, but the workflow matters more. Good isolation, clear coordination, actionable rules, and measured outcomes turn AI coding assistants from helpful tools into powerful collaborators.

*This document is part of a collection of lessons learned from AI-driven coding experiments. See the [learning directory](./README.md) for related documents and resources.*
