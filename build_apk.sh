#!/bin/bash

# VC POS Alpha - APK Build Script
# Run this script in Termux to build the debug APK

echo "🏗️  VC POS Alpha - APK Build Starting..."
echo "=================================="

# Check Java installation
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "📥 Please install Java 17+ in Termux:"
    echo "   pkg install openjdk-17"
    echo "   export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk"
    exit 1
fi

echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

# Check if we're in the correct directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Not in VC POS project directory"
    echo "📁 Please run this script from the vc_pos folder"
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

echo "🔧 Cleaning previous builds..."
./gradlew clean

echo "🏗️  Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK Location:"
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo "   $APK_PATH"
        echo ""
        echo "📊 APK Size: $(du -h "$APK_PATH" | cut -f1)"
        echo ""
        echo "🚀 To install on your Samsung Tab S7:"
        echo "   adb install $APK_PATH"
        echo ""
        echo "📋 Testing Checklist:"
        echo "   □ Receipt printing (PDF generation)"
        echo "   □ Stock management and deduction"
        echo "   □ Low stock alerts"
        echo "   □ Inventory reports"
        echo "   □ Transaction history with stock impact"
    else
        echo "⚠️  APK file not found at expected location"
    fi
else
    echo "❌ Build failed!"
    echo "🔍 Check the error messages above"
    echo ""
    echo "🛠️  Common solutions:"
    echo "   - Ensure Android SDK is installed"
    echo "   - Check internet connection for dependencies"
    echo "   - Try: ./gradlew --refresh-dependencies assembleDebug"
    exit 1
fi

echo ""
echo "✨ Build process completed!"
echo "📱 Ready for testing on Samsung Tab S7"