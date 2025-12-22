import 'package:flutter/material.dart';
import 'label_theme_extension.dart';

LabelThemeExtension getLabelLightTheme() {
  return LabelThemeExtension(
    primaryTextColor: const Color(0xFF111827), // Dark gray text from "View execution Report"
    secondaryTextColor: const Color(0xFF6B7280), // Medium gray text
    errorTextColor: const Color(0xFFDC2626), // Red text
    warningTextColor: const Color(0xFFF59E0B), // Amber text
    successTextColor: const Color(0xFF059669), // Green text
    disabledTextColor: const Color(0xFF9CA3AF), // Light gray disabled text
    linkTextColor: const Color(0xFF2563EB), // Blue link
    linkHoverTextColor: const Color(0xFF1D4ED8), // Darker blue hover
    
    // Background colors
    backgroundColor: null, // Transparent background (label has no background in image)
    hoverBackgroundColor: const Color(0xFFF9FAFB), // Light gray hover
    selectedBackgroundColor: const Color(0xFFEBF8FF), // Light blue selected
    
    // Border colors
    borderColor: const Color(0xFFD1D5DB), // Light gray border
    focusBorderColor: const Color(0xFF2563EB), // Blue focus border
    
    // Typography properties - extracted from light theme image text
    fontSize: 14.0, // Standard font size from "View execution Report" text
    smallFontSize: 12.0, // Smaller text
    largeFontSize: 16.0, // Larger text
    fontWeight: FontWeight.w400, // Normal weight
    boldFontWeight: FontWeight.w600, // Bold weight
    fontFamily: 'System',
    letterSpacing: 0.0,
    lineHeight: 1.4,
    
    // Complete TextStyle properties
    primaryTextStyle: const TextStyle(
      color: Color(0xFF111827),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    secondaryTextStyle: const TextStyle(
      color: Color(0xFF6B7280),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    errorTextStyle: const TextStyle(
      color: Color(0xFFDC2626),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    warningTextStyle: const TextStyle(
      color: Color(0xFFF59E0B),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    successTextStyle: const TextStyle(
      color: Color(0xFF059669),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    disabledTextStyle: const TextStyle(
      color: Color(0xFF9CA3AF),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    linkTextStyle: const TextStyle(
      color: Color(0xFF2563EB),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
      decoration: TextDecoration.underline,
    ),
    
    // Dimensions 
    minHeight: 20.0, // Minimum height for label
    minWidth: 0.0, // No minimum width
    maxWidth: double.infinity, // No maximum width constraint
    
    // Spacing and padding 
    padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 2.0),
    margin: EdgeInsets.zero,
    horizontalPadding: 4.0,
    verticalPadding: 2.0,
    iconTextSpacing: 8.0, // Space between icon and text
    
    // Border properties
    borderRadius: 4.0, // Slight rounding
    borderWidth: 1.0, // Standard border
    focusBorderWidth: 2.0, // Thicker focus border
    
    // Icon properties
    iconSize: 16.0, // Standard icon size
    smallIconSize: 14.0, // Small icon
    largeIconSize: 20.0, // Large icon
    
    // Interactive properties
    isSelectable: false, // Labels generally not selectable
    showTooltip: false, // No tooltip by default
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    
    // Alignment and positioning
    textAlign: TextAlign.start, // Left aligned
    mainAxisAlignment: MainAxisAlignment.start,
    crossAxisAlignment: CrossAxisAlignment.center,
    
    // Decoration properties
    textDecoration: TextDecoration.none,
    hoverTextDecoration: TextDecoration.none,
    decorationThickness: 1.0,
  );
}

LabelThemeExtension getLabelDarkTheme() {
  return LabelThemeExtension(
    // Text colors 
    primaryTextColor: const Color(0xFFF9FAFB), // Light text 
    secondaryTextColor: const Color(0xFF9CA3AF), // Medium gray text
    errorTextColor: const Color(0xFFF87171), // Light red text
    warningTextColor: const Color(0xFFFBBF24), // Light amber text
    successTextColor: const Color(0xFF34D399), // Light green text
    disabledTextColor: const Color(0xFF6B7280), // Darker gray disabled text
    linkTextColor: const Color(0xFF60A5FA), // Light blue link
    linkHoverTextColor: const Color(0xFF3B82F6), // Brighter blue hover
    
    // Background colors
    backgroundColor: null, // Transparent background 
    hoverBackgroundColor: const Color(0xFF374151), // Dark gray hover
    selectedBackgroundColor: const Color(0xFF1E3A8A), // Dark blue selected
    
    // Border colors
    borderColor: const Color(0xFF4B5563), // Dark gray border
    focusBorderColor: const Color(0xFF60A5FA), // Light blue focus border
    
    // Typography properties
    fontSize: 14.0, // Standard font size from "View execution Report" text
    smallFontSize: 12.0, // Smaller text
    largeFontSize: 16.0, // Larger text
    fontWeight: FontWeight.w400, // Normal weight
    boldFontWeight: FontWeight.w600, // Bold weight
    fontFamily: 'System',
    letterSpacing: 0.0,
    lineHeight: 1.4,
    
    // Complete TextStyle properties
    primaryTextStyle: const TextStyle(
      color: Color(0xFFF9FAFB),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    secondaryTextStyle: const TextStyle(
      color: Color(0xFF9CA3AF),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    errorTextStyle: const TextStyle(
      color: Color(0xFFF87171),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    warningTextStyle: const TextStyle(
      color: Color(0xFFFBBF24),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    successTextStyle: const TextStyle(
      color: Color(0xFF34D399),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    disabledTextStyle: const TextStyle(
      color: Color(0xFF6B7280),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
    ),
    linkTextStyle: const TextStyle(
      color: Color(0xFF60A5FA),
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      fontFamily: 'System',
      letterSpacing: 0.0,
      height: 1.4,
      decoration: TextDecoration.underline,
    ),
    
    // Dimensions 
    minHeight: 20.0, // Minimum height for label
    minWidth: 0.0, // No minimum width
    maxWidth: double.infinity, // No maximum width constraint
    
    // Spacing and padding 
    padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 2.0),
    margin: EdgeInsets.zero,
    horizontalPadding: 4.0,
    verticalPadding: 2.0,
    iconTextSpacing: 8.0, // Space between icon and text
    
    // Border properties
    borderRadius: 4.0, // Slight rounding
    borderWidth: 1.0, // Standard border
    focusBorderWidth: 2.0, // Thicker focus border
    
    // Icon properties
    iconSize: 16.0, // Standard icon size
    smallIconSize: 14.0, // Small icon
    largeIconSize: 20.0, // Large icon
    
    // Interactive properties
    isSelectable: false, // Labels generally not selectable
    showTooltip: false, // No tooltip by default
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    
    // Alignment and positioning
    textAlign: TextAlign.start, // Left aligned
    mainAxisAlignment: MainAxisAlignment.start,
    crossAxisAlignment: CrossAxisAlignment.center,
    
    // Decoration properties
    textDecoration: TextDecoration.none,
    hoverTextDecoration: TextDecoration.none,
    decorationThickness: 1.0,
  );
}

