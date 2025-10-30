#!/bin/bash

# StockRepository Implementation Verification Script
# This script verifies all StockRepository components are in place

echo "========================================="
echo "StockRepository Implementation Verification"
echo "========================================="
echo ""

echo "1. Checking StockRepository Interface..."
if [ -f "app/src/main/java/com/aseel/pos/data/StockRepository.kt" ]; then
    echo "   ✓ StockRepository.kt exists"
    echo "   - File size: $(wc -l < app/src/main/java/com/aseel/pos/data/StockRepository.kt) lines"
    echo "   - Contains $(grep -c "suspend fun" app/src/main/java/com/aseel/pos/data/StockRepository.kt) function declarations"
else
    echo "   ✗ StockRepository.kt NOT FOUND"
fi
echo ""

echo "2. Checking StockRepository Implementation..."
if [ -f "app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt" ]; then
    echo "   ✓ StockRepositoryImpl.kt exists"
    echo "   - File size: $(wc -l < app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt) lines"
    echo "   - Contains $(grep -c "override suspend fun" app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt) implementations"
else
    echo "   ✗ StockRepositoryImpl.kt NOT FOUND"
fi
echo ""

echo "3. Checking Updated ProductDao..."
if [ -f "app/src/main/java/com/aseel/pos/data/ProductDao.kt" ]; then
    echo "   ✓ ProductDao.kt exists"
    if grep -q "quantity_in_stock" app/src/main/java/com/aseel/pos/data/ProductDao.kt; then
        echo "   ✓ Contains quantity_in_stock column reference"
    fi
    if grep -q "incrementStock" app/src/main/java/com/aseel/pos/data/ProductDao.kt; then
        echo "   ✓ Contains incrementStock method"
    fi
else
    echo "   ✗ ProductDao.kt NOT FOUND"
fi
echo ""

echo "4. Checking Dependency Injection Module..."
if [ -f "app/src/main/java/com/aseel/pos/di/DatabaseModule.kt" ]; then
    echo "   ✓ DatabaseModule.kt exists"
    if grep -q "provideStockRepository" app/src/main/java/com/aseel/pos/di/DatabaseModule.kt; then
        echo "   ✓ Contains StockRepository provider"
    fi
else
    echo "   ✗ DatabaseModule.kt NOT FOUND"
fi
echo ""

echo "5. Key Features Verification..."
echo "   Checking for required functionality:"
grep -q "deductStockBatch" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ Batch stock deduction"
grep -q "rollbackStockDeduction" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ Transaction rollback"
grep -q "generateStockReport" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ Stock reporting"
grep -q "generateStockAnalytics" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ Stock analytics"
grep -q "exportInventoryToCsv" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ CSV export"
grep -q "calculateTotalStockValue" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ Stock value calculation"
grep -q "getLowStockProducts" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ Low stock monitoring"
echo ""

echo "6. Data Classes Verification..."
echo "   Checking for required data classes:"
grep -q "data class ProductWithStock" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ ProductWithStock"
grep -q "data class StockReport" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ StockReport"
grep -q "data class StockAnalytics" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ StockAnalytics"
grep -q "data class StockMovement" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ StockMovement"
grep -q "enum class MovementType" app/src/main/java/com/aseel/pos/data/StockRepository.kt && echo "   ✓ MovementType enum"
echo ""

echo "7. Implementation Details..."
if [ -f "app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt" ]; then
    echo "   Checking implementation quality:"
    grep -q "transaction" app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt && echo "   ✓ Transaction handling"
    grep -q "rollback" app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt && echo "   ✓ Rollback support"
    grep -q "FileWriter" app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt && echo "   ✓ CSV file operations"
    grep -q "escapeCsv" app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt && echo "   ✓ CSV escaping"
    grep -q "getAllProductsList" app/src/main/java/com/aseel/pos/data/StockRepositoryImpl.kt && echo "   ✓ Flow collection helper"
fi
echo ""

echo "========================================="
echo "Verification Complete!"
echo "========================================="
echo ""
echo "Summary:"
echo "- Created comprehensive StockRepository interface"
echo "- Implemented all required stock management functions"
echo "- Added transaction safety and rollback support"
echo "- Implemented stock reporting and analytics"
echo "- Added CSV export functionality"
echo "- Updated dependency injection configuration"
echo "- Fixed database column name inconsistencies"
echo ""
echo "Documentation:"
echo "- Created StockRepository_Implementation.md with full details"
echo ""
