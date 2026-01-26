import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:swtflutter/src/theme/theme.dart';
import 'dart:ui' as ui;

import 'package:swtflutter/screenshot.dart';
import 'measure_all.dart' show getThemes;
import 'measure_data.dart';

// Configuration
const bool ENABLE_SCREENSHOTS = true;
const Duration MEASUREMENT_DELAY = Duration.zero;
const FromTheme = -1;
// const Duration MEASUREMENT_DELAY = Duration(seconds: 2);

// Theme configuration for multi-theme measurement
class ThemeConfig {
  final String name; // e.g., "Default", "NonDefault"
  final ThemeData Function() themeFactory;

  ThemeConfig(this.name, this.themeFactory);
}

String widgetName(String fqn) => fqn.split('.').last;

// Measurement test case - generic for any widget
class MeasurementCase {
  final String fqn;
  String name;
  final String style; // Style name like "LEFT", "READ_ONLY", "HORIZONTAL|SEPARATOR"
  final Map<String, dynamic> expectedComponents;
  final Widget Function(GlobalKey key) widgetBuilder;
  final bool useFontTheme;

  MeasurementCase({
    required String descr,
    required this.fqn,
    required this.style,
    required this.expectedComponents,
    required this.widgetBuilder,
    this.useFontTheme = false,
  }) : name = "${widgetName(fqn)}_${style}_$descr";

  Widget buildWidget(GlobalKey key) {
    return RepaintBoundary(child: widgetBuilder(key));
  }
}

// Result of measuring a single case
class MeasurementResult {
  final String fqn;
  final String name;
  final String style; // Style name like "LEFT", "READ_ONLY", "HORIZONTAL|SEPARATOR"
  final Size finalSize;
  final Map<String, dynamic> expectedComponents;
  final Map<String, dynamic> discoveredComponents;
  final Map<String, RenderBoxInfo> renderBoxHierarchy;
  final bool useFontTheme;

  MeasurementResult(
    this.fqn,
    this.name,
    this.style,
    this.finalSize,
    this.expectedComponents,
    this.discoveredComponents,
    this.renderBoxHierarchy,
    this.useFontTheme,
  );

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'finalSize': {'width': finalSize.width, 'height': finalSize.height},
      'discoveredComponents': discoveredComponents,
      'renderBoxes': renderBoxHierarchy.map((k, v) => MapEntry(k, v.toJson())),
    };
  }
}

// Information about a RenderBox in the hierarchy
class RenderBoxInfo {
  final String type;
  final Size size;
  final EdgeInsetsGeometry? padding;
  final EdgeInsets? border;
  final EdgeInsets? margin;
  final int depth;
  final List<RenderBoxInfo> children;
  final String? textContent;
  final String? imageSource;
  final TextStyle? textStyle;

  RenderBoxInfo(
    this.type,
    this.size,
    this.depth, {
    this.padding,
    this.border,
    this.margin,
    this.textContent,
    this.imageSource,
    this.textStyle,
    this.children = const [],
  });

  Map<String, dynamic> toJson() {
    return {
      'type': type,
      'size': {'width': size.width, 'height': size.height},
      'depth': depth,
      if (textContent != null) 'textContent': textContent,
      if (imageSource != null) 'imageSource': imageSource,
      if (textStyle != null)
        'textStyle': {
          'fontFamily': textStyle!.fontFamily,
          'fontSize': textStyle!.fontSize,
          'fontWeight': textStyle!.fontWeight?.index,
          'fontStyle': textStyle!.fontStyle?.index,
        },
      if (padding != null)
        'padding': {
          'left': (padding is EdgeInsetsDirectional) ? (padding as EdgeInsetsDirectional)!.start : (padding as EdgeInsets)!.left,
          'top': (padding is EdgeInsetsDirectional) ? (padding as EdgeInsetsDirectional)!.top : (padding as EdgeInsets)!.top,
          'right': (padding is EdgeInsetsDirectional) ? (padding as EdgeInsetsDirectional)!.end : (padding as EdgeInsets)!.right,
          'bottom': (padding is EdgeInsetsDirectional) ? (padding as EdgeInsetsDirectional)!.bottom : (padding as EdgeInsets)!.bottom,
        },
      if (border != null)
        'border': {
          'left': border!.left,
          'top': border!.top,
          'right': border!.right,
          'bottom': border!.bottom,
        },
      if (margin != null)
        'margin': {
          'left': margin!.left,
          'top': margin!.top,
          'right': margin!.right,
          'bottom': margin!.bottom,
        },
      if (children.isNotEmpty)
        'children': children.map((c) => c.toJson()).toList(),
    };
  }
}

// Analysis results for a widget type
class WidgetAnalysis {
  final String widgetType;
  final List<MeasurementResult> measurements;
  final Map<String, dynamic> derivedConstants;
  final String algorithm;

  WidgetAnalysis(
    this.widgetType,
    this.measurements,
    this.derivedConstants,
    this.algorithm,
  );

  Map<String, dynamic> toJson() {
    return {
      'widgetType': widgetType,
      'measurements': measurements.map((m) => m.toJson()).toList(),
      'derivedConstants': derivedConstants,
      'algorithm': algorithm,
    };
  }
}

// Main measurement orchestrator
class WidgetMeasurer {
  final List<MeasurementCase> testCases = [];
  final List<MeasurementCase> themeSamplingCases = [];
  final List<MeasurementResult> results = [];
  int currentCaseIndex = 0;

  final Duration measurementDelay;

  // Threshold for determining if padding is consistent (low variance = consistent padding)
  static const double VARIANCE_THRESHOLD = 5.0;

  // Multi-theme support
  List<ThemeConfig> themesToMeasure = getThemes();
  int currentPhase = 1; // 1 = sizing measurements, 2 = theme sampling
  int currentThemeIndex = 0;
  // Map: themeName -> widgetType:style -> textStyle
  Map<String, Map<String, Map<String, dynamic>>> extractedThemes = {};

  WidgetMeasurer({this.measurementDelay = MEASUREMENT_DELAY});

  void addTestCase(MeasurementCase testCase) {
    testCases.add(testCase);
  }

  void addThemeCase(MeasurementCase testCase) {
    themeSamplingCases.add(testCase);
  }

  Widget buildCurrentCase(GlobalKey key) {
    if (currentPhase == 1) {
      // Phase 1: Regular sizing measurements
      if (currentCaseIndex >= testCases.length) {
        return Container();
      }
      return testCases[currentCaseIndex].buildWidget(key);
    } else {
      // Phase 2: Theme sampling
      if (currentCaseIndex >= themeSamplingCases.length) {
        return Container();
      }
      var tCase = themeSamplingCases[currentCaseIndex];
      tCase.name = "${widgetName(tCase.fqn)}_${tCase.style}_theme${getCurrentThemeName()}";
      return tCase.buildWidget(key);
    }
  }

  ThemeData getCurrentTheme() {
    if (themesToMeasure.isEmpty) {
      return ThemeData();
    }
    return themesToMeasure[currentThemeIndex].themeFactory();
  }

  String getCurrentThemeName() {
    if (themesToMeasure.isEmpty) {
      return 'Unknown';
    }
    return themesToMeasure[currentThemeIndex].name;
  }

  Future<void> measureCurrentCase(GlobalKey key) async {
    final testCasesList = currentPhase == 1 ? testCases : themeSamplingCases;

    if (currentCaseIndex >= testCasesList.length) {
      if (currentPhase == 1) {
        _finishPhase1();
      } else {
        _finishPhase2();
      }
      return;
    }

    final renderBox = key.currentContext?.findRenderObject() as RenderBox?;
    if (renderBox == null) {
      print(
        'ERROR: Could not find render box for case ${testCasesList[currentCaseIndex].name}',
      );
      _nextCase(key);
      return;
    }

    final size = renderBox.size;
    final testCase = testCasesList[currentCaseIndex];

    final phaseLabel = currentPhase == 1
        ? 'SIZING'
        : 'THEME:${getCurrentThemeName()}';
    print('=== [$phaseLabel] Measuring: ${testCase.name} ===');
    print('Final size: $size');

    if (ENABLE_SCREENSHOTS) {
      await captureScreenshot(renderBox, testCase.fqn, testCase.name);
    }

    final hierarchy = <String, RenderBoxInfo>{};
    final rootInfo = _analyzeRenderBox(renderBox, 0);
    hierarchy['root'] = rootInfo;

    final discoveredComponents = _discoverComponents(
      rootInfo,
      testCase.expectedComponents,
    );

    print('Expected components: ${testCase.expectedComponents}');
    print('Discovered components: $discoveredComponents');

    if (currentPhase == 1) {
      // Phase 1: Store full results for sizing analysis
      final result = MeasurementResult(
        testCase.fqn,
        testCase.name,
        testCase.style,
        size,
        testCase.expectedComponents,
        discoveredComponents,
        hierarchy,
        testCase.useFontTheme,
      );
      results.add(result);
    } else {
      // Phase 2: Extract TextStyle only
      if (discoveredComponents.containsKey('text')) {
        final textInfo = discoveredComponents['text'];
        if (textInfo['textStyle'] != null) {
          final textStyleMap = textInfo['textStyle'] as Map<String, dynamic>;

          // Use style from test case directly (no parsing needed)
          final widgetType = widgetName(testCase.fqn);
          final style = testCase.style;
          final key = '$widgetType:$style';

          final themeName = getCurrentThemeName();
          extractedThemes.putIfAbsent(themeName, () => {});
          extractedThemes[themeName]![key] = {
            'fontFamily': textStyleMap['fontFamily'] ?? 'System',
            'fontSize': (textStyleMap['fontSize'] as double?)?.toInt() ?? 12,
            'fontBold':
                textStyleMap['fontWeight'] != null &&
                (textStyleMap['fontWeight'] as int) >= FontWeight.w600.index,
            'fontItalic':
                textStyleMap['fontStyle'] != null &&
                (textStyleMap['fontStyle'] as int) == FontStyle.italic.index,
          };

          print(
            '  Extracted TextStyle for $themeName/$key: ${extractedThemes[themeName]![key]}',
          );
        }
      }
    }

    print('Captured ${hierarchy.length} render box entries\n');

    _nextCase(key);

    // Check if we just completed the current phase (after incrementing index)
    if (currentCaseIndex >= testCasesList.length) {
      if (currentPhase == 1) {
        _finishPhase1();
      } else {
        _finishPhase2();
      }
    }
  }

  void _nextCase(GlobalKey key) {
    currentCaseIndex++;
  }

  bool hasMoreCases() {
    if (currentPhase == 1) {
      return currentCaseIndex < testCases.length;
    } else {
      return currentCaseIndex < themeSamplingCases.length;
    }
  }

  void _finishPhase1() {
    print('=== PHASE 1 COMPLETE (Sizing Analysis) ===');
    print('Total sizing cases measured: ${results.length}');

    // Analyze sizing and export (but don't generate themes yet)
    final analyses = _analyzeResultsByStyle();
    _exportSizingResults(analyses);

    // Check if we need Phase 2 (theme sampling)
    if (themesToMeasure.isNotEmpty && themeSamplingCases.isNotEmpty) {
      print('\n=== STARTING PHASE 2 (Theme Sampling) ===');
      print(
        'Themes to measure: ${themesToMeasure.map((t) => t.name).join(', ')}',
      );
      print('Sampling cases per theme: ${themeSamplingCases.length}');

      currentPhase = 2;
      currentThemeIndex = 0;
      currentCaseIndex = 0;

      // Phase 2 will be triggered by _MeasurementAppState detecting hasMoreCases() = true
    } else {
      // No theme sampling needed, finish immediately
      print('No theme sampling configured, measurements complete.');
      if (measurementDelay == Duration.zero) exit(0);
    }
  }

  void _finishPhase2() {
    print('=== Theme sampling complete for ${getCurrentThemeName()} ===');

    // Move to next theme
    currentThemeIndex++;

    if (currentThemeIndex < themesToMeasure.length) {
      // More themes to measure
      print('\n=== Switching to theme: ${getCurrentThemeName()} ===');
      currentCaseIndex = 0;
      // Continue Phase 2 with next theme
    } else {
      // All themes measured, finish
      _finishAllMeasurements();
    }
  }

  void _finishAllMeasurements() {
    print('\n=== ALL MEASUREMENTS COMPLETE ===');
    print('Total sizing cases: ${results.length}');
    print('Total themes sampled: ${extractedThemes.length}');

    // Now generate theme files from extractedThemes
    _exportThemeResults();

    if (measurementDelay == Duration.zero) exit(0);
  }

  void _exportSizingResults(List<WidgetAnalysis> analyses) {
    final combinedJson = {'analyses': analyses.map((a) => a.toJson()).toList()};

    final json = JsonEncoder.withIndent('  ').convert(combinedJson);

    final file = File('./build/measurements.json');
    file.writeAsStringSync(json);

    // Group analyses by FQN
    final byFqn = <String, List<WidgetAnalysis>>{};
    for (var analysis in analyses) {
      final fqn = analysis.measurements.first.fqn;
      byFqn.putIfAbsent(fqn, () => []).add(analysis);
    }

    // Generate sizing Java files only (no themes yet)
    for (var entry in byFqn.entries) {
      final fqn = entry.key;
      final widgetAnalyses = entry.value;
      _generateJavaWidgetSizes(fqn, widgetAnalyses);
    }

    print('\n======== SIZING ALGORITHMS BY STYLE ========\n');
    for (var analysis in analyses) {
      print(analysis.algorithm);
      print('Constants: ${analysis.derivedConstants}\n');
    }

    print('\nExported sizing results to ${file.path}');
  }

  void _exportThemeResults() {
    print('\n=== Generating Theme Files ===');

    // Group by widget type
    final byWidgetType = <String, Map<String, Map<String, dynamic>>>{};

    for (var themeEntry in extractedThemes.entries) {
      final themeName = themeEntry.key;
      final stylesMap = themeEntry.value;

      for (var styleEntry in stylesMap.entries) {
        final key = styleEntry.key; // e.g., "label:HORIZONTAL"
        final textStyle = styleEntry.value;

        final parts = key.split(':');
        final widgetType = parts[0]; // e.g., "label"
        final style = parts[1]; // e.g., "HORIZONTAL"

        byWidgetType.putIfAbsent(widgetType, () => {});
        byWidgetType[widgetType]!.putIfAbsent(themeName, () => {});
        byWidgetType[widgetType]![themeName]![style] = textStyle;
      }
    }

    // Find widget types that have theme sampling cases but no extracted text
    // These need a default theme with TextStyle.def()
    final widgetTypesWithThemeCases = <String, String>{};
    for (var testCase in themeSamplingCases) {
      final widgetType = widgetName(testCase.fqn);
      widgetTypesWithThemeCases[widgetType] = testCase.fqn;
    }

    // Generate theme file per widget type
    for (var entry in byWidgetType.entries) {
      final widgetType = entry.key;
      final themesByStyle = entry.value;

      // Get FQN from first result
      final fqn = results.firstWhere((r) => r.name.startsWith(widgetType)).fqn;

      _generateJavaWidgetThemeFromExtracted(fqn, widgetType, themesByStyle);
    }

    // Generate default theme for widgets without text
    for (var entry in widgetTypesWithThemeCases.entries) {
      final widgetType = entry.key;
      final fqn = entry.value;

      if (!byWidgetType.containsKey(widgetType)) {
        print('Generating default theme for $widgetType (no text component)');
        _generateJavaWidgetThemeFromExtracted(fqn, widgetType, null);
      }
    }
  }

  RenderBoxInfo _analyzeRenderBox(RenderBox renderBox, int depth) {
    final type = renderBox.runtimeType.toString();
    final size = renderBox.size;

    EdgeInsetsGeometry? padding;
    EdgeInsets? border;
    EdgeInsets? margin;
    String? textContent;
    String? imageSource;

    if (renderBox is RenderPadding) {
      padding = renderBox.padding as EdgeInsetsGeometry?;
    }

    if (renderBox is RenderDecoratedBox) {
      final decoration = renderBox.decoration;
      if (decoration is BoxDecoration) {
        if (decoration.border != null) {
          final b = decoration.border!;
          border = EdgeInsets.only(
            left: (b as dynamic).left?.width ?? 0,
            top: (b as dynamic).top?.width ?? 0,
            right: (b as dynamic).right?.width ?? 0,
            bottom: (b as dynamic).bottom?.width ?? 0,
          );
        }
      }
    }

    TextStyle? textStyle;
    if (renderBox is RenderParagraph) {
      final inlineSpan = renderBox.text;
      textContent = inlineSpan.toPlainText();

      // Extract TextStyle from the TextSpan
      if (inlineSpan is TextSpan) {
        textStyle = inlineSpan.style;
      }
    } else if (renderBox is RenderEditable) {
      // Handle TextField/TextFormField which use RenderEditable
      final inlineSpan = renderBox.text;
      if (inlineSpan != null) {
        textContent = inlineSpan.toPlainText();
        if (inlineSpan is TextSpan) {
          textStyle = inlineSpan.style;
        }
      }
    }

    if (type.contains('RenderImage')) {
      imageSource = 'image_found';
    }

    final children = <RenderBoxInfo>[];
    renderBox.visitChildren((child) {
      if (child is RenderBox) {
        children.add(_analyzeRenderBox(child, depth + 1));
      }
    });

    return RenderBoxInfo(
      type,
      size,
      depth,
      padding: padding,
      border: border,
      margin: margin,
      textContent: textContent,
      imageSource: imageSource,
      textStyle: textStyle,
      children: children,
    );
  }

  Map<String, dynamic> _discoverComponents(
    RenderBoxInfo root,
    Map<String, dynamic> expectedComponents,
  ) {
    final discovered = <String, dynamic>{};

    if (expectedComponents.containsKey('text')) {
      final expectedText = expectedComponents['text'];
      final textBox = _findTextBox(root, expectedText);
      if (textBox != null) {
        discovered['text'] = {
          'content': textBox.textContent,
          'width': textBox.size.width,
          'height': textBox.size.height,
          if (textBox.textStyle != null)
            'textStyle': {
              'fontFamily': textBox.textStyle!.fontFamily,
              'fontSize': textBox.textStyle!.fontSize,
              'fontWeight': textBox.textStyle!.fontWeight?.index,
              'fontStyle': textBox.textStyle!.fontStyle?.index,
            },
        };
      }
    }

    if (expectedComponents.containsKey('image')) {
      final imageBox = _findImageBox(root);
      if (imageBox != null) {
        discovered['image'] = {
          'width': imageBox.size.width,
          'height': imageBox.size.height,
        };
      }
    }

    return discovered;
  }

  RenderBoxInfo? _findTextBox(RenderBoxInfo box, String expectedText) {
    if (box.textContent != null && box.textContent == expectedText) {
      return box;
    }

    for (var child in box.children) {
      final found = _findTextBox(child, expectedText);
      if (found != null) return found;
    }

    return null;
  }

  RenderBoxInfo? _findImageBox(RenderBoxInfo box) {
    if (box.imageSource != null) {
      return box;
    }

    for (var child in box.children) {
      final found = _findImageBox(child);
      if (found != null) return found;
    }

    return null;
  }

  List<WidgetAnalysis> _analyzeResultsByStyle() {
    final groupedResults = <String, List<MeasurementResult>>{};

    for (var result in results) {
      // Use the style field directly instead of parsing from name
      final widgetType = widgetName(result.fqn);
      final style = result.style;
      final key = '$widgetType:$style';
      groupedResults.putIfAbsent(key, () => []).add(result);
    }

    final analyses = <WidgetAnalysis>[];
    for (var entry in groupedResults.entries) {
      final parts = entry.key.split(':');
      final widgetType = parts[0];
      final style = parts[1];
      final styleResults = entry.value;

      print(
        'Analyzing $widgetType:${style.toUpperCase()} (${styleResults.length} cases)',
      );
      final analysis = _analyzeResultsForStyle(widgetType, style, styleResults);
      analyses.add(analysis);
    }

    return analyses;
  }

  WidgetAnalysis _analyzeResultsForStyle(
    String widgetType,
    String style,
    List<MeasurementResult> styleResults,
  ) {
    final constants = <String, dynamic>{};

    double? minWidth;
    double? minHeight;
    for (var result in styleResults) {
      if (minWidth == null || result.finalSize.width < minWidth) {
        minWidth = result.finalSize.width;
      }
      if (minHeight == null || result.finalSize.height < minHeight) {
        minHeight = result.finalSize.height;
      }
    }

    constants['minWidth'] = minWidth;
    constants['minHeight'] = minHeight;

    // Extract TextStyle from cases that have useFontTheme=true
    // Only consider results where useFontTheme is true
    final resultsWithFontTheme = styleResults
        .where((r) => r.useFontTheme)
        .toList();

    Map<String, dynamic>? textStyleMap;
    if (resultsWithFontTheme.isNotEmpty) {
      for (var result in resultsWithFontTheme) {
        if (result.discoveredComponents.containsKey('text')) {
          final textInfo = result.discoveredComponents['text'];
          if (textInfo['textStyle'] != null) {
            textStyleMap = textInfo['textStyle'] as Map<String, dynamic>;
            break;
          }
        }
      }
      if (textStyleMap != null) {
        constants['textStyle'] = {
          'fontFamily': textStyleMap['fontFamily'] ?? 'System',
          'fontSize': (textStyleMap['fontSize'] as double?)?.toInt() ?? 12,
          'fontBold':
              textStyleMap['fontWeight'] != null &&
              (textStyleMap['fontWeight'] as int) >= FontWeight.w600.index,
          'fontItalic':
              textStyleMap['fontStyle'] != null &&
              (textStyleMap['fontStyle'] as int) == FontStyle.italic.index,
        };
      }
    }

    // Check if empty text affects sizing (e.g., Label measures empty text "", Button CHECK/RADIO have indicator padding)
    // This is true if: the expected text was empty AND either:
    //   1. Empty text was discovered (widget renders empty text), OR
    //   2. Widget size indicates a fixed element like checkbox/radio indicator is present
    bool emptyTextAffectsSizing = false;
    for (var result in styleResults) {
      // Check if this was an empty text test case
      if (result.expectedComponents.containsKey('text')) {
        final expectedText = result.expectedComponents['text'] as String?;
        if (expectedText != null && expectedText.isEmpty) {
          // Empty text was expected
          if (result.discoveredComponents.containsKey('text')) {
            // Text was discovered even when empty - definitely affects sizing (e.g., Label)
            emptyTextAffectsSizing = true;
            break;
          }
        }
      }
    }
    constants['emptyTextAffectsSizing'] = emptyTextAffectsSizing;

    // Determine if this is a VERTICAL style (affects code generation due to dimension swap)
    final isVerticalStyle = style.toUpperCase() == 'VERTICAL';
    constants['isVerticalStyle'] = isVerticalStyle;

    // Analyze padding from text-only cases (clean baseline)
    final paddingTextOnly = <String, dynamic>{};

    for (var result in styleResults) {
      if (result.discoveredComponents.containsKey('text') &&
          !result.discoveredComponents.containsKey('image')) {
        final textInfo = result.discoveredComponents['text'];
        var textWidth = textInfo['width'] as double;
        var textHeight = textInfo['height'] as double;

        // For VERTICAL labels, text is rotated, so swap dimensions
        final isVertical = style.toUpperCase() == 'VERTICAL';
        if (isVertical) {
          final temp = textWidth;
          textWidth = textHeight;
          textHeight = temp;
        }

        // Text-only: padding = finalSize - textSize
        paddingTextOnly[result.name] = {
          'horizontalExtra': result.finalSize.width - textWidth,
          'verticalExtra': result.finalSize.height - textHeight,
        };
      }
    }

    final paddingAnalysis = paddingTextOnly;

    if (paddingAnalysis.isNotEmpty) {
      final horizontalExtras = paddingAnalysis.values
          .map((v) => v['horizontalExtra'] as double)
          .toList();
      final verticalExtras = paddingAnalysis.values
          .map((v) => v['verticalExtra'] as double)
          .toList();

      final avgHorizontal =
          horizontalExtras.reduce((a, b) => a + b) / horizontalExtras.length;
      final avgVertical =
          verticalExtras.reduce((a, b) => a + b) / verticalExtras.length;

      constants['horizontalPadding'] = avgHorizontal;
      constants['verticalPadding'] = avgVertical;
      constants['paddingVariance'] = {
        'horizontal':
            horizontalExtras
                .map((e) => (e - avgHorizontal).abs())
                .reduce((a, b) => a + b) /
            horizontalExtras.length,
        'vertical':
            verticalExtras
                .map((e) => (e - avgVertical).abs())
                .reduce((a, b) => a + b) /
            verticalExtras.length,
      };
    } else {
      // No text component found - widget has constant size
      constants['horizontalPadding'] = 0.0;
      constants['verticalPadding'] = 0.0;
      constants['paddingVariance'] = null; // Indicates constant-size widget
    }

    // Analyze image contribution if images are present
    final imageAnalysis = <String, dynamic>{};
    for (var result in styleResults) {
      if (result.discoveredComponents.containsKey('image')) {
        final imageInfo = result.discoveredComponents['image'];
        final imageWidth = imageInfo['width'];
        final imageHeight = imageInfo['height'];

        imageAnalysis[result.name] = {
          'imageWidth': imageWidth,
          'imageHeight': imageHeight,
          'finalWidth': result.finalSize.width,
          'finalHeight': result.finalSize.height,
        };
      }
    }

    if (imageAnalysis.isNotEmpty) {
      // Check if image affects widget size by comparing cases with and without images
      final withImage = imageAnalysis.values.toList();
      final avgImageWidth =
          withImage
              .map((v) => v['imageWidth'] as double)
              .reduce((a, b) => a + b) /
          withImage.length;
      final avgImageHeight =
          withImage
              .map((v) => v['imageHeight'] as double)
              .reduce((a, b) => a + b) /
          withImage.length;

      // Simple heuristic: if any widget with image is significantly larger than minWidth/minHeight, image affects size
      final maxWidthWithImage = withImage
          .map((v) => v['finalWidth'] as double)
          .reduce((a, b) => a > b ? a : b);
      final maxHeightWithImage = withImage
          .map((v) => v['finalHeight'] as double)
          .reduce((a, b) => a > b ? a : b);

      final imageAffectsWidth =
          (maxWidthWithImage - minWidth!) > avgImageWidth * 0.5;
      final imageAffectsHeight =
          (maxHeightWithImage - minHeight!) > avgImageHeight * 0.5;

      // Analyze layout orientation for cases with both text and image
      String? imageLayout; // 'horizontal', 'vertical', or null
      double? imageSpacing;
      bool? imageUsesMax; // true if height uses MAX, false if uses SUM

      for (var result in styleResults) {
        if (result.discoveredComponents.containsKey('text') &&
            result.discoveredComponents.containsKey('image')) {
          final textInfo = result.discoveredComponents['text'];
          final imageInfo = result.discoveredComponents['image'];
          var textWidth = textInfo['width'] as double;
          var textHeight = textInfo['height'] as double;
          final imageWidth = imageInfo['width'];
          final imageHeight = imageInfo['height'];
          final finalWidth = result.finalSize.width;
          final finalHeight = result.finalSize.height;

          // For VERTICAL labels, text is rotated 90°, so swap width/height
          final isVertical = style.toUpperCase() == 'VERTICAL';
          if (isVertical) {
            final temp = textWidth;
            textWidth = textHeight;
            textHeight = temp;
          }

          // Get padding from constants (already calculated from text-only cases)
          final hPad = constants['horizontalPadding'] as double;
          final vPad = constants['verticalPadding'] as double;

          // Analyze width: finalWidth = textWidth + imageWidth + spacing + hPad
          final widthForComponents = finalWidth - hPad;
          final widthSpacing = widthForComponents - textWidth - imageWidth;

          // Analyze height: could be MAX(text, image) + vPad OR textHeight + imageHeight + spacing + vPad
          final heightForComponents = finalHeight - vPad;
          final maxHeight = textHeight > imageHeight ? textHeight : imageHeight;
          final sumHeight = textHeight + imageHeight;

          final diffFromMax = (heightForComponents - maxHeight).abs();
          final diffFromSum = (heightForComponents - sumHeight).abs();

          if (diffFromMax < 5) {
            // Height uses MAX (horizontal layout)
            imageLayout = 'horizontal';
            imageSpacing = widthSpacing;
            imageUsesMax = true;
          } else if (diffFromSum < 5) {
            // Height uses SUM (vertical layout)
            imageLayout = 'vertical';
            imageSpacing = heightForComponents - sumHeight;
            imageUsesMax = false;
          }

          print(
            '  Layout analysis: width=$finalWidth (text=$textWidth + image=$imageWidth + spacing=$widthSpacing + pad=$hPad)',
          );
          print(
            '               height=$finalHeight (MAX($textHeight, $imageHeight)=$maxHeight vs SUM=${sumHeight}, diffMax=$diffFromMax, diffSum=$diffFromSum)',
          );
          break; // Only need to analyze one case
        }
      }

      constants['imageAffectsWidth'] = imageAffectsWidth;
      constants['imageAffectsHeight'] = imageAffectsHeight;
      constants['imageLayout'] =
          imageLayout ?? 'horizontal'; // default to horizontal
      constants['imageSpacing'] = imageSpacing ?? 0.0;
      constants['imageUsesMax'] = imageUsesMax ?? true; // default to MAX

      if (imageAffectsWidth || imageAffectsHeight) {
        print(
          '  Image affects sizing: width=$imageAffectsWidth, height=$imageAffectsHeight, layout=$imageLayout, spacing=$imageSpacing, usesMax=$imageUsesMax',
        );
      }
    } else {
      constants['imageAffectsWidth'] = false;
      constants['imageAffectsHeight'] = false;
      constants['imageLayout'] = null;
      constants['imageSpacing'] = 0.0;
      constants['imageUsesMax'] = true;
    }

    final algorithm = _deriveAlgorithm(widgetType, style, constants);

    // Capitalize widget type for display (e.g., "btn" -> "Button")
    final capitalizedWidgetType =
        widgetType[0].toUpperCase() + widgetType.substring(1);
    return WidgetAnalysis(
      '${capitalizedWidgetType}-${style.toUpperCase()}',
      styleResults,
      constants,
      algorithm,
    );
  }

  String _deriveAlgorithm(
    String widgetType,
    String style,
    Map<String, dynamic> constants,
  ) {
    final capitalizedWidgetType =
        widgetType[0].toUpperCase() + widgetType.substring(1);
    final buffer = StringBuffer();
    buffer.writeln(
      '${style.toUpperCase()} $capitalizedWidgetType sizing algorithm (inferred):',
    );

    final paddingVariance = constants['paddingVariance'];

    // Check if this is a constant-size widget (no text component)
    if (paddingVariance == null) {
      buffer.writeln('width = ${constants['minWidth']}');
      buffer.writeln('  ↳ Constant-size widget (no text component)');
      buffer.writeln('height = ${constants['minHeight']}');
      buffer.writeln('  ↳ Constant-size widget (no text component)');
      buffer.writeln('');

      // Set flags for code generation
      constants['useHorizontalPadding'] = false;
      constants['useVerticalPadding'] = false;
      constants['isConstantSize'] = true;

      return buffer.toString();
    }

    // Widget has text component - analyze padding variance
    final varianceMap = paddingVariance as Map<String, dynamic>;
    final horizontalVariance = varianceMap['horizontal'] as double;
    final verticalVariance = varianceMap['vertical'] as double;

    final bool useHorizontalPadding = horizontalVariance < VARIANCE_THRESHOLD;
    final bool useVerticalPadding = verticalVariance < VARIANCE_THRESHOLD;
    constants['useHorizontalPadding'] = useHorizontalPadding;
    constants['useVerticalPadding'] = useVerticalPadding;
    constants['isConstantSize'] = false;

    if (useHorizontalPadding) {
      buffer.writeln(
        'width = max(textWidth + ${constants['horizontalPadding']}, ${constants['minWidth']})',
      );
      buffer.writeln(
        '  ↳ Horizontal padding is CONSISTENT (variance: ${horizontalVariance.toStringAsFixed(2)})',
      );
    } else {
      buffer.writeln('width = max(textWidth, ${constants['minWidth']})');
      buffer.writeln(
        '  ↳ Horizontal padding is VARIABLE (variance: ${horizontalVariance.toStringAsFixed(2)}), not adding to textWidth',
      );
    }

    if (useVerticalPadding) {
      buffer.writeln(
        'height = max(textHeight + ${constants['verticalPadding']}, ${constants['minHeight']})',
      );
      buffer.writeln(
        '  ↳ Vertical padding is CONSISTENT (variance: ${verticalVariance.toStringAsFixed(2)})',
      );
    } else {
      buffer.writeln('height = max(textHeight, ${constants['minHeight']})');
      buffer.writeln(
        '  ↳ Vertical padding is VARIABLE (variance: ${verticalVariance.toStringAsFixed(2)}), not adding to textHeight',
      );
    }

    // buffer.writeln('');
    return buffer.toString();
  }

  void _generateJavaWidgetSizes(String fqn, List<WidgetAnalysis> analyses) {
    // Extract simple class name from FQN (e.g., "Button" from "org.eclipse.swt.widgets.Button")
    final widgetType = widgetName(fqn);

    final buffer = StringBuffer();

    // Extract package and class name to create DartClassName import
    final lastDotIndex = fqn.lastIndexOf('.');
    final packageName = fqn.substring(0, lastDotIndex);
    final className = fqn.substring(lastDotIndex + 1);
    final dartFqn = '$packageName.Dart$className';

    // Check if any analysis uses images
    final hasAnyImageSupport = analyses.any(
      (a) =>
          a.derivedConstants['imageAffectsWidth'] == true ||
          a.derivedConstants['imageAffectsHeight'] == true,
    );

    buffer.writeln('package dev.equo.swt.size;');
    buffer.writeln();
    buffer.writeln('import dev.equo.swt.Config;');
    buffer.writeln('import dev.equo.swt.FontMetricsUtil;');
    if (hasAnyImageSupport) {
      buffer.writeln('import dev.equo.swt.ImageMetricUtil;');
    }
    buffer.writeln('import org.eclipse.swt.SWT;');
    buffer.writeln('import org.eclipse.swt.graphics.FontData;');
    if (hasAnyImageSupport) {
      buffer.writeln('import org.eclipse.swt.graphics.Image;');
    }
    buffer.writeln('import org.eclipse.swt.graphics.Point;');
    buffer.writeln('import $dartFqn;');
    buffer.writeln();
    buffer.writeln('import static dev.equo.swt.Styles.hasFlags;');
    buffer.writeln();
    buffer.writeln('/**');
    buffer.writeln(' * Auto-generated sizing for $widgetType widgets.');
    buffer.writeln(' * Generated from Flutter measurements.');
    buffer.writeln(' *');
    buffer.writeln(' * DO NOT EDIT MANUALLY - regenerate from measure.dart');
    buffer.writeln(' */');
    buffer.writeln('public class ${widgetType}Sizes {');
    buffer.writeln();

    // Group analyses by their constants to avoid duplicating identical sizing logic
    // IMPORTANT: VERTICAL styles must never be grouped with HORIZONTAL styles due to dimension swap
    String getConstantsKey(Map<String, dynamic> constants) {
      final isConstantSize = constants['isConstantSize'] == true;
      final useHorizontalPadding = constants['useHorizontalPadding'] as bool;
      final useVerticalPadding = constants['useVerticalPadding'] as bool;
      final isVerticalStyle = constants['isVerticalStyle'] == true;
      return '${constants['minWidth']}_${constants['minHeight']}_'
          '${isConstantSize}_${useHorizontalPadding}_${useVerticalPadding}_'
          '${constants['horizontalPadding']}_${constants['verticalPadding']}_'
          'vertical:$isVerticalStyle';
    }

    final Map<String, List<WidgetAnalysis>> groupsByConstants = {};
    for (var analysis in analyses) {
      final key = getConstantsKey(analysis.derivedConstants);
      groupsByConstants.putIfAbsent(key, () => []).add(analysis);
    }

    // Map from style to class name (for merged groups)
    final Map<String, String> styleToClassName = {};

    // Generate inner classes only for unique constant sets
    for (var group in groupsByConstants.values) {
      // Use the simplest (shortest) style name for the class
      final styles = group.map((a) => a.widgetType.split('-')[1]).toList();
      styles.sort((a, b) => a.length.compareTo(b.length));
      final representativeStyle = styles.first;
      final javaClassName = representativeStyle.replaceAll('|', '_');

      // Map all styles in this group to the same class name
      for (var style in styles) {
        styleToClassName[style] = javaClassName;
      }

      final constants = group.first.derivedConstants;
      final isConstantSize = constants['isConstantSize'] == true;
      final useHorizontalPadding = constants['useHorizontalPadding'] as bool;
      final useVerticalPadding = constants['useVerticalPadding'] as bool;

      buffer.writeln('    static class $javaClassName {');
      buffer.writeln(
        '        static final double MIN_WIDTH = ${constants['minWidth']};',
      );
      buffer.writeln(
        '        static final double MIN_HEIGHT = ${constants['minHeight']};',
      );

      // Only generate padding constants if they're used by the algorithm
      if (!isConstantSize && useHorizontalPadding) {
        buffer.writeln(
          '        static final double HORIZONTAL_PADDING = ${constants['horizontalPadding']};',
        );
      }
      if (!isConstantSize && useVerticalPadding) {
        buffer.writeln(
          '        static final double VERTICAL_PADDING = ${constants['verticalPadding']};',
        );
      }

      // Generate IMAGE_SPACING constant if images affect sizing
      final imageAffectsWidth = constants['imageAffectsWidth'] as bool;
      final imageAffectsHeight = constants['imageAffectsHeight'] as bool;
      if (imageAffectsWidth || imageAffectsHeight) {
        final imageSpacing = constants['imageSpacing'] as double;
        if (imageSpacing > 0) {
          buffer.writeln(
            '        static final double IMAGE_SPACING = ${imageSpacing.toStringAsFixed(1)};',
          );
        }
      }

      // Generate EMPTY_TEXT_AFFECTS_SIZING constant for text-based widgets
      if (!isConstantSize) {
        final emptyTextAffectsSizing = constants['emptyTextAffectsSizing'] as bool;
        buffer.writeln(
          '        static final boolean EMPTY_TEXT_AFFECTS_SIZING = $emptyTextAffectsSizing;',
        );
      }

      buffer.writeln('    }');
      buffer.writeln();
    }

    // Check if any style is constant-size
    final hasAnyTextBasedWidget = analyses.any(
      (a) => a.derivedConstants['isConstantSize'] != true,
    );

    // Check if any style is VERTICAL
    final hasVerticalStyle = analyses.any(
      (a) => a.widgetType.split('-')[1] == 'VERTICAL',
    );

    // Generate computeSize method (public API)
    buffer.writeln(
      '    public static Point computeSize(Dart$widgetType widget, int wHint, int hHint, boolean changed) {',
    );
    buffer.writeln(
      '        return computeSizes(widget, wHint, hHint, changed).widget;',
    );
    buffer.writeln('    }');
    buffer.writeln();

    // Generate computeSizes method (internal implementation)
    buffer.writeln(
      '    static Measure computeSizes(Dart$widgetType widget, int wHint, int hHint, boolean changed) {',
    );
    buffer.writeln('        int style = widget.getStyle();');
    buffer.writeln();
    buffer.writeln('        Measure m = new Measure();');
    buffer.writeln();
    buffer.writeln('        double width, height;');
    buffer.writeln();

    void generateSizeCalculation(
      String styleName,
      bool isConstantSize,
      bool useHorizontalPadding,
      bool useVerticalPadding,
      bool imageAffectsWidth,
      bool imageAffectsHeight,
      Map<String, dynamic> constants,
      bool isVertical, {
      String indent = '            ',
    }) {
      if (isConstantSize) {
        // Constant-size widget - just use the constants directly
        buffer.writeln('${indent}width = $styleName.MIN_WIDTH;');
        buffer.writeln('${indent}height = $styleName.MIN_HEIGHT;');
      } else {
        // Make padding conditional on text/image existence when empty text doesn't affect sizing
        final emptyTextAffectsSizing = constants['emptyTextAffectsSizing'] as bool;

        // Text-based widget - calculate size based on text, image, spacing, and padding
        if (hasAnyTextBasedWidget) {
          buffer.writeln('${indent}m.text = computeText(widget, m, $styleName.EMPTY_TEXT_AFFECTS_SIZING);');
        }
        if (hasAnyImageSupport) {
          buffer.writeln('${indent}m.image = computeImage(widget);');
        }

        final imageUsesMax = constants['imageUsesMax'] as bool;
        final imageSpacing = constants['imageSpacing'] as double;
        final hasImageSpacing = imageSpacing > 0;

        // For VERTICAL styles, text dimensions are swapped (text is rotated 90°)
        final textX = isVertical ? 'm.text.y()' : 'm.text.x()';
        final textY = isVertical ? 'm.text.x()' : 'm.text.y()';

        // Width calculation: for horizontal layout, conditionally include image spacing when image exists
        String textWidthExpr;
        if (imageAffectsWidth && hasImageSpacing) {
          textWidthExpr =
              '($textX + m.image.x() + (m.image.x() > 0 ? $styleName.IMAGE_SPACING : 0))';
        } else if (imageAffectsWidth) {
          textWidthExpr = '($textX + m.image.x())';
        } else {
          textWidthExpr = textX;
        }

        // Height calculation: MAX for horizontal layout, SUM for vertical
        String textHeightExpr;
        if (imageAffectsHeight) {
          if (imageUsesMax) {
            // Horizontal layout - no spacing needed in height
            textHeightExpr = 'Math.max($textY, m.image.y())';
          } else if (hasImageSpacing) {
            // Vertical layout with conditional spacing
            textHeightExpr =
                '($textY + m.image.y() + (m.image.y() > 0 ? $styleName.IMAGE_SPACING : 0))';
          } else {
            // Vertical layout without spacing
            textHeightExpr = '($textY + m.image.y())';
          }
        } else {
          textHeightExpr = textY;
        }

        // // Make padding conditional on text/image existence when empty text doesn't affect sizing
        // final emptyTextAffectsSizing =
        //     constants['emptyTextAffectsSizing'] as bool;

        // Width: conditionally add padding when text exists
        // If IMAGE_SPACING is present, don't check for image - it's already handled separately
        final widthCondition = (hasAnyImageSupport && imageAffectsWidth && !hasImageSpacing)
            ? '($textX > 0 || m.image.x() > 0)'
            : '$textX > 0';

        if (useHorizontalPadding) {
          buffer.writeln(
            '${indent}width = Math.max($textWidthExpr + ($widthCondition ? $styleName.HORIZONTAL_PADDING : 0), $styleName.MIN_WIDTH);',
          );
        } else {
          buffer.writeln(
            '${indent}width = Math.max($textWidthExpr, $styleName.MIN_WIDTH);',
          );
        }

        if (useVerticalPadding) {
          if (emptyTextAffectsSizing) {
            // Always add padding (for widgets like Label)
            buffer.writeln(
              '${indent}height = Math.max($textHeightExpr + $styleName.VERTICAL_PADDING, $styleName.MIN_HEIGHT);',
            );
          } else {
            // Conditionally add padding when text or image exists (for widgets like Button)
            final heightCondition = (hasAnyImageSupport && imageAffectsHeight)
                ? '($textY > 0 || m.image.y() > 0)'
                : '$textY > 0';
            buffer.writeln(
              '${indent}height = Math.max($textHeightExpr + ($heightCondition ? $styleName.VERTICAL_PADDING : 0), $styleName.MIN_HEIGHT);',
            );
          }
        } else {
          buffer.writeln(
            '${indent}height = Math.max($textHeightExpr, $styleName.MIN_HEIGHT);',
          );
        }
      }
    }

    // Sort groups by specificity (number of styles in the simplest member)
    // This ensures combinations like HORIZONTAL|SEPARATOR are checked before HORIZONTAL alone
    final sortedGroups = groupsByConstants.values.toList()
      ..sort((a, b) {
        final aMinStyles = a
            .map(
              (analysis) => analysis.widgetType.split('-')[1].split('|').length,
            )
            .reduce((min, count) => count < min ? count : min);
        final bMinStyles = b
            .map(
              (analysis) => analysis.widgetType.split('-')[1].split('|').length,
            )
            .reduce((min, count) => count < min ? count : min);
        final aMaxStyles = a
            .map(
              (analysis) => analysis.widgetType.split('-')[1].split('|').length,
            )
            .reduce((max, count) => count > max ? count : max);
        final bMaxStyles = b
            .map(
              (analysis) => analysis.widgetType.split('-')[1].split('|').length,
            )
            .reduce((max, count) => count > max ? count : max);
        // Sort by max styles descending, then by min styles descending
        if (bMaxStyles != aMaxStyles) return bMaxStyles.compareTo(aMaxStyles);
        return bMinStyles.compareTo(aMinStyles);
      });

    // Determine fallback (use first analysis as fallback)
    final fallbackAnalysis = analyses.first;
    final fallbackStyle = fallbackAnalysis.widgetType.split('-')[1];
    final fallbackJavaClassName = fallbackStyle.replaceAll('|', '_');
    final fallbackClassName = styleToClassName[fallbackStyle]!;

    int branchCount = 0;
    final List<String> fallbackStyles = [];

    for (int i = 0; i < sortedGroups.length; i++) {
      final group = sortedGroups[i];
      final styles = group.map((a) => a.widgetType.split('-')[1]).toList();

      // Get the representative class name
      final javaClassName = styleToClassName[styles.first]!;

      // Skip if this is the fallback branch (will be in else)
      if (javaClassName == fallbackClassName) {
        fallbackStyles.addAll(styles);
        continue;
      }

      // Build condition: OR all styles in the group
      final conditions = styles.map((style) {
        final styleCheck = style.contains('|')
            ? '(${style.split('|').map((s) => 'SWT.$s').join(' | ')})'
            : 'SWT.$style';
        return 'hasFlags(style, $styleCheck)';
      }).toList();

      final constants = group.first.derivedConstants;
      final isConstantSize = constants['isConstantSize'] == true;
      final useHorizontalPadding = constants['useHorizontalPadding'] as bool;
      final useVerticalPadding = constants['useVerticalPadding'] as bool;
      final imageAffectsWidth = constants['imageAffectsWidth'] as bool;
      final imageAffectsHeight = constants['imageAffectsHeight'] as bool;
      final isVertical =
          styles.first.contains('VERTICAL') &&
          !styles.first.contains('SEPARATOR');

      final prefix = branchCount == 0 ? '        if' : '        } else if';
      final condition = conditions.length == 1
          ? conditions.first
          : conditions.join(' || ');
      buffer.writeln('$prefix ($condition) {');
      generateSizeCalculation(
        javaClassName,
        isConstantSize,
        useHorizontalPadding,
        useVerticalPadding,
        imageAffectsWidth,
        imageAffectsHeight,
        constants,
        isVertical,
      );
      branchCount++;
    }

    // Generate fallback branch with comment showing what styles it covers
    final fallbackConstants = fallbackAnalysis.derivedConstants;
    final fallbackIsConstantSize = fallbackConstants['isConstantSize'] == true;
    final fallbackUseHorizontalPadding =
        fallbackConstants['useHorizontalPadding'] as bool;
    final fallbackUseVerticalPadding =
        fallbackConstants['useVerticalPadding'] as bool;
    final fallbackImageAffectsWidth =
        fallbackConstants['imageAffectsWidth'] as bool;
    final fallbackImageAffectsHeight =
        fallbackConstants['imageAffectsHeight'] as bool;
    final fallbackIsVertical =
        fallbackStyle.contains('VERTICAL') &&
        !fallbackStyle.contains('SEPARATOR');

    final fallbackComment = fallbackStyles.isNotEmpty
        ? ' // ${fallbackStyles.join(', ')}'
        : ' // default';

    if (branchCount > 0) {
      // There were if/else if branches, so we need an else block
      buffer.writeln('        } else {$fallbackComment');
      generateSizeCalculation(
        fallbackClassName,
        fallbackIsConstantSize,
        fallbackUseHorizontalPadding,
        fallbackUseVerticalPadding,
        fallbackImageAffectsWidth,
        fallbackImageAffectsHeight,
        fallbackConstants,
        fallbackIsVertical,
      );
      buffer.writeln('        }');
    } else {
      // No branches - all styles use the same constants, just generate the code directly
      generateSizeCalculation(
        fallbackClassName,
        fallbackIsConstantSize,
        fallbackUseHorizontalPadding,
        fallbackUseVerticalPadding,
        fallbackImageAffectsWidth,
        fallbackImageAffectsHeight,
        fallbackConstants,
        fallbackIsVertical,
        indent: '        ',
      );
    }
    buffer.writeln();
    buffer.writeln(
      '        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));',
    );
    buffer.writeln('        return m;');
    buffer.writeln('    }');
    buffer.writeln();

    // Generate computeImage helper if any style supports images
    if (hasAnyImageSupport) {
      buffer.writeln(
        '    private static PointD computeImage(Dart$widgetType widget) {',
      );
      buffer.writeln('        Image image = widget.getImage();');
      buffer.writeln('        if (image != null) {');
      buffer.writeln(
        '            return ImageMetricUtil.getImageSize(image.getImageData());',
      );
      buffer.writeln('        }');
      buffer.writeln('        return PointD.zero;');
      buffer.writeln('    }');
      buffer.writeln();
    }

    // Generate computeText helper if any style is text-based
    if (hasAnyTextBasedWidget) {
      buffer.writeln(
        '    private static PointD computeText(Dart$widgetType widget, Measure m, boolean emptyTextAffectsSizing) {',
      );
      buffer.writeln('        String text = widget.getText();');
      // PASSWORD style only applies to Text widget
      if (widgetType == 'Text') {
        buffer.writeln('        if (text != null && hasFlags(widget.getStyle(), SWT.PASSWORD)) {');
        buffer.writeln('            text = "*".repeat(text.length());');
        buffer.writeln('        }');
      }

      // Use parameter to decide whether empty text should be measured
      buffer.writeln('        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {');

      buffer.writeln(
        '            if (!Config.getConfigFlags().use_swt_fonts) {',
      );
      buffer.writeln(
        '                m.textStyle = ${widgetType}Theme.get().textStyle();',
      );
      buffer.writeln('            } else {');
      buffer.writeln(
        '                m.textStyle = TextStyle.from(widget.getFont());',
      );
      buffer.writeln('            }');
      buffer.writeln(
        '            return FontMetricsUtil.getFontSize(text, m.textStyle);',
      );
      buffer.writeln('        }');
      buffer.writeln('        return PointD.zero;');
      buffer.writeln('    }');
    }

    buffer.writeln('}');

    final javaFile = File(
      '../swt_native/src/main/java/dev/equo/swt/size/${widgetType}Sizes.java',
    );
    javaFile.writeAsStringSync(buffer.toString());
    print('Generated: ${javaFile.path}');
  }

  void _generateJavaWidgetThemeFromExtracted(
    String fqn,
    String widgetType,
    Map<String, Map<String, dynamic>>? themesByStyle,
  ) {
    // themesByStyle format: themeName -> styleName -> textStyle
    // If null, widget has no text - use TextStyle.def()
    // We need to generate methods for each theme: getNonDefaultTheme(), getDefaultTheme(), etc.

    final widgetClass = widgetName(fqn);

    // Check if all styles have the same textStyle for ALL themes
    // Also true when themesByStyle is null (no text, use default)
    bool allStylesSameForAllThemes = true;
    if (themesByStyle != null) {
      for (var themeEntry in themesByStyle.entries) {
        final stylesMap = themeEntry.value;
        Map<String, dynamic>? firstTextStyle;
        for (var textStyle in stylesMap.values) {
          if (firstTextStyle == null) {
            firstTextStyle = textStyle;
          } else {
            if (textStyle['fontFamily'] != firstTextStyle['fontFamily'] ||
                textStyle['fontSize'] != firstTextStyle['fontSize'] ||
                textStyle['fontBold'] != firstTextStyle['fontBold'] ||
                textStyle['fontItalic'] != firstTextStyle['fontItalic']) {
              allStylesSameForAllThemes = false;
              break;
            }
          }
        }
        if (!allStylesSameForAllThemes) break;
      }
    }

    final buffer = StringBuffer();
    buffer.writeln('package dev.equo.swt.size;');
    buffer.writeln();

    if (allStylesSameForAllThemes) {
      // Simple case: single textStyle across all styles (or no text at all)
      buffer.writeln(
        'public record ${widgetClass}Theme (TextStyle textStyle) {',
      );
      buffer.writeln('    public static ${widgetClass}Theme get() {');
      buffer.writeln(
        '        return Themes.getTheme().${widgetField(widgetClass)};',
      );
      buffer.writeln('    }');
      buffer.writeln();

      if (themesByStyle != null) {
        // Generate one method per theme with extracted text styles
        for (var themeEntry in themesByStyle.entries) {
          final themeName = themeEntry.key;
          final stylesMap = themeEntry.value;
          final textStyle = stylesMap.values.first; // All styles are the same

          final fontFamily = ((textStyle['fontFamily'] ?? 'System') as String)
              .replaceAll(".AppleSystemUIFont", "System")
              .replaceAll("Roboto", "System")
              .replaceAll("Segoe UI", "System");
          final fontSize = textStyle['fontSize'] ?? 12;
          final fontBold = textStyle['fontBold'] ?? false;
          final fontItalic = textStyle['fontItalic'] ?? false;

          buffer.writeln(
            '    public static ${widgetClass}Theme get${themeName}Theme() {',
          );
          buffer.writeln(
            '        return new ${widgetClass}Theme(new TextStyle("$fontFamily", $fontSize, $fontBold, $fontItalic));',
          );
          buffer.writeln('    }');
          buffer.writeln();
        }
      } else {
        // No text component - generate default themes using TextStyle.def()
        for (var themeConfig in themesToMeasure) {
          final themeName = themeConfig.name;
          buffer.writeln(
            '    public static ${widgetClass}Theme get${themeName}Theme() {',
          );
          buffer.writeln(
            '        return new ${widgetClass}Theme(TextStyle.def());',
          );
          buffer.writeln('    }');
          buffer.writeln();
        }
      }
      buffer.writeln('}');
    } else {
      // Complex case: different textStyles per style
      buffer.writeln('import java.util.Map;');
      buffer.writeln();
      buffer.writeln(
        'public record ${widgetClass}Theme (Map<String, TextStyle> textStyles) {',
      );
      buffer.writeln('    public static ${widgetClass}Theme get() {');
      buffer.writeln(
        '        return Themes.getTheme().${widgetField(widgetClass)};',
      );
      buffer.writeln('    }');
      buffer.writeln();
      buffer.writeln('    public TextStyle getTextStyle(int style) {');

      // Use styles from first theme to generate the getTextStyle method
      // themesByStyle is guaranteed non-null here (else branch only reached when styles differ)
      final firstTheme = themesByStyle!.values.first;
      int count = 0;
      for (var styleName in firstTheme.keys) {
        final prefix = count == 0 ? '        if' : '        } else if';

        if (styleName.contains('|')) {
          final flags = styleName.split('|').map((s) => 'SWT.$s').join(' | ');
          buffer.writeln('$prefix ((style & ($flags)) != 0) {');
        } else {
          buffer.writeln('$prefix ((style & SWT.$styleName) != 0) {');
        }
        buffer.writeln('            return textStyles.get("$styleName");');
        count++;
      }

      final firstStyle = firstTheme.keys.first;
      buffer.writeln('        } else {');
      buffer.writeln('            return textStyles.get("$firstStyle");');
      buffer.writeln('        }');
      buffer.writeln('    }');
      buffer.writeln();

      // Generate one method per theme
      for (var themeEntry in themesByStyle!.entries) {
        final themeName = themeEntry.key;
        final stylesMap = themeEntry.value;

        buffer.writeln(
          '    public static ${widgetClass}Theme get${themeName}Theme() {',
        );
        buffer.writeln(
          '        return new ${widgetClass}Theme(Map.of(',
        );

        final entries = <String>[];
        for (var styleEntry in stylesMap.entries) {
          final styleName = styleEntry.key;
          final textStyle = styleEntry.value;
          final fontFamily = ((textStyle['fontFamily'] ?? 'System') as String)
              .replaceAll(".AppleSystemUIFont", "System")
              .replaceAll("Roboto", "System")
              .replaceAll("Segoe UI", "System");
          final fontSize = textStyle['fontSize'] ?? 12;
          final fontBold = textStyle['fontBold'] ?? false;
          final fontItalic = textStyle['fontItalic'] ?? false;
          entries.add(
            '            "$styleName", new TextStyle("$fontFamily", $fontSize, $fontBold, $fontItalic)',
          );
        }

        buffer.writeln(entries.join(',\n'));
        buffer.writeln('        ));');
        buffer.writeln('    }');
        buffer.writeln();
      }
      buffer.writeln('}');
    }

    final themeFile = File(
      '../swt_native/src/main/java/dev/equo/swt/size/${widgetClass}Theme.java',
    );
    themeFile.writeAsStringSync(buffer.toString());
    print('Generated: ${themeFile.path}');
  }

  String widgetField(String widgetType) => widgetType[0].toLowerCase() + widgetType.substring(1);
}

// Test app that runs measurements
class MeasurementApp extends StatefulWidget {
  final WidgetMeasurer measurer;

  const MeasurementApp({required this.measurer, super.key});

  @override
  State<MeasurementApp> createState() => _MeasurementAppState();
}

class _MeasurementAppState extends State<MeasurementApp> {
  GlobalKey _key = GlobalKey();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _measureCurrentCase();
    });
  }

  Future<void> _measureCurrentCase() async {
    // Always call measureCurrentCase to allow it to detect completion and trigger phase transitions
    final previousPhase = widget.measurer.currentPhase;
    await widget.measurer.measureCurrentCase(_key);

    // Continue if we transitioned phases OR if there are more cases in current phase
    final phaseChanged = widget.measurer.currentPhase != previousPhase;
    if (phaseChanged || widget.measurer.hasMoreCases()) {
      setState(() {
        _key = GlobalKey();
      });

      if (widget.measurer.measurementDelay > Duration.zero) {
        await Future.delayed(widget.measurer.measurementDelay);
      }

      WidgetsBinding.instance.addPostFrameCallback((_) {
        _measureCurrentCase();
      });
    }
    // If no more cases and no phase change, we're done
  }

  @override
  Widget build(BuildContext context) {
    final measurer = widget.measurer;
    final currentIndex = measurer.currentCaseIndex;
    final currentPhase = measurer.currentPhase;

    // Display different info based on phase
    String statusText;
    if (currentPhase == 1) {
      final totalCases = measurer.testCases.length;
      statusText = currentIndex < totalCases
          ? 'Phase 1: Sizing ${currentIndex + 1}/$totalCases'
          : 'Phase 1 Complete';
    } else {
      final totalCases = measurer.themeSamplingCases.length;
      final themeName = measurer.getCurrentThemeName();
      statusText = currentIndex < totalCases
          ? 'Phase 2: $themeName ${currentIndex + 1}/$totalCases'
          : 'Phase 2 Complete';
      print(statusText);
    }

    // Use a unique key for MaterialApp that changes when theme changes
    // This forces a complete rebuild when switching themes
    final appKey = ValueKey('theme_${measurer.currentThemeIndex}');

    return MaterialApp(
      key: appKey,
      title: "Widget Measurement",
      theme: measurer.getCurrentTheme(),
      darkTheme: measurer.getCurrentTheme(),
      themeMode: ThemeMode.light,
      home: Scaffold(
        appBar: AppBar(title: Text(statusText)),
        body: measurer.buildCurrentCase(_key),
      ),
    );
  }
}
