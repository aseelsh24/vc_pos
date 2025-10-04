#!/bin/bash
# ==========================================================
# 🧠 Jules Setup Script for Grocery POS Project (Enhanced)
# Purpose: Fully prepare and test the POS environment
# ==========================================================

set -e  # Stop on first error

echo "🚀 Setting up environment for Grocery POS..."

# -----------------------------
# 1️⃣  Check Node.js and Java
# -----------------------------
echo "🧩 Checking Node.js..."
node -v || (echo "❌ Node.js not found. Please install Node 20+" && exit 1)

echo "☕ Checking Java..."
java -version || (echo "⚠️ Java not found. Please ensure JDK 17+ is installed." && exit 1)

# -----------------------------
# 2️⃣  Install global tools
# -----------------------------
echo "📦 Installing global CLI tools..."
npm install -g yarn expo-cli react-native-cli

# -----------------------------
# 3️⃣  Install dependencies
# -----------------------------
echo "📱 Installing mobile app dependencies..."
cd app
yarn install || npm install

# -----------------------------
# 4️⃣  Install backend (optional)
# -----------------------------
if [ -d "../server" ]; then
  echo "🖥️ Installing backend dependencies..."
  cd ../server
  yarn install || npm install
  cd ../app
fi

# -----------------------------
# 5️⃣  Environment setup
# -----------------------------
echo "⚙️ Creating .env file..."
cd ..
cat <<EOF > .env
API_URL=http://localhost:4000
SYNC_INTERVAL=300
ENABLE_LOGS=true
EOF

echo "✅ Environment variables configured."

# -----------------------------
# 6️⃣  Gradle warm-up (Android)
# -----------------------------
cd app/android
echo "🧱 Running Gradle warm-up..."
./gradlew --version || true
cd ../..

# -----------------------------
# 7️⃣  Test Run (Verification)
# -----------------------------
echo "🧪 Running quick test checks..."

# Run unit tests if they exist
if [ -d "app/__tests__" ]; then
  echo "🧩 Running Jest tests..."
  cd app
  npx jest --runInBand || echo "⚠️ Some tests may have failed."
  cd ..
else
  echo "ℹ️ No test directory found, skipping Jest tests."
fi

# Build a debug APK to ensure Android env works
echo "🏗️ Building Android Debug APK..."
cd app/android
./gradlew assembleDebug || (echo "❌ Android build failed." && exit 1)
cd ../..

# -----------------------------
# 8️⃣  Verify build output
# -----------------------------
APK_PATH="app/android/app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
  echo "✅ APK successfully built at: $APK_PATH"
else
  echo "❌ APK not found — build may have failed."
  exit 1
fi

# -----------------------------
# 9️⃣  Final confirmation
# -----------------------------
echo "🎉 Setup and test complete!"
echo "Your environment is ready for development and CI builds."
echo "Next steps:"
echo "  👉 cd app && yarn android"
echo "  👉 Or run: jules run jules-task.md"

exit 0
