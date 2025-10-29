# VC POS - نقطة بيع الأصيل

![Android CI](https://github.com/aseelsh24/vc_pos/workflows/Android%20CI/badge.svg)

**Tablet-first Arabic POS system for Samsung Tab S7 with multi-currency support**

## Overview

VC POS is a modern Point-of-Sale application built specifically for 11" tablets (Samsung Tab S7) with:

- **Arabic-first UI** with full RTL (Right-to-Left) support
- **Multi-currency support** (YER / USD / SAR) with manual exchange rates
- **Offline-first** operation with Room database
- **Tablet-optimized** interface with Material3 design
- **Clean architecture** using MVVM + Repository pattern
- **Modern Android stack** (Jetpack Compose, Hilt, DataStore)

## Features (v0.1.0-alpha)

### ✅ Core Functionality
- [x] Product catalog with categories
- [x] Shopping cart with quantity management
- [x] Multi-currency display (YER base, USD, SAR)
- [x] Manual exchange rate configuration
- [x] Transaction history
- [x] Cash/Card/Transfer payment methods
- [x] Tablet-optimized 11" layout
- [x] Arabic RTL interface

### 🚧 Coming Soon
- [ ] Barcode scanning
- [ ] Receipt printing (ESC/POS + PDF)
- [ ] Inventory management
- [ ] User roles & permissions
- [ ] Discount & tax rules
- [ ] Reports dashboard
- [ ] Backup/Restore
- [ ] Online sync (Supabase)

## Technical Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin (JDK 17) |
| **UI** | Jetpack Compose + Material3 |
| **Architecture** | MVVM + Repository |
| **DI** | Hilt |
| **Database** | Room (SQLite) |
| **Settings** | DataStore (Proto) |
| **Navigation** | Navigation Compose |
| **Min/Target SDK** | 26 / 35 |

## Build Instructions

### Prerequisites
- Android Studio Ladybug or later
- JDK 17
- Android SDK with API 35

### Clone & Build
```bash
git clone https://github.com/aseelsh24/vc_pos.git
cd vc_pos
./gradlew assembleDebug
```

### Install on Device
```bash
./gradlew installDebug
```

### Run Tests
```bash
./gradlew testDebugUnitTest
```

## Configuration

### Setting Exchange Rates

1. Open the app and navigate to **Settings** (gear icon)
2. Select a currency (YER / USD / SAR)
3. Tap **Edit Rate** to modify the exchange rate
4. Enter the rate: `1 [CURRENCY] = X YER`
5. Tap **Save**

**Example rates (as of project creation):**
- 1 USD = 250 YER
- 1 SAR = 66.67 YER  
- 1 YER = 1 YER (base)

All prices are stored in YER and converted for display based on the selected currency.

### Seed Data

The app automatically seeds the database on first run with:
- 10 product categories
- 40 sample products with prices and stock

## Download Alpha APK

### From GitHub Actions (Latest Build)
1. Go to [Actions](https://github.com/aseelsh24/vc_pos/actions)
2. Click on the latest successful workflow run
3. Download **vc_pos-debug-apk** artifact
4. Extract and install the APK

### From Releases (Tagged Versions)
1. Go to [Releases](https://github.com/aseelsh24/vc_pos/releases)
2. Download the latest `.apk` file
3. Install on your tablet

**Note:** For alpha releases, APKs are unsigned. Enable "Install from Unknown Sources" in Android settings.

## Project Structure

```
vc_pos/
├── app/
│   └── src/main/
│       ├── java/com/aseel/pos/
│       │   ├── core/          # Currency, enums, utils
│       │   ├── data/          # Entities, DAOs, repositories
│       │   ├── di/            # Hilt modules
│       │   ├── ui/            # Screens, ViewModels, theme
│       │   └── nav/           # Navigation graph
│       └── res/
│           ├── values/        # Strings (Arabic default)
│           ├── values-ar/     # Arabic strings
│           └── values-en/     # English strings
├── .github/workflows/
│   └── android-ci.yml
└── README.md
```

## Contributing

This is an alpha release. Contributions welcome! See the [roadmap issues](https://github.com/aseelsh24/vc_pos/issues) for planned features.

### Development Workflow
1. Create a feature branch from `main`
2. Make changes following existing code style
3. Write tests for new features
4. Run `./gradlew spotlessApply` before committing
5. Open a PR to `main`

## Roadmap

See [GitHub Issues](https://github.com/aseelsh24/vc_pos/issues) for the complete roadmap with AI development prompts.

## License

[Add license information]

## Support

For issues or questions, please [open an issue](https://github.com/aseelsh24/vc_pos/issues/new).

---

**Version:** 0.1.0-alpha  
**Last Updated:** 2025-10-30  
**Maintainer:** @aseelsh24
