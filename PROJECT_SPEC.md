مشروع: Grocery POS — Offline-first Tablet App (مواصفات)

لمحة عامة

تطبيق نقطة بيع (POS) مصمّم لبيع وإدارة بقالة مواد غذائية على تابلت Android. يعمل دون اتصال بالشبكة مع ميكانيكية مزامنة عند توفر الإنترنت. يدعم الباركود، الطباعة الحرارية عبر Bluetooth، إدارة مخزون متعدد الوحدات، وإصدار تقارير ومفاتيح صلاحية للمستخدمين.

الأهداف الوظيفية الرئيسة

تسجيل الدخول وإدارة المستخدمين مع صلاحيات (Cashier, Manager, Admin).

إضافة/تعديل المنتجات مع صور، barcode, سعر بيع/شراء، وحدة قياس، حد تنبيه للمخزون.

عملية بيع POS: بحث/مسح باركود، إضافة للكارت، حساب خصومات/ضرائب، دفع نقدي/بطاقة/محفظة.

طباعة إيصال حراري عبر Bluetooth.

مرتجعات وبدل صنف.

تقارير: مبيعات يومية، حركة مخزون، أرباح إجمالية، قائمة منتجات منخفضة.

مزامنة ثنائية الاتجاه مع خادم مركزي (push sales, pull updates).

دعم اللغة العربية (RTL) والإنجليزية.


التكنولوجيا المقترحة

واجهة الموبايل: React Native (React Native CLI)

مكتبات مقترحة: react-navigation, redux/zustand, react-native-ble-plx أو react-native-bluetooth-serial, react-native-sqlite-storage أو WatermelonDB/Realm, react-native-camera أو react-native-vision-camera للباركود.


خادم المزامنة (اختياري): Node.js + Express + PostgreSQL أو تكامل مع ERPNext.

CI/CD: GitHub Actions

اختبارات: Jest (unit), Detox (E2E) أو Appium

توجيه البيانات: JSON over HTTPS, JWT auth


هيكل المشروع (مقترح)

/project-root
  /app                      # React Native app
    /src
      /screens
      /components
      /services             # syncService, apiClient
      /store                # redux/zustand
      /db                   # local DB models, migrations
      /utils
      /assets
    android/
    ios/
    package.json
  /server                   # optional sync server (node/express)
    /src
    package.json
  /ci
    android-build.yml
  /docs
    PROJECT_SPEC.md
    README.md
  .github/workflows
    android-build.yml

مخطط قاعدة البيانات (نموذجي — server وlocal)

products

id (uuid)

sku (string)

barcode (string)

name_ar (string)

name_en (string)

description

price_sell (decimal)

price_buy (decimal)

unit (string) e.g., "pcs", "kg"

stock (integer) -- (local cache, authoritative on server)

low_stock_threshold (integer)

updated_at, created_at


sales

id (uuid)

device_id (string)

user_id

total_amount (decimal)

tax_amount (decimal)

discount_amount (decimal)

paid_amount (decimal)

status (enum: pending/synced/failed)

created_at, updated_at


sale_items

id

sale_id (fk)

product_id

qty (decimal)

unit_price (decimal)

line_total (decimal)


stock_movements

id

product_id

change (decimal positive/negative)

reason (sale/return/adjustment)

ref_id (sale_id if sale)

created_at


users

id

username

password_hash (never plain)

role (cashier/manager/admin)

last_seen


device_sync_logs

id

device_id

last_sync_at

last_sync_status

pending_changes_count


واجهات API (نموذجية)

> كل واجهة تعمل عبر HTTPS وJWT auth



POST /api/v1/device/register

Request:

{
  "device_id": "tablet-001",
  "device_name": "Aseel's Tablet",
  "app_version": "1.0.0"
}

Response:

{ "ok": true, "device_id": "tablet-001", "registered_at": "2025-10-03T..." }

GET /api/v1/products?since=2025-10-01T00:00:00Z

Response:

{ "ok": true, "products": [ { "id":"...", "name_ar":"موز", "barcode":"123456", "price_sell":1.5, "updated_at":"..." }, ... ] }

POST /api/v1/sales/bulk

Request:

{
  "device_id":"tablet-001",
  "sales":[
    {
      "id":"sale-uuid-1",
      "user_id":"u1",
      "created_at":"2025-10-03T10:01:00Z",
      "total_amount": 40.5,
      "items":[ { "product_id":"p1", "qty":2, "unit_price":10 }, ... ]
    }
  ]
}

Response:

{ "ok": true, "accepted": ["sale-uuid-1"], "conflicts": [] }

استراتيجيات المزامنة (Sync)

المبدأ: التطبيق يسجّل عمليات محلية في جدول outbox (operation log).

Push: عندما تصبح الشبكة متاحة، يجمع outbox ويبعث sales/bulk ثم يعلّم السجلات كـ synced.

Pull: يسأل الخادم عن التحديثات منذ last_synced_at ثم يطبّقها محليًا.

حل التعارض: استخدم operation log + timestamps. للمنتجات: server authoritative لـ stock/price. للحركة: لا تقم بإعادة احتساب كامل المخزون على الجهاز بل استبداله بقيم server عند تعارض مهم. سياسة مبسطة: last-write-wins لحقول غير متعلقة بالكمية، وmerge للـ stock (apply server delta).

Retry & Backoff: تنفيذ Retry مع exponential backoff وحفظ حالات الفشل في device_sync_logs.


سيناريوهات واجهة المستخدم (Screens)

1. شاشة تسجيل الدخول (username/password) — اختيار اللغة


2. Dashboard (صندوق نقدي يومي، إجمالي المبيعات)


3. Products (قائمة — بحث — فلتر low-stock) — زر Add/Edit


4. POS / Checkout screen:

حقل بحث أو زر scan barcode

سطر لكل صنف (qty, unit, modifiers)

خيارات دفع: Cash, Card, Wallet

زر Print Receipt, Finish Sale



5. Sales History (قائمة فواتير — فتح لإرجاع)


6. Returns screen


7. Settings: Device registration, Sync now, Printer pairing, Backup/Restore


8. Reports: Daily sales, Inventory movements



تجربة المستخدم على التابلت (نقاط تصميم)

واجهة RTL للغة العربية؛ عناصر كبيرة للمس (48–56 dp حد أدنى للأزرار الهامة).

شاشة POS تقسم إلى قائمتين: قائمة المنتجات/بحث على اليسار، سلة على اليمين.

دعم وضع العرض الأفقي/الرأسي.

مؤشر حالة الشبكة واعرض "Offline" واضح مع عدد العناصر غير المزامنة.


متطلبات الأجهزة والتكاملات

كاميرا للباركود أو دعم ماسح باركود USB/Bluetooth.

طابعة حرارية Bluetooth (عمل مثال لطباعة نص أعظمي 40 حرف/سطر).

مساحة تخزين محلية كافية (صور المنتجات، DB).

البطارية: حفظ التقدم المحلي عند انقطاع الطاقة المفاجئ.


تفاصيل تقنية ونماذج كود

مثال: نموذج جدول SQLite (pseudo)

CREATE TABLE products (
  id TEXT PRIMARY KEY,
  sku TEXT, barcode TEXT, name_ar TEXT, name_en TEXT,
  price_sell REAL, price_buy REAL, unit TEXT,
  stock REAL, updated_at TEXT
);

CREATE TABLE sales (
  id TEXT PRIMARY KEY,
  device_id TEXT, user_id TEXT, total_amount REAL, tax_amount REAL,
  status TEXT, created_at TEXT
);

CREATE TABLE sale_items (
  id TEXT PRIMARY KEY, sale_id TEXT, product_id TEXT, qty REAL, unit_price REAL
);

CREATE TABLE outbox (
  id TEXT PRIMARY KEY, op_type TEXT, payload TEXT, created_at TEXT, status TEXT
);

مثال: كود React Native (Products list + sync) — simplified

// src/services/syncService.js (pseudo)
import DB from './db';
import api from './api';

export async function syncAll() {
  // push local outbox
  const pending = await DB.getOutbox();
  if (pending.length) {
    const resp = await api.post('/sales/bulk', { device_id: DEVICE_ID, sales: pending.map(p=>p.payload) });
    if (resp.ok) {
      await DB.markOutboxSynced(pending.map(p=>p.id));
    }
  }
  // pull updates
  const last = await DB.getLastSyncAt();
  const updates = await api.get(`/products?since=${last}`);
  if (updates.products && updates.products.length) {
    await DB.upsertProducts(updates.products);
    await DB.setLastSyncAt(new Date().toISOString());
  }
}

مثال: طباعة إيصال عبر react-native-bluetooth-serial (pseudo)

import BluetoothSerial from 'react-native-bluetooth-serial';

async function printReceipt(text){
  const devices = await BluetoothSerial.list();
  const device = devices.find(d=>d.name.includes('BT-Printer'));
  if (!device) throw new Error('Printer not found');
  await BluetoothSerial.connect(device.id);
  await BluetoothSerial.write(text + '\n\n\n');
  await BluetoothSerial.disconnect();
}

CI: GitHub Actions — مثال مبسّط لبناء Debug APK

احفظ الملف في .github/workflows/android-build.yml

name: Build Android Debug APK

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v3

      - name: Setup Node
        uses: actions/setup-node@v3
        with:
          node-version: '16'
          cache: 'yarn'

      - name: Install dependencies
        run: |
          cd app
          yarn install --frozen-lockfile

      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '11'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Build Debug APK
        run: |
          cd app/android
          ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: tailpos-debug-apk
          path: app/android/app/build/outputs/apk/debug/app-debug.apk

اختبارات وقبول (Acceptance Criteria)

إنشاء عملية بيع محلياً بدون اتصال وتسجيلها في جدول sales مع status=pending.

بعد مزامنة ناجحة: status=synced ومرئيات على الخادم.

طباعة إيصال تجريبي عبر Bluetooth بنجاح.

وظيفة بحث بالباركود تعمل بواسطة الكاميرا أو إدخال يدوي.

عرض تقارير يومية صحيحة عند عرض التاريخ.


خطّة تنفيذ معيارية (MVP → v1 → v2)

MVP (2–4 أسابيع): POS أساسي، تخزين محلي SQLite، مزامنة sales/pull products، طباعة حرارية debug, Arabic UI.

v1 (إضافات بعد MVP): إدارة موردين، تقارير موسعة، إدارة عروض وخصومات، multi-store sync.

v2: تكامل ERPNext/محاسبة، إصدارات متعددة devices, تحسين الأداء (WatermelonDB).


المتطلبات التشغيلية والتصاريح

إعداد Secrets للـ keystore في GitHub إذا أردت Release signing.

SSL certificate للخادم (Let's Encrypt).

سياسة احتفاظ البيانات: نسخ احتياطي يومي للـ server DB.



---

ملحقات (نصائح تنفيذية)

استخدم WatermelonDB إذا كنت تتوقع آلاف المنتجات وسجلات كثيرة؛ SQLite التقليدي صالح للمشاريع الصغيرة إلى المتوسطة.

لإدارة الـimages، استعمل تخزين سحابي (S3) مع CDN واحتفظ بـthumbnail محلي.

فكر في offline-first libraries: redux-offline أو custom outbox pattern.

سجّل جميع عمليات الـsync في ملف لوج (log) لتسهيل استكشاف الأخطاء.


