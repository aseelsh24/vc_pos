# Issue 2: Tablet Responsive & Accessibility Pass

## Labels
`roadmap`, `enhancement`, `accessibility`

## Description

### Scope
Implement adaptive two-pane layout for larger tablets, add TalkBack labels, and ensure accessibility compliance.

### Acceptance Criteria
- [ ] Adaptive layout using WindowSizeClass (w≥840dp → two-pane)
- [ ] TalkBack navigation works correctly
- [ ] All interactive elements have contentDescription
- [ ] Font scales correctly (1.0x - 2.0x)
- [ ] Color contrast meets WCAG AA standards
- [ ] Landscape and portrait orientations supported

### Tech Notes
- Use `androidx.compose.material3.windowsizeclass:windowsizeclass`
- Add Semantics to all Composables
- Test with TalkBack enabled
- Use Accessibility Scanner tool

### AI-DEV PROMPT

```
You are working on the VC POS Android project (Jetpack Compose + Material3).

TASK: Add adaptive tablet layout and accessibility support.

REQUIREMENTS:
1. WindowSizeClass Integration:
   - Add dependency: androidx.compose.material3.windowsizeclass
   - Implement adaptive layout for w≥840dp (show master-detail view)
   - Test on 10" and 12" tablets

2. Accessibility:
   - Add contentDescription to all Icons, Images, and interactive components
   - Verify RTL semantics (ensure proper reading order)
   - Test with TalkBack enabled
   - Support font scaling (test at 1.5x and 2.0x)

3. Testing:
   - Add UI test for accessibility: ui/tests/AccessibilityTest.kt
   - Use Accessibility Scanner on all screens
   - Fix any contrast or touch target issues

DELIVERABLES:
- Commit with: feat(ui): adaptive tablet layout + a11y pass
- Update README.md with accessibility features
- Add screenshots showing two-pane layout

Test commands:
```bash
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest
```
```

---
