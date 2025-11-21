# Persona Enhancements: Making Personas More Lifelike

## Overview

Enhanced personas to be more lifelike and diverse, mirroring real-life people with:
- **Realistic backgrounds** (age, occupation, interests)
- **AI-generated emails** (instead of procedural patterns)
- **Diverse characteristics** (personality, life circumstances)
- **Real-world email patterns** (AOL for older users, Gmail +tags for tech-savvy, etc.)

---

## Enhanced Personas

### 1. Sarah Johnson - Tech Novice (Older Adult)
- **Age**: 68, retired teacher
- **Background**: Non-technical, recently got first smartphone
- **Email Pattern**: Older email accounts (AOL, Yahoo from early 2000s)
- **Personality**: Patient, methodical, gets frustrated with technology
- **Interests**: Reading, gardening, grandchildren

**Email Examples** (AI-generated):
- `sarah.johnson@yahoo.com`
- `sarah1975@aol.com`
- `sarah.johnson@gmail.com`

### 2. Alex Martinez - Tech Savvy (Young Professional)
- **Age**: 28, software developer
- **Background**: Technical professional, uses multiple apps daily
- **Email Pattern**: Gmail with +tags, advanced features
- **Personality**: Efficient, goal-oriented, expects things to work quickly
- **Interests**: Technology, productivity apps, gaming

**Email Examples** (AI-generated):
- `alex.martinez+test@gmail.com`
- `alex.martinez+app@gmail.com`
- `amartinez@outlook.com`

### 3. Jennifer Chen - Moderate User (Middle-Aged Parent)
- **Age**: 42, marketing manager
- **Background**: Moderate technical exposure, work and personal balance
- **Email Pattern**: Mix of work (Outlook) and personal (Gmail)
- **Personality**: Patient but expects clarity, balances work and personal
- **Interests**: Family activities, work-life balance, social media

**Email Examples** (AI-generated):
- `jennifer.chen@gmail.com`
- `jennifer.chen@outlook.com`
- `jchen@company.com`

### 4. Jordan Taylor - Student (Tech Comfortable)
- **Age**: 20, university student
- **Background**: Digital native, comfortable with technology
- **Email Pattern**: Gmail primarily, may use university email
- **Personality**: Quick and efficient, may skip instructions
- **Interests**: Social media, music, social activities

**Email Examples** (AI-generated):
- `jordan.taylor@gmail.com`
- `jordan.taylor@university.edu`
- `jtaylor123@gmail.com`

---

## AI Email Generation

### Why AI?

**Procedural Generation** (Old):
- Simple patterns: `firstname.lastname@gmail.com`
- Doesn't reflect real diversity
- Doesn't match persona characteristics

**AI Generation** (New):
- Considers age, background, occupation, interests
- Generates emails that real people would actually use
- Matches persona characteristics (AOL for older, +tags for tech-savvy)

### How It Works

```kotlin
AIEmailGenerator.generateEmail(persona)
  ├─→ Build prompt with persona characteristics
  │   ├─→ Age group (older → AOL/Yahoo, younger → Gmail)
  │   ├─→ Technical skill (tech-savvy → +tags, novice → simple)
  │   ├─→ Background (work → Outlook, personal → Gmail)
  │   └─→ Occupation (student → .edu, professional → work email)
  │
  ├─→ Call OpenAI API
  │   └─→ Generate realistic email
  │
  └─→ Validate and return
      └─→ Fallback to procedural if AI unavailable
```

### Fallback

If AI unavailable (no API key, network error, etc.):
- Falls back to procedural generation
- Still persona-appropriate (based on technical skill)
- System continues to work

---

## Persona Data Structure

### Enhanced Fields

```yaml
persona:
  name: "Sarah Johnson"  # Real name, not just "Tech Novice"
  description: "A 68-year-old retired teacher..."  # Detailed background
  age_group: "older"  # NEW: Age group
  background: "Retired teacher, non-technical"  # NEW: Background
  occupation: "Retired"  # NEW: Occupation
  interests:  # NEW: Personal interests
    - "Reading"
    - "Gardening"
  personality:  # NEW: Personality traits
    - "Patient and methodical"
    - "Gets frustrated with technology"
```

### Benefits

- ✅ **More realistic** - Personas feel like real people
- ✅ **Diverse** - Different ages, backgrounds, circumstances
- ✅ **AI-generated emails** - Match persona characteristics
- ✅ **Better testing** - Tests how real people would use the app

---

## Usage

### Automatic (Default)

Personas are automatically used when specified in context:
```bash
./gradlew run --args="--task 'Sign up' --context 'tech_novice persona'"
```

### AI Email Generation

Requires `OPENAI_API_KEY` environment variable:
```bash
export OPENAI_API_KEY=your_key_here
./gradlew run --args="--task 'Sign up' --context 'tech_novice persona'"
```

If API key not provided, falls back to procedural generation.

---

## Test Scenarios

### Recommended Tasks

1. **Sign Up** - Test account creation with different personas
2. **Sign In** - Test authentication with different personas
3. **Add Mood** - Test core functionality
4. **Error Recovery** - Test how different personas handle errors

### Example Scenarios

```yaml
# test-scenarios/signup-tech-novice.yaml
task: "Sign up and add a mood value"
context: "tech_novice persona"
persona: "tech_novice"

# test-scenarios/signup-tech-savvy.yaml
task: "Sign up and add a mood value"
context: "tech_savvy persona"
persona: "tech_savvy"
```

---

## Summary

✅ **Enhanced Personas**: 4 diverse, lifelike personas  
✅ **AI Email Generation**: Realistic emails matching persona characteristics  
✅ **Real-World Patterns**: AOL for older, Gmail +tags for tech-savvy, etc.  
✅ **Fallback Support**: Works even without AI API key  

**Result**: More realistic, diverse personas that mirror real-life people for better testing.

