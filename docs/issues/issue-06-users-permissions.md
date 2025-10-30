# Issue 6: Users & Permissions (Admin/Cashier)

## Labels
`roadmap`, `feature`

## Description

### Scope
Add local user roles with PIN authentication and role-based access control.

### Acceptance Criteria
- [ ] User entity with role enum (ADMIN, CASHIER)
- [ ] PIN login screen (4-6 digits)
- [ ] Role-based navigation guards
- [ ] Only Admin can: change rates, delete transactions, manage users
- [ ] Cashier can: process sales, view own transactions
- [ ] Persist last logged-in user

### Tech Notes
- Add User entity to Room database
- Store current user in DataStore
- Use PIN hashing (bcrypt or Argon2)
- Navigation guards in NavGraph

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Implement multi-user support with role-based permissions.

REQUIREMENTS:
1. User Entity:
   - Create User entity with: id, name, pinHash, role (ADMIN/CASHIER)
   - Add UserDao and UserRepository
   - Seed default admin user (PIN: 1234 - changeable)

2. PIN Login:
   - Create LoginScreen composable
   - 4-6 digit PIN input
   - Hash PIN before storage (use bcrypt: at.favre.lib:bcrypt:0.10.2)
   - Store current user ID in DataStore

3. Role-Based Access:
   - Modify SettingsScreen: only ADMIN can edit rates
   - Modify TransactionsScreen: ADMIN sees all, CASHIER sees own
   - Add navigation guard in NavGraph

4. User Management:
   - Add UserManagementScreen (Admin only)
   - Create/Edit/Delete users
   - Change own PIN

DELIVERABLES:
- Commit as: feat(auth): local roles + PIN
- Add tests: AuthRepositoryTest.kt
- Update README with default credentials

Default User:
- Username: admin
- PIN: 1234
- Role: ADMIN
```

---
