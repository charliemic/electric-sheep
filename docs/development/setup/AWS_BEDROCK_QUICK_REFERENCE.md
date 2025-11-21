# AWS Bedrock Model Selection - Quick Reference

**One-page guide for daily use**

---

## 🎯 The Simple Rule

**Just code normally** → Cursor uses Sonnet automatically (works for 80% of tasks)

**Only specify a model when:**
- Simple task → Say "Use Haiku: [your request]"
- Complex task → Say "Use Opus: [your request]"

---

## 📊 Model Comparison

| Model | Speed | Cost | When to Use |
|-------|-------|------|-------------|
| **Haiku** | ⚡⚡⚡ Very Fast | $0.80/$4.00 per million | Simple edits, formatting, quick questions |
| **Sonnet** (Default) | ⚡⚡ Fast | $3.00/$15.00 per million | Most development work (automatic) |
| **Opus** | ⚡ Slower | $15.00/$75.00 per million | Complex architecture, difficult debugging |

---

## 💡 When System Will Inform You

**You'll be notified when:**
- ✅ Simple task detected → "Use Haiku to save ~73%?"
- ✅ Complex task detected → "Use Opus for better results?"
- ✅ High-cost operation → "Cost comparison: Sonnet vs Haiku"

**You won't be interrupted for:**
- Standard development tasks
- Quick questions
- Routine code editing

---

## 🚀 Examples

### Example 1: Simple Task
```
You: "Format this code block"
System: 💡 Simple task. Use Haiku? (73% cheaper, faster)
You: [Click "Use Haiku"] or [Continue with Sonnet]
```

### Example 2: Standard Task
```
You: "Implement a new settings screen"
System: [Uses Sonnet automatically, no prompt]
```

### Example 3: Complex Task
```
You: "Design data sync architecture"
System: ⚠️ Complex task. Use Opus? (better results, 5x cost)
You: [Choose based on importance]
```

---

## ⚙️ One-Time Setup

### Option 1: Automated (Recommended)
```bash
./scripts/setup-bedrock-models.sh
```
Follow the on-screen instructions.

### Option 2: Manual
1. Open Cursor Settings (`Cmd + ,`)
2. Go to `Models > AWS Bedrock`
3. Set Region: `eu-west-1`
4. Set Model: `anthropic.claude-sonnet-4-5-20250929-v1:0`
5. Save

**Done!** System handles the rest automatically.

---

## 📈 Cost Savings

**Current (All Sonnet)**: ~$100/month  
**Optimized (Haiku + Sonnet + Opus)**: ~$35-50/month  
**Savings**: 50-65% cost reduction

---

## 🔍 Quick Decision Guide

```
┌─────────────────────────────────────────┐
│  What type of task?                    │
├─────────────────────────────────────────┤
│                                         │
│  🟢 Simple (formatting, rename, etc.)  │
│     → Say "Use Haiku: [task]"          │
│                                         │
│  🟡 Standard (most development work)   │
│     → Just code normally (auto)        │
│                                         │
│  🔴 Complex (architecture, hard bugs)  │
│     → Say "Use Opus: [task]"           │
│                                         │
└─────────────────────────────────────────┘
```

---

**See full guide**: `AWS_BEDROCK_MODEL_OPTIMIZATION.md` for detailed information.

