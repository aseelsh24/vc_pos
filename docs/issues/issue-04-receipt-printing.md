# Issue 4: Receipt Printing (ESC/POS + Android Print)

## Labels
`roadmap`, `feature`

## Description

### Scope
Implement receipt printing to Bluetooth thermal printers (ESC/POS) and Android Print Framework (PDF).

### Acceptance Criteria
- [ ] Receipt template with store header, items, totals, currency
- [ ] Android Print Framework integration (PDF output)
- [ ] ESC/POS command generation (stub for Bluetooth)
- [ ] Print Preview screen
- [ ] Receipt includes transaction ID, date, payment method
- [ ] Monospace formatting for alignment

### Tech Notes
- ESC/POS library: consider `dantsu/escpos-coffee-android`
- Use Android `PrintDocumentAdapter` for PDF
- No Bluetooth pairing UI in this phase (manual pairing required)

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Implement receipt printing with ESC/POS and Android Print Framework.

REQUIREMENTS:
1. Receipt Template:
   - Create ReceiptFormatter utility class
   - Format: Store name, date, items (name, qty, price), subtotal, tax, total
   - Use monospace font for alignment
   - Include transaction ID and payment method

2. Android Print (PDF):
   - Implement ReceiptPrintDocumentAdapter
   - Generate PDF from receipt template
   - Add "Print to PDF" button in transaction detail

3. ESC/POS (Stub):
   - Add dependency: com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0
   - Create EscPosService with stub methods
   - Generate ESC/POS commands (no Bluetooth connection yet)
   - Add "Print to Thermal" button (show "Not Connected" toast)

4. UI:
   - Create ReceiptPreviewScreen composable
   - Show receipt before printing
   - Add print options dialog

DELIVERABLES:
- Commit as: feat(printing): receipts (pdf + escpos stub)
- Update README with printing instructions
- Add sample receipt screenshot

Testing:
1. Complete a transaction
2. Tap transaction in history
3. Tap "Print Preview"
4. Select "Print to PDF" → verify PDF generated
5. Select "Print to Thermal" → verify toast shown
```

---
