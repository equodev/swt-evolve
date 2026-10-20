import 'dart:math' as math;
import 'package:flutter/gestures.dart' show kPrimaryButton;
import '../gen/color.dart';
import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/scale.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../theme/theme_extensions/scale_theme_extension.dart';
import '../theme/theme_settings/scale_theme_settings.dart';
import 'utils/widget_utils.dart';

class ScaleImpl<T extends ScaleSwt, V extends VScale>
    extends ControlImpl<T, V> {
  @override
  bool get keepsWheel => true;

  bool _changedDuringPress = false;
  bool _pressed = false;

  /// Set by an [_InertMargin] for the press it took, which reaches it before this control's own
  /// handler: a press beyond the ends of the track counts for nothing.
  bool _pressOnMargin = false;

  /// A Scale takes keyboard focus like any other control: clicking it focuses it, and the arrow,
  /// page, home and end keys then move the value. Java routes a keystroke to whatever
  /// getFocusControl() returns, so a Scale that never reports focus never receives one.
  final FocusNode _focusNode = FocusNode(debugLabel: 'Scale');

  @override
  FocusNode? get swtFocusNode => _focusNode;

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ScaleThemeExtension>()!;

    final minimum = state.minimum ?? widgetTheme.defaultMinimum;
    final maximum = state.maximum ?? widgetTheme.defaultMaximum;
    final selection = state.selection ?? widgetTheme.defaultSelection;

    final currentValue = selection.clamp(minimum, maximum).toDouble();
    final enabled = state.enabled ?? true;
    final isVertical = hasStyle(state.style, SWT.VERTICAL);
    final size = getScaleSize(state, widgetTheme, isVertical: isVertical);

    return wrap(Listener(
      onPointerDown: (e) {
        _changedDuringPress = false;
        // Only a press on the track counts; one beyond its ends does nothing (see _InertMargin).
        _pressed = e.buttons == kPrimaryButton && !_pressOnMargin;
        _pressOnMargin = false;
      },
      // A primary press that moves nothing still reports a Selection, ahead of its MouseUp, as a
      // native Scale does for a click on its knob; this Listener is reached before the control's
      // own. The second press of a double click reports none: Win32 and GTK leave it to the first.
      onPointerUp: (_) {
        final primary = _pressed;
        _pressed = false;
        if (!primary || _changedDuringPress || !enabled || swtClickCount > 1) return;
        widget.sendSelectionSelection(state, VEvent()
          ..index = state.selection
          ..stateMask = SWT.BUTTON1);
      },
      onPointerCancel: (_) => _pressed = false,
      child: _ThemedScale(
        background: state.background,
        foreground: state.foreground,
        value: currentValue,
        min: minimum.toDouble(),
        max: maximum.toDouble(),
        enabled: enabled,
        width: size.width,
        height: size.height,
        isVertical: isVertical,
        widgetTheme: widgetTheme,
        onMarginPress: () => _pressOnMargin = true,
        onChanged: (value) {
          _changedDuringPress = true;
          state.selection = value.round();
          widget.sendSelectionSelection(
            state,
            VEvent()
              ..index = state.selection
              ..stateMask = _pressed ? SWT.BUTTON1 : 0,
          );
        },
        onChangeEnd: (value) {
          // No event of its own: a Scale sends Selection and nothing else, and the change that
          // ends the gesture has already sent one through onChanged.
          state.selection = value.round();
        },
        focusNode: _focusNode,
        onFocusChanged: (hasFocus) {
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            widget.sendFocusFocusOut(state, null);
          }
        },
      ),
    ));
  }
}

class _ThemedScale extends StatefulWidget {
  /// The application's own ground, when it set one.
  final VColor? background;
  /// The application's own foreground: the track and thumb follow it, as Slider's does.
  final VColor? foreground;
  final double value;
  final double min;
  final double max;
  final bool enabled;
  final double width;
  final double height;
  final bool isVertical;
  final ScaleThemeExtension widgetTheme;
  final ValueChanged<double> onChanged;
  final ValueChanged<double> onChangeEnd;
  final FocusNode focusNode;
  final ValueChanged<bool> onFocusChanged;
  final VoidCallback onMarginPress;

  const _ThemedScale({
    this.background,
    this.foreground,
    required this.value,
    required this.min,
    required this.max,
    required this.enabled,
    required this.width,
    required this.height,
    required this.isVertical,
    required this.widgetTheme,
    required this.onChanged,
    required this.onMarginPress,
    required this.onChangeEnd,
    required this.focusNode,
    required this.onFocusChanged,
  });

  @override
  State<_ThemedScale> createState() => _ThemedScaleState();
}

class _ThemedScaleState extends State<_ThemedScale> {
  double? _localValue;
  bool _isHovered = false;

  /// How far the pointer pressed on the knob is from the knob's value. A press on the knob moves
  /// nothing and a drag moves the knob by where it was grabbed, as on GTK and Cocoa; the Slider
  /// alone would jump the value to the pointer.
  double _grabOffset = 0;

  @override
  void initState() {
    super.initState();
    widget.focusNode.addListener(_reportFocus);
  }

  @override
  void dispose() {
    widget.focusNode.removeListener(_reportFocus);
    super.dispose();
  }

  void _reportFocus() => widget.onFocusChanged(widget.focusNode.hasFocus);

  @override
  Widget build(BuildContext context) {
    final displayValue = _localValue ?? widget.value;
    final ticks = generateTickPositions(
      widget.min,
      widget.max,
      widget.widgetTheme.tickMarkCount,
    );

    final tickMarkColor = getScaleTickMarkColor(
      widget.widgetTheme,
      isEnabled: widget.enabled,
    );

    final scale = MouseRegion(
      // Hover here only drives the thumb's own appearance. MouseEnter and MouseExit belong to
      // ControlImpl.wrap(), which reports them for every control; sending them here as well is
      // what made a Scale report entering twice.
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: _TickMarkPainter(
                ticks: ticks,
                min: widget.min,
                max: widget.max,
                color: tickMarkColor,
                tickHeight: widget.widgetTheme.tickMarkHeight,
                tickWidth: widget.widgetTheme.tickMarkWidth,
                horizontalPadding: widget.widgetTheme.horizontalPadding,
              ),
            ),
          ),
          _buildSlider(displayValue),
        ],
      ),
    );

    if (widget.isVertical) {
      return _buildVerticalScale(scale);
    }
    return _buildHorizontalScale(scale);
  }

  Widget _buildSlider(double displayValue) {
    final activeTrackColor = getScaleActiveTrackColor(
      context,
      widget.widgetTheme,
      isEnabled: widget.enabled,
      foreground: widget.foreground,
    );
    final inactiveTrackColor = getScaleInactiveTrackColor(
      widget.widgetTheme,
      isEnabled: widget.enabled,
    );
    final thumbColor = getScaleThumbColor(
      context,
      widget.widgetTheme,
      isEnabled: widget.enabled,
      isHovered: _isHovered,
      foreground: widget.foreground,
    );

    return SliderTheme(
      data: SliderThemeData(
        activeTrackColor: activeTrackColor,
        inactiveTrackColor: inactiveTrackColor,
        thumbColor: thumbColor,
        overlayColor: widget.widgetTheme.overlayColor,
        trackHeight: widget.widgetTheme.trackHeight,
        thumbShape: RoundSliderThumbShape(
          enabledThumbRadius: _isHovered
              ? widget.widgetTheme.thumbHoverRadius
              : widget.widgetTheme.thumbRadius,
        ),
        tickMarkShape: SliderTickMarkShape.noTickMark,
        // The track is inset by the overlay radius; tying it to the tick marks' padding keeps the
        // value under the pointer where the ticks say it is.
        overlayShape: RoundSliderOverlayShape(
          overlayRadius: widget.widgetTheme.horizontalPadding,
        ),
      ),
      child: Stack(
        children: [
          LayoutBuilder(
            builder: (context, constraints) => Listener(
              onPointerDown: (e) => _grabOffset = _offsetFromKnob(e.localPosition.dx, constraints.maxWidth),
              child: Slider(
                value: displayValue.clamp(widget.min, widget.max),
                min: widget.min,
                max: widget.max,
                focusNode: widget.focusNode,
                onChanged: widget.enabled ? _handleChanged : null,
                onChangeEnd: widget.enabled ? _handleChangeEnd : null,
              ),
            ),
          ),
          // Beyond the ends of the track a press moves nothing, as on Win32 and GTK; Flutter's
          // Slider would take it as a press on the track's nearest end.
          for (final left in [true, false])
            Positioned(
              left: left ? 0 : null,
              right: left ? null : 0,
              top: 0,
              bottom: 0,
              width: _marginWidth(left: left),
              child: _InertMargin(onPress: widget.onMarginPress),
            ),
        ],
      ),
    );
  }

  /// The padding at one end of the track, up to where the knob begins: near that end the knob
  /// reaches into the padding, and natively it can be grabbed anywhere on it.
  double _marginWidth({required bool left}) {
    final pad = widget.widgetTheme.horizontalPadding;
    final length = widget.isVertical ? widget.height : widget.width;
    if (widget.max <= widget.min || length <= 2 * pad) return pad;
    final centre = _valueToX(widget.value, widget.min, widget.max, length, pad);
    final edge = left ? centre - widget.widgetTheme.thumbRadius : length - centre - widget.widgetTheme.thumbRadius;
    return math.max(0, math.min(pad, edge));
  }

  Widget _buildHorizontalScale(Widget scale) {
    return AnimatedContainer(
      duration: widget.widgetTheme.animationDuration,
      width: widget.width,
      height: widget.height,
      color: _appBackground(context),
      child: scale,
    );
  }

  /// The ground the application gave the Scale, when the product owns the theme.
  Color? _appBackground(BuildContext context) => getBackgroundColor(
        background: widget.background,
        defaultColor: null,
        context: context,
      );

  Widget _buildVerticalScale(Widget scale) {
    return AnimatedContainer(
      duration: widget.widgetTheme.animationDuration,
      width: widget.width,
      height: widget.height,
      color: _appBackground(context),
      child: RotatedBox(
        quarterTurns: 3,
        child: SizedBox(
          width: widget.height,
          height: widget.width,
          child: scale,
        ),
      ),
    );
  }

  /// The value under [x] minus the knob's value, when [x] is on the knob; 0 anywhere else.
  double _offsetFromKnob(double x, double length) {
    final pad = widget.widgetTheme.horizontalPadding;
    final track = length - 2 * pad;
    final range = widget.max - widget.min;
    if (track <= 0 || range <= 0) return 0;
    final current = (_localValue ?? widget.value).clamp(widget.min, widget.max).toDouble();
    final knobX = _valueToX(current, widget.min, widget.max, length, pad);
    final radius = math.max(widget.widgetTheme.thumbRadius, widget.widgetTheme.thumbHoverRadius);
    if ((x - knobX).abs() > radius) return 0;
    return (x - knobX) / track * range;
  }

  void _handleChanged(double raw) {
    // Touching a Scale focuses it, as it does natively; Flutter's Slider does not do this itself.
    if (!widget.focusNode.hasFocus) widget.focusNode.requestFocus();
    final value = (raw - _grabOffset).clamp(widget.min, widget.max).toDouble();
    if (value.round() == (_localValue ?? widget.value).round()) return;
    setState(() => _localValue = value);
    widget.onChanged(value);
  }

  void _handleChangeEnd(double raw) {
    final value = _localValue ?? widget.value;
    _grabOffset = 0;
    setState(() => _localValue = null);
    widget.onChangeEnd(value);
  }
}

/// Where [value] sits along a track of [length] that the slider insets by [pad] at each end.
double _valueToX(double value, double min, double max, double length, double pad) =>
    pad + (value - min) / (max - min) * (length - 2 * pad);

class _TickMarkPainter extends CustomPainter {
  final List<double> ticks;
  final double min;
  final double max;
  final Color color;
  final double tickHeight;
  final double tickWidth;
  final double horizontalPadding;

  const _TickMarkPainter({
    required this.ticks,
    required this.min,
    required this.max,
    required this.color,
    required this.tickHeight,
    required this.tickWidth,
    required this.horizontalPadding,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = tickWidth
      ..style = PaintingStyle.stroke;

    final verticalCenter = size.height / 2;
    final halfTickHeight = tickHeight / 2;

    for (final tickValue in ticks) {
      final x = _valueToX(tickValue, min, max, size.width, horizontalPadding);

      canvas.drawLine(
        Offset(x, verticalCenter - halfTickHeight),
        Offset(x, verticalCenter + halfTickHeight),
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(_TickMarkPainter oldDelegate) {
    return oldDelegate.ticks != ticks ||
        oldDelegate.min != min ||
        oldDelegate.max != max ||
        oldDelegate.color != color ||
        oldDelegate.tickHeight != tickHeight ||
        oldDelegate.tickWidth != tickWidth ||
        oldDelegate.horizontalPadding != horizontalPadding;
  }
}

/// Takes the pointer so the Slider beneath never sees it, and reports the press through [onPress];
/// the control's own handlers, which are ancestors, still see it.
class _InertMargin extends StatelessWidget {
  final VoidCallback onPress;

  const _InertMargin({required this.onPress});

  @override
  Widget build(BuildContext context) => Listener(
        behavior: HitTestBehavior.opaque,
        onPointerDown: (_) => onPress(),
        child: const SizedBox.expand(),
      );
}
