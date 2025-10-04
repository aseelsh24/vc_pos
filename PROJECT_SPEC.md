# مواصفات نظام نقاط البيع (POS) - البقالة

## جدول المحتويات
1. [نظرة عامة](#نظرة-عامة)
2. [المتطلبات الوظيفية](#المتطلبات-الوظيفية)
3. [المتطلبات غير الوظيفية](#المتطلبات-غير-الوظيفية)
4. [هيكل المشروع](#هيكل-المشروع)
5. [قاعدة البيانات](#قاعدة-البيانات)
6. [واجهات API](#واجهات-api)
7. [واجهات المستخدم](#واجهات-المستخدم)
8. [استراتيجية المزامنة](#استراتيجية-المزامنة)
9. [الأمان](#الأمان)
10. [الاختبار](#الاختبار)
11. [النشر والتوزيع](#النشر-والتوزيع)

## نظرة عامة

نظام نقاط بيع متكامل مصمم خصيصاً لتجار الجملة والتجزئة (بقالة) يعمل بنظام **Offline-first**، يدعم العمل الكامل بدون اتصال مع مزامنة تلقائية عند توفر الشبكة.

### المميزات الرئيسية
- ✅ عمل كامل بدون اتصال إنترنت
- ✅ واجهة مستخدم عربية (RTL)
- ✅ إدارة مخزون متقدمة
- ✅ فواتير مبيعات ومرتجعات
- ✅ دعم الباركود والطابعات الحرارية
- ✅ تقارير وأداء متجر
- ✅ مستخدمين متعددين بصلاحيات
- ✅ مزامنة مع خادم مركزي

## المتطلبات الوظيفية

### 1. إدارة المنتجات
- إضافة/تعديل/حذف المنتجات
- تصنيفات المنتجات (أطعمة، مشروبات، الخ)
- وحدات قياس متعددة (قطعة، كيلو، لتر)
- إدارة الأسعار والتكلفة
- حدود التنبيه للمخزون
- رموز الباركود (دعم ماسح الكاميرا والماسح الخارجي)

### 2. نقاط البيع (POS)
- واجهة بيع سريعة
- بحث سريع بالاسم/الباركود
- إضافة عناصر متعددة الكميات
- تطبيق خصومات (نسبة، مبلغ ثابت)
- إضافة ضريبة القيمة المضافة
- طرق دفع متعددة (نقدي، بطاقة، محفظة)
- طباعة إيصالات حرارية
- حفظ الفواتير مؤقتاً

### 3. إدارة المخزون
- حركة المخزون (دخول، خروج، تعديل)
- جرد المخزون
- تقارير نفاذ المنتجات
- سجل الحركات

### 4. التقارير
- تقرير المبيعات اليومية
- تقرير الإيرادات
- تقرير أفضل المنتجات مبيعاً
- تقرير حركة المخزون
- تقرير أداء الموظفين

### 5. إدارة المستخدمين
- مستويات الصلاحيات (مدير، مشرف، كاشير)
- تسجيل الدخول/الخروج
- سجل الأنشطة

### 6. المزامنة
- مزامنة تلقائية عند توفر الشبكة
- حل التعارضات (Last Write Wins)
- سجل المزامنة
- إعادة المزامنة اليدوية

## المتطلبات غير الوظيفية

### الأداء
- وقت استجابة واجهة المستخدم < 200ms
- تحميل أولي للتطبيق < 3 ثواني
- دعم حتى 10,000 منتج محلياً

### الموثوقية
- عمل مستمر حتى مع انقطاع الشبكة
- نسخ احتياطي تلقائي للبيانات
- استعادة البيانات بعد الأعطال

### الأمان
- تشفير البيانات الحساسة محلياً
- اتصال آمن مع الخادم (HTTPS)
- مصادقة المستخدمين
- سجل audit للعمليات الحساسة

### التوافق
- أندرويد 8.0+ (API 26+)
- شاشات 10 بوصة فما فوق
- دعم الاتصال بالطابعات الحرارية عبر Bluetooth
- دعم ماسحات الباركود (كاميرا، HID Bluetooth)

## هيكل المشروع

```

grocery-pos/
├──app/                          # تطبيق الموبايل
│├── src/
││   ├── components/          # مكونات قابلة لإعادة الاستخدام
││   ├── screens/             # شاشات التطبيق
││   ├── navigation/          # التنقل بين الشاشات
││   ├── services/            # الخدمات (قاعدة بيانات، مزامنة، طباعة)
││   ├── store/               # إدارة الحالة (Zustand/Redux)
││   ├── utils/               # أدوات مساعدة
││   └── constants/           # الثوابت
│├── assets/
││   ├── images/
││   ├── fonts/               # خطوط عربية
││   └── icons/
│└── tests/
├──server/                      # خدمة المزامنة (اختياري)
│├── src/
││   ├── controllers/
││   ├── models/
││   ├── routes/
││   └── sync/
│└── tests/
├──docs/                        # الوثائق
├──ci/                         # إعدادات CI/CD
└──scripts/                    # سكريبتات المساعدة

```

## قاعدة البيانات

### الجداول المحلية (SQLite)

#### products
```sql
CREATE TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    barcode TEXT UNIQUE,
    name_ar TEXT NOT NULL,
    name_en TEXT,
    category_id INTEGER,
    unit TEXT DEFAULT 'pcs', -- pcs, kg, liter
    price REAL NOT NULL,
    cost REAL,
    stock_quantity REAL DEFAULT 0,
    min_stock_level REAL DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'synced' -- synced, pending, error
);
```

categories

```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name_ar TEXT NOT NULL,
    name_en TEXT,
    parent_id INTEGER,
    is_active BOOLEAN DEFAULT 1
);
```

sales

```sql
CREATE TABLE sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    receipt_number TEXT UNIQUE,
    total_amount REAL NOT NULL,
    discount_amount REAL DEFAULT 0,
    tax_amount REAL DEFAULT 0,
    final_amount REAL NOT NULL,
    payment_method TEXT DEFAULT 'cash', -- cash, card, wallet
    payment_status TEXT DEFAULT 'paid', -- paid, pending, refunded
    customer_id INTEGER,
    user_id INTEGER NOT NULL,
    device_id TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sync_status TEXT DEFAULT 'pending'
);
```

sale_items

```sql
CREATE TABLE sale_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price REAL NOT NULL,
    total_price REAL NOT NULL,
    discount_amount REAL DEFAULT 0
);
```

stock_movements

```sql
CREATE TABLE stock_movements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    movement_type TEXT NOT NULL, -- in, out, adjustment
    quantity REAL NOT NULL,
    reference_type TEXT, -- sale, purchase, adjustment
    reference_id INTEGER,
    notes TEXT,
    user_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

users

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    name_ar TEXT NOT NULL,
    name_en TEXT,
    role TEXT DEFAULT 'cashier', -- admin, supervisor, cashier
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

sync_logs

```sql
CREATE TABLE sync_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    operation TEXT NOT NULL, -- push, pull, full_sync
    entity_type TEXT NOT NULL, -- products, sales, etc.
    records_count INTEGER,
    status TEXT DEFAULT 'pending', -- success, error
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

واجهات API

أساسيات

· Base URL: https://api.example.com/v1
· Authentication: Bearer Token
· Content-Type: application/json

Endpoints الأساسية

1. تسجيل الجهاز

```http
POST /devices/register
```

Request:

```json
{
    "device_id": "device_unique_id",
    "store_id": "store_123",
    "device_name": "Tablet_1"
}
```

Response:

```json
{
    "success": true,
    "device_token": "encrypted_device_token",
    "store_info": {
        "name": "متجر التميمي",
        "currency": "SAR",
        "tax_rate": 0.15
    }
}
```

2. جلب التحديثات

```http
POST /sync/pull
```

Request:

```json
{
    "device_id": "device_unique_id",
    "last_sync_at": "2024-01-15T10:30:00Z",
    "entities": ["products", "categories"]
}
```

Response:

```json
{
    "success": true,
    "data": {
        "products": [
            {
                "id": 1,
                "barcode": "6297001377784",
                "name_ar": "أرز بسمتي",
                "name_en": "Basmati Rice",
                "price": 25.5,
                "stock_quantity": 100,
                "updated_at": "2024-01-20T08:00:00Z"
            }
        ],
        "categories": [],
        "deleted_ids": {
            "products": [5, 8, 12]
        }
    }
}
```

3. رفع المبيعات

```http
POST /sync/push
```

Request:

```json
{
    "device_id": "device_unique_id",
    "sales": [
        {
            "local_id": 123,
            "receipt_number": "INV-001",
            "total_amount": 150.0,
            "items": [
                {
                    "product_id": 1,
                    "quantity": 2,
                    "unit_price": 25.5
                }
            ],
            "created_at": "2024-01-20T10:15:00Z"
        }
    ]
}
```

Response:

```json
{
    "success": true,
    "synced_ids": {
        "sales": [456]
    }
}
```

واجهات المستخدم

الشاشات الرئيسية

1. تسجيل الدخول

· حقل اسم المستخدم
· حقل كلمة المرور
· زر تسجيل الدخول
· تذكرني (اختياري)
· اللغة (عربي/إنجليزي)

2. لوحة التحكم

· إحصائيات سريعة (مبيعات اليوم، المنتجات المنتهية)
· وصول سريع للوظائف الرئيسية
· إشعارات المزامنة

3. شاشة البيع (POS)

· شريط البحث (نص، باركود)
· قائمة المنتجات (صور، أسماء، أسعار، مخزون)
· سلة المشتريات
· تفاصيل الدفع (المجموع، الخصم، الضريبة، الإجمالي)
· خيارات الدفع
· زر طباعة الإيصال

4. إدارة المنتجات

· قائمة المنتجات مع إمكانية البحث والتصفية
· زر إضافة منتج جديد
· تحرير وحذف المنتجات
· استيراد/تصدير

5. التقارير

· تقرير المبيعات اليومية
· تقرير حركة المخزون
· تقرير المنتجات الأكثر مبيعاً
· خيارات التصدير

استراتيجية المزامنة

مبدأ العمل

1. Offline-First: جميع العمليات تتم محلياً أولاً
2. Queue-based Sync: العمليات تضاف إلى طابور المزامنة
3. Conflict Resolution: Last Write Wins
4. Incremental Sync: نقل البيانات المتغيرة فقط

خوارزمية المزامنة

```javascript
class SyncService {
    async fullSync() {
        // 1. جلب آخر التحديثات من الخادم
        const updates = await this.pullUpdates();
        
        // 2. تطبيق التحديثات محلياً
        await this.applyUpdates(updates);
        
        // 3. رفع البيانات المحلية غير المزامنة
        await this.pushLocalChanges();
        
        // 4. تحديث حالة المزامنة
        await this.updateSyncStatus();
    }
    
    async pushLocalChanges() {
        const pendingSales = await this.getPendingSales();
        const pendingProducts = await this.getPendingProducts();
        
        // محاولة رفع البيانات مع إعادة المحاولة
        await this.retrySync(() => 
            this.api.pushChanges({
                sales: pendingSales,
                products: pendingProducts
            })
        );
    }
}
```

الأمان

التخزين الآمن

· استخدام React Native Keychain/Keystore
· تشفير البيانات الحساسة
· تخزين آمن للتوكن

أمان الاتصال

· HTTPS مع pinning للشهادة
· تجديد التوكن التلقائي
· حماية ضد replay attacks

أمان التطبيق

· منع reverse engineering (ProGuard)
· فحص integrity للتطبيق
· إخفاء المفاتيح والبيانات الحساسة

الاختبار

Unit Tests (Jest)

```javascript
// tests/products.test.js
describe('Product Management', () => {
    test('should add product to database', async () => {
        const product = {
            name_ar: 'تفاح',
            price: 10.5,
            barcode: '123456'
        };
        
        const result = await ProductService.addProduct(product);
        expect(result.id).toBeDefined();
        expect(result.sync_status).toBe('pending');
    });
});
```

E2E Tests (Detox)

```javascript
describe('POS Flow', () => {
    it('should complete a sale', async () => {
        await element(by.id('search-input')).typeText('أرز');
        await element(by.id('product-1')).tap();
        await element(by.id('quantity-input')).typeText('2');
        await element(by.id('add-to-cart')).tap();
        await element(by.id('checkout-button')).tap();
        await expect(element(by.id('receipt-screen'))).toBeVisible();
    });
});
```

النشر والتوزيع

بناء APK

```bash
# بناء Debug
cd android && ./gradlew assembleDebug

# بناء Release
cd android && ./gradlew assembleRelease
```

إعدادات CI/CD

· GitHub Actions لبناء APK تلقائياً
· فحص الأخطاء والاختبارات
· توقيع APK باستخدام secrets
· رفع إلى Play Console (اختياري)

متطلبات النظام

· Node.js 16+
· React Native 0.72+
· Android SDK
· Java 11
