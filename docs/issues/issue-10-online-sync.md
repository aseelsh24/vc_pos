# Issue 10: Online Sync (Future - Supabase Plan)

## Labels
`roadmap`, `research`, `future`

## Description

### Scope
Design document for online synchronization layer with conflict resolution strategy.

### Acceptance Criteria
- [ ] Design doc: `/docs/sync-plan.md`
- [ ] Entity sync strategy (which tables to sync)
- [ ] Conflict resolution rules (last-write-wins vs operational transform)
- [ ] Offline-first constraints
- [ ] Authentication flow
- [ ] Sync frequency and triggers
- [ ] Network error handling

### Tech Notes
- Consider Supabase Realtime or Firebase Firestore
- Design for eventual consistency
- Local-first architecture (sync is enhancement, not requirement)
- Handle network partitions gracefully

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Draft a synchronization design document for multi-device support.

REQUIREMENTS:
1. Architecture Analysis:
   - Identify entities to sync: Products, Categories, Transactions
   - Determine sync direction: bi-directional or server-authoritative
   - Define conflict scenarios (e.g., product price changed on 2 devices)

2. Technology Options:
   - Option A: Supabase (PostgreSQL + Realtime)
     * Pros: mature, realtime, RLS policies
     * Cons: requires network, complexity
   - Option B: Firebase Firestore
     * Pros: offline support, auto-sync
     * Cons: NoSQL, potential cost
   - Option C: Custom REST API + local sync queue
     * Pros: full control, simple
     * Cons: more dev work

3. Conflict Resolution:
   - Products/Categories: server wins (admin updates)
   - Transactions: merge (append-only log)
   - Settings: device-specific (no sync)

4. Implementation Phases:
   - Phase 1: Read-only sync (products/categories from server)
   - Phase 2: Transaction upload (one-way to server)
   - Phase 3: Bi-directional sync with conflict resolution

5. Offline-First Constraints:
   - App must work 100% offline
   - Sync is optional enhancement
   - Network failures are gracefully handled
   - Queue pending changes for later sync

DELIVERABLES:
- Create: /docs/sync-plan.md (comprehensive design doc)
- Sections:
  1. Overview & Goals
  2. Entity Sync Strategy
  3. Technology Recommendation
  4. Conflict Resolution Rules
  5. Implementation Phases
  6. Security Considerations
  7. Testing Strategy
- Commit as: docs(sync): proposal

Questions to Answer:
- How to handle deleted products that have transactions?
- How to sync exchange rates across devices?
- Should transactions be editable after sync?
- What happens if device is offline for weeks?
```

---
