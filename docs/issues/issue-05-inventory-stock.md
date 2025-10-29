# Issue 5: Inventory & Stock Deduction

## Labels
`roadmap`, `feature`

## Description

### Scope
Implement stock decrement on completed sales and basic product CRUD screen.

### Acceptance Criteria
- [ ] Selling a product decreases stock quantity
- [ ] Negative stock prevented or flagged
- [ ] Product CRUD dialog (Add/Edit/Delete)
- [ ] Stock alert when low (< 10 items)
- [ ] Transaction rollback if insufficient stock

### Tech Notes
- Update ProductRepository with stock management
- Add transaction atomicity (Room @Transaction)
- Create ProductManagementScreen composable

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Implement inventory management with stock deduction.

REQUIREMENTS:
1. Stock Deduction:
   - Modify checkout flow to decrement stock after transaction
   - Use Room @Transaction for atomicity
   - Prevent negative stock (show error if insufficient)
   - Add unit tests for stock math

2. Product CRUD:
   - Create ProductManagementScreen (accessible from Settings)
   - Tablet dialog for Add/Edit Product
   - Fields: SKU, Name (AR/EN), Price, Stock, Category
   - Delete product with confirmation

3. Stock Alerts:
   - Show warning icon when stock < 10
   - Add low-stock filter in product grid

4. Repository Updates:
   - Add ProductRepository.decrementStock(productId, qty)
   - Add ProductRepository.updateProduct(product)
   - Handle concurrent stock updates safely

DELIVERABLES:
- Commit as: feat(inventory): stock decrement on sale + CRUD
- Add tests: InventoryRepositoryTest.kt
- Update README with inventory features

Test commands:
```bash
./gradlew testDebugUnitTest
```
```

---
