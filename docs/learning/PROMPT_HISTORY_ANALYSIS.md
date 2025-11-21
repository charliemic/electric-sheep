# Prompt History Analysis

Guidance on accessing and analyzing prompt/conversation history to evaluate the evolution of AI-driven coding practices.

## Storage Location

### Primary Location
**Database:** `~/Library/Application Support/Cursor/User/globalStorage/state.vscdb`

This SQLite database contains chat/agent conversation history and related metadata.

### Database Structure
```sql
CREATE TABLE ItemTable (key TEXT UNIQUE ON CONFLICT REPLACE, value BLOB);
CREATE TABLE cursorDiskKV (key TEXT UNIQUE ON CONFLICT REPLACE, value BLOB);
```

### Chat-Related Keys
The database contains keys such as:
- `chat.participantNameRegistry` - Registry of chat participants
- `chat.workspaceTransfer` - Workspace transfer data
- `workbench.panel.composerChatViewPane.*.hidden` - UI state for chat panels
- `cursor/agentLayout.*` - Agent layout preferences

## Accessing Prompt History

### Via UI (Recommended)
1. Press `Ctrl+Shift+L` (or `Cmd+Shift+L` on Mac) to open prompt tab
2. Click the "Show Chat History" icon
3. View, edit titles, delete, or open previous conversations

### Via CLI
```bash
# List previous conversations
cursor-agent ls

# Resume from existing thread
cursor-agent --resume [thread-id]
```

### Via Database (Advanced)
```bash
# Access the database
sqlite3 ~/Library/Application\ Support/Cursor/User/globalStorage/state.vscdb

# Query chat-related data
SELECT key, LENGTH(value) as size 
FROM ItemTable 
WHERE key LIKE '%chat%' OR key LIKE '%agent%' 
ORDER BY size DESC;
```

## Analyzing Evolution

### What to Track

1. **Prompt Patterns**
   - Common prompt structures that work well
   - Prompts that consistently produce good results
   - Prompts that fail or need refinement

2. **Interaction Evolution**
   - How prompts have changed over time
   - Increasing specificity or clarity
   - Development of prompt templates

3. **Workflow Refinement**
   - Changes in how AI is used
   - Emergence of best practices
   - Identification of anti-patterns

4. **Rule Development**
   - Rules that emerged from repeated issues
   - Rules that were added based on prompt outcomes
   - Rules that were refined based on experience

### Analysis Approach

#### 1. Extract Conversation Data
```bash
# Export chat data (if accessible)
sqlite3 ~/Library/Application\ Support/Cursor/User/globalStorage/state.vscdb \
  "SELECT key, value FROM ItemTable WHERE key LIKE 'chat.%';" > chat_data.json
```

#### 2. Identify Patterns
- Group conversations by topic/type
- Identify successful vs. unsuccessful interactions
- Note common issues or misunderstandings

#### 3. Track Evolution
- Compare early prompts vs. recent prompts
- Identify improvements in prompt clarity
- Note development of prompt templates

#### 4. Document Insights
- Create prompt template library
- Document what works and what doesn't
- Share effective patterns with team

## Limitations

### Current Challenges
1. **Database Format** - Conversation data may be stored in binary/blob format
2. **Access Method** - Direct database access may not reveal full conversation structure
3. **Export Tools** - No built-in export functionality for analysis

### Recommended Approach
1. **Use UI for Review** - Review conversations via Cursor's history panel
2. **Manual Documentation** - Document effective prompts as you discover them
3. **Create Prompt Library** - Build a library of effective prompts over time
4. **Track Metrics** - Note which prompts lead to successful outcomes

## Building a Prompt Library

### Effective Prompt Patterns

#### 1. Task-Oriented Prompts
```
"Implement [feature] following [pattern] with [requirements]"
```

#### 2. Context-Rich Prompts
```
"Given [context], modify [component] to [goal] while maintaining [constraints]"
```

#### 3. Iterative Refinement
```
"Refine [previous output] to [improvement] considering [feedback]"
```

### Prompt Evolution Tracking

#### Early Stage
- Generic prompts
- Trial and error
- Learning what works

#### Mature Stage
- Specific, structured prompts
- Reusable templates
- Clear patterns

#### Advanced Stage
- Prompt libraries
- Context-aware prompts
- Optimized for outcomes

## Recommendations

### For Teams
1. **Document Effective Prompts** - Create a shared prompt library
2. **Review History Regularly** - Identify patterns and improvements
3. **Share Learnings** - Communicate what works across team
4. **Iterate on Templates** - Refine prompt templates based on outcomes

### For Analysis
1. **Start with UI** - Use Cursor's history panel for initial review
2. **Extract Patterns** - Identify common successful prompt structures
3. **Document Evolution** - Track how prompts improve over time
4. **Create Templates** - Build reusable prompt templates

### For Future
1. **Automated Analysis** - Develop tools to analyze prompt history
2. **Metrics Tracking** - Measure prompt effectiveness
3. **A/B Testing** - Test different prompt approaches
4. **Continuous Improvement** - Regular review and refinement

## Integration with Lessons Learned

### How Prompt History Informs Practice

1. **Workflow Refinement**
   - Prompts reveal workflow inefficiencies
   - Identify where process can be improved
   - Document successful interaction patterns

2. **Rule Development**
   - Rules emerge from repeated prompt corrections
   - Identify common mistakes to prevent
   - Refine rules based on prompt outcomes

3. **Collaboration Patterns**
   - Understand how AI-human collaboration evolves
   - Identify effective communication patterns
   - Document successful collaboration approaches

4. **Outcome Measurement**
   - Track which prompts lead to best outcomes
   - Measure prompt effectiveness
   - Optimize for desired results

## Next Steps

1. **Access History** - Review conversations via Cursor UI
2. **Identify Patterns** - Note effective prompt structures
3. **Document Templates** - Create reusable prompt library
4. **Share Learnings** - Communicate findings with team
5. **Iterate** - Continuously improve based on analysis

---

## Notes

- Prompt history is stored locally in SQLite database
- Direct database access may be limited by format
- UI access (Ctrl+Shift+L) is recommended for review
- Manual documentation of effective prompts is valuable
- Building a prompt library over time is recommended approach

