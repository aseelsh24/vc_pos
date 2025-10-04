## 🧾 Project Title
**Offline-First Grocery POS (React Native Android Tablet)**

---

## 🧭 Purpose
This application is a complete **Point of Sale (POS)** system for small grocery shops.  
It must work **fully offline** and synchronize data with a remote server whenever internet access is available.

---

## 🧩 Functional Requirements

### 1. User Authentication
- Local user login (no need for internet).
- Secure password storage using device keystore.
- Role-based access: Admin, Cashier, Viewer.

### 2. Product Management
- Local product list (synced from backend).
- Fields: name, SKU/barcode, unit, price, stock quantity.
- Ability to scan product barcodes via camera or Bluetooth HID scanner.

### 3. Sales (POS Screen)
- Add multiple items to a cart.
- Adjust quantity and auto-calculate subtotal, tax, discount, total.
- Process payments: cash, card, or mixed.
- Save sale locally even if offline.

### 4. Invoice Printing
- Print simple receipt via Bluetooth ESC/POS printer (text format).
- Include store name, date/time, itemized list, total, and thank-you message.

### 5. Data Synchronization
- Background sync job (every X minutes or manual trigger).
- Sync rules:
  - Push unsynced sales to server.
  - Pull updated products and prices.
  - Conflict resolution: **last-write-wins** with `updated_at` timestamps.

### 6. Reports
- Daily and weekly sales summaries.
- Filter by date or cashier.
- Show total revenue and number of transactions.

### 7. Multi-language Support
- Arabic (RTL) and English (LTR).
- Language toggle stored in local preferences.

---

## 🧱 Database Schema

### Table: `users`
| Field | Type | Description |
|--------|------|-------------|
| id | integer | Primary key |
| username | text | Unique |
| password_hash | text | Hashed password |
| role | text | e.g. "admin", "cashier" |

### Table: `products`
| Field | Type | Description |
|--------|------|-------------|
| id | integer | Primary key |
| name | text | Product name |
| sku | text | Barcode or code |
| price | real | Unit price |
| stock_qty | real | Available stock |
| updated_at | timestamp | Last sync time |

### Table: `sales`
| Field | Type | Description |
|--------|------|-------------|
| id | integer | Primary key |
| user_id | integer | Linked to users |
| total_amount | real | Sale total |
| payment_type | text | "cash", "card" |
| created_at | timestamp | Local timestamp |
| synced | boolean | True if uploaded |

### Table: `sale_items`
| Field | Type | Description |
|--------|------|-------------|
| id | integer | Primary key |
| sale_id | integer | Linked to sales |
| product_id | integer | Linked to products |
| qty | real | Quantity sold |
| price | real | Unit price |

### Table: `device_sync_logs`
| Field | Type | Description |
|--------|------|-------------|
| id | integer | Primary key |
| last_sync_at | timestamp | Last successful sync |
| status | text | "success" / "error" |
| message | text | Details if failed |

---

## 🔌 API Endpoints (for backend mock)

### `POST /device/register`
Registers a new POS device.
```json
{
  "device_id": "tablet_123",
  "user": "admin"
}

Response:

{ "status": "ok", "registered": true }


---

GET /products

Fetch all products (for sync). Response:

[
  { "id": 1, "name": "Rice 1kg", "price": 5.00, "sku": "890123", "updated_at": "2025-09-20T12:00:00Z" }
]


---

POST /sales

Upload local sales.

{
  "sales": [
    {
      "id": 1001,
      "user_id": 1,
      "total_amount": 25.50,
      "payment_type": "cash",
      "items": [
        { "product_id": 2, "qty": 1, "price": 5.00 },
        { "product_id": 3, "qty": 2, "price": 10.25 }
      ],
      "created_at": "2025-09-21T14:00:00Z"
    }
  ]
}

Response:

{ "status": "ok", "synced": 1 }


---

POST /sync

Handles two-way synchronization.

{
  "device_id": "tablet_123",
  "last_sync": "2025-09-20T10:00:00Z"
}

Response:

{
  "new_products": [],
  "updated_prices": [],
  "sales_confirmed": [1001],
  "server_time": "2025-09-21T14:00:00Z"
}


---

💾 Local Storage Strategy

Use WatermelonDB for high-performance offline storage and sync.

Alternatively: SQLite (via react-native-sqlite-storage) if simpler.

Maintain a local “sync status” flag per record.

Store local logs in device_sync_logs.



---

📱 UI Components (Minimum MVP)

Screen	Components	Description

Login	username, password	Auth local user
Product List	search bar, product tiles	Browse local products
POS (Sale)	item list, quantity input, total	Build order and checkout
Receipt	text preview, print button	Send data to printer
Sync	button, status display	Trigger manual sync



---

🔒 Security Notes

Passwords are hashed using SHA-256 or bcrypt.

Sensitive data stored in Android Keystore.

Communication with backend uses HTTPS.

Include token-based authentication in future sync API.



---

⚡ Build Instructions

Development

cd app
yarn install
yarn android

Testing

yarn test

CI (GitHub Actions)

The workflow should:

Install dependencies.

Build Debug APK.

Upload artifact named pos-debug-apk.



---

📦 Deliverables

After Jules completes the build:

✅ Functional minimal POS app

✅ Working offline with sample products

✅ Bluetooth print mock

✅ Mock backend in /server

✅ ci/android-build.yml

✅ Documentation (README.md + this spec)

✅ Pull Request ready to merge



---

🧠 Notes for Jules

Use actual code, not pseudocode.

Prefer TypeScript if supported.

Implement ProductListScreen and POSScreen first.

Include 1 test file per major component.

Ensure app runs offline after first sync.
