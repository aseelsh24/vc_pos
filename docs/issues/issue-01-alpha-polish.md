# Issue 1: Alpha v0.1 Polish & Bug-Bash

## Labels
`roadmap`, `bug`, `enhancement`

## Description

### Scope
Stabilize POS/Transactions/Settings screens and fix layout glitches on Samsung Tab S7 (2560×1600).

### Acceptance Criteria
- [ ] No crashes during normal usage flow
- [ ] Currency switch persists across app restarts
- [ ] Manual rate changes persist correctly
- [ ] Smooth scrolling (30+ FPS) in product grid and transaction list
- [ ] RTL layout correct on all screens
- [ ] Touch targets ≥ 48dp for all interactive elements

### Tech Notes
- Test on physical Tab S7 (11", 2560×1600)
- Profile with Android Studio Profiler for performance issues
- Verify Room database persistence
- Check DataStore proto serialization

### AI-DEV PROMPT

```
You are working on branch feat/alpha-pos-tablet-arabic in the VC POS Android project.

TASK: Audit and stabilize the alpha release for Samsung Tab S7 (11" tablet, 2560×1600 resolution).

REQUIREMENTS:
1. Layout Audit:
   - Review all Composables in ui/screens/ for 11" tablet (600-840dp width)
   - Fix any RTL quirks (mirroring of rows, icons, alignments)
   - Ensure proper spacing and touch targets (minimum 48dp)
   
2. Persistence Testing:
   - Verify currency selection persists via DataStore
   - Test manual exchange rate updates
   - Confirm transaction history loads correctly

3. Performance:
   - Add 5 unit tests for CurrencyFormatter rounding rules
   - Profile product grid scrolling performance
   - Optimize LazyColumn/LazyVerticalGrid if needed

4. Bug Fixes:
   - Test end-to-end: add products → checkout → view transactions
   - Fix any crashes or data loss issues
   - Document any known limitations

DELIVERABLES:
- Commit fixes with conventional commits (fix: ..., test: ..., perf: ...)
- Push to feat/alpha-pos-tablet-arabic
- Update README.md with any new known issues

Run tests before pushing:
```bash
./gradlew testDebugUnitTest
./gradlew spotlessCheck
```
```

---
