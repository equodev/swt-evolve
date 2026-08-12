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
const bool ENABLE_INTERACTIVITY =
    true; // When true, starts paused with controls
const FromTheme = -1;

/// Supported font weights for theme extraction and font metrics IDs (align with FontList.kt / FontMetricsUtil).
const List<int> supportedFontWeights = [300, 400, 500, 600, 700];

int snapToSupportedWeight(int? weight) {
  if (weight == null) return 400;
  if (supportedFontWeights.contains(weight)) return weight;
  int nearest = supportedFontWeights.first;
  for (final w in supportedFontWeights) {
    if ((w - weight).abs() < (nearest - weight).abs()) nearest = w;
  }
  return nearest;
}

int fontWeightFromStyle(TextStyle? style) {
  final fw = style?.fontWeight;
  final w = fw != null ? (fw.index + 1) * 100 : null;
  return snapToSupportedWeight(w);
}

bool fontItalicFromStyle(TextStyle? style) {
  return style?.fontStyle == FontStyle.italic;
}

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
  final String
  style; // Style name like "LEFT", "READ_ONLY", "HORIZONTAL|SEPARATOR"
  final Map<String, dynamic> expectedComponents;
  final Widget Function(GlobalKey key) widgetBuilder;
  final bool useFontTheme;
  final String? itemsAccessor;

  MeasurementCase({
    required String descr,
    required this.fqn,
    required this.style,
    required this.expectedComponents,
    required this.widgetBuilder,
    this.useFontTheme = false,
    this.itemsAccessor,
  }) : name = "${widgetName(fqn)}_${style}_$descr";

  Widget buildWidget(GlobalKey key) {
    return RepaintBoundary(child: widgetBuilder(key));
  }
}

// Result of measuring a single case
class MeasurementResult {
  final String fqn;
  final String name;
  final String
  style; // Style name like "LEFT", "READ_ONLY", "HORIZONTAL|SEPARATOR"
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

  /// Top-left of this box relative to the measured root. Sizes alone cannot express where a
  /// widget's parts sit, and some Java-side geometry is about position rather than extent — the
  /// x of a Tree's expander arrow, for instance, is what `Tree.getItem(Point)` hit-tests against.
  final Offset offset;
  final EdgeInsetsGeometry? padding;
  final EdgeInsets? border;
  final EdgeInsets? margin;
  final int depth;
  final List<RenderBoxInfo> children;
  final String? textContent;
  final String? imageSource;
  final TextStyle? textStyle;
  final bool? softWrap;
  final String? sizeProbeName;

  /// The chain of widgets that created this render object, e.g.
  /// `SizedBox ← MouseRegion ← ... ← TreeItemSwt ← ...`. Debug-only Flutter data, which is all
  /// the measurement tool ever runs in — it lets the tool find a widget's parts by the code that
  /// built them, instead of the widget having to mark them for it.
  final String? creator;

  RenderBoxInfo(
    this.type,
    this.size,
    this.depth, {
    this.offset = Offset.zero,
    this.padding,
    this.border,
    this.margin,
    this.textContent,
    this.imageSource,
    this.textStyle,
    this.softWrap,
    this.sizeProbeName,
    this.creator,
    this.children = const [],
  });

  bool createdBy(String widgetType) => creator?.contains(widgetType) ?? false;

  Map<String, dynamic> toJson() {
    return {
      'type': type,
      'size': {'width': size.width, 'height': size.height},
      'offset': {'x': offset.dx, 'y': offset.dy},
      'depth': depth,
      if (textContent != null) 'textContent': textContent,
      if (imageSource != null) 'imageSource': imageSource,
      if (softWrap != null) 'softWrap': softWrap,
      if (sizeProbeName != null) 'sizeProbeName': sizeProbeName,
      if (textStyle != null)
        'textStyle': {
          'fontFamily': textStyle!.fontFamily,
          'fontSize': textStyle!.fontSize,
          'fontWeight': textStyle!.fontWeight?.index,
          'fontStyle': textStyle!.fontStyle?.index,
          'height': textStyle!.height ?? 0.0,
        },
      if (padding != null)
        'padding': {
          'left': (padding is EdgeInsetsDirectional)
              ? (padding as EdgeInsetsDirectional)!.start
              : (padding as EdgeInsets)!.left,
          'top': (padding is EdgeInsetsDirectional)
              ? (padding as EdgeInsetsDirectional)!.top
              : (padding as EdgeInsets)!.top,
          'right': (padding is EdgeInsetsDirectional)
              ? (padding as EdgeInsetsDirectional)!.end
              : (padding as EdgeInsets)!.right,
          'bottom': (padding is EdgeInsetsDirectional)
              ? (padding as EdgeInsetsDirectional)!.bottom
              : (padding as EdgeInsets)!.bottom,
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

/// Tree widths the row-geometry cases are measured at. Two are needed because the tree's left
/// edge gap is a fraction of the tree width: a single sample cannot tell that fraction apart from
/// the fixed row padding beside it.
const List<int> geometryWidths = [300, 600];

/// Font height the row-geometry cases are re-measured at, to find out whether a widget's row and
/// header heights follow the font (Table) or are fixed theme values (Tree).
const int fontProbeSize = 28;

/// Fallback widths for `computeSize` when no width hint is given, as `(perColumn, noColumns)`.
///
/// These are *not* measurements and cannot be: a row widget fills the width it is given, so an
/// unbounded measurement returns the measurement window, not a property of the widget (that is
/// exactly how the generic emitter used to produce MIN_WIDTH = 1264). They are SWT layout policy,
/// carried over verbatim from what Sizes.java did by hand, so generating computeSize preserves
/// the sizes every existing layout already gets.
const Map<String, (int, int)> rowWidgetFallbackWidth = {
  'Tree': (30, 200),
  'Table': (70, 70),
};

/// What the native `Scrollable.computeTrim` reserves on each axis for the scroll bars and the
/// frame, whether or not they end up showing (on macOS, `NSScrollView.frameSizeForContentSize`
/// with legacy scrollers). Like the fallback widths above these are not measurements and cannot
/// be: Flutter reserves nothing there, but callers size themselves against the native numbers —
/// code that subtracts a fixed slack from `computeSize` to fit a list in a popup, for instance,
/// loses its last row when the preferred size is the exact content size.
const int nativeScrollerSize = 15;
const int nativeBezelSize = 2;

/// Row geometry read off a rendered row widget (Tree, Table), for one column-count variant.
/// The expander fields are only populated for a widget that draws one.
class _RowGeometry {
  final double rowHeight;
  final double rowPaddingVertical;
  final double? cellPaddingHorizontal;
  final double? borderWidth;
  final double? arrowWidth;
  final double? gapFraction;
  final double? paddingLeft;
  final double? indent;

  _RowGeometry({
    required this.rowHeight,
    required this.rowPaddingVertical,
    this.cellPaddingHorizontal,
    this.borderWidth,
    this.arrowWidth,
    this.gapFraction,
    this.paddingLeft,
    this.indent,
  });

  bool get hasExpander => arrowWidth != null;
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
  // Map from case name to result - allows re-running without duplicates
  final Map<String, MeasurementResult> resultsMap = {};
  int currentCaseIndex = 0;

  // Threshold for determining if padding is consistent (low variance = consistent padding)
  static const double VARIANCE_THRESHOLD = 5.0;

  // Multi-theme support
  List<ThemeConfig> themesToMeasure = getThemes();
  int currentPhase = 1; // 1 = sizing measurements, 2 = theme sampling
  int currentThemeIndex = 0;
  // Map: themeName -> widgetType:style -> textStyle
  Map<String, Map<String, Map<String, dynamic>>> extractedThemes = {};

  // Interactive controls state
  bool isPaused = ENABLE_INTERACTIVITY;
  bool isFinished = false;

  WidgetMeasurer();

  List<MeasurementResult> get results => resultsMap.values.toList();

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
      tCase.name =
          "${widgetName(tCase.fqn)}_${tCase.style}_theme${getCurrentThemeName()}";
      return tCase.buildWidget(key);
    }
  }

  ThemeData getCurrentTheme() {
    if (themesToMeasure.isEmpty ||
        currentThemeIndex >= themesToMeasure.length) {
      return ThemeData();
    }
    return themesToMeasure[currentThemeIndex].themeFactory();
  }

  String getCurrentThemeName() {
    if (themesToMeasure.isEmpty ||
        currentThemeIndex >= themesToMeasure.length) {
      return '';
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
      // Phase 1: Store full results for sizing analysis (map allows re-running)
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
      resultsMap[testCase.name] = result;
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
          final rawWeight = textStyleMap['fontWeight'] != null
              ? ((textStyleMap['fontWeight'] as int).clamp(0, 8) + 1) * 100
              : 400;
          final fontWeight = snapToSupportedWeight(rawWeight);
          final fontItalic =
              textStyleMap['fontStyle'] != null &&
              (textStyleMap['fontStyle'] as int) == FontStyle.italic.index;
          extractedThemes[themeName]![key] = {
            'fontFamily': textStyleMap['fontFamily'] ?? 'System',
            'fontSize': (textStyleMap['fontSize'] as double?)?.toInt() ?? 12,
            'fontWeight': fontWeight,
            'fontItalic': fontItalic,
            'height': textStyleMap['height'] as double? ?? 0.0,
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

  // Get all cases for the current phase (for dropdown)
  List<MeasurementCase> get currentPhaseCases {
    return currentPhase == 1 ? testCases : themeSamplingCases;
  }

  // Get total case count for current phase
  int get totalCasesInPhase {
    return currentPhaseCases.length;
  }

  // Get all cases across all phases (for unified dropdown)
  List<(int phase, int themeIdx, MeasurementCase case_)> get allCases {
    final result = <(int, int, MeasurementCase)>[];
    // Phase 1: sizing cases
    for (var c in testCases) {
      result.add((1, 0, c));
    }
    // Phase 2: theme sampling cases for each theme
    for (int t = 0; t < themesToMeasure.length; t++) {
      for (var c in themeSamplingCases) {
        result.add((2, t, c));
      }
    }
    return result;
  }

  // Get total case count across all phases
  int get totalAllCases {
    return testCases.length +
        (themeSamplingCases.length * themesToMeasure.length);
  }

  // Get current global index (across all phases)
  int get globalIndex {
    if (currentPhase == 1) {
      return currentCaseIndex;
    } else {
      return testCases.length +
          (currentThemeIndex * themeSamplingCases.length) +
          currentCaseIndex;
    }
  }

  // Navigate to previous case (across phases)
  void goToPrevious() {
    // Handle finished state - go to last valid case
    if (isFinished ||
        (currentPhase == 2 && currentThemeIndex >= themesToMeasure.length)) {
      currentPhase = 2;
      currentThemeIndex = themesToMeasure.length - 1;
      currentCaseIndex = themeSamplingCases.length - 1;
      isFinished = false;
      return;
    }

    if (currentCaseIndex > 0) {
      currentCaseIndex--;
    } else if (currentPhase == 2 && currentThemeIndex > 0) {
      // Go to previous theme's last case
      currentThemeIndex--;
      currentCaseIndex = themeSamplingCases.length - 1;
    } else if (currentPhase == 2 && currentThemeIndex == 0) {
      // Go back to phase 1's last case
      currentPhase = 1;
      currentCaseIndex = testCases.length - 1;
    }
    isFinished = false;
  }

  // Navigate to next case (across phases)
  void goToNext() {
    isFinished = false;
    if (currentPhase == 1) {
      if (currentCaseIndex < testCases.length - 1) {
        currentCaseIndex++;
      } else if (themeSamplingCases.isNotEmpty && themesToMeasure.isNotEmpty) {
        // Move to phase 2
        currentPhase = 2;
        currentThemeIndex = 0;
        currentCaseIndex = 0;
      }
    } else {
      if (currentCaseIndex < themeSamplingCases.length - 1) {
        currentCaseIndex++;
      } else if (currentThemeIndex < themesToMeasure.length - 1) {
        // Move to next theme
        currentThemeIndex++;
        currentCaseIndex = 0;
      }
      // At the very end, don't advance further
    }
  }

  // Jump to specific global index (across all phases)
  void goToGlobalIndex(int globalIdx) {
    if (globalIdx < 0) return;

    if (globalIdx < testCases.length) {
      // Phase 1
      currentPhase = 1;
      currentCaseIndex = globalIdx;
      isFinished = false;
    } else {
      // Phase 2
      final phase2Idx = globalIdx - testCases.length;
      if (themeSamplingCases.isEmpty) return;
      final themeIdx = phase2Idx ~/ themeSamplingCases.length;
      final caseIdx = phase2Idx % themeSamplingCases.length;

      if (themeIdx < themesToMeasure.length) {
        currentPhase = 2;
        currentThemeIndex = themeIdx;
        currentCaseIndex = caseIdx;
        isFinished = false;
      }
    }
  }

  // Toggle play/pause
  void togglePause() {
    isPaused = !isPaused;
  }

  // Reset to beginning of current phase
  void reset() {
    currentCaseIndex = 0;
    isFinished = false;
  }

  void _finishPhase1() {
    print('=== PHASE 1 COMPLETE (Sizing Analysis) ===');
    print('Total sizing cases measured: ${resultsMap.length}');

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
      isFinished = true;
      if (!ENABLE_INTERACTIVITY) exit(0);
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
    print('Total sizing cases: ${resultsMap.length}');
    print('Total themes sampled: ${extractedThemes.length}');

    // Now generate theme files from extractedThemes
    _exportThemeResults();

    isFinished = true;
    if (!ENABLE_INTERACTIVITY) exit(0);
  }

  void _exportSizingResults(List<WidgetAnalysis> analyses) {
    // Group analyses by FQN
    final byFqn = <String, List<WidgetAnalysis>>{};
    for (var analysis in analyses) {
      final fqn = analysis.measurements.first.fqn;
      byFqn.putIfAbsent(fqn, () => []).add(analysis);
    }

    // Generate per-widget JSON files and Java sizing files
    for (var entry in byFqn.entries) {
      final fqn = entry.key;
      final widgetAnalyses = entry.value;
      final widgetType = widgetName(fqn).toLowerCase();

      // Write per-widget JSON file
      final widgetJson = {
        'analyses': widgetAnalyses.map((a) => a.toJson()).toList(),
      };
      final json = JsonEncoder.withIndent('  ').convert(widgetJson);
      final file = File('./build/measurements_$widgetType.json');
      file.writeAsStringSync(json);
      print('Exported: ${file.path}');

      // Generate Java sizing file
      _generateJavaWidgetSizes(fqn, widgetAnalyses);
    }

    print('\n======== SIZING ALGORITHMS BY STYLE ========\n');
    for (var analysis in analyses) {
      print(analysis.algorithm);
      print('Constants: ${analysis.derivedConstants}\n');
    }
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

  RenderBoxInfo _analyzeRenderBox(
    RenderBox renderBox,
    int depth, {
    Offset? rootOrigin,
  }) {
    final type = renderBox.runtimeType.toString();
    final size = renderBox.size;

    Offset origin = Offset.zero;
    Offset offset = Offset.zero;
    if (renderBox.attached && renderBox.hasSize) {
      final global = renderBox.localToGlobal(Offset.zero);
      origin = rootOrigin ?? global;
      offset = global - origin;
    } else {
      origin = rootOrigin ?? Offset.zero;
    }

    EdgeInsetsGeometry? padding;
    EdgeInsets? border;
    EdgeInsets? margin;
    String? textContent;
    String? imageSource;
    String? sizeProbeName;

    if (renderBox is RenderSemanticsAnnotations) {
      final identifier = renderBox.properties.identifier;
      if (identifier != null && identifier.startsWith('sizeprobe:')) {
        sizeProbeName = identifier.substring('sizeprobe:'.length);
      }
    }

    String? creator;
    final debugCreator = renderBox.debugCreator;
    if (debugCreator is DebugCreator) {
      creator = debugCreator.element.debugGetCreatorChain(20);
    }

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
    bool? softWrap;
    if (renderBox is RenderParagraph) {
      final inlineSpan = renderBox.text;
      textContent = inlineSpan.toPlainText();

      // Extract TextStyle from the TextSpan
      if (inlineSpan is TextSpan) {
        textStyle = inlineSpan.style;
        softWrap = renderBox.softWrap;
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
    // Descend through render objects that are not boxes (a ListView's viewport reaches its rows
    // through a RenderSliver) — stopping at them would hide every scrolled child, which is where
    // a Tree keeps its rows.
    void collect(RenderObject node) {
      node.visitChildren((child) {
        if (child is RenderBox) {
          children.add(_analyzeRenderBox(child, depth + 1, rootOrigin: origin));
        } else {
          collect(child);
        }
      });
    }

    collect(renderBox);

    return RenderBoxInfo(
      type,
      size,
      depth,
      offset: offset,
      padding: padding,
      border: border,
      margin: margin,
      textContent: textContent,
      imageSource: imageSource,
      textStyle: textStyle,
      softWrap: softWrap,
      sizeProbeName: sizeProbeName,
      creator: creator,
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
          'softWrap': textBox.softWrap ?? false,
          if (textBox.textStyle != null)
            'textStyle': {
              'fontFamily': textBox.textStyle!.fontFamily,
              'fontSize': textBox.textStyle!.fontSize,
              'fontWeight': textBox.textStyle!.fontWeight?.index,
              'fontStyle': textBox.textStyle!.fontStyle?.index,
              'height': textBox.textStyle!.height ?? 0.0,
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
      } else if (expectedComponents['image'] != null) {
        // Try icon-sized placeholder first (FutureBuilder SizedBox for icon images)
        final iconBox = _findIconBox(root);
        if (iconBox != null) {
          discovered['image'] = {
            'width': iconBox.size.width,
            'height': iconBox.size.height,
          };
        } else {
          final img = expectedComponents['image'] as (int, int);
          discovered['image'] = {
            'width': img.$1.toDouble(),
            'height': img.$2.toDouble(),
          };
        }
      }
    }

    final namedComponents = expectedComponents['named'];
    if (namedComponents is List) {
      for (final name in namedComponents) {
        final namedBox = _findNamedBox(root, name as String);
        if (namedBox != null) {
          discovered[name] = _probeJson(namedBox);
        }
      }
    }

    // Row geometry, found from the render tree itself rather than from markers the widget has to
    // carry for us: the rows of a row widget, and (for a Tree) the expander arrow in each.
    final rowWidget = expectedComponents['rowsOf'];
    if (rowWidget is String) {
      final header = _findNamedBox(root, 'header');
      final rows = _findRowBoxes(root, rowWidget, header);
      if (rows.isNotEmpty) {
        discovered['row'] = rows.map(_probeJson).toList();
        final expanders = rows
            .map(_findExpanderBox)
            .whereType<RenderBoxInfo>()
            .toList();
        if (expanders.isNotEmpty) {
          discovered['expander'] = expanders.map(_probeJson).toList();
        }
      }
      final borderWidth = _firstBorderWidth(root);
      if (borderWidth != null) {
        discovered['frame'] = {
          'width': root.size.width,
          'height': root.size.height,
          'left': root.offset.dx,
          'top': root.offset.dy,
          'borderWidth': borderWidth,
        };
      }
    }

    return discovered;
  }

  /// The laid-out rows of a row widget, outermost box per row, top to bottom.
  ///
  /// Two shapes, because the two widgets build rows differently and neither needs to know this
  /// tool exists: a Tree renders each row as its own `{Widget}ItemSwt`, while a Table builds one
  /// Flutter `Table` whose cells are the rows (its items are not widgets of their own). The
  /// header is excluded — it is a row of the same `Table`.
  List<RenderBoxInfo> _findRowBoxes(
    RenderBoxInfo root,
    String rowWidget,
    RenderBoxInfo? header,
  ) {
    final byWidget = <RenderBoxInfo>[];
    void collectOutermost(RenderBoxInfo box) {
      if (identical(box, header)) return;
      if (box.createdBy(rowWidget)) {
        byWidget.add(box);
        return; // outermost box of the row; its children are the row's parts
      }
      for (final child in box.children) {
        collectOutermost(child);
      }
    }

    collectOutermost(root);
    if (byWidget.isNotEmpty) {
      byWidget.sort((a, b) => a.offset.dy.compareTo(b.offset.dy));
      return byWidget;
    }

    // Table: take the first cell of each row of the body's RenderTable.
    final tables = <RenderBoxInfo>[];
    void collectTables(RenderBoxInfo box) {
      if (identical(box, header)) return;
      if (box.type.contains('RenderTable')) tables.add(box);
      for (final child in box.children) {
        collectTables(child);
      }
    }

    collectTables(root);
    if (tables.isEmpty) return const [];
    final cells = <double, RenderBoxInfo>{};
    for (final cell in tables.first.children) {
      final existing = cells[cell.offset.dy];
      if (existing == null || cell.offset.dx < existing.offset.dx) {
        cells[cell.offset.dy] = cell;
      }
    }
    final tops = cells.keys.toList()..sort();
    return [for (final top in tops) cells[top]!];
  }

  /// The expand/collapse arrow inside a row: the first square icon box in it. A widget without
  /// one (a Table, or a leaf row) simply yields none.
  RenderBoxInfo? _findExpanderBox(RenderBoxInfo row) {
    if (row.createdBy('Icon') &&
        row.size.width > 0 &&
        row.size.width == row.size.height) {
      return row;
    }
    for (final child in row.children) {
      final found = _findExpanderBox(child);
      if (found != null) return found;
    }
    return null;
  }

  Map<String, dynamic> _probeJson(RenderBoxInfo box) {
    final paddingVertical = _firstPaddingVertical(box);
    final paddingHorizontal = _firstPaddingHorizontal(box);
    final borderWidth = _firstBorderWidth(box);
    return {
      'width': box.size.width,
      'height': box.size.height,
      'left': box.offset.dx,
      'top': box.offset.dy,
      // The padding inside a probed box is the part of its height that is *not* text. A widget
      // whose rows grow with the font needs it as a constant, with the text measured in Java.
      if (paddingVertical != null) 'paddingVertical': paddingVertical,
      // The horizontal counterpart, for a widget that has to size itself to its widest item:
      // the text is measured in Java, the cell's own inset is this constant.
      if (paddingHorizontal != null) 'paddingHorizontal': paddingHorizontal,
      if (borderWidth != null) 'borderWidth': borderWidth,
    };
  }

  double? _firstPaddingVertical(RenderBoxInfo box) {
    final padding = box.padding;
    if (padding is EdgeInsets) return padding.vertical;
    for (final child in box.children) {
      final found = _firstPaddingVertical(child);
      if (found != null) return found;
    }
    return null;
  }

  double? _firstPaddingHorizontal(RenderBoxInfo box) {
    final padding = box.padding;
    if (padding is EdgeInsets) return padding.horizontal;
    for (final child in box.children) {
      final found = _firstPaddingHorizontal(child);
      if (found != null) return found;
    }
    return null;
  }

  double? _firstBorderWidth(RenderBoxInfo box) {
    final border = box.border;
    if (border != null && border.left > 0) return border.left;
    for (final child in box.children) {
      final found = _firstBorderWidth(child);
      if (found != null) return found;
    }
    return null;
  }

  RenderBoxInfo? _findNamedBox(RenderBoxInfo box, String name) {
    if (box.sizeProbeName == name) {
      return box;
    }

    for (var child in box.children) {
      final found = _findNamedBox(child, name);
      if (found != null) return found;
    }

    return null;
  }

  void _collectNamedBoxes(
    RenderBoxInfo box,
    String name,
    List<RenderBoxInfo> into,
  ) {
    if (box.sizeProbeName == name) into.add(box);
    for (var child in box.children) {
      _collectNamedBoxes(child, name, into);
    }
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

  // Finds an icon-sized placeholder: a square leaf box (FutureBuilder SizedBox fallback for icon images).
  RenderBoxInfo? _findIconBox(RenderBoxInfo box) {
    if (box.imageSource == null &&
        box.size.width == box.size.height &&
        box.size.width > 0 &&
        box.children.isEmpty) {
      return box;
    }
    for (var child in box.children) {
      final found = _findIconBox(child);
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
        final rawWeight = textStyleMap['fontWeight'] != null
            ? ((textStyleMap['fontWeight'] as int).clamp(0, 8) + 1) * 100
            : 400;
        final fontWeight = snapToSupportedWeight(rawWeight);
        final fontItalic =
            textStyleMap['fontStyle'] != null &&
            (textStyleMap['fontStyle'] as int) == FontStyle.italic.index;
        constants['textStyle'] = {
          'fontFamily': textStyleMap['fontFamily'] ?? 'System',
          'fontSize': (textStyleMap['fontSize'] as double?)?.toInt() ?? 12,
          'fontWeight': fontWeight,
          'fontItalic': fontItalic,
          'height': textStyleMap['height'] as double? ?? 0.0,
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

      // Detect icon-type image: image width ≈ text height for all cases (image renders at fontSize).
      bool isIconImage = false;
      bool isFixedIconSize = false;
      double fixedIconWidth = 16.0;
      final iconPairs = styleResults.where((r) =>
        r.discoveredComponents.containsKey('image') &&
        r.discoveredComponents.containsKey('text')
      ).toList();
      if (iconPairs.isNotEmpty) {
        int iconCount = 0;
        for (var r in iconPairs) {
          final imgW = r.discoveredComponents['image']!['width'] as double;
          final txtH = r.discoveredComponents['text']!['height'] as double;
          if (txtH > 0 && (imgW / txtH - 1.0).abs() < 0.3) iconCount++;
        }
        isIconImage = iconCount >= iconPairs.length * 0.5;

        if (isIconImage) {
          // Distinguish font-scaled icons (TableItem) from fixed-size ones (TreeItem) by checking whether image width varies across discovered font sizes.
          final byFontSize = <int, List<double>>{};
          for (var r in iconPairs) {
            final imgW = r.discoveredComponents['image']!['width'] as double;
            final fontSize =
                (r.discoveredComponents['text']!['textStyle']?['fontSize']
                        as double?)
                    ?.round();
            if (fontSize != null) {
              byFontSize.putIfAbsent(fontSize, () => []).add(imgW);
            }
          }
          if (byFontSize.length > 1) {
            final avgByFontSize = byFontSize.map(
              (k, v) => MapEntry(k, v.reduce((a, b) => a + b) / v.length),
            );
            final widths = avgByFontSize.values.toList();
            final maxW = widths.reduce((a, b) => a > b ? a : b);
            final minW = widths.reduce((a, b) => a < b ? a : b);
            if ((maxW - minW).abs() < 2.0) {
              isFixedIconSize = true;
              fixedIconWidth = widths.reduce((a, b) => a + b) / widths.length;
            }
          }
        }
      }
      constants['isIconImage'] = isIconImage;
      constants['isFixedIconSize'] = isFixedIconSize;
      constants['fixedIconWidth'] = fixedIconWidth;
      if (isIconImage) {
        print(
          isFixedIconSize
              ? '  Icon-type image detected (fixed size ≈ $fixedIconWidth, independent of font)'
              : '  Icon-type image detected (size ≈ fontSize)',
        );
      }

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
      constants['isIconImage'] = false;
    }

    final softWrapCount = styleResults
        .where((r) => r.discoveredComponents['text']?['softWrap'] == true)
        .length;
    constants['wrapsMode'] = softWrapCount == 0
        ? 'none'
        : softWrapCount == styleResults.length
            ? 'always'
            : 'whenWrapStyle';

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

    // A row widget (Tree, Table) does not size itself from its own text — it fills what it is
    // given — so the "constant-size widget" inference below reduces it to the measurement window.
    // What Java needs from these is the row geometry it cannot ask Flutter for synchronously.
    // Keyed on the probes the measurement actually produced, not on the class name.
    if (_hasRowGeometry(analyses)) {
      _generateJavaRowWidgetSizes(fqn, analyses);
      return;
    }

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
    final isIconImageWidget =
        hasAnyImageSupport &&
        analyses.any((a) => a.derivedConstants['isIconImage'] == true);
    final isFixedIconSizeWidget =
        isIconImageWidget &&
        analyses.any((a) => a.derivedConstants['isFixedIconSize'] == true);
    final fixedIconWidthValue = isFixedIconSizeWidget
        ? analyses
            .firstWhere((a) => a.derivedConstants['isFixedIconSize'] == true)
            .derivedConstants['fixedIconWidth']
        : null;
    if (hasAnyImageSupport && !isIconImageWidget) {
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
          'vertical:${isVerticalStyle}_wrap:${constants['wrapsMode'] ?? 'none'}';
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
        final emptyTextAffectsSizing =
            constants['emptyTextAffectsSizing'] as bool;
        buffer.writeln(
          '        static final boolean EMPTY_TEXT_AFFECTS_SIZING = $emptyTextAffectsSizing;',
        );
      }

      if (!isConstantSize && widgetType == 'Button' && javaClassName == 'CHECK') {
        buffer.writeln('        static final double TEXT_SLACK = 1.0;');
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
        buffer.writeln('${indent}width = wHint != SWT.DEFAULT ? wHint : $styleName.MIN_WIDTH;');
        buffer.writeln('${indent}height = hHint != SWT.DEFAULT ? hHint : $styleName.MIN_HEIGHT;');
      } else {
        // Make padding conditional on text/image existence when empty text doesn't affect sizing
        final emptyTextAffectsSizing =
            constants['emptyTextAffectsSizing'] as bool;

        // Text-based widget - calculate size based on text, image, spacing, and padding
        if (hasAnyTextBasedWidget) {
          buffer.writeln(
            '${indent}m.text = computeText(widget, m, $styleName.EMPTY_TEXT_AFFECTS_SIZING);',
          );
        }
        if (hasAnyImageSupport) {
          if (isFixedIconSizeWidget) {
            buffer.writeln('${indent}m.image = computeImage(widget);');
          } else if (isIconImageWidget) {
            buffer.writeln(
              '${indent}m.image = computeImage(widget, m.textStyle);',
            );
          } else {
            buffer.writeln('${indent}m.image = computeImage(widget);');
          }
        }

        final imageUsesMax = constants['imageUsesMax'] as bool;
        final imageSpacing = constants['imageSpacing'] as double;
        final hasImageSpacing = imageSpacing > 0;

        // For VERTICAL styles, text dimensions are swapped (text is rotated 90°)
        final textX = isVertical ? 'm.text.y()' : 'm.text.x()';
        final textY = isVertical ? 'm.text.x()' : 'm.text.y()';

        // Include image spacing whenever the image exists — it's the icon's own margin, not conditioned on sibling text.
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
            // Same reasoning as the width case above.
            textHeightExpr =
                '($textY + m.image.y() + (m.image.y() > 0 ? $styleName.IMAGE_SPACING : 0))';
          } else {
            // Vertical layout without spacing
            textHeightExpr = '($textY + m.image.y())';
          }
        } else {
          textHeightExpr = textY;
        }

        // HORIZONTAL_PADDING applies whenever any content (image or text) exists.
        final widthCondition = (hasAnyImageSupport && imageAffectsWidth)
            ? '($textX > 0 || m.image.x() > 0)'
            : '$textX > 0';

        final slackTerm = (widgetType == 'Button' && styleName == 'CHECK')
            ? ' + ($textX > 0 ? $styleName.TEXT_SLACK : 0)'
            : '';
        final String naturalBase;
        if (useHorizontalPadding) {
          naturalBase =
              'Math.max($textWidthExpr + ($widthCondition ? $styleName.HORIZONTAL_PADDING : 0)$slackTerm, $styleName.MIN_WIDTH)';
        } else {
          naturalBase =
              'Math.max($textWidthExpr$slackTerm, $styleName.MIN_WIDTH)';
        }
        final bool zeroWhenEmpty = widgetType == 'Label';
        final String naturalExpr = zeroWhenEmpty
            ? '($widthCondition ? $naturalBase : 0.0)'
            : naturalBase;

        if (widgetType == 'Text') {
          buffer.writeln('${indent}double naturalWidth = $naturalExpr;');
          buffer.writeln(
            '${indent}boolean singleLine = !hasFlags(style, SWT.MULTI) && !hasFlags(style, SWT.WRAP);',
          );
          buffer.writeln('${indent}if (wHint != SWT.DEFAULT) {');
          buffer.writeln(
            '${indent}    width = singleLine ? wHint + $styleName.HORIZONTAL_PADDING : wHint;',
          );
          buffer.writeln('${indent}} else {');
          buffer.writeln('${indent}    width = naturalWidth;');
          buffer.writeln('${indent}}');
        } else {
          buffer.writeln(
            '${indent}width = wHint != SWT.DEFAULT ? wHint : $naturalExpr;',
          );
        }

        final wrapsMode = constants['wrapsMode'] as String? ?? 'none';
        if (wrapsMode != 'none' && !isConstantSize) {
          if (wrapsMode == 'whenWrapStyle') {
            buffer.writeln(
              '${indent}boolean wraps = hasFlags(style, SWT.WRAP);',
            );
          }
          final wrapCondition = wrapsMode == 'whenWrapStyle'
              ? 'wHint != SWT.DEFAULT && wraps && m.textStyle != null'
              : 'wHint != SWT.DEFAULT && m.textStyle != null';
          buffer.writeln('${indent}if (hHint != SWT.DEFAULT) {');
          buffer.writeln('${indent}    height = hHint;');
          buffer.writeln('${indent}} else if ($wrapCondition) {');
          if (wrapsMode == 'always') {
            buffer.writeln('${indent}    String rawText = widget.getText();');
            buffer.writeln(
              '${indent}    String visualText = rawText != null ? rawText.replaceAll("<[^>]+>", "") : "";',
            );
            final aw = useHorizontalPadding
                ? 'wHint - $styleName.HORIZONTAL_PADDING'
                : '(double) wHint';
            buffer.writeln(
              '${indent}    double availableWidth = Math.max(1.0, $aw);',
            );
            buffer.writeln(
              '${indent}    PointD wrapped = FontMetricsUtil.getFontSizeWrapped(visualText, m.textStyle, availableWidth);',
            );
            final hExpr = useVerticalPadding
                ? 'wrapped.y() + $styleName.VERTICAL_PADDING'
                : 'wrapped.y()';
            buffer.writeln(
              '${indent}    height = Math.max($hExpr, $styleName.MIN_HEIGHT);',
            );
          } else {
            if (imageAffectsWidth) {
              final sp = hasImageSpacing
                  ? 'imageWidth > 0 ? $styleName.IMAGE_SPACING : 0'
                  : '0';
              buffer.writeln('${indent}    double imageWidth = m.image.x();');
              buffer.writeln('${indent}    double imageSpacing = $sp;');
              final pad = useHorizontalPadding
                  ? '(m.text.x() > 0 || imageWidth > 0) ? $styleName.HORIZONTAL_PADDING : 0'
                  : '0';
              buffer.writeln(
                '${indent}    double availableWidth = Math.max(1.0, wHint - ($pad) - imageWidth - imageSpacing);',
              );
            } else {
              final aw = useHorizontalPadding
                  ? 'wHint - $styleName.HORIZONTAL_PADDING'
                  : '(double) wHint';
              buffer.writeln(
                '${indent}    double availableWidth = Math.max(1.0, $aw);',
              );
            }
            buffer.writeln(
              '${indent}    PointD wrapped = FontMetricsUtil.getFontSizeWrapped(widget.getText(), m.textStyle, availableWidth);',
            );
            if (imageAffectsHeight && useVerticalPadding) {
              buffer.writeln(
                '${indent}    height = Math.max(Math.max(wrapped.y(), m.image.y()) + (wrapped.y() > 0 || m.image.y() > 0 ? $styleName.VERTICAL_PADDING : 0), $styleName.MIN_HEIGHT);',
              );
            } else if (useVerticalPadding) {
              buffer.writeln(
                '${indent}    height = Math.max(wrapped.y() + $styleName.VERTICAL_PADDING, $styleName.MIN_HEIGHT);',
              );
            } else {
              buffer.writeln(
                '${indent}    height = Math.max(wrapped.y(), $styleName.MIN_HEIGHT);',
              );
            }
          }
          buffer.writeln('${indent}} else {');
          if (useVerticalPadding) {
            if (emptyTextAffectsSizing) {
              buffer.writeln(
                '${indent}    height = Math.max($textHeightExpr + $styleName.VERTICAL_PADDING, $styleName.MIN_HEIGHT);',
              );
            } else {
              final hc = (hasAnyImageSupport && imageAffectsHeight)
                  ? '($textY > 0 || m.image.y() > 0)'
                  : '$textY > 0';
              buffer.writeln(
                '${indent}    height = Math.max($textHeightExpr + ($hc ? $styleName.VERTICAL_PADDING : 0), $styleName.MIN_HEIGHT);',
              );
            }
          } else {
            buffer.writeln(
              '${indent}    height = Math.max($textHeightExpr, $styleName.MIN_HEIGHT);',
            );
          }
          buffer.writeln('${indent}}');
        } else {
          if (useVerticalPadding) {
            if (emptyTextAffectsSizing) {
              buffer.writeln(
                '${indent}height = hHint != SWT.DEFAULT ? hHint : Math.max($textHeightExpr + $styleName.VERTICAL_PADDING, $styleName.MIN_HEIGHT);',
              );
            } else {
              final heightCondition = (hasAnyImageSupport && imageAffectsHeight)
                  ? '($textY > 0 || m.image.y() > 0)'
                  : '$textY > 0';
              buffer.writeln(
                '${indent}height = hHint != SWT.DEFAULT ? hHint : Math.max($textHeightExpr + ($heightCondition ? $styleName.VERTICAL_PADDING : 0), $styleName.MIN_HEIGHT);',
              );
            }
          } else {
            buffer.writeln(
              '${indent}height = hHint != SWT.DEFAULT ? hHint : Math.max($textHeightExpr, $styleName.MIN_HEIGHT);',
            );
          }
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

      // Build condition: OR non-redundant styles in the group.
      // A style is redundant if another style in the group has fewer flags that are
      // all contained in it — e.g. CHECK already subsumes CHECK|FLAT and CHECK|WRAP.
      final styleFlags = styles.map((s) => s.split('|').toSet()).toList();
      final minimalStyles = [
        for (int i = 0; i < styles.length; i++)
          if (!styleFlags.any((other) =>
              other.length < styleFlags[i].length &&
              other.every((f) => styleFlags[i].contains(f))))
            styles[i],
      ];

      final conditions = minimalStyles.map((style) {
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
      if (isFixedIconSizeWidget) {
        // Image renders as an icon at a fixed theme size, independent of the widget's font.
        buffer.writeln(
          '    private static PointD computeImage(Dart$widgetType widget) {',
        );
        buffer.writeln('        if (widget.getImage() == null) return PointD.zero;');
        buffer.writeln(
          '        return new PointD($fixedIconWidthValue, $fixedIconWidthValue);',
        );
        buffer.writeln('    }');
      } else if (isIconImageWidget) {
        // Image renders as icon at fontSize — use textStyle.size() instead of pixel dimensions.
        buffer.writeln(
          '    private static PointD computeImage(Dart$widgetType widget, TextStyle ts) {',
        );
        buffer.writeln('        if (widget.getImage() == null) return PointD.zero;');
        buffer.writeln('        double iconSize = ts != null ? (double) ts.size() : 16.0;');
        buffer.writeln('        return new PointD(iconSize, iconSize);');
        buffer.writeln('    }');
      } else {
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
      }
      buffer.writeln();
    }

    // Generate computeText helper if any style is text-based
    if (hasAnyTextBasedWidget) {
      buffer.writeln(
        '    private static PointD computeText(Dart$widgetType widget, Measure m, boolean emptyTextAffectsSizing) {',
      );
      buffer.writeln('        String text = widget.getText();');
      if (widgetType == 'Link') {
        buffer.writeln('        if (text != null) {');
        buffer.writeln('            text = text.replaceAll("<[^>]+>", "");');
        buffer.writeln('        }');
      }
      // PASSWORD style only applies to Text widget
      if (widgetType == 'Text') {
        buffer.writeln(
          '        if (text != null && hasFlags(widget.getStyle(), SWT.PASSWORD)) {',
        );
        buffer.writeln('            text = "*".repeat(text.length());');
        buffer.writeln('        }');
      }

      // Use parameter to decide whether empty text should be measured
      buffer.writeln(
        '        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {',
      );

      buffer.writeln(
        '            if (!Config.getConfigFlags().use_swt_fonts) {',
      );
      buffer.writeln(
        '                m.textStyle = ${widgetType}Theme.get().textStyle().withStyleFrom(widget.getFont());',
      );
      buffer.writeln('            } else {');
      buffer.writeln(
        '                m.textStyle = TextStyle.from(widget.getFont());',
      );
      buffer.writeln('            }');
      String? itemsExpr;
      for (final c in testCases) {
        if (c.fqn == fqn && c.itemsAccessor != null) {
          itemsExpr = c.itemsAccessor;
          break;
        }
      }
      if (itemsExpr == null) {
        buffer.writeln(
          '            return FontMetricsUtil.getFontSize(text, m.textStyle);',
        );
      } else {
        buffer.writeln(
          '            PointD widest = FontMetricsUtil.getFontSize(text, m.textStyle);',
        );
        buffer.writeln('            String[] items = widget.$itemsExpr;');
        buffer.writeln('            if (items != null) {');
        buffer.writeln('                for (String item : items) {');
        buffer.writeln('                    if (item == null) continue;');
        buffer.writeln(
          '                    PointD size = FontMetricsUtil.getFontSize(item, m.textStyle);',
        );
        buffer.writeln('                    if (size.x() > widest.x()) {');
        buffer.writeln('                        widest = size;');
        buffer.writeln('                    }');
        buffer.writeln('                }');
        buffer.writeln('            }');
        buffer.writeln('            return widest;');
      }
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

  /// Number formatted as a Java `double` literal, with float noise from the layout rounded off.
  String _d(double v) {
    final rounded = double.parse(v.toStringAsFixed(6));
    return rounded == rounded.roundToDouble()
        ? rounded.toStringAsFixed(1)
        : '$rounded';
  }

  MeasurementResult? _caseNamed(List<MeasurementResult> all, String suffix) {
    for (final m in all) {
      if (m.name.endsWith(suffix)) return m;
    }
    return null;
  }

  /// Reads the geometry of a tree row: the height of a row and the x/width of the expander arrow
  /// at each nesting level, for a given column count, at each measured width.
  _RowGeometry? _readRowGeometry(List<MeasurementResult> all, int columnCount) {
    final expanderSamples = <int, List<double>>{};
    double? rowHeight;
    double? rowPaddingVertical;
    double? cellPaddingHorizontal;
    double? borderWidth;
    double? arrowWidth;

    for (final width in geometryWidths) {
      final result = _caseNamed(all, 'geometry_width${width}_cols$columnCount');
      if (result == null) return null;
      final rows = result.discoveredComponents['row'];
      if (rows is! List || rows.isEmpty) return null;
      rowHeight ??= (rows.first['height'] as num).toDouble();
      rowPaddingVertical ??= (rows.first['paddingVertical'] as num?)
          ?.toDouble();
      cellPaddingHorizontal ??= (rows.first['paddingHorizontal'] as num?)
          ?.toDouble();
      final frame = result.discoveredComponents['frame'];
      if (frame is Map) {
        borderWidth ??= (frame['borderWidth'] as num?)?.toDouble();
      }

      // Only a widget that draws an expander reports one; a Table simply has none.
      final expanders = result.discoveredComponents['expander'];
      if (expanders is List && expanders.length >= 2) {
        expanderSamples[width] = expanders
            .map((e) => (e['left'] as num).toDouble())
            .toList();
        arrowWidth ??= (expanders.first['width'] as num).toDouble();
      }
    }

    if (rowHeight == null) return null;

    double? gapFraction;
    double? paddingLeft;
    double? indent;
    final widths = expanderSamples.keys.toList()..sort();
    if (widths.length >= 2) {
      final narrow = widths.first;
      final wide = widths.last;
      final leftNarrow = expanderSamples[narrow]![0];
      final leftWide = expanderSamples[wide]![0];

      // The left edge gap is a fraction of the widget width; the row padding is fixed. Two
      // widths separate them: the slope is the fraction, the intercept is the padding.
      gapFraction = (leftWide - leftNarrow) / (wide - narrow);
      paddingLeft = leftNarrow - narrow * gapFraction;
      indent = expanderSamples[narrow]![1] - expanderSamples[narrow]![0];
    } else {
      arrowWidth = null;
    }

    return _RowGeometry(
      rowHeight: rowHeight,
      rowPaddingVertical: rowPaddingVertical ?? 0,
      cellPaddingHorizontal: cellPaddingHorizontal,
      borderWidth: borderWidth,
      arrowWidth: arrowWidth,
      gapFraction: gapFraction,
      paddingLeft: paddingLeft,
      indent: indent,
    );
  }

  bool _hasRowGeometry(List<WidgetAnalysis> analyses) => analyses.any(
    (a) => a.measurements.any((m) => m.discoveredComponents['row'] is List),
  );

  /// Emits `computeSize`, so a row widget reports its preferred size the same way every other
  /// generated widget does instead of keeping the arithmetic by hand in Sizes.java.
  ///
  /// The height is the measured composition — header, plus one row per row on screen, plus the
  /// widget's border. Which rows count is the one part that is not arithmetic: a widget whose
  /// items nest shows only the expanded ones, a flat one shows them all.
  void _writeComputeSize(
    StringBuffer buffer, {
    required String widgetType,
    required bool nested,
    required bool hasBorder,
    required bool measuresContent,
    required String themeClass,
  }) {
    final param = widgetType.toLowerCase();
    final itemType = '${widgetType}Item';
    buffer.writeln(
      '    public static Point computeSize(Dart$widgetType $param, int wHint, int hHint, boolean changed) {',
    );
    buffer.writeln('        int columnCount = $param.getColumnCount();');
    buffer.writeln('        int style = $param.getStyle();');
    // `!= SWT.DEFAULT` alone, as every other generated Sizes class does: a hint of 0 is a real
    // hint, not an absent one. Table already read it that way; Tree used to also require > 0.
    // A hinted axis is taken verbatim; an unhinted one carries the native scroll-bar trim, which
    // getPreferredHeight deliberately does not (that one is what Flutter draws).
    buffer.writeln('        int width;');
    buffer.writeln('        if (wHint != SWT.DEFAULT) {');
    buffer.writeln('            width = wHint;');
    buffer.writeln('        } else {');
    if (measuresContent) {
      buffer.writeln(
        '            width = columnCount > 0 ? columnCount * WIDTH_PER_COLUMN',
      );
      buffer.writeln(
        '                    : Math.max(WIDTH_NO_COLUMNS, getContentWidth($param));',
      );
    } else {
      buffer.writeln(
        '            width = columnCount > 0 ? columnCount * WIDTH_PER_COLUMN : WIDTH_NO_COLUMNS;',
      );
    }
    buffer.writeln(
      '            if ((style & SWT.V_SCROLL) != 0) width += NATIVE_SCROLLER_AND_BEZEL_TRIM;',
    );
    buffer.writeln('        }');
    buffer.writeln('        int height;');
    buffer.writeln('        if (hHint != SWT.DEFAULT) {');
    buffer.writeln('            height = hHint;');
    buffer.writeln('        } else {');
    buffer.writeln('            height = getPreferredHeight($param);');
    final borderTrim = hasBorder
        ? ' - 2 * getBorderWidth()'
        : '';
    buffer.writeln(
      '            if ((style & SWT.H_SCROLL) != 0) height += NATIVE_SCROLLER_AND_BEZEL_TRIM$borderTrim;',
    );
    buffer.writeln('        }');
    buffer.writeln('        return new Point(width, height);');
    buffer.writeln('    }');
    buffer.writeln();
    if (measuresContent) {
      // The widest item drives the width, the way the native computeSize does for a widget with
      // no columns. VIRTUAL is skipped: reading the items to size the widget would materialise
      // the whole model.
      buffer.writeln(
        '    private static int getContentWidth(Dart$widgetType $param) {',
      );
      buffer.writeln(
        '        if (($param.getStyle() & SWT.VIRTUAL) != 0) return 0;',
      );
      buffer.writeln('        int widest = 0;');
      buffer.writeln(
        '        for ($itemType item : $param.getItems()) {',
      );
      buffer.writeln('            if (item == null) continue;');
      buffer.writeln('            TextStyle ts;');
      buffer.writeln('            if (!Config.getConfigFlags().use_swt_fonts) {');
      buffer.writeln(
        '                ts = $themeClass.get().textStyle().withStyleFrom(item.getFont());',
      );
      buffer.writeln('            } else {');
      buffer.writeln('                ts = TextStyle.from(item.getFont());');
      buffer.writeln('            }');
      buffer.writeln('            int cellWidth = 0;');
      buffer.writeln('            String text = item.getText();');
      buffer.writeln('            if (text != null && !text.isEmpty()) {');
      buffer.writeln(
        '                cellWidth += (int) Math.ceil(FontMetricsUtil.getFontSize(text, ts).x());',
      );
      buffer.writeln('            }');
      buffer.writeln('            if (item.getImage() != null) {');
      buffer.writeln(
        '                cellWidth += ts.size() + CELL_PADDING_LEFT;',
      );
      buffer.writeln('            }');
      buffer.writeln('            widest = Math.max(widest, cellWidth);');
      buffer.writeln('        }');
      buffer.writeln('        if (widest == 0) return 0;');
      buffer.writeln(
        '        if (($param.getStyle() & SWT.CHECK) != 0) widest += CHECKBOX_WIDTH + CELL_PADDING_LEFT;',
      );
      buffer.writeln(
        '        return widest + CELL_PADDING_HORIZONTAL + CELL_MARGIN;',
      );
      buffer.writeln('    }');
      buffer.writeln();
    }
    buffer.writeln(
      '    public static int getPreferredHeight(Dart$widgetType $param) {',
    );
    final rows = nested
        ? 'countVisibleRows($param.getApi().getItems())'
        : '$param.getItemCount()';
    final border = hasBorder ? ' + 2 * getBorderWidth()' : '';
    buffer.writeln(
      '        return getHeaderHeight($param) + $rows * getItemHeight($param)$border;',
    );
    buffer.writeln('    }');
    if (nested) {
      buffer.writeln();
      buffer.writeln(
        '    private static int countVisibleRows($itemType[] items) {',
      );
      buffer.writeln('        int count = 0;');
      buffer.writeln('        for ($itemType item : items) {');
      buffer.writeln('            count++;');
      buffer.writeln(
        '            if (item.getExpanded()) count += countVisibleRows(item.getItems());',
      );
      buffer.writeln('        }');
      buffer.writeln('        return count;');
      buffer.writeln('    }');
    }
  }

  /// Emits one height accessor. A dimension that tracks the font is computed in Java from font
  /// metrics plus the measured padding; one that does not is emitted as the measured constant.
  void _writeRowDimension(
    StringBuffer buffer, {
    required String widgetType,
    required String method,
    required String themeClass,
    required String constant,
    required String padding,
    required bool fontSensitive,
    required bool headerGuard,
  }) {
    final param = widgetType.toLowerCase();
    buffer.writeln('    public static int $method(Dart$widgetType $param) {');
    if (headerGuard) {
      buffer.writeln('        if (!$param.getHeaderVisible()) return 0;');
    }
    buffer.writeln(
      '        double minHeight = $param.getColumnCount() > 1 ? ${constant}_WITH_COLS : $constant;',
    );
    if (fontSensitive) {
      buffer.writeln('        TextStyle ts;');
      buffer.writeln('        if (!Config.getConfigFlags().use_swt_fonts) {');
      buffer.writeln(
        '            ts = $themeClass.get().textStyle().withStyleFrom($param.getFont());',
      );
      buffer.writeln('        } else {');
      buffer.writeln('            ts = TextStyle.from($param.getFont());');
      buffer.writeln('        }');
      // Flutter lays these rows out with a minimum height, so text only grows them past the
      // theme's floor — a plain text+padding sum would shrink a Tree row from 34px to 18px.
      buffer.writeln(
        '        double textHeight = FontMetricsUtil.getFontSize("Ag", ts).y() + $padding;',
      );
      buffer.writeln(
        '        return (int) Math.ceil(Math.max(textHeight, minHeight));',
      );
    } else {
      buffer.writeln('        return (int) Math.ceil(minHeight);');
    }
    buffer.writeln('    }');
  }

  /// A dimension that grows with the widget's font has to be computed in Java from font metrics,
  /// not baked in as a constant. Measuring the same case at two font sizes is what tells them
  /// apart — a Table's rows track the font, a Tree's are a fixed theme height.
  bool _isFontSensitive(double? atDefaultFont, double? atLargerFont) =>
      atDefaultFont != null &&
      atLargerFont != null &&
      (atLargerFont - atDefaultFont).abs() > 0.5;

  void _generateJavaRowWidgetSizes(String fqn, List<WidgetAnalysis> analyses) {
    final widgetType = widgetName(fqn);
    final all = analyses.expand((a) => a.measurements).toList();

    Map<String, dynamic>? probe(String caseSuffix, String name) {
      final found = _caseNamed(all, caseSuffix)?.discoveredComponents[name];
      if (found is Map<String, dynamic>) return found;
      if (found is List && found.isNotEmpty) {
        final first = found.first;
        if (first is Map<String, dynamic>) return first;
      }
      return null;
    }

    double? heightOf(String caseSuffix, String name) =>
        (probe(caseSuffix, name)?['height'] as num?)?.toDouble();

    double? headerHeight(int columnCount) =>
        heightOf('header_visibletrue_cols$columnCount', 'header');
    double? headerPadding(int columnCount) =>
        (probe(
                  'header_visibletrue_cols$columnCount',
                  'header',
                )?['paddingVertical']
                as num?)
            ?.toDouble();

    final plain = _readRowGeometry(all, 0) ?? _readRowGeometry(all, 3);
    final withCols = _readRowGeometry(all, 3) ?? plain;
    final headerPlain = headerHeight(0) ?? headerHeight(3);
    final headerWithCols = headerHeight(3) ?? headerPlain;

    if (plain == null ||
        withCols == null ||
        headerPlain == null ||
        headerWithCols == null) {
      // Refuse to write a file that would drop methods the widget's Dart impl calls. Better a
      // loud skip than a silently truncated Sizes class that only fails at compile time.
      print(
        'SKIPPED ${widgetType}Sizes.java: measure_${widgetType.toLowerCase()}.dart did not '
        'produce the header and geometry cases it needs '
        '(header_visibletrue_cols{0,3}, geometry_width{...}_cols{0,3}).',
      );
      return;
    }

    // Does the widget's row/header height track its font? Measured, not assumed.
    final rowFontSensitive = _isFontSensitive(
      plain.rowHeight,
      heightOf('fontprobe_cols0', 'row') ?? heightOf('fontprobe_cols3', 'row'),
    );
    final headerFontSensitive = _isFontSensitive(
      headerPlain,
      heightOf('fontprobe_cols0', 'header') ??
          heightOf('fontprobe_cols3', 'header'),
    );
    final headerPaddingVertical = headerPadding(0) ?? headerPadding(3) ?? 0.0;
    final needsFontMetrics = rowFontSensitive || headerFontSensitive;

    final fallbackWidth = rowWidgetFallbackWidth[widgetType];
    final nested = plain.hasExpander;
    // A flat widget with no columns paints its items in an implicit column 0, so its preferred
    // width has to follow the widest item instead of the fallback constant. A nested one is left
    // alone: its rows carry indent and expanders that this arithmetic does not model.
    final measuresContent =
        !nested &&
        fallbackWidth != null &&
        rowFontSensitive &&
        plain.cellPaddingHorizontal != null;

    final buffer = StringBuffer();
    buffer.writeln('package dev.equo.swt.size;');
    buffer.writeln();
    if (needsFontMetrics) {
      buffer.writeln('import dev.equo.swt.Config;');
      buffer.writeln('import dev.equo.swt.FontMetricsUtil;');
    }
    if (fallbackWidth != null) {
      buffer.writeln('import org.eclipse.swt.SWT;');
    }
    if (plain.hasExpander) {
      buffer.writeln('import org.eclipse.swt.graphics.Rectangle;');
    }
    if (fallbackWidth != null) {
      buffer.writeln('import org.eclipse.swt.graphics.Point;');
    }
    buffer.writeln('import org.eclipse.swt.widgets.Dart$widgetType;');
    if (nested || measuresContent) {
      buffer.writeln('import org.eclipse.swt.widgets.${widgetType}Item;');
    }
    buffer.writeln();
    buffer.writeln('public class ${widgetType}Sizes {');
    buffer.writeln();
    // Heights at the default font. For a widget whose rows track the font these are the floor
    // Flutter lays out against, not the final height.
    buffer.writeln(
      '    private static final double ROW_HEIGHT = ${_d(plain.rowHeight)};',
    );
    buffer.writeln(
      '    private static final double ROW_HEIGHT_WITH_COLS = ${_d(withCols.rowHeight)};',
    );
    buffer.writeln();
    buffer.writeln(
      '    private static final double HEADER_HEIGHT = ${_d(headerPlain)};',
    );
    buffer.writeln(
      '    private static final double HEADER_HEIGHT_WITH_COLS = ${_d(headerWithCols)};',
    );
    buffer.writeln();
    if (rowFontSensitive) {
      buffer.writeln(
        '    private static final double ROW_PADDING_VERTICAL = ${_d(plain.rowPaddingVertical)};',
      );
    }
    if (headerFontSensitive) {
      buffer.writeln(
        '    private static final double HEADER_PADDING_VERTICAL = ${_d(headerPaddingVertical)};',
      );
    }
    if (plain.borderWidth != null) {
      buffer.writeln(
        '    private static final double BORDER_WIDTH = ${_d(plain.borderWidth!)};',
      );
    }
    if (fallbackWidth != null) {
      buffer.writeln(
        '    private static final int WIDTH_PER_COLUMN = ${fallbackWidth.$1};',
      );
      buffer.writeln(
        '    private static final int WIDTH_NO_COLUMNS = ${fallbackWidth.$2};',
      );
      buffer.writeln(
        '    private static final int NATIVE_SCROLLER_AND_BEZEL_TRIM = $nativeScrollerSize + $nativeBezelSize;',
      );
    }
    if (measuresContent) {
      final cellPaddingHorizontal = plain.cellPaddingHorizontal!.round();
      buffer.writeln(
        '    private static final int CELL_PADDING_LEFT = ${cellPaddingHorizontal ~/ 2};',
      );
      buffer.writeln(
        '    private static final int CELL_PADDING_HORIZONTAL = $cellPaddingHorizontal;',
      );
      // Slack the cell keeps beyond its own padding, and the checkbox column a CHECK widget
      // reserves ahead of column 0 — both mirror what the row impl draws.
      buffer.writeln('    private static final int CELL_MARGIN = 2;');
      buffer.writeln('    private static final int CHECKBOX_WIDTH = 20;');
    }
    if (plain.hasExpander) {
      // Horizontal row geometry: the left edge gap is a fraction of the widget width, then a
      // fixed row padding, then one indent per nesting level, then the expander arrow itself.
      buffer.writeln();
      buffer.writeln(
        '    private static final double EDGE_GAP_FRACTION = ${_d(plain.gapFraction!)};',
      );
      buffer.writeln(
        '    private static final double EDGE_GAP_FRACTION_WITH_COLS = ${_d(withCols.gapFraction!)};',
      );
      buffer.writeln(
        '    private static final double ITEM_PADDING_LEFT = ${_d(plain.paddingLeft!)};',
      );
      buffer.writeln(
        '    private static final double ITEM_PADDING_LEFT_WITH_COLS = ${_d(withCols.paddingLeft!)};',
      );
      buffer.writeln(
        '    private static final double ITEM_INDENT = ${_d(plain.indent!)};',
      );
      buffer.writeln(
        '    private static final double ITEM_INDENT_WITH_COLS = ${_d(withCols.indent!)};',
      );
      buffer.writeln(
        '    private static final double EXPAND_ICON_SIZE = ${_d(plain.arrowWidth!)};',
      );
    }
    buffer.writeln();
    if (fallbackWidth != null) {
      _writeComputeSize(
        buffer,
        widgetType: widgetType,
        nested: nested,
        measuresContent: measuresContent,
        themeClass: '${widgetType}ItemTheme',
        hasBorder: plain.borderWidth != null,
      );
      buffer.writeln();
    }
    if (plain.borderWidth != null) {
      buffer.writeln('    public static int getBorderWidth() {');
      buffer.writeln('        return (int) BORDER_WIDTH;');
      buffer.writeln('    }');
      buffer.writeln();
    }
    _writeRowDimension(
      buffer,
      widgetType: widgetType,
      method: 'getItemHeight',
      themeClass: '${widgetType}ItemTheme',
      constant: 'ROW_HEIGHT',
      padding: 'ROW_PADDING_VERTICAL',
      fontSensitive: rowFontSensitive,
      headerGuard: false,
    );
    buffer.writeln();
    _writeRowDimension(
      buffer,
      widgetType: widgetType,
      method: 'getHeaderHeight',
      themeClass: '${widgetType}HeaderTheme',
      constant: 'HEADER_HEIGHT',
      padding: 'HEADER_PADDING_VERTICAL',
      fontSensitive: headerFontSensitive,
      headerGuard: true,
    );
    if (!plain.hasExpander) {
      buffer.writeln('}');
      _writeSizesFile(widgetType, buffer);
      return;
    }

    final param = widgetType.toLowerCase();
    final itemType = '${widgetType}Item';
    // x of the left edge of a level's expand/collapse arrow, relative to the widget's top-left.
    buffer.writeln();
    buffer.writeln(
      '    public static double getExpanderLeft(Dart$widgetType $param, int level) {',
    );
    buffer.writeln('        boolean withCols = $param.getColumnCount() > 1;');
    buffer.writeln(
      '        double gapFraction = withCols ? EDGE_GAP_FRACTION_WITH_COLS : EDGE_GAP_FRACTION;',
    );
    buffer.writeln(
      '        double paddingLeft = withCols ? ITEM_PADDING_LEFT_WITH_COLS : ITEM_PADDING_LEFT;',
    );
    buffer.writeln(
      '        double indent = withCols ? ITEM_INDENT_WITH_COLS : ITEM_INDENT;',
    );
    buffer.writeln('        Rectangle bounds = $param.getBounds();');
    buffer.writeln(
      '        double widgetWidth = bounds != null ? bounds.width : 0;',
    );
    buffer.writeln(
      '        return widgetWidth * gapFraction + paddingLeft + indent * level;',
    );
    buffer.writeln('    }');
    buffer.writeln();
    buffer.writeln('    public static double getExpanderWidth() {');
    buffer.writeln('        return EXPAND_ICON_SIZE;');
    buffer.writeln('    }');
    // Whether x falls on an item's arrow. Leaves have none, matching the Flutter row, which only
    // claims the toggle band when the item has children. Native SWT resolves no item over the
    // twistie (TVHT_ONITEMBUTTON on Win32, the outline cell on macOS, left of the cell area on
    // GTK) — that null is what lets an application clear its selection on an arrow click, and
    // what Dart{Widget}.getItem(Point) reproduces.
    buffer.writeln();
    buffer.writeln(
      '    public static boolean isOverExpander(Dart$widgetType $param, $itemType item, int x) {',
    );
    buffer.writeln(
      '        if (item == null || item.getItemCount() == 0) return false;',
    );
    buffer.writeln('        int level = 0;');
    buffer.writeln(
      '        for ($itemType parent = item.getParentItem(); parent != null; parent = parent.getParentItem()) {',
    );
    buffer.writeln('            level++;');
    buffer.writeln('        }');
    buffer.writeln('        double left = getExpanderLeft($param, level);');
    buffer.writeln('        return x >= left && x < left + EXPAND_ICON_SIZE;');
    buffer.writeln('    }');
    buffer.writeln('}');
    _writeSizesFile(widgetType, buffer);
  }

  void _writeSizesFile(String widgetType, StringBuffer buffer) {
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
                textStyle['fontWeight'] != firstTextStyle['fontWeight'] ||
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
          final fontWeight = textStyle['fontWeight'] ?? 400;
          final fontItalic = textStyle['fontItalic'] ?? false;

          buffer.writeln(
            '    public static ${widgetClass}Theme get${themeName}Theme() {',
          );
          buffer.writeln(
            '        return new ${widgetClass}Theme(new TextStyle("$fontFamily", $fontSize, $fontItalic, $fontWeight${themeName != 'Default' && (textStyle['height'] ?? 0.0) != 0.0 ? ', ${textStyle['height']}' : ''}));',
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
        buffer.writeln('        return new ${widgetClass}Theme(Map.of(');

        final entries = <String>[];
        for (var styleEntry in stylesMap.entries) {
          final styleName = styleEntry.key;
          final textStyle = styleEntry.value;
          final fontFamily = ((textStyle['fontFamily'] ?? 'System') as String)
              .replaceAll(".AppleSystemUIFont", "System")
              .replaceAll("Roboto", "System")
              .replaceAll("Segoe UI", "System");
          final fontSize = textStyle['fontSize'] ?? 12;
          final fontWeight = textStyle['fontWeight'] ?? 400;
          final fontItalic = textStyle['fontItalic'] ?? false;
          entries.add(
            '            "$styleName", new TextStyle("$fontFamily", $fontSize, $fontItalic, $fontWeight${themeName != 'Default' && (textStyle['height'] ?? 0.0) != 0.0 ? ', ${textStyle['height']}' : ''})',
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

  String widgetField(String widgetType) =>
      widgetType[0].toLowerCase() + widgetType.substring(1);
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
  bool _isRunning = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!widget.measurer.isPaused) {
        _startMeasuring();
      }
    });
  }

  void _startMeasuring() {
    if (_isRunning) return;
    _isRunning = true;
    _measureCurrentCase();
  }

  Future<void> _measureCurrentCase() async {
    if (widget.measurer.isPaused) {
      _isRunning = false;
      return;
    }

    // Always call measureCurrentCase to allow it to detect completion and trigger phase transitions
    final previousPhase = widget.measurer.currentPhase;
    await widget.measurer.measureCurrentCase(_key);

    // Continue if we transitioned phases OR if there are more cases in current phase
    final phaseChanged = widget.measurer.currentPhase != previousPhase;
    if ((phaseChanged || widget.measurer.hasMoreCases()) &&
        !widget.measurer.isPaused) {
      setState(() {
        _key = GlobalKey();
      });

      WidgetsBinding.instance.addPostFrameCallback((_) {
        _measureCurrentCase();
      });
    } else {
      _isRunning = false;
      setState(() {}); // Update UI to reflect completion
    }
  }

  Future<void> _measureSingleCase() async {
    // Save full state before measuring (measureCurrentCase may advance/transition)
    final savedPhase = widget.measurer.currentPhase;
    final savedThemeIndex = widget.measurer.currentThemeIndex;
    final savedIndex = widget.measurer.currentCaseIndex;
    final wasFinished = widget.measurer.isFinished;

    await widget.measurer.measureCurrentCase(_key);

    // Always restore state for manual single-case measurement
    widget.measurer.currentPhase = savedPhase;
    widget.measurer.currentThemeIndex = savedThemeIndex;
    widget.measurer.currentCaseIndex = savedIndex;
    widget.measurer.isFinished = wasFinished;

    setState(() {
      _key = GlobalKey();
    });
  }

  void _onPlayPause() {
    setState(() {
      widget.measurer.togglePause();
    });
    if (!widget.measurer.isPaused) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _startMeasuring();
      });
    }
  }

  void _onPrevious() {
    setState(() {
      widget.measurer.goToPrevious();
      _key = GlobalKey();
    });
    // Measure the case after navigating
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _measureSingleCase();
    });
  }

  void _onNext() {
    setState(() {
      widget.measurer.goToNext();
      _key = GlobalKey();
    });
    // Measure the case after navigating
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _measureSingleCase();
    });
  }

  void _onCaseSelected(int? index) {
    if (index != null) {
      setState(() {
        widget.measurer.goToGlobalIndex(index);
        _key = GlobalKey();
      });
      // Measure the selected case
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _measureSingleCase();
      });
    }
  }

  void _onRunCurrent() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _measureSingleCase();
    });
  }

  @override
  Widget build(BuildContext context) {
    final measurer = widget.measurer;
    final globalIdx = measurer.globalIndex;
    final totalCases = measurer.totalAllCases;
    final allCases = measurer.allCases;
    final currentPhase = measurer.currentPhase;

    // Display different info based on phase
    String phaseText;
    if (measurer.isFinished) {
      phaseText = 'Complete';
    } else if (currentPhase == 1) {
      phaseText = 'P1: Sizing';
    } else {
      final themeName = measurer.getCurrentThemeName();
      phaseText = 'P2: $themeName';
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
        appBar: AppBar(
          title: Stack(
            children: [
              // Phase info (left-aligned, vertically centered)
              // Positioned.fill(
              //   child: Align(
              //     alignment: Alignment.centerLeft,
              //     child: Text(measurer.isFinished ? phaseText : '$phaseText ${currentIndex + 1}/$totalCases', style: const TextStyle(fontSize: 14)),
              //   ),
              // ),
              // Buttons (truly centered)
              Positioned.fill(
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.skip_previous),
                      onPressed: globalIdx > 0 ? _onPrevious : null,
                      tooltip: 'Previous',
                      iconSize: 20,
                    ),
                    IconButton(
                      icon: Icon(
                        measurer.isPaused ? Icons.play_arrow : Icons.pause,
                      ),
                      onPressed: _onPlayPause,
                      tooltip: measurer.isPaused ? 'Play' : 'Pause',
                      iconSize: 20,
                    ),
                    IconButton(
                      icon: const Icon(Icons.skip_next),
                      onPressed: globalIdx < totalCases - 1 ? _onNext : null,
                      tooltip: 'Next',
                      iconSize: 20,
                    ),
                    IconButton(
                      icon: const Icon(Icons.refresh),
                      onPressed: _onRunCurrent,
                      tooltip: 'Run current',
                      iconSize: 20,
                    ),
                    SizedBox(width: 8),
                    Text(
                      measurer.isFinished
                          ? phaseText
                          : '$phaseText ${globalIdx + 1}/$totalCases',
                      style: const TextStyle(fontSize: 14),
                    ),
                  ],
                ),
              ),
              // Dropdown (right-aligned)
              Align(
                alignment: Alignment.centerRight,
                child: SizedBox(
                  width: 360,
                  child: DropdownButton<int>(
                    value: globalIdx < totalCases ? globalIdx : null,
                    isExpanded: true,
                    underline: Container(),
                    items: List.generate(totalCases, (i) {
                      final entry = allCases[i];
                      final phase = entry.$1;
                      final themeIdx = entry.$2;
                      final caseItem = entry.$3;
                      final isSelected = i == globalIdx;
                      // Build label with phase info
                      String prefix;
                      if (phase == 1) {
                        prefix = 'P1';
                      } else {
                        final themeName =
                            measurer.themesToMeasure[themeIdx].name;
                        prefix = 'P2:$themeName';
                      }
                      return DropdownMenuItem<int>(
                        value: i,
                        child: Text(
                          '${i + 1}. [$prefix] ${caseItem.name}',
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 11,
                            color: isSelected ? Colors.indigo : null,
                            fontWeight: isSelected
                                ? FontWeight.bold
                                : FontWeight.normal,
                          ),
                        ),
                      );
                    }),
                    onChanged: _onCaseSelected,
                  ),
                ),
              ),
            ],
          ),
        ),
        body: measurer.buildCurrentCase(_key),
      ),
    );
  }
}
