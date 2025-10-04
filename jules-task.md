# JULES TASK: Offline-first POS for Grocery Shop (React Native Android Tablet)

## 🧩 Project Overview
We are building a **React Native Point of Sale (POS)** system for a grocery store that must work **offline-first** on Android tablets (10" or larger) and sync data with a central backend when the internet is available.

The POS must allow:
- Offline product browsing, sales, and invoice printing.
- Daily/weekly reports, user permissions, and local data security.
- Optional backend (Node.js or ERPNext) for sync and central reporting.

---

## 🎯 Objective for This Jules Task
> **Create the initial codebase + folder structure + first working demo for the POS app.**

The output should include:
1. A well-organized project repo with folders:

/app          -> React Native mobile client /server       -> Mock backend (Node.js + Express) /docs         -> Documentation files /ci           -> GitHub Actions workflow

2. Working React Native app that can:
- Display a **login screen** (local auth with stored credentials)
- Show a **product list** from a local SQLite or WatermelonDB database
- Allow adding items to a **cart (POS screen)**, calculating totals
- Print a simple **receipt over Bluetooth** (mock print text)
- Perform basic **sync** with mock server (upload sales, pull updates)
3. Include files:
- `PROJECT_SPEC.md` (detailed specification)
- `README.md` (installation + run guide)
- `ci/android-build.yml` (GitHub Action to build a debug APK)

---

## ⚙️ Technical Requirements

### Frontend
- **React Native CLI** (preferred) or **Expo** if Bluetooth integration is smoother.
- **Local storage:** SQLite or WatermelonDB.
- **Languages:** Arabic (RTL) + English (LTR).
- **UI library:** React Native Paper or NativeBase (optional).
- **Printing:** Bluetooth Serial / ESC/POS library for simple text printing.
- **Barcode:** Camera or Bluetooth HID barcode scanner.

### Backend (mock server)
- Node.js + Express + PostgreSQL (or JSON files) for API simulation.
- Endpoints:
- `GET /products`
- `POST /sales`
- `POST /sync`
- `POST /device/register`

### Offline-first
- App must work fully offline: local database updates first, then sync when online.
- Sync strategy: **last-write-wins** or **timestamp-based merge**.

---

## 🔒 Security
- Local credentials stored securely (Keystore / SecureStore).
- Passwords hashed before storage.
- HTTPS for backend endpoints.

---

## 🧱 Database Schema
| Table | Fields |
|-------|---------|
| `products` | id, name, sku, price, stock_qty, updated_at |
| `sales` | id, user_id, total_amount, created_at, synced |
| `sale_items` | id, sale_id, product_id, qty, price |
| `users` | id, username, password_hash, role |
| `device_sync_logs` | id, last_sync_at, status |

---

## 🧪 Tests
- **Unit tests** using Jest for frontend logic.
- **Basic E2E** test using Detox or Appium.

---

## 🚀 CI/CD
Add workflow file `/ci/android-build.yml`:
- Runs on push or PR.
- Installs dependencies (`yarn install`).
- Builds Android debug APK.
- Uploads artifact.

Example steps:
```yaml
- uses: actions/checkout@v4
- uses: actions/setup-node@v4
with:
 node-version: '20'
- run: yarn install
- run: cd app && ./gradlew assembleDebug
- uses: actions/upload-artifact@v4
with:
 name: pos-debug-apk
 path: app/android/app/build/outputs/apk/debug/app-debug.apk


---

📦 Deliverables

When Jules completes the task, the repository should include:

✅ Complete folder structure

✅ Working minimal app (login, products, POS screen)

✅ Mock backend for testing sync

✅ Docs and CI files

✅ PR ready for review and merge



---

🗣️ Next Steps (After This Task)

If the base app works correctly, the next Jules tasks will be:

1. Add reporting dashboard


2. Add customer management


3. Add role-based permissions


4. Optimize sync conflict handling


5. Integrate real Bluetooth printer models




---

💡 Notes for Jules Agent

Do not generate placeholder code only — implement minimal working examples.

Prefer TypeScript if possible.

Ensure that commands are tested: yarn android or npm run android.

Create sample data (/server/sample_data/products.json).

Ensure all components use RTL layout support.
