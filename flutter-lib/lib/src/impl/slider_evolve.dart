import 'dart:async';

import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/slider.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../impl/control_evolve.dart';
import '../theme/theme_extensions/slider_theme_extension.dart';
import 'utils/widget_utils.dart';

class SliderImpl<T extends SliderSwt, V extends VSlider>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<SliderThemeExtension>()!;

    final minimum = state.minimum ?? widgetTheme.minimum;
    final maximum = state.maximum ?? widgetTheme.maximum;
    final selection = state.selection ?? widgetTheme.selection;
    final increment = state.increment ?? widgetTheme.increment;
    final pageIncrement = state.pageIncrement ?? widgetTheme.pageIncrement;
    final thumb = state.thumb ?? widgetTheme.thumb;

    final travelMaximum = maximum - thumb < minimum ? minimum : maximum - thumb;
    final currentValue = selection!.clamp(minimum, travelMaximum).toDouble();

    final enabled = state.enabled ?? true;
    final isVertical = hasStyle(state.style, SWT.VERTICAL);

    final hasValidBounds = hasBounds(state.bounds);
    final double width;
    final double height;

    if (hasValidBounds) {
      width = state.bounds!.width.toDouble();
      height = state.bounds!.height.toDouble();
    } else {
      if (isVertical) {
        width = widgetTheme.minVerticalWidth;
        height = widgetTheme.minVerticalHeight;
      } else {
        width = widgetTheme.minWidth;
        height = widgetTheme.minHeight;
      }
    }

    final activeTrackColor = getSliderActiveTrackColor(
      state,
      widgetTheme,
      enabled: enabled,
    );
    final inactiveTrackColor = getSliderInactiveTrackColor(
      state,
      widgetTheme,
      enabled: enabled,
    );
    final thumbColor = getSliderThumbColor(
      state,
      widgetTheme,
      enabled: enabled,
    );

    return wrap(
      _StyledSlider(
        value: currentValue,
        min: minimum.toDouble(),
        max: travelMaximum.toDouble(),
        enabled: enabled,
        width: width,
        height: height,
        isVertical: isVertical,
        hasValidBounds: hasValidBounds,
        widgetTheme: widgetTheme,
        activeTrackColor: activeTrackColor,
        inactiveTrackColor: inactiveTrackColor,
        thumbColor: thumbColor,
        increment: increment,
        pageIncrement: pageIncrement,
        thumb: thumb,
        onChanged: (value) {
          state.selection = value.round();
          var e = VEvent()..index = state.selection;
          widget.sendSelectionSelection(state, e);
        },
        onChangeEnd: (value) {
          state.selection = value.round();
          var e = VEvent()..index = state.selection;
          widget.sendSelectionDefaultSelection(state, e);
        },
      ),
    );
  }
}

Color getSliderActiveTrackColor(
  VSlider state,
  SliderThemeExtension widgetTheme, {
  required bool enabled,
}) {
  if (!enabled) {
    return widgetTheme.disabledActiveTrackColor;
  }
  return getForegroundColor(
    foreground: state.foreground,
    defaultColor: widgetTheme.activeTrackColor,
  );
}

Color getSliderInactiveTrackColor(
  VSlider state,
  SliderThemeExtension widgetTheme, {
  required bool enabled,
}) {
  if (!enabled) {
    return widgetTheme.disabledInactiveTrackColor;
  }
  return getBackgroundColor(
        background: state.background,
        defaultColor: widgetTheme.inactiveTrackColor,
      ) ??
      widgetTheme.inactiveTrackColor;
}

Color getSliderThumbColor(
  VSlider state,
  SliderThemeExtension widgetTheme, {
  required bool enabled,
}) {
  if (!enabled) {
    return widgetTheme.disabledThumbColor;
  }
  return getForegroundColor(
    foreground: state.foreground,
    defaultColor: widgetTheme.thumbColor,
  );
}

class _StyledSlider extends StatefulWidget {
  final double value;
  final double min;
  final double max;
  final bool enabled;
  final ValueChanged<double> onChanged;
  final ValueChanged<double> onChangeEnd;
  final double width;
  final double height;
  final bool isVertical;
  final bool hasValidBounds;
  final SliderThemeExtension widgetTheme;
  final Color activeTrackColor;
  final Color inactiveTrackColor;
  final Color thumbColor;
  final int increment;
  final int pageIncrement;
  final int thumb;

  const _StyledSlider({
    required this.value,
    required this.min,
    required this.max,
    required this.enabled,
    required this.onChanged,
    required this.onChangeEnd,
    required this.width,
    required this.height,
    required this.isVertical,
    required this.hasValidBounds,
    required this.widgetTheme,
    required this.activeTrackColor,
    required this.inactiveTrackColor,
    required this.thumbColor,
    required this.increment,
    required this.pageIncrement,
    required this.thumb,
  });

  @override
  _StyledSliderState createState() => _StyledSliderState();
}

class _StyledSliderState extends State<_StyledSlider> {
  double? _localValue;
  bool _dragging = false;
  Timer? _replay;
  int? _sentSelection;

  static const Duration _replayQuiet = Duration(milliseconds: 250);

  @override
  void dispose() {
    _replay?.cancel();
    super.dispose();
  }

  void _awaitReplay() {
    _replay?.cancel();
    _replay = Timer(_replayQuiet, () {
      if (!mounted) return;
      setState(() {
        _replay = null;
        _localValue = null;
      });
    });
  }

  @override
  void didUpdateWidget(_StyledSlider oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_dragging) return;

    if (widget.min != oldWidget.min || widget.max != oldWidget.max) {
      _replay?.cancel();
      _replay = null;
      _localValue = null;
      return;
    }
    if (widget.value == oldWidget.value) return;

    if (_replay != null) {
      _awaitReplay();
      return;
    }

    _localValue = null;
  }

  @override
  Widget build(BuildContext context) {
    final displayValue = _localValue ?? widget.value;

    final slider = SliderTheme(
      data: SliderThemeData(
        activeTrackColor: widget.activeTrackColor,
        inactiveTrackColor: widget.inactiveTrackColor,
        thumbColor: widget.thumbColor,
        overlayColor: widget.widgetTheme.overlayColor,
        trackHeight: widget.widgetTheme.trackHeight,
        thumbShape: RoundSliderThumbShape(
          enabledThumbRadius: widget.widgetTheme.thumbRadius,
        ),
        trackShape: const RoundedRectSliderTrackShape(),
      ),
      child: Slider(
        value: displayValue.clamp(widget.min, widget.max),
        min: widget.min,
        max: widget.max,
        onChangeStart: widget.enabled
            ? (_) {
                _replay?.cancel();
                _replay = null;
                _dragging = true;
                _sentSelection = null;
              }
            : null,
        onChanged: widget.enabled
            ? (value) {
                setState(() {
                  _localValue = value;
                });
                final selection = value.round();
                if (selection == _sentSelection) return;
                _sentSelection = selection;
                widget.onChanged(value);
              }
            : null,
        onChangeEnd: widget.enabled
            ? (value) {
                final committed =
                    value.roundToDouble().clamp(widget.min, widget.max);
                setState(() {
                  _dragging = false;
                  _localValue = committed;
                });
                _sentSelection = committed.round();
                _awaitReplay();
                widget.onChangeEnd(value);
              }
            : null,
      ),
    );

    Widget result;
    if (widget.isVertical) {
      result = RotatedBox(
        quarterTurns: 1,
        child: SizedBox(
          width: widget.height,
          height: widget.width,
          child: slider,
        ),
      );
    } else {
      result = slider;
    }

    return SizedBox(width: widget.width, height: widget.height, child: result);
  }
}
