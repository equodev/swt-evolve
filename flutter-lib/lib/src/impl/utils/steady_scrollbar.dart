import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

/// Material's desktop [Scrollbar], except that a thumb shown at all times is there from the first
/// frame, as a native scroll bar is, instead of fading in over 300 ms. Colors, thickness, track and
/// hover behave as Material's do; a scroll bar that shows only while scrolling still fades.
class SteadyScrollbar extends StatelessWidget {
  const SteadyScrollbar({
    super.key,
    required this.child,
    this.controller,
    this.thumbVisibility,
    this.notificationPredicate,
  });

  final Widget child;
  final ScrollController? controller;
  final bool? thumbVisibility;
  final ScrollNotificationPredicate? notificationPredicate;

  @override
  Widget build(BuildContext context) {
    switch (Theme.of(context).platform) {
      case TargetPlatform.android:
      case TargetPlatform.iOS:
      case TargetPlatform.fuchsia:
        // Material draws these differently (Cupertino, thinner bars): its own widget keeps that.
        return Scrollbar(
          controller: controller,
          thumbVisibility: thumbVisibility,
          notificationPredicate: notificationPredicate,
          child: child,
        );
      case TargetPlatform.linux:
      case TargetPlatform.macOS:
      case TargetPlatform.windows:
        return _SteadyScrollbar(
          controller: controller,
          thumbVisibility: thumbVisibility,
          notificationPredicate: notificationPredicate,
          child: child,
        );
    }
  }
}

const double _thickness = 8.0;
const double _thicknessWithTrack = 12.0;
const double _margin = 2.0;
const double _minLength = 48.0;
const Radius _radius = Radius.circular(8.0);

/// An opacity as an 8-bit alpha, rounded as Material's `withOpacity` rounds it.
int _alpha(double opacity) => (255 * opacity).round();

class _SteadyScrollbar extends RawScrollbar {
  const _SteadyScrollbar({
    required super.child,
    super.controller,
    super.thumbVisibility,
    ScrollNotificationPredicate? notificationPredicate,
  }) : super(
          fadeDuration: thumbVisibility ?? false ? Duration.zero : const Duration(milliseconds: 300),
          timeToFade: const Duration(milliseconds: 600),
          pressDuration: Duration.zero,
          notificationPredicate: notificationPredicate ?? defaultScrollNotificationPredicate,
        );

  @override
  _SteadyScrollbarState createState() => _SteadyScrollbarState();
}

class _SteadyScrollbarState extends RawScrollbarState<_SteadyScrollbar> {
  late AnimationController _hoverAnimationController;
  bool _dragIsActive = false;
  bool _hoverIsActive = false;
  late ColorScheme _colorScheme;
  late ScrollbarThemeData _scrollbarTheme;

  @override
  bool get showScrollbar =>
      widget.thumbVisibility ?? _scrollbarTheme.thumbVisibility?.resolve(_states) ?? false;

  @override
  bool get enableGestures => _scrollbarTheme.interactive ?? true;

  bool _trackVisible(Set<WidgetState> states) => _scrollbarTheme.trackVisibility?.resolve(states) ?? false;

  Set<WidgetState> get _states => <WidgetState>{
        if (_dragIsActive) WidgetState.dragged,
        if (_hoverIsActive) WidgetState.hovered,
      };

  Color _thumbColor(Set<WidgetState> states) {
    final onSurface = _colorScheme.onSurface;
    final dark = _colorScheme.brightness == Brightness.dark;
    final dragColor = onSurface.withAlpha(_alpha(dark ? 0.75 : 0.6));
    final hoverColor = onSurface.withAlpha(_alpha(dark ? 0.65 : 0.5));
    final idleColor = onSurface.withAlpha(_alpha(dark ? 0.3 : 0.1));
    final themed = _scrollbarTheme.thumbColor?.resolve(states);
    if (states.contains(WidgetState.dragged)) return themed ?? dragColor;
    // With the track showing, the thumb takes its hover color at once.
    if (_trackVisible(states)) return themed ?? hoverColor;
    return Color.lerp(themed ?? idleColor, themed ?? hoverColor, _hoverAnimationController.value)!;
  }

  Color _trackColor(Set<WidgetState> states) {
    if (!showScrollbar || !_trackVisible(states)) return const Color(0x00000000);
    final dark = _colorScheme.brightness == Brightness.dark;
    return _scrollbarTheme.trackColor?.resolve(states) ??
        _colorScheme.onSurface.withAlpha(_alpha(dark ? 0.05 : 0.03));
  }

  Color _trackBorderColor(Set<WidgetState> states) {
    if (!showScrollbar || !_trackVisible(states)) return const Color(0x00000000);
    final dark = _colorScheme.brightness == Brightness.dark;
    return _scrollbarTheme.trackBorderColor?.resolve(states) ??
        _colorScheme.onSurface.withAlpha(_alpha(dark ? 0.25 : 0.1));
  }

  double _thicknessFor(Set<WidgetState> states) {
    if (states.contains(WidgetState.hovered) && _trackVisible(states)) {
      return _scrollbarTheme.thickness?.resolve(states) ?? _thicknessWithTrack;
    }
    return _scrollbarTheme.thickness?.resolve(states) ?? _thickness;
  }

  @override
  void initState() {
    super.initState();
    _hoverAnimationController = AnimationController(vsync: this, duration: const Duration(milliseconds: 200))
      ..addListener(updateScrollbarPainter);
  }

  @override
  void didChangeDependencies() {
    _colorScheme = Theme.of(context).colorScheme;
    _scrollbarTheme = ScrollbarTheme.of(context);
    super.didChangeDependencies();
  }

  @override
  void updateScrollbarPainter() {
    final states = _states;
    scrollbarPainter
      ..color = _thumbColor(states)
      ..trackColor = _trackColor(states)
      ..trackBorderColor = _trackBorderColor(states)
      ..textDirection = Directionality.of(context)
      ..thickness = _thicknessFor(states)
      ..radius = _scrollbarTheme.radius ?? _radius
      ..crossAxisMargin = _scrollbarTheme.crossAxisMargin ?? _margin
      ..mainAxisMargin = _scrollbarTheme.mainAxisMargin ?? 0.0
      ..minLength = _scrollbarTheme.minThumbLength ?? _minLength
      ..padding = MediaQuery.paddingOf(context)
      ..scrollbarOrientation = widget.scrollbarOrientation
      ..ignorePointer = !enableGestures;
  }

  @override
  void handleThumbPressStart(Offset localPosition) {
    super.handleThumbPressStart(localPosition);
    setState(() => _dragIsActive = true);
  }

  @override
  void handleThumbPressEnd(Offset localPosition, Velocity velocity) {
    super.handleThumbPressEnd(localPosition, velocity);
    setState(() => _dragIsActive = false);
  }

  @override
  void handleHover(PointerHoverEvent event) {
    super.handleHover(event);
    if (isPointerOverScrollbar(event.position, event.kind, forHover: true)) {
      setState(() => _hoverIsActive = true);
      _hoverAnimationController.forward();
    } else if (_hoverIsActive) {
      setState(() => _hoverIsActive = false);
      _hoverAnimationController.reverse();
    }
  }

  @override
  void handleHoverExit(PointerExitEvent event) {
    super.handleHoverExit(event);
    setState(() => _hoverIsActive = false);
    _hoverAnimationController.reverse();
  }

  @override
  void dispose() {
    _hoverAnimationController.dispose();
    super.dispose();
  }
}
