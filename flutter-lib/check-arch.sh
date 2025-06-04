#!/bin/bash

# Check current Flutter macOS architecture setting

FLUTTER_DEBUG_CONFIG="macos/Flutter/Flutter-Debug.xcconfig"

echo "Current macOS build architecture setting:"
echo "========================================"

if [ -f "$FLUTTER_DEBUG_CONFIG" ]; then
    CURRENT_ARCH=$(grep "FLUTTER_MACOS_ARCHS" "$FLUTTER_DEBUG_CONFIG" | head -1 | cut -d'=' -f2 | xargs)
    echo "Architecture(s): $CURRENT_ARCH"
    
    case "$CURRENT_ARCH" in
        "x86_64")
            echo "Target: Intel Macs only"
            ;;
        "arm64")
            echo "Target: Apple Silicon Macs only"
            ;;
        "x86_64 arm64")
            echo "Target: Universal binary (both Intel and Apple Silicon)"
            ;;
        *)
            echo "Target: Unknown/Custom configuration"
            ;;
    esac
else
    echo "Error: Flutter config file not found"
    exit 1
fi

echo ""
echo "To change architecture, run: ./set-arch.sh [x86_64|arm64|universal]"