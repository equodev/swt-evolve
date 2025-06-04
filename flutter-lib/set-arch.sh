#!/bin/bash

# Flutter macOS Architecture Switcher
# Usage: ./set-arch.sh [x86_64|arm64|universal]

ARCH_CONFIG_FILE="macos/Runner/Configs/Architecture.xcconfig"
FLUTTER_DEBUG_CONFIG="macos/Flutter/Flutter-Debug.xcconfig"
FLUTTER_RELEASE_CONFIG="macos/Flutter/Flutter-Release.xcconfig"

show_usage() {
    echo "Usage: $0 [x86_64|arm64|universal]"
    echo ""
    echo "  x86_64     - Build for Intel Macs only"
    echo "  arm64      - Build for Apple Silicon Macs only"
    echo "  universal  - Build universal binary (both architectures)"
    echo ""
    echo "Current architecture setting:"
    if [ -f "$ARCH_CONFIG_FILE" ]; then
        grep "FLUTTER_MACOS_ARCHS" "$FLUTTER_DEBUG_CONFIG" | head -1
    else
        echo "  Architecture config not found"
    fi
}

set_architecture() {
    local arch="$1"
    local archs_value=""
    
    case "$arch" in
        "x86_64")
            archs_value="x86_64"
            echo "Setting architecture to Intel (x86_64) only..."
            ;;
        "arm64")
            archs_value="arm64"
            echo "Setting architecture to Apple Silicon (arm64) only..."
            ;;
        "universal")
            archs_value="x86_64 arm64"
            echo "Setting architecture to Universal (both x86_64 and arm64)..."
            ;;
        *)
            echo "Error: Invalid architecture '$arch'"
            show_usage
            exit 1
            ;;
    esac
    
    # Update the Flutter config files
    sed -i.bak "s/FLUTTER_MACOS_ARCHS = .*/FLUTTER_MACOS_ARCHS = $archs_value/" "$FLUTTER_DEBUG_CONFIG"
    sed -i.bak "s/FLUTTER_MACOS_ARCHS = .*/FLUTTER_MACOS_ARCHS = $archs_value/" "$FLUTTER_RELEASE_CONFIG"
    
    # Clean up backup files
    rm -f "$FLUTTER_DEBUG_CONFIG.bak" "$FLUTTER_RELEASE_CONFIG.bak"
    
    echo "Architecture set to: $archs_value"
    echo ""
    echo "You can now run 'flutter build macos' to build for the selected architecture(s)."
}

# Check if we're in the right directory
if [ ! -f "pubspec.yaml" ]; then
    echo "Error: This script must be run from the flutter-lib directory"
    exit 1
fi

# Parse command line arguments
if [ $# -eq 0 ]; then
    show_usage
    exit 0
elif [ $# -eq 1 ]; then
    set_architecture "$1"
else
    echo "Error: Too many arguments"
    show_usage
    exit 1
fi