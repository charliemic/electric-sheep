# 🧠 Test Automation: Human-Like Cognitive Process

---

## 🎯 Test Session Goal

**Task**: Sign up for a new account and add a mood value  
**Persona**: Tech Novice (less comfortable with technology)  
**Approach**: Complete the task using visual perception, adaptive planning, and human-like decision-making

---

## 📋 The Journey: Step-by-Step

### **Phase 1: Initial Assessment**
👁️ **PERCEPTION**: Extracted 179 characters of text from the landing screen  
🧠 **PLANNING**: Decomposed task → identified goal: `AUTHENTICATE`  
🧠 **PLANNING**: Observed gap: `LANDING_SCREEN` → need `NAVIGATE_TO_AUTHENTICATION`  
💡 **DECISION**: Visual detection found no buttons → fallback to semantic identifiers

### **Phase 2: Navigation**
✋ **ACTION**: Tapped "Mood Management"  
💭 *Persona thought: "This looks like the main feature. Let me explore it."*  
✅ **RESULT**: Successfully navigated to sign-up screen

### **Phase 3: Account Creation**
✋ **ACTION**: Tapped "Show email and password sign in"  
✋ **ACTION**: Typed email: `emily6566@outlook.com`  
👁️ **PERCEPTION**: Monitored screen → OCR text increased (179 → 243 chars)  
👁️ **PERCEPTION**: Detected keyboard presence via text indicators  
✋ **ACTION**: Typed password  
👁️ **PERCEPTION**: Continued monitoring (245 → 253 chars)  
✋ **ACTION**: Tapped "Create Account"  
👁️ **PERCEPTION**: Watched for response (257-258 chars, keyboard still visible)  
⏳ **WAIT**: System waited for app to process account creation

### **Phase 4: Mood Entry**
✋ **ACTION**: Typed mood score: `6`  
👁️ **PERCEPTION**: Observed text change (257 → 269 → 263 chars)  
✋ **ACTION**: Tapped "Save Mood"  
👁️ **PERCEPTION**: Monitored final state (234-245 chars)

### **Phase 5: Verification**
✅ **VERIFICATION**: Checked authentication status  
   - 👁️ Visually searched for authenticated indicators: "mood management", "mood history"  
   - 👁️ Confirmed no unauthenticated indicators: "sign in", "login"  
   - ✅ **Result**: Authenticated  
✅ **VERIFICATION**: Checked mood history  
   - 👁️ Visually searched for "Mood History" text in screenshot  
   - ✅ **Result**: Found

---

## ✅ Task Completed Successfully

**Outcome**: User authenticated and mood value added (`auth=true`, `mood=true`)  
**Method**: Visual verification confirmed completion by "seeing" the result, not querying internal state

---

## 🧠 The Brain: Four Cognitive Modes

| Mode | Function | How It Works |
|------|----------|--------------|
| **👁️ PERCEPTION** | "What do I see?" | Uses OCR to extract text from screenshots (179-269 chars), visually detects keyboard, monitors state changes every 500ms |
| **🧠 PLANNING** | "What should I do?" | Decomposes tasks into abstract goals (`AUTHENTICATE`), identifies current state vs. goal state, plans actions to close gaps |
| **✋ ACTION** | "Let me do it" | Executes taps, typing, swipes using semantic identifiers when visual detection fails |
| **✅ VERIFICATION** | "Did it work?" | Visually confirms success by searching for indicators in screenshots (OCR), not by querying internal app state |

### Key Insight
The system mimics human cognition: it **sees** (perception), **thinks** (planning), **acts** (action), and **verifies** (verification) — all through visual observation, just like a human would interact with an app.

---

*Generated from automated test execution log*  
*Demonstrating cognitive automation that mirrors human behavior*

