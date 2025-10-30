#!/bin/bash

# Verification script for Stock Impact Implementation

echo "🔍 Verifying Stock Impact Implementation..."
echo "=============================================="

# Check if all required files exist
echo "📁 Checking file existence..."

files=(
    "app/src/main/java/com/aseel/pos/data/Transaction.kt"
    "app/src/main/java/com/aseel/pos/ui/TransactionsViewModel.kt"
    "app/src/main/java/com/aseel/pos/ui/screens/TransactionsScreen.kt"
    "app/src/main/java/com/aseel/pos/data/PosDatabase.kt"
    "app/src/main/java/com/aseel/pos/data/migrations/Migration2to3.kt"
    "app/src/main/java/com/aseel/pos/di/DatabaseModule.kt"
)

missing_files=()

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ $file - MISSING"
        missing_files+=("$file")
    fi
done

echo ""
echo "🔍 Checking for key implementations..."

# Check Transaction.kt for StockImpact
if grep -q "StockImpact" "app/src/main/java/com/aseel/pos/data/Transaction.kt"; then
    echo "✅ StockImpact data model in Transaction.kt"
else
    echo "❌ StockImpact data model missing in Transaction.kt"
fi

# Check TransactionsViewModel.kt for filtering
if grep -q "updateStockImpactFilter\|filteredTransactions" "app/src/main/java/com/aseel/pos/ui/TransactionsViewModel.kt"; then
    echo "✅ Stock impact filtering in TransactionsViewModel"
else
    echo "❌ Stock impact filtering missing in TransactionsViewModel"
fi

# Check TransactionsScreen.kt for UI components
if grep -q "StockImpactRow\|StockImpactSummaryCard" "app/src/main/java/com/aseel/pos/ui/screens/TransactionsScreen.kt"; then
    echo "✅ Stock impact UI components in TransactionsScreen"
else
    echo "❌ Stock impact UI components missing in TransactionsScreen"
fi

# Check for database migration
if grep -q "MIGRATION_2_3" "app/src/main/java/com/aseel/pos/data/migrations/Migration2to3.kt"; then
    echo "✅ Database migration exists"
else
    echo "❌ Database migration missing"
fi

# Check for imports and dependencies
echo ""
echo "🔍 Checking imports and dependencies..."

if grep -q "import com.aseel.pos.data.StockImpactStatus" "app/src/main/java/com/aseel/pos/ui/screens/TransactionsScreen.kt"; then
    echo "✅ Required imports in TransactionsScreen"
else
    echo "❌ Missing imports in TransactionsScreen"
fi

# Check for visual indicators
echo ""
echo "🔍 Checking visual indicators..."

visual_elements=(
    "Icons.Outlined.CheckCircle"
    "Icons.Outlined.Error" 
    "Icons.Outlined.RemoveCircle"
    "Color(0xFF4CAF50)"
    "Color(0xFFF44336)"
)

for element in "${visual_elements[@]}"; do
    if grep -q "$element" "app/src/main/java/com/aseel/pos/ui/screens/TransactionsScreen.kt"; then
        echo "✅ $element found"
    else
        echo "⚠️  $element not found"
    fi
done

echo ""
echo "📊 Summary"
echo "=========="

if [ ${#missing_files[@]} -eq 0 ]; then
    echo "✅ All required files are present"
else
    echo "⚠️  Missing files: ${#missing_files[@]}"
    printf '   - %s\n' "${missing_files[@]}"
fi

echo ""
echo "🎯 Implementation Status"
echo "========================"

# Count implementations
stock_impact_count=$(grep -c "StockImpact" app/src/main/java/com/aseel/pos/data/Transaction.kt)
filter_count=$(grep -c "FilterDialog\|SortDialog" app/src/main/java/com/aseel/pos/ui/screens/TransactionsScreen.kt)
ui_components_count=$(grep -c "StockImpactRow\|StockImpactSummaryBar" app/src/main/java/com/aseel/pos/ui/screens/TransactionsScreen.kt)

echo "📈 Stock Impact Status Models: $stock_impact_count"
echo "🎨 Filter/Sort Dialogs: $filter_count"
echo "🖼️  UI Components: $ui_components_count"

if [ $stock_impact_count -gt 0 ] && [ $filter_count -gt 0 ] && [ $ui_components_count -gt 0 ]; then
    echo "✅ Implementation appears complete!"
    echo ""
    echo "🚀 Next Steps:"
    echo "1. Test the application compilation"
    echo "2. Verify database migration works"
    echo "3. Test stock impact functionality in UI"
    echo "4. Verify backward compatibility with existing transactions"
else
    echo "❌ Implementation may be incomplete"
fi

echo ""
echo "✨ Verification complete!"