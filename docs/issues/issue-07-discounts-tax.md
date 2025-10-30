# Issue 7: Discounts & Tax Rules

## Labels
`roadmap`, `feature`

## Description

### Scope
Implement line-item and total discounts, optional VAT, and proper totals calculation across currencies.

### Acceptance Criteria
- [ ] Discount modes: None, Line, Total
- [ ] Line discount: per-item percentage or fixed amount
- [ ] Total discount: applied to cart subtotal
- [ ] VAT configuration (0-100%)
- [ ] Totals recalculation with discount + tax
- [ ] Receipt shows discount and tax breakdown
- [ ] Currency conversion preserves accuracy

### Tech Notes
- Add discount fields to Transaction and LineItem
- Update PosViewModel totals calculation
- Store VAT rate in PosSettings

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Implement discount and tax calculation system.

REQUIREMENTS:
1. Data Model:
   - Add to LineItem: discount (Double), discountType (NONE/PERCENTAGE/FIXED)
   - Add to Transaction: totalDiscount, taxRate, taxAmount
   - Add to PosSettings: taxRate (default 0.0), taxIncluded (Boolean)

2. UI Implementation:
   - Add discount input in CartItemCard (per-item)
   - Add total discount button in POS screen
   - Add VAT config in Settings

3. Calculation Logic:
   - Create DiscountCalculator utility
   - Line discount: apply to subtotal
   - Total discount: apply after line items summed
   - Tax: apply after discounts
   - Formula: (Subtotal - Discount) * (1 + TaxRate) = Total

4. Currency Handling:
   - Apply discount in base currency (YER)
   - Convert final total to display currency
   - Add tests for mixed discount + VAT + currency conversion

DELIVERABLES:
- Commit as: feat(pricing): discount & VAT
- Add tests: DiscountCalculatorTest.kt (at least 10 test cases)
- Update README with pricing rules

Test Cases:
1. 10% line discount
2. 500 YER fixed discount
3. 15% VAT on discounted total
4. Currency conversion with discount + tax
```

---
