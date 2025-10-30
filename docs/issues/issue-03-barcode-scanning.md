# Issue 3: Barcode Scanning (Phase-in)

## Labels
`roadmap`, `feature`, `good-first-issue`

## Description

### Scope
Integrate ZXing or ML Kit barcode scanner with camera permissions and tablet-optimized viewfinder.

### Acceptance Criteria
- [ ] Scan EAN-13 and QR codes
- [ ] Camera permission flow implemented
- [ ] Tablet viewfinder UI (landscape-optimized)
- [ ] Scanned SKU adds product to cart automatically
- [ ] Fallback manual input remains available
- [ ] Feature toggle in Settings

### Tech Notes
- Use ZXing Embedded or ML Kit Barcode Scanning
- Request `android.permission.CAMERA`
- Create separate module: `feature/barcode`
- Compose interop with Android View for camera

### AI-DEV PROMPT

```
You are working on the VC POS Android project.

TASK: Integrate barcode scanning functionality for product lookup.

REQUIREMENTS:
1. Dependencies:
   - Add ZXing Embedded: com.journeyapps:zxing-android-embedded:4.3.0
   - OR ML Kit: com.google.mlkit:barcode-scanning:17.3.0

2. Implementation:
   - Create BarcodeScanner screen/composable
   - Request CAMERA permission with proper rationale
   - Build tablet-optimized viewfinder (landscape)
   - On successful scan: lookup product by SKU and add to cart
   - Handle scan failures gracefully

3. Settings Integration:
   - Add "Enable Barcode Scanner" toggle in Settings
   - Show scanner button in POS screen only when enabled

4. Testing:
   - Test with various barcode formats (EAN-13, QR)
   - Handle invalid/not-found SKUs
   - Verify camera release on navigation

DELIVERABLES:
- Commit as: feat(barcode): integrate ZXing scanner with camera
- Open PR to alpha branch
- Add usage instructions to README.md

Example scan flow:
1. Tap barcode icon in POS screen
2. Grant camera permission (if first time)
3. Point camera at barcode
4. Product automatically added to cart
5. Return to POS screen
```

---
