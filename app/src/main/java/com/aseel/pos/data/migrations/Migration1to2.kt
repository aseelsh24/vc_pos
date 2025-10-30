package com.aseel.pos.data.migrations

import androidx.room.migration.Migration

val MIGRATION_1_2 = Migration(1, 2) { database ->
    // Create new products table with quantity_in_stock instead of stockQty
    database.execSQL("""
        CREATE TABLE products_new (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sku TEXT NOT NULL,
            nameAr TEXT NOT NULL,
            nameEn TEXT,
            priceBase REAL NOT NULL,
            quantity_in_stock INTEGER NOT NULL DEFAULT 0,
            imagePath TEXT,
            categoryId INTEGER,
            FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE SET NULL
        )
    """)
    
    // Copy data from old table to new table
    database.execSQL("""
        INSERT INTO products_new (id, sku, nameAr, nameEn, priceBase, quantity_in_stock, imagePath, categoryId)
        SELECT id, sku, nameAr, nameEn, priceBase, stockQty, imagePath, categoryId FROM products
    """)
    
    // Drop the old table
    database.execSQL("DROP TABLE products")
    
    // Rename the new table
    database.execSQL("ALTER TABLE products_new RENAME TO products")
    
    // Recreate indices
    database.execSQL("CREATE INDEX index_products_categoryId ON products(categoryId)")
    database.execSQL("CREATE INDEX index_products_sku ON products(sku)")
}
