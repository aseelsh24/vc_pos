# Issue 9: Backup/Restore (Local Export)

## Labels
`roadmap`, `feature`, `good-first-issue`

## Description

### Scope
Export and import Room database to user-selected location with version safety.

### Acceptance Criteria
- [ ] Export database to .db file
- [ ] Import database from .db file
- [ ] Version compatibility check
- [ ] User selects location via Storage Access Framework
- [ ] Success/failure notifications
- [ ] Restored data produces identical app state

### Tech Notes
- Use Storage Access Framework (SAF)
- Copy Room database file
- Add database version metadata
- Test import with different versions

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Implement local database backup and restore functionality.

REQUIREMENTS:
1. Backup:
   - Create BackupManager class
   - Use Storage Access Framework to select export location
   - Copy Room database file: /data/data/.../databases/pos_database
   - Include metadata: version, timestamp, checksum
   - Export format: vc_pos_backup_YYYYMMDD_HHMMSS.db

2. Restore:
   - Use SAF to select backup file
   - Verify database version compatibility
   - Check checksum integrity
   - Close current database, replace file, reopen
   - Show confirmation dialog before restore

3. UI:
   - Add Backup/Restore buttons in Settings
   - Show progress indicator during operations
   - Toast notifications for success/failure

4. Safety:
   - Backup current database before restore
   - Handle errors gracefully
   - Verify restored data integrity

DELIVERABLES:
- Commit as: feat(backup): export/import DB
- Add tests: BackupManagerTest.kt
- Update README with backup instructions

Usage Flow:
1. Settings → Backup → Select location → Export
2. Settings → Restore → Select file → Confirm → Import
3. App restarts with restored data
```

---
