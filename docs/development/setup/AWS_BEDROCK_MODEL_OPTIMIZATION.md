# AWS Bedrock Model Optimization Guide for Cursor

**Purpose**: Optimize AWS Bedrock model selection for cost, activity types, and development workflow efficiency when using Cursor's auto model selection.

## Current Setup

- **Region**: `eu-west-1`
- **Test Model**: `anthropic.claude-sonnet-4-5-20250929-v1:0`
- **Cursor Feature**: Auto model selection enabled
- **Workflow**: Kotlin/Android development, test automation, documentation, architecture

---

## Quick Start: Easy Configuration

### Automated Setup (Recommended)

**Run the setup script:**
```bash
./scripts/setup-bedrock-models.sh
```

This script will:
- ✅ Check AWS credentials
- ✅ Verify Bedrock access
- ✅ Display recommended configuration
- ✅ Provide step-by-step setup instructions
- ✅ Show cost estimates

### Manual Setup (Alternative)

1. **Open Cursor Settings**
   - Press `Cmd + ,` (macOS) or `Ctrl + ,` (Windows/Linux)

2. **Configure AWS Bedrock**
   - Go to `Models > AWS Bedrock`
   - **Region**: `eu-west-1`
   - **Primary Model**: `anthropic.claude-sonnet-4-5-20250929-v1:0`
   - **Save**

3. **That's it!** Cursor will use Sonnet for most tasks automatically.

**Note**: The AI assistant will automatically suggest model alternatives when helpful (see Cursor rule: `.cursor/rules/bedrock-model-optimization.mdc`).

### When You Need to Choose a Model

**Cursor will automatically use Sonnet for most tasks.** You only need to manually select a model when:

#### ✅ Use Haiku (Faster & Cheaper) When:
- Quick formatting or style fixes
- Simple variable renaming
- Adding basic comments
- Simple syntax corrections
- **How**: In Cursor Chat, you can specify: "Use Haiku: [your request]"

#### ✅ Use Opus (More Capable) When:
- Complex architectural decisions
- Difficult debugging that Sonnet can't solve
- Security-sensitive code reviews
- Multi-file refactoring across many files
- **How**: In Cursor Chat, specify: "Use Opus: [your request]"

#### ℹ️ System Will Inform You When:
- Auto-selection might not be optimal (e.g., very simple task that could use Haiku)
- Task complexity suggests Opus might be better
- Cost considerations (e.g., "This simple task could use Haiku to save ~73%")

### Default Behavior (No Action Needed)

**For 80% of tasks, just use Cursor normally:**
- Chat, Composer, and code completion will use Sonnet automatically
- No need to specify a model
- System optimizes automatically

---

## Available Bedrock Models & Pricing (2025)

### Anthropic Claude Models

#### Claude 3.5 Sonnet (Recommended Default)
- **Model ID**: `anthropic.claude-3-5-sonnet-20241022-v2:0` (latest) or `anthropic.claude-sonnet-4-5-20250929-v1:0`
- **Input**: $3.00 per million tokens
- **Output**: $15.00 per million tokens
- **Best For**: 
  - ✅ General code generation and editing
  - ✅ Complex reasoning tasks
  - ✅ Architecture decisions
  - ✅ Code reviews
  - ✅ Documentation writing
- **Speed**: Fast (good balance)
- **Cost Efficiency**: ⭐⭐⭐⭐ (4/5)

#### Claude 3.5 Haiku (Cost-Effective)
- **Model ID**: `anthropic.claude-3-5-haiku-20241022-v2:0`
- **Input**: $0.80 per million tokens
- **Output**: $4.00 per million tokens
- **Best For**:
  - ✅ Simple code edits
  - ✅ Quick questions
  - ✅ Code completion
  - ✅ Simple refactoring
  - ✅ Formatting and style fixes
- **Speed**: Very fast
- **Cost Efficiency**: ⭐⭐⭐⭐⭐ (5/5)
- **Cost Savings**: ~73% cheaper than Sonnet

#### Claude 3 Opus (High Complexity)
- **Model ID**: `anthropic.claude-3-opus-20240229-v1:0`
- **Input**: $15.00 per million tokens
- **Output**: $75.00 per million tokens
- **Best For**:
  - ✅ Complex architectural decisions
  - ✅ Difficult debugging
  - ✅ Research and analysis
  - ✅ Complex multi-file refactoring
- **Speed**: Slower
- **Cost Efficiency**: ⭐⭐ (2/5) - Use sparingly
- **Cost**: 5x more expensive than Sonnet

#### Claude 3 Sonnet (Legacy)
- **Model ID**: `anthropic.claude-3-sonnet-20240229-v1:0`
- **Input**: $3.00 per million tokens
- **Output**: $15.00 per million tokens
- **Status**: Superseded by Claude 3.5 Sonnet (use 3.5 instead)

### Amazon Titan Models

#### Titan Text G1 - Express
- **Model ID**: `amazon.titan-text-express-v1`
- **Input**: $0.0008 per 1,000 tokens (~$0.80 per million)
- **Output**: $0.0016 per 1,000 tokens (~$1.60 per million)
- **Best For**:
  - ✅ Simple text generation
  - ✅ Basic code comments
  - ✅ Simple documentation
- **Cost Efficiency**: ⭐⭐⭐⭐⭐ (5/5) - Very cheap
- **Note**: Less capable than Claude models for complex tasks

---

## Activity-Based Model Selection Strategy

### 1. Simple/Quick Tasks → Claude 3.5 Haiku

**Activities:**
- Quick code completion
- Simple variable renaming
- Formatting fixes
- Adding comments
- Simple syntax corrections
- Quick questions about code

**Model**: `anthropic.claude-3-5-haiku-20241022-v2:0`  
**Cost**: ~$0.80/$4.00 per million tokens  
**Savings**: 73% vs Sonnet

**Example Prompts:**
- "Add a comment explaining this function"
- "Rename this variable to follow naming conventions"
- "Format this code block"

---

### 2. Standard Development Tasks → Claude 3.5 Sonnet (Default)

**Activities:**
- Code generation
- Feature implementation
- Bug fixes
- Code refactoring
- Test writing
- Documentation
- Code reviews
- Architecture discussions

**Model**: `anthropic.claude-sonnet-4-5-20250929-v1:0` (your current default)  
**Cost**: $3.00/$15.00 per million tokens  
**Balance**: Best balance of capability and cost

**Example Prompts:**
- "Implement a new screen following our design system"
- "Refactor this function to use the repository pattern"
- "Write unit tests for this ViewModel"
- "Review this PR and suggest improvements"

---

### 3. Complex/High-Stakes Tasks → Claude 3 Opus

**Activities:**
- Complex architectural decisions
- Multi-file refactoring
- Difficult debugging
- Security reviews
- Performance optimization
- Complex algorithm design
- Research and analysis

**Model**: `anthropic.claude-3-opus-20240229-v1:0`  
**Cost**: $15.00/$75.00 per million tokens  
**Use**: Only when Sonnet struggles or task is critical

**Example Prompts:**
- "Design a new data synchronization architecture"
- "Debug this complex race condition"
- "Review this security-sensitive code"

---

## Cost Optimization Strategies

### 1. Right-Size Model Selection

**Principle**: Start with smaller models, scale up only if needed.

**Strategy:**
```
Haiku → Sonnet → Opus
(Simple) → (Standard) → (Complex)
```

**Implementation in Cursor:**
- Configure auto model selection to prefer Haiku for simple tasks
- Use Sonnet as default for most work
- Manually select Opus only for complex tasks

### 2. Prompt Optimization

**Cost Impact**: Shorter prompts = lower costs

**Best Practices:**
- ✅ Be specific and concise
- ✅ Use code references instead of pasting large code blocks
- ✅ Break complex tasks into smaller prompts
- ✅ Use context from open files (Cursor does this automatically)

**Example:**
```kotlin
// ❌ BAD: Long prompt with full code
"Here's my entire file: [500 lines of code]. Can you refactor it?"

// ✅ GOOD: Reference existing code
"Refactor the UserRepository class to use the Result pattern (see file:app/.../UserRepository.kt)"
```

### 3. Batch Similar Tasks

**Strategy**: Group similar simple tasks and use Haiku

**Example:**
- Instead of 5 separate Haiku calls for formatting, do one Sonnet call for "format all files in this directory"

### 4. Use Prompt Caching (When Available)

**Benefit**: Up to 90% cost reduction for repeated prompts

**How It Works:**
- AWS Bedrock caches common prompt prefixes
- Reduces cost for similar requests
- Automatically handled by Bedrock API

---

## Cursor Auto Model Selection Configuration

### How Auto Model Selection Works

**Cursor's auto model selection:**
- ✅ Automatically uses your configured primary model (Sonnet) for most tasks
- ✅ Analyzes prompt complexity to suggest optimal models
- ✅ Can be overridden manually when needed
- ✅ Shows model indicator in chat interface

### Recommended Setup

**Primary Model (Default)**: Claude 3.5 Sonnet
- **Model ID**: `anthropic.claude-sonnet-4-5-20250929-v1:0`
- **Use For**: 80% of development work
- **Configuration**: Set once in Cursor Settings, then forget about it

**Fallback Models** (manual selection when needed):
1. **Haiku** (`anthropic.claude-3-5-haiku-20241022-v2:0`) - For simple tasks
2. **Opus** (`anthropic.claude-3-opus-20240229-v1:0`) - For complex tasks

### Configuration Steps

1. **Open Cursor Settings**
   - `Cmd + ,` (macOS) or `Ctrl + ,` (Windows/Linux)

2. **Navigate to Models > AWS Bedrock**

3. **Configure Primary Model**
   - **Region**: `eu-west-1`
   - **Model**: `anthropic.claude-sonnet-4-5-20250929-v1:0`
   - **Save**

4. **Done!** Cursor will use Sonnet automatically for most tasks.

### When System Should Inform User

**The system should inform you when:**

1. **Simple Task Detected** (Could Save Money)
   - **Message**: "💡 This looks like a simple task. Using Haiku would be ~73% cheaper and faster. Continue with Sonnet or switch to Haiku?"
   - **Action**: User can choose to continue or switch

2. **Complex Task Detected** (Might Need More Power)
   - **Message**: "⚠️ This appears to be a complex task. Opus might provide better results but costs 5x more. Continue with Sonnet or switch to Opus?"
   - **Action**: User can choose based on importance

3. **Cost-Sensitive Operations**
   - **Message**: "💰 This operation will process ~X tokens. Estimated cost: $Y with Sonnet, $Z with Haiku. Choose model?"
   - **Action**: User can optimize for cost vs. speed

### Manual Model Selection

**How to Override Auto-Selection in Cursor:**

#### Option 1: Specify in Prompt
```
"Use Haiku: Format this code block"
"Use Opus: Design a new data synchronization architecture"
```

#### Option 2: Model Selector (if available)
- Look for model selector dropdown in Cursor Chat
- Select model before sending prompt

#### Option 3: Settings Override
- Temporarily change primary model in Settings
- Change back after task

### When to Use Each Model

**Use Haiku When:**
- ✅ Quick formatting or style fixes
- ✅ Simple variable renaming
- ✅ Adding basic comments
- ✅ Simple syntax corrections
- ✅ Quick questions about code

**Use Sonnet When (Default):**
- ✅ Most development tasks
- ✅ Feature implementation
- ✅ Code refactoring
- ✅ Test writing
- ✅ Documentation
- ✅ Standard debugging

**Use Opus When:**
- ✅ Complex architectural decisions
- ✅ Difficult debugging (Sonnet can't solve)
- ✅ Security-sensitive code reviews
- ✅ Multi-file refactoring across many files
- ✅ Research and analysis tasks

---

## Activity Type Mapping

### Code Generation & Editing

| Activity | Recommended Model | Cost/Task* | Speed |
|----------|------------------|------------|-------|
| Simple edits | Haiku | $0.01-0.05 | Very Fast |
| Standard features | Sonnet | $0.05-0.20 | Fast |
| Complex features | Opus | $0.20-1.00 | Slower |

*Estimated cost per typical task (varies by code size)

### Code Review & Analysis

| Activity | Recommended Model | Cost/Task* | Speed |
|----------|------------------|------------|-------|
| Quick review | Haiku | $0.01-0.03 | Very Fast |
| Standard review | Sonnet | $0.05-0.15 | Fast |
| Deep analysis | Opus | $0.15-0.50 | Slower |

### Documentation

| Activity | Recommended Model | Cost/Task* | Speed |
|----------|------------------|------------|-------|
| Simple docs | Haiku | $0.01-0.02 | Very Fast |
| Standard docs | Sonnet | $0.03-0.10 | Fast |
| Complex architecture docs | Opus | $0.10-0.30 | Slower |

### Testing

| Activity | Recommended Model | Cost/Task* | Speed |
|----------|------------------|------------|-------|
| Simple unit tests | Haiku | $0.01-0.03 | Very Fast |
| Standard tests | Sonnet | $0.03-0.10 | Fast |
| Complex integration tests | Opus | $0.10-0.30 | Slower |

### Debugging

| Activity | Recommended Model | Cost/Task* | Speed |
|----------|------------------|------------|-------|
| Simple bugs | Haiku | $0.01-0.03 | Very Fast |
| Standard bugs | Sonnet | $0.05-0.15 | Fast |
| Complex bugs | Opus | $0.15-0.50 | Slower |

---

## Cost Estimation Examples

### Example 1: Daily Development Workflow

**Typical Day:**
- 20 simple edits (Haiku): ~$0.20
- 10 standard features (Sonnet): ~$1.00
- 2 complex tasks (Opus): ~$0.40
- **Daily Total**: ~$1.60

**Monthly Estimate** (20 working days): ~$32/month

### Example 2: Heavy Development Day

**Heavy Day:**
- 30 simple edits (Haiku): ~$0.30
- 20 standard features (Sonnet): ~$2.00
- 5 complex tasks (Opus): ~$1.00
- **Daily Total**: ~$3.30

**Monthly Estimate** (5 heavy days, 15 normal): ~$50/month

### Example 3: All Sonnet (Current Default)

**If using Sonnet for everything:**
- 50 tasks/day × $0.10 average = $5.00/day
- **Monthly**: ~$100/month

**Savings with optimization**: ~50-70% cost reduction

---

## Workflow Efficiency Recommendations

### 1. Use Code References

**Benefit**: Reduces token usage, improves accuracy

**How:**
- Cursor automatically includes open file context
- Use `@filename` to reference specific files
- Avoid pasting large code blocks

### 2. Break Down Complex Tasks

**Strategy**: 
- Start with Haiku for simple parts
- Use Sonnet for standard parts
- Reserve Opus for truly complex parts

**Example:**
```
❌ "Refactor entire app to use new architecture"
✅ Step 1: "Refactor data layer" (Sonnet)
✅ Step 2: "Refactor UI layer" (Sonnet)
✅ Step 3: "Update integration points" (Opus if complex)
```

### 3. Leverage Cursor's Context Awareness

**Features:**
- Open files are automatically included
- Recent changes are tracked
- Codebase search provides context

**Benefit**: Less manual context needed = lower costs

### 4. Use Composer for Multi-File Changes

**When**: Making changes across multiple files

**Benefit**: 
- Single request instead of multiple
- Better context understanding
- More efficient token usage

---

## Monitoring & Optimization

### Track Usage

**AWS Cost Explorer:**
- Monitor Bedrock costs
- Identify high-cost activities
- Optimize model selection

**Key Metrics:**
- Cost per task type
- Model usage distribution
- Average tokens per request

### Optimization Checklist

- [ ] Review weekly cost reports
- [ ] Identify tasks that could use Haiku
- [ ] Verify Opus usage is justified
- [ ] Optimize prompts to reduce tokens
- [ ] Use code references instead of pasting
- [ ] Batch similar simple tasks

---

## Recommended Configuration

### For Your Workflow

**Primary Model**: Claude 3.5 Sonnet
- **Model ID**: `anthropic.claude-sonnet-4-5-20250929-v1:0`
- **Region**: `eu-west-1`
- **Use For**: 80% of tasks

**Secondary Models**:
1. **Haiku** (`anthropic.claude-3-5-haiku-20241022-v2:0`)
   - Use for: Simple edits, quick questions, formatting
   - Expected usage: 15% of tasks
   - Cost savings: ~73% vs Sonnet

2. **Opus** (`anthropic.claude-3-opus-20240229-v1:0`)
   - Use for: Complex architecture, difficult debugging
   - Expected usage: 5% of tasks
   - Cost: 5x Sonnet, but justified for complex work

### Expected Cost Impact

**Current (All Sonnet)**: ~$100/month  
**Optimized (Haiku + Sonnet + Opus)**: ~$35-50/month  
**Savings**: 50-65% cost reduction

---

## Quick Reference

### Model Selection Decision Tree

**For Users:**
```
Just code normally → Sonnet (automatic)
Simple task? → Say "Use Haiku: [task]"
Complex task? → Say "Use Opus: [task]"
```

**For System/Advanced Users:**
```
Is it a simple task?
├─ Yes → Use Haiku ($0.80/$4.00 per million)
└─ No → Is it a standard task?
    ├─ Yes → Use Sonnet ($3.00/$15.00 per million)
    └─ No → Is it complex/critical?
        ├─ Yes → Use Opus ($15.00/$75.00 per million)
        └─ No → Use Sonnet
```

### Cost Comparison

| Model | Input Cost | Output Cost | Relative Cost |
|-------|-----------|-------------|---------------|
| Haiku | $0.80/M | $4.00/M | 1x (baseline) |
| Sonnet | $3.00/M | $15.00/M | 3.75x |
| Opus | $15.00/M | $75.00/M | 18.75x |

### Speed Comparison

| Model | Speed | Best For |
|-------|-------|----------|
| Haiku | ⚡⚡⚡ Very Fast | Quick tasks |
| Sonnet | ⚡⚡ Fast | Standard work |
| Opus | ⚡ Slower | Complex tasks |

---

## User Experience: Making It Easy

### Principle: "Set It and Forget It" by Default

**Goal**: Users shouldn't need to think about model selection for 80% of tasks.

**Implementation:**
1. ✅ **Default to Sonnet** - Works well for most tasks
2. ✅ **Auto-detect complexity** - System suggests alternatives when beneficial
3. ✅ **Inform, don't require** - Show suggestions, but don't block workflow
4. ✅ **Easy override** - Simple way to switch models when needed

### When to Inform User (Not Block)

**Inform user when:**
- Simple task detected → Suggest Haiku (save money)
- Complex task detected → Suggest Opus (better results)
- High-cost operation → Show cost comparison
- User explicitly asks → Show model options

**Don't inform when:**
- Standard development tasks → Just use Sonnet silently
- Quick questions → Use default model
- Routine code editing → No need to interrupt

### Quick Decision Guide

**For Users: Quick Reference Card**

```
┌─────────────────────────────────────────┐
│  Quick Model Selection Guide            │
├─────────────────────────────────────────┤
│                                         │
│  🟢 Just code normally                  │
│     → Uses Sonnet automatically        │
│                                         │
│  🟡 Quick/simple task?                  │
│     → Say "Use Haiku: [task]"          │
│     → Saves ~73% cost, faster          │
│                                         │
│  🔴 Complex/critical task?              │
│     → Say "Use Opus: [task]"           │
│     → Better results, costs 5x more    │
│                                         │
└─────────────────────────────────────────┘
```

### Example User Interactions

**Scenario 1: Simple Task (System Informs)**
```
User: "Format this code block"

System: 💡 This is a simple task. Using Haiku would be ~73% cheaper 
        and faster. Continue with Sonnet or switch to Haiku?
        
User: [Clicks "Use Haiku"] or [Continues with Sonnet]
```

**Scenario 2: Standard Task (No Interruption)**
```
User: "Implement a new settings screen"

System: [Uses Sonnet automatically, no prompt needed]
```

**Scenario 3: Complex Task (System Informs)**
```
User: "Design a new data synchronization architecture"

System: ⚠️ This appears to be a complex task. Opus might provide 
        better results but costs 5x more. Continue with Sonnet or 
        switch to Opus?
        
User: [Chooses based on importance]
```

## Next Steps

1. **One-Time Setup** (2 minutes)
   - Configure Sonnet as primary model in Cursor Settings
   - Done! System handles the rest automatically

2. **Optional: Learn When to Override**
   - Try Haiku for simple tasks to see speed/cost difference
   - Try Opus for complex tasks to see quality difference
   - Most of the time, just use Cursor normally

3. **Monitor Usage** (Optional)
   - Check AWS Cost Explorer monthly
   - Review if costs are higher than expected
   - Adjust if needed (but default should work well)

4. **Share Learnings** (Optional)
   - Note which tasks work well with each model
   - Share with team if helpful
   - Update this guide if you find better patterns

---

## Automation & Tools

### Setup Script
- **Script**: `scripts/setup-bedrock-models.sh`
- **Purpose**: Automated setup and verification
- **Usage**: `./scripts/setup-bedrock-models.sh`
- **Features**:
  - Checks AWS credentials
  - Verifies Bedrock access
  - Displays recommended configuration
  - Provides step-by-step instructions

### Cursor Rule (AI Assistant Guidance)
- **Rule**: `.cursor/rules/bedrock-model-optimization.mdc`
- **Purpose**: Guides AI assistant to suggest optimal models
- **Behavior**: Automatically suggests Haiku for simple tasks, Opus for complex tasks
- **User Experience**: Informs when helpful, doesn't block workflow

## Related Documentation

- **📄 Quick Reference**: `AWS_BEDROCK_QUICK_REFERENCE.md` - One-page daily reference guide
- **🤖 Cursor Rule**: `.cursor/rules/bedrock-model-optimization.mdc` - AI assistant guidance
- **🔧 Setup Script**: `scripts/setup-bedrock-models.sh` - Automated setup
- `docs/development/setup/AWS_BEDROCK_SETUP.md` - Basic setup guide
- `docs/development/setup/AWS_BEDROCK_CURSOR_SETUP.md` - Detailed setup
- AWS Bedrock Pricing: https://aws.amazon.com/bedrock/pricing/
- AWS Bedrock Model Access: https://console.aws.amazon.com/bedrock/

---

**Last Updated**: 2025-01-21  
**Region**: eu-west-1  
**Cursor Version**: 2.0.77+

