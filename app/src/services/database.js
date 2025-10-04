import SQLite from 'react-native-sqlite-storage';

// Enable promise-based API
SQLite.enablePromise(true);

const DB_NAME = 'grocery-pos.db';
const DB_LOCATION = 'default';

const getDbConnection = async () => {
  return SQLite.openDatabase({ name: DB_NAME, location: DB_LOCATION });
};

const createTables = async (db) => {
  // Use a transaction to ensure all tables are created successfully
  await db.transaction(tx => {
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS products (
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
    `);
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS categories (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          name_ar TEXT NOT NULL,
          name_en TEXT,
          parent_id INTEGER,
          is_active BOOLEAN DEFAULT 1
      );
    `);
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS sales (
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
    `);
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS sale_items (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          sale_id INTEGER NOT NULL,
          product_id INTEGER NOT NULL,
          quantity REAL NOT NULL,
          unit_price REAL NOT NULL,
          total_price REAL NOT NULL,
          discount_amount REAL DEFAULT 0
      );
    `);
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS stock_movements (
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
    `);
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS users (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          username TEXT UNIQUE NOT NULL,
          password_hash TEXT NOT NULL,
          name_ar TEXT NOT NULL,
          name_en TEXT,
          role TEXT DEFAULT 'cashier', -- admin, supervisor, cashier
          is_active BOOLEAN DEFAULT 1,
          created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      );
    `);
    tx.executeSql(`
      CREATE TABLE IF NOT EXISTS sync_logs (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          operation TEXT NOT NULL, -- push, pull, full_sync
          entity_type TEXT NOT NULL, -- products, sales, etc.
          records_count INTEGER,
          status TEXT DEFAULT 'pending', -- success, error
          error_message TEXT,
          created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      );
    `);
  });
};

export const initializeDatabase = async () => {
  try {
    const db = await getDbConnection();
    await createTables(db);
    console.log('Database and tables created successfully.');
    return db;
  } catch (error) {
    console.error('Failed to initialize database:', error);
    throw error;
  }
};