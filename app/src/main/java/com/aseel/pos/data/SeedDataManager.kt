package com.aseel.pos.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PosDatabase
) {
    suspend fun seedIfNeeded() {
        withContext(Dispatchers.IO) {
            val categoryDao = database.categoryDao()
            val productDao = database.productDao()
            
            // Check if data already exists
            val existingProducts = productDao.getAllProducts()
            // If products exist, skip seeding
            // For simplicity, we'll seed every time in alpha for testing
            
            // Seed categories
            val categories = listOf(
                Category(nameAr = "مشروبات", nameEn = "Beverages"),
                Category(nameAr = "وجبات خفيفة", nameEn = "Snacks"),
                Category(nameAr = "ألبان ومجمدات", nameEn = "Dairy & Frozen"),
                Category(nameAr = "مخبوزات", nameEn = "Bakery"),
                Category(nameAr = "معلبات", nameEn = "Canned Goods"),
                Category(nameAr = "حلويات", nameEn = "Sweets"),
                Category(nameAr = "منظفات", nameEn = "Cleaning"),
                Category(nameAr = "عناية شخصية", nameEn = "Personal Care"),
                Category(nameAr = "خضروات وفواكه", nameEn = "Produce"),
                Category(nameAr = "متنوعات", nameEn = "Miscellaneous")
            )
            
            categoryDao.insertCategories(categories)
            
            // Seed products (40 products across categories)
            val products = mutableListOf<Product>()
            
            // Beverages
            products.addAll(listOf(
                Product(sku = "BEV001", nameAr = "ماء معدني 1.5 لتر", nameEn = "Mineral Water 1.5L", priceBase = 500.0, stockQty = 100, categoryId = 1),
                Product(sku = "BEV002", nameAr = "عصير برتقال 1 لتر", nameEn = "Orange Juice 1L", priceBase = 1500.0, stockQty = 50, categoryId = 1),
                Product(sku = "BEV003", nameAr = "مشروب غازي 330 مل", nameEn = "Soda 330ml", priceBase = 800.0, stockQty = 200, categoryId = 1),
                Product(sku = "BEV004", nameAr = "شاي أخضر 25 كيس", nameEn = "Green Tea 25 bags", priceBase = 2000.0, stockQty = 30, categoryId = 1)
            ))
            
            // Snacks
            products.addAll(listOf(
                Product(sku = "SNK001", nameAr = "شيبس بطاطس 150 جرام", nameEn = "Potato Chips 150g", priceBase = 1200.0, stockQty = 80, categoryId = 2),
                Product(sku = "SNK002", nameAr = "بسكويت شوكولاته 200 جرام", nameEn = "Chocolate Biscuits 200g", priceBase = 1800.0, stockQty = 60, categoryId = 2),
                Product(sku = "SNK003", nameAr = "فشار جاهز 100 جرام", nameEn = "Ready Popcorn 100g", priceBase = 900.0, stockQty = 90, categoryId = 2),
                Product(sku = "SNK004", nameAr = "مكسرات مشكلة 250 جرام", nameEn = "Mixed Nuts 250g", priceBase = 3500.0, stockQty = 40, categoryId = 2)
            ))
            
            // Dairy & Frozen
            products.addAll(listOf(
                Product(sku = "DAI001", nameAr = "حليب طازج 1 لتر", nameEn = "Fresh Milk 1L", priceBase = 2500.0, stockQty = 50, categoryId = 3),
                Product(sku = "DAI002", nameAr = "جبنة شيدر 200 جرام", nameEn = "Cheddar Cheese 200g", priceBase = 3000.0, stockQty = 30, categoryId = 3),
                Product(sku = "DAI003", nameAr = "زبادي 125 جرام", nameEn = "Yogurt 125g", priceBase = 600.0, stockQty = 100, categoryId = 3),
                Product(sku = "DAI004", nameAr = "آيس كريم 500 مل", nameEn = "Ice Cream 500ml", priceBase = 2800.0, stockQty = 25, categoryId = 3)
            ))
            
            // Bakery
            products.addAll(listOf(
                Product(sku = "BAK001", nameAr = "خبز طازج", nameEn = "Fresh Bread", priceBase = 400.0, stockQty = 150, categoryId = 4),
                Product(sku = "BAK002", nameAr = "كعك محلى 6 قطع", nameEn = "Sweet Muffins 6pc", priceBase = 1500.0, stockQty = 40, categoryId = 4),
                Product(sku = "BAK003", nameAr = "كرواسون 4 قطع", nameEn = "Croissant 4pc", priceBase = 2200.0, stockQty = 35, categoryId = 4),
                Product(sku = "BAK004", nameAr = "دونات محشي 4 قطع", nameEn = "Filled Donuts 4pc", priceBase = 1800.0, stockQty = 50, categoryId = 4)
            ))
            
            // Canned Goods
            products.addAll(listOf(
                Product(sku = "CAN001", nameAr = "تونة معلبة 160 جرام", nameEn = "Canned Tuna 160g", priceBase = 2000.0, stockQty = 70, categoryId = 5),
                Product(sku = "CAN002", nameAr = "ذرة حلوة 340 جرام", nameEn = "Sweet Corn 340g", priceBase = 1200.0, stockQty = 60, categoryId = 5),
                Product(sku = "CAN003", nameAr = "فول معلب 400 جرام", nameEn = "Canned Beans 400g", priceBase = 1500.0, stockQty = 55, categoryId = 5),
                Product(sku = "CAN004", nameAr = "طماطم معجون 400 جرام", nameEn = "Tomato Paste 400g", priceBase = 1800.0, stockQty = 45, categoryId = 5)
            ))
            
            // Sweets
            products.addAll(listOf(
                Product(sku = "SWT001", nameAr = "شوكولاتة 100 جرام", nameEn = "Chocolate Bar 100g", priceBase = 2500.0, stockQty = 80, categoryId = 6),
                Product(sku = "SWT002", nameAr = "حلوى مشكلة 250 جرام", nameEn = "Mixed Candy 250g", priceBase = 1600.0, stockQty = 70, categoryId = 6),
                Product(sku = "SWT003", nameAr = "علكة نعناع 30 قطعة", nameEn = "Mint Gum 30pc", priceBase = 1000.0, stockQty = 100, categoryId = 6),
                Product(sku = "SWT004", nameAr = "بونبون كراميل 200 جرام", nameEn = "Caramel Candy 200g", priceBase = 1400.0, stockQty = 65, categoryId = 6)
            ))
            
            // Cleaning
            products.addAll(listOf(
                Product(sku = "CLN001", nameAr = "سائل غسيل أطباق 500 مل", nameEn = "Dish Soap 500ml", priceBase = 2200.0, stockQty = 50, categoryId = 7),
                Product(sku = "CLN002", nameAr = "منظف أرضيات 1 لتر", nameEn = "Floor Cleaner 1L", priceBase = 3000.0, stockQty = 40, categoryId = 7),
                Product(sku = "CLN003", nameAr = "مناديل معقمة 50 ورقة", nameEn = "Disinfecting Wipes 50ct", priceBase = 1800.0, stockQty = 60, categoryId = 7),
                Product(sku = "CLN004", nameAr = "معطر جو 300 مل", nameEn = "Air Freshener 300ml", priceBase = 2500.0, stockQty = 45, categoryId = 7)
            ))
            
            // Personal Care
            products.addAll(listOf(
                Product(sku = "PER001", nameAr = "صابون استحمام 125 جرام", nameEn = "Bath Soap 125g", priceBase = 1500.0, stockQty = 80, categoryId = 8),
                Product(sku = "PER002", nameAr = "شامبو 400 مل", nameEn = "Shampoo 400ml", priceBase = 3500.0, stockQty = 40, categoryId = 8),
                Product(sku = "PER003", nameAr = "معجون أسنان 100 مل", nameEn = "Toothpaste 100ml", priceBase = 2000.0, stockQty = 70, categoryId = 8),
                Product(sku = "PER004", nameAr = "مناديل ورقية 200 ورقة", nameEn = "Tissues 200ct", priceBase = 1200.0, stockQty = 90, categoryId = 8)
            ))
            
            // Produce
            products.addAll(listOf(
                Product(sku = "PRO001", nameAr = "تفاح أحمر 1 كيلو", nameEn = "Red Apples 1kg", priceBase = 3000.0, stockQty = 50, categoryId = 9),
                Product(sku = "PRO002", nameAr = "موز 1 كيلو", nameEn = "Bananas 1kg", priceBase = 2500.0, stockQty = 60, categoryId = 9),
                Product(sku = "PRO003", nameAr = "طماطم 1 كيلو", nameEn = "Tomatoes 1kg", priceBase = 2000.0, stockQty = 70, categoryId = 9),
                Product(sku = "PRO004", nameAr = "خيار 1 كيلو", nameEn = "Cucumbers 1kg", priceBase = 1800.0, stockQty = 65, categoryId = 9)
            ))
            
            // Miscellaneous
            products.addAll(listOf(
                Product(sku = "MIS001", nameAr = "بطاريات AA عبوة 4", nameEn = "AA Batteries 4pk", priceBase = 2500.0, stockQty = 50, categoryId = 10),
                Product(sku = "MIS002", nameAr = "ولاعة غاز", nameEn = "Gas Lighter", priceBase = 500.0, stockQty = 100, categoryId = 10),
                Product(sku = "MIS003", nameAr = "كيس قمامة 30 قطعة", nameEn = "Garbage Bags 30ct", priceBase = 1800.0, stockQty = 60, categoryId = 10),
                Product(sku = "MIS004", nameAr = "ورق ألمنيوم 25 متر", nameEn = "Aluminum Foil 25m", priceBase = 2200.0, stockQty = 45, categoryId = 10)
            ))
            
            productDao.insertProducts(products)
        }
    }
}
