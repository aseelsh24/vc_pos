# Issue 8: Reports Dashboard (Daily/Weekly/Monthly)

## Labels
`roadmap`, `feature`, `analytics`

## Description

### Scope
Create simple analytics dashboard with sales aggregates and transaction metrics.

### Acceptance Criteria
- [ ] Dashboard with filter: Today, This Week, This Month, Custom Range
- [ ] Metrics: Total Sales, Transaction Count, Average Ticket
- [ ] Sales by payment method breakdown
- [ ] Sales by category chart
- [ ] Top-selling products list
- [ ] Works offline (Room aggregation)

### Tech Notes
- Add ReportsRepository with aggregate queries
- Use Room @Query with SUM, COUNT, AVG
- Create ReportsScreen composable with charts
- Consider MPAndroidChart or Compose charting library

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Build reports dashboard with sales analytics.

REQUIREMENTS:
1. Repository Layer:
   - Create ReportsRepository
   - Add aggregate queries:
     * getSalesByDateRange(start, end): total, count, avg
     * getSalesByPaymentMethod(start, end): Map<PaymentMethod, Double>
     * getSalesByCategory(start, end): Map<Category, Double>
     * getTopProducts(start, end, limit): List<Product>

2. UI:
   - Create ReportsScreen composable
   - Date range picker (Today / Week / Month / Custom)
   - Metric cards: Total Sales, Transactions, Avg Ticket
   - Payment method pie chart
   - Category bar chart
   - Top products list

3. Charting:
   - Use Compose-friendly library (e.g., Vico Charts)
   - Dependency: com.patrykandpatrick.vico:compose:1.13.1
   - Handle empty data gracefully

4. Testing:
   - Add sample transactions for testing
   - Verify aggregates match raw data
   - Test date range filtering

DELIVERABLES:
- Commit as: feat(reports): dashboard aggregates
- Add tests: ReportsRepositoryTest.kt
- Update README with reports feature

Metrics to Display:
- Total Sales (in selected currency)
- Number of Transactions
- Average Ticket Size
- Sales by Payment Method
- Sales by Category
- Top 10 Products
```

---
