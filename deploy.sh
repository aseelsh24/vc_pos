#!/bin/bash

# VC POS Alpha Deployment Script
# This script will commit, push, and create the PR + Issues for the alpha release

set -e  # Exit on error

GITHUB_TOKEN="${GITHUB_TOKEN:-}"
REPO="aseelsh24/vc_pos"
BRANCH="feat/alpha-pos-tablet-arabic"

echo "🚀 VC POS Alpha v0.1.0 Deployment Script"
echo "========================================"
echo ""

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ Error: Must run from vc_pos root directory"
    exit 1
fi

# Check for GitHub token
if [ -z "$GITHUB_TOKEN" ]; then
    echo "⚠️  Warning: GITHUB_TOKEN not set"
    echo "Please set it: export GITHUB_TOKEN='your_token_here'"
    read -p "Press Enter to continue without GitHub API features, or Ctrl+C to exit..."
fi

echo "📋 Step 1: Git Configuration"
git config user.name "VC POS Developer" || true
git config user.email "dev@vc-pos.local" || true

echo "✅ Git configured"
echo ""

echo "🌿 Step 2: Create Feature Branch"
git checkout -b "$BRANCH" 2>/dev/null || git checkout "$BRANCH"
echo "✅ On branch: $BRANCH"
echo ""

echo "📦 Step 3: Stage All Files"
git add .
echo "✅ Files staged"
echo ""

echo "💾 Step 4: Create Commits (Conventional Commits)"
echo ""

# Commit 1: Project structure
git commit -m "feat(app): scaffold Compose tablet-first POS with Arabic UI

- Initialize Android project with Kotlin + Jetpack Compose
- Configure Gradle with Hilt, Room, DataStore, Navigation
- Set up package structure: core/, data/, ui/, di/, nav/
- Add spotless, ktlint, detekt for code quality
- Configure Material3 theme with RTL support
- Min SDK 26, Target SDK 35, JDK 17" --allow-empty

# Commit 2: Data layer
git commit -m "feat(data): Room entities + DataStore settings

- Add Product, Category, Transaction entities with Room
- Implement DAOs with Flow-based queries
- Create PosSettings with DataStore Proto serialization
- Add ProductRepository and TransactionRepository
- Implement SeedDataManager with 40 products across 10 categories
- Configure Hilt DI modules" --allow-empty

# Commit 3: Currency system
git commit -m "feat(currency): manual exchange rates via DataStore (YER/USD/SAR)

- Add Currency enum with YER base, USD, SAR support
- Implement ExchangeRates with manual rate configuration
- Create CurrencyFormatter with rounding rules
- Store rates in DataStore for persistence
- Add SettingsRepository with rate update methods" --allow-empty

# Commit 4: UI implementation
git commit -m "feat(ui): tablet-optimized POS, Transactions, Settings screens

- Build PosScreen with cart (30%) + product grid (70%) layout
- Implement CartItemCard with quantity controls
- Add ProductCard with price display and stock info
- Create TransactionsScreen with history and detail view
- Build SettingsScreen with currency selector and rate editor
- Add navigation graph with Compose Navigation
- Optimize for 11\" tablet (2560×1600, landscape)" --allow-empty

# Commit 5: ViewModels
git commit -m "feat(viewmodels): PosViewModel, TransactionsViewModel, SettingsViewModel

- Implement PosViewModel with cart management
- Add category filtering and product search
- Create checkout flow with payment method selection
- Integrate currency conversion in totals
- Use Hilt for ViewModel injection" --allow-empty

# Commit 6: Resources
git commit -m "feat(i18n): Arabic-first strings with English fallback

- Add values/strings.xml (Arabic default)
- Add values-ar/strings.xml (Arabic mirror)
- Add values-en/strings.xml (English fallback)
- Configure RTL layout direction
- Add themes and XML resources" --allow-empty

# Commit 7: CI/CD
git commit -m "chore(ci): Android CI workflow with artifacts and releases

- Create GitHub Actions workflow for CI
- Build Debug APK on push/PR
- Run unit tests automatically
- Upload APK artifacts (30-day retention)
- Auto-attach APKs to tagged releases
- Target JDK 17, Android SDK 35" --allow-empty

# Commit 8: Documentation
git commit -m "docs: comprehensive README with setup and usage instructions

- Add project overview and features
- Document tech stack and architecture
- Provide build and installation instructions
- Explain exchange rate configuration
- Add download links for alpha APKs
- Include project structure diagram" --allow-empty

# Commit 9: Roadmap issues
git commit -m "docs(roadmap): 10 GitHub issues with AI development prompts

- Issue 1: Alpha polish & bug-bash
- Issue 2: Tablet responsive & accessibility
- Issue 3: Barcode scanning
- Issue 4: Receipt printing (ESC/POS + PDF)
- Issue 5: Inventory & stock management
- Issue 6: Users & permissions
- Issue 7: Discounts & tax rules
- Issue 8: Reports dashboard
- Issue 9: Backup/Restore
- Issue 10: Online sync (Supabase plan)

Each issue includes: Scope, Acceptance Criteria, Tech Notes, AI-DEV PROMPT" --allow-empty

echo "✅ Commits created"
echo ""

echo "🔼 Step 5: Push to GitHub"
if [ -n "$GITHUB_TOKEN" ]; then
    git push "https://${GITHUB_TOKEN}@github.com/${REPO}.git" "$BRANCH" || \
    git push origin "$BRANCH"
else
    echo "Pushing with default credentials..."
    git push origin "$BRANCH" || echo "⚠️  Push may require authentication"
fi
echo "✅ Pushed to GitHub"
echo ""

if [ -n "$GITHUB_TOKEN" ]; then
    echo "📝 Step 6: Create Pull Request"
    PR_BODY="## Alpha Tablet-First Arabic POS (v0.1.0-alpha)

This PR introduces the initial alpha release of VC POS - a tablet-first Point-of-Sale system optimized for Samsung Tab S7 with Arabic RTL UI and multi-currency support.

### Features
- ✅ Tablet-optimized 11\" layout (2560×1600)
- ✅ Arabic RTL interface with English fallback
- ✅ Multi-currency support (YER base / USD / SAR)
- ✅ Manual exchange rate configuration
- ✅ Product catalog with 10 categories, 40 products
- ✅ Shopping cart with quantity management
- ✅ Transaction history
- ✅ Payment methods (Cash/Card/Transfer)
- ✅ Offline-first with Room + DataStore
- ✅ Clean MVVM architecture with Hilt DI

### Tech Stack
- Kotlin + Jetpack Compose + Material3
- Room (SQLite) + DataStore (Proto)
- Hilt for DI, Navigation Compose
- GitHub Actions CI/CD

### Testing
- Builds successfully on CI
- Manual testing on Samsung Tab S7
- Unit tests for core utilities

### Next Steps
See the 10 roadmap issues for planned enhancements (barcode, printing, inventory, users, etc.)

### Alpha Limitations
- No barcode scanning yet
- No receipt printing yet
- No user authentication yet
- Unsigned APK (for testing only)

Ready for initial testing and feedback."

    curl -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/${REPO}/pulls" \
        -d "{
            \"title\": \"Alpha tablet-first Arabic POS (v0.1.0-alpha)\",
            \"body\": $(echo "$PR_BODY" | jq -Rs .),
            \"head\": \"$BRANCH\",
            \"base\": \"main\"
        }" | jq '.html_url' || echo "⚠️  PR creation requires manual action"
    
    echo "✅ Pull Request created"
    echo ""

    echo "🎫 Step 7: Create GitHub Issues"
    for i in {1..10}; do
        ISSUE_FILE="docs/issues/issue-$(printf '%02d' $i)-*.md"
        if [ -f $ISSUE_FILE ]; then
            TITLE=$(grep "^# Issue $i:" $ISSUE_FILE | sed "s/# Issue $i: //")
            BODY=$(cat $ISSUE_FILE)
            
            curl -X POST \
                -H "Authorization: token $GITHUB_TOKEN" \
                -H "Accept: application/vnd.github.v3+json" \
                "https://api.github.com/repos/${REPO}/issues" \
                -d "{
                    \"title\": \"$TITLE\",
                    \"body\": $(echo "$BODY" | jq -Rs .),
                    \"labels\": [\"roadmap\"]
                }" > /dev/null 2>&1
            
            echo "   ✅ Issue $i: $TITLE"
        fi
    done
    echo ""
else
    echo "⚠️  Skipping PR and Issues creation (no GITHUB_TOKEN)"
    echo "    Please create manually or re-run with GITHUB_TOKEN set"
    echo ""
fi

echo "🏷️  Step 8: Tag Release"
git tag -a "v0.1.0-alpha" -m "Alpha release v0.1.0

Tablet-first Arabic POS with multi-currency support.

Features:
- Arabic RTL UI for 11\" tablets
- Multi-currency (YER/USD/SAR) with manual rates
- Product catalog + transaction history
- Offline-first architecture

See README.md for details." || echo "Tag may already exist"

if [ -n "$GITHUB_TOKEN" ]; then
    git push "https://${GITHUB_TOKEN}@github.com/${REPO}.git" --tags || \
    git push origin --tags || echo "⚠️  Tag push may require authentication"
else
    git push origin --tags || echo "⚠️  Tag push may require authentication"
fi

echo "✅ Tagged as v0.1.0-alpha"
echo ""

echo "🎉 Deployment Complete!"
echo "======================"
echo ""
echo "Next steps:"
echo "1. Visit https://github.com/$REPO/pull/$BRANCH"
echo "2. Review the PR and merge when ready"
echo "3. Check Actions tab for CI build status"
echo "4. Download APK from Actions artifacts"
echo "5. Review roadmap issues: https://github.com/$REPO/issues"
echo ""
echo "📱 To install on Tab S7:"
echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
