# Terminology Clarification: Session vs Agent

**Date**: 2025-11-21  
**Purpose**: Define what "session" and "agent finished" mean

## Definitions

### Agent
- **Agent** = Prompting in Cursor on repeat until archived
- **Agent finished** = When you archive it (stop prompting that agent)
- **Agent is ephemeral** - Each archived agent is done

### Session
- **Session** = The period of work/activity
- **Session finished** = When prompting is done, you archive it
- **Session might continue** = If work passes to another agent, session isn't complete
- **Session complete** = All work done, no handover needed

### Work Period
- **Not needed** - Moot when you stop
- Work happens during the session, but we don't need a separate term

## What This Means

**Agent Finished:**
- You archive the agent (stop prompting)
- That specific agent instance is done
- Agent's work might be complete, or might need handover

**Session Finished:**
- You archive the session (stop prompting)
- Session might be complete (all work done)
- Or session might continue with new agent (handover)

**Session Complete:**
- All work is done
- No handover needed
- Session is finished AND complete

**Session Incomplete (Handover):**
- Work not done, but agent finished
- Handover created for new agent
- Session continues with new agent

## Current State

**Current situation:**
- **Agent**: This agent instance (prompting in Cursor)
- **Session**: AI optimization work (23 commits, PR #41)
- **Agent finished**: When you archive this agent
- **Session status**: 
  - If work complete → Session complete, agent finished
  - If work incomplete → Session continues, handover needed

**The pinned principle:**
- "When a session is finished, an agent is finished" 
- **Means**: When you archive the session, you archive the agent
- **But**: Session might continue with new agent if handover needed

## Clarification Needed

**The pinned principle needs updating:**
- Current: "When session finished, agent finished"
- Reality: Session might continue with new agent (handover)
- Better: "When agent finished, session might continue with new agent"

**Or:**
- "Session finished" = You archive it (work done or not)
- "Session complete" = All work done, no handover
- "Session continues" = Handover to new agent

---

**Next Step**: Update the pinned principle to match these definitions!

