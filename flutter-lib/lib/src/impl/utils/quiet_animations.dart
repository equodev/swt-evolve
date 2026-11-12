import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

import 'cached_text.dart';

// Implicit animations that animate as Flutter's do, without asking for a rebuild from inside their
// own update, and where they can, without rebuilding on each step either.
//
// An implicitly animated widget restarts its controller when a target changes, and the controller's
// listener calls setState right there, in the middle of the build. The element is rebuilt straight
// after anyway, but any widget marked dirty during a build makes the build owner re-sort its whole
// dirty list: changing a thousand controls at once re-sorted it a thousand times.

/// An [AnimatedContainer] whose update does not mark it dirty, and which, while only what it paints
/// moves, follows each step of the animation by repainting rather than rebuilding.
///
/// Its constraints, padding and margin take a new value at once: they follow the bounds Java lays a
/// control out at, and a native control's edges do not move over time.
///
/// What it paints alone moves when its decorations change but keep the same border widths and
/// nothing else changes; any other change rebuilds it on each step, as [AnimatedContainer] does.
class QuietAnimatedContainer extends AnimatedContainer {
  QuietAnimatedContainer({
    super.key,
    super.alignment,
    super.padding,
    super.color,
    super.decoration,
    super.foregroundDecoration,
    super.width,
    super.height,
    super.constraints,
    super.margin,
    super.transform,
    super.transformAlignment,
    super.child,
    super.clipBehavior,
    super.curve,
    required super.duration,
    super.onEnd,
  });

  @override
  AnimatedWidgetBaseState<AnimatedContainer> createState() => _QuietAnimatedContainerState();
}

class _QuietAnimatedContainerState extends AnimatedWidgetBaseState<AnimatedContainer> {
  AlignmentGeometryTween? _alignment;
  DecorationTween? _decoration;
  DecorationTween? _foregroundDecoration;
  Matrix4Tween? _transform;
  AlignmentGeometryTween? _transformAlignment;

  /// Whether the last build drew each step itself, so that a step has to rebuild it.
  bool _rebuildsOnStep = true;
  bool _updating = false;

  /// The decorations' animations, kept while their tweens and curve are, so that the boxes painting
  /// them do not listen anew on every build.
  Animation<Decoration>? _decorationSteps;
  Animation<Decoration>? _foregroundSteps;
  Animation<double>? _curve;
  DecorationTween? _decorationTween;
  DecorationTween? _foregroundTween;

  @override
  void didUpdateWidget(AnimatedContainer oldWidget) {
    // Restarting the controller steps it; an update is always followed by a build.
    _updating = true;
    try {
      super.didUpdateWidget(oldWidget);
    } finally {
      _updating = false;
    }
  }

  /// Looked up when the dependencies change rather than on every update of the boxes.
  late ImageConfiguration _configuration;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _configuration = createLocalImageConfiguration(context);
  }

  /// The boxes painting the decorations, told of each step of the animation here, by the listener
  /// this state already has, rather than each listening to it: removing a listener from an animation
  /// hashes it, and a box detaches every time its control goes away.
  final _boxes = _StepRelay();

  /// Called on each step of the animation, which rebuilds only when the last build drew the step.
  @override
  void setState(VoidCallback fn) {
    if (_updating) {
      fn();
    } else if (!_rebuildsOnStep) {
      fn();
      _boxes.step();
    } else {
      super.setState(fn);
    }
  }

  @override
  void forEachTween(TweenVisitor<dynamic> visitor) {
    _alignment = visitor(_alignment, widget.alignment,
        (dynamic value) => AlignmentGeometryTween(begin: value as AlignmentGeometry)) as AlignmentGeometryTween?;
    _decoration = visitor(_decoration, widget.decoration,
        (dynamic value) => DecorationTween(begin: value as Decoration)) as DecorationTween?;
    _foregroundDecoration = visitor(_foregroundDecoration, widget.foregroundDecoration,
        (dynamic value) => DecorationTween(begin: value as Decoration)) as DecorationTween?;
    _transform = visitor(_transform, widget.transform,
        (dynamic value) => Matrix4Tween(begin: value as Matrix4)) as Matrix4Tween?;
    _transformAlignment = visitor(_transformAlignment, widget.transformAlignment,
        (dynamic value) => AlignmentGeometryTween(begin: value as AlignmentGeometry)) as AlignmentGeometryTween?;
  }

  static bool _still(Tween<Object?>? tween) => tween == null || tween.begin == tween.end;

  /// Whether a decoration moving along [tween] keeps its padding, the room its border takes, at every
  /// step: between two box decorations whose borders are of one kind and as wide.
  static bool _keepsPadding(DecorationTween? tween) {
    if (tween == null) return true;
    final (begin, end) = (tween.begin, tween.end);
    return begin is BoxDecoration &&
        end is BoxDecoration &&
        begin.border?.runtimeType == end.border?.runtimeType &&
        begin.padding == end.padding;
  }

  /// Whether nothing but what the decorations paint moves. A matrix moving between two equal ones is
  /// not exactly still, so any transform rebuilds on each step.
  bool get _onlyPaintMoves =>
      _transform == null &&
      widget.clipBehavior == Clip.none &&
      _still(_alignment) &&
      _still(_transformAlignment) &&
      _keepsPadding(_decoration);

  @override
  Widget build(BuildContext context) {
    final Animation<double> animation = this.animation;
    _rebuildsOnStep = !_onlyPaintMoves;
    final Animation<Decoration>? decoration;
    final Animation<Decoration>? foreground;
    if (_rebuildsOnStep) {
      decoration = _decoration == null ? null : AlwaysStoppedAnimation(_decoration!.evaluate(animation));
      foreground = _foregroundDecoration == null
          ? null
          : AlwaysStoppedAnimation(_foregroundDecoration!.evaluate(animation));
    } else {
      if (!identical(_curve, animation)) _decorationSteps = _foregroundSteps = null;
      _curve = animation;
      if (!identical(_decorationTween, _decoration)) _decorationSteps = null;
      _decorationTween = _decoration;
      if (!identical(_foregroundTween, _foregroundDecoration)) _foregroundSteps = null;
      _foregroundTween = _foregroundDecoration;
      decoration = _decoration == null ? null : _decorationSteps ??= _decoration!.animate(animation);
      foreground =
          _foregroundDecoration == null ? null : _foregroundSteps ??= _foregroundDecoration!.animate(animation);
    }
    final alignment = _alignment?.evaluate(animation);
    final padding = widget.padding;
    final constraints = widget.constraints;
    final margin = widget.margin;
    final transform = _transform?.evaluate(animation);
    final transformAlignment = _transformAlignment?.evaluate(animation);
    final child = widget.child;

    // What Container builds from the same values, with the decorations taken from their animations;
    // built the same way on both paths, so that changing path keeps what is below.
    final decorationNow = decoration?.value;
    Widget? current = child;
    if (child == null && (constraints == null || !constraints.isTight)) {
      current = LimitedBox(
        maxWidth: 0.0,
        maxHeight: 0.0,
        child: ConstrainedBox(constraints: const BoxConstraints.expand()),
      );
    } else if (alignment != null) {
      current = Align(alignment: alignment, child: current);
    }
    final effectivePadding = switch ((padding, decorationNow?.padding)) {
      (null, final EdgeInsetsGeometry? padding) => padding,
      (final EdgeInsetsGeometry? padding, null) => padding,
      (_) => padding!.add(decorationNow!.padding),
    };
    if (effectivePadding != null) current = Padding(padding: effectivePadding, child: current);
    if (widget.clipBehavior != Clip.none) {
      current = ClipPath(
        clipper: _DecorationClipper(textDirection: Directionality.maybeOf(context), decoration: decorationNow!),
        clipBehavior: widget.clipBehavior,
        child: current,
      );
    }
    if (decoration != null) {
      current = _FollowingDecoratedBox(
          decoration: decoration, relay: _boxes, configuration: _configuration, child: current);
    }
    if (foreground != null) {
      current = _FollowingDecoratedBox(
        decoration: foreground,
        relay: _boxes,
        configuration: _configuration,
        position: DecorationPosition.foreground,
        child: current,
      );
    }
    if (constraints != null) current = ConstrainedBox(constraints: constraints, child: current);
    if (margin != null) current = Padding(padding: margin, child: current);
    if (transform != null) current = Transform(transform: transform, alignment: transformAlignment, child: current);
    return current!;
  }
}

/// Clips to a decoration's shape, as a [Container] clips to its decoration.
class _DecorationClipper extends CustomClipper<Path> {
  _DecorationClipper({TextDirection? textDirection, required this.decoration})
      : textDirection = textDirection ?? TextDirection.ltr;

  final TextDirection textDirection;
  final Decoration decoration;

  @override
  Path getClip(Size size) => decoration.getClipPath(Offset.zero & size, textDirection);

  @override
  bool shouldReclip(_DecorationClipper oldClipper) =>
      oldClipper.decoration != decoration || oldClipper.textDirection != textDirection;
}

/// The attached boxes of one [QuietAnimatedContainer], at most one per [DecorationPosition].
class _StepRelay {
  _RenderFollowingDecoratedBox? _background;
  _RenderFollowingDecoratedBox? _foreground;

  void step() {
    _background?._follow();
    _foreground?._follow();
  }

  void _attach(_RenderFollowingDecoratedBox box) {
    if (box.position == DecorationPosition.background) {
      _background = box;
    } else {
      _foreground = box;
    }
  }

  void _detach(_RenderFollowingDecoratedBox box) {
    if (identical(_background, box)) _background = null;
    if (identical(_foreground, box)) _foreground = null;
  }
}

/// A [DecoratedBox] whose decoration is the current value of [decoration], taken again on each step
/// [relay] passes on.
class _FollowingDecoratedBox extends SingleChildRenderObjectWidget {
  const _FollowingDecoratedBox({
    required this.decoration,
    required this.relay,
    required this.configuration,
    this.position = DecorationPosition.background,
    super.child,
  });

  final Animation<Decoration> decoration;
  final _StepRelay relay;
  final ImageConfiguration configuration;
  final DecorationPosition position;

  @override
  _RenderFollowingDecoratedBox createRenderObject(BuildContext context) => _RenderFollowingDecoratedBox(
        decoration,
        relay,
        position: position,
        configuration: configuration,
      );

  @override
  void updateRenderObject(BuildContext context, _RenderFollowingDecoratedBox renderObject) {
    renderObject
      ..position = position
      ..relay = relay
      ..steps = decoration
      ..configuration = configuration;
  }
}

class _RenderFollowingDecoratedBox extends RenderDecoratedBox {
  _RenderFollowingDecoratedBox(this._steps, this._relay, {required super.position, required super.configuration})
      : super(decoration: _steps.value);

  Animation<Decoration> _steps;
  set steps(Animation<Decoration> value) {
    _steps = value;
    _follow();
  }

  _StepRelay _relay;
  set relay(_StepRelay value) {
    if (attached) {
      _relay._detach(this);
      value._attach(this);
    }
    _relay = value;
  }

  @override
  set position(DecorationPosition value) {
    if (value == position) return;
    if (attached) _relay._detach(this);
    super.position = value;
    if (attached) _relay._attach(this);
  }

  void _follow() => decoration = _steps.value;

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    _relay._attach(this);
    _follow();
  }

  @override
  void detach() {
    _relay._detach(this);
    super.detach();
  }
}
/// A [decoration] [width] wide and as tall as the space it fills, [left] from the start of it, moving
/// to a new [left] as [AnimatedPositioned] moves its child.
///
/// Under `Positioned.fill` it draws what
/// `AnimatedPositioned(left: left, top: 0, bottom: 0, child: Container(width: width, decoration: decoration))`
/// draws in the same [Stack]. A step of the animation, or a new [width], only repaints it.
class SlidingBox extends ImplicitlyAnimatedWidget {
  const SlidingBox({
    super.key,
    required this.left,
    required this.width,
    required this.decoration,
    super.curve,
    required super.duration,
    super.onEnd,
  });

  final double left;
  final double width;
  final Decoration decoration;

  @override
  ImplicitlyAnimatedWidgetState<SlidingBox> createState() => _SlidingBoxState();
}

class _SlidingBoxState extends ImplicitlyAnimatedWidgetState<SlidingBox> {
  Tween<double>? _left;

  /// Kept while its tween and curve are, so that the box does not listen anew on every build.
  Animation<double>? _leftAnimation;
  Animation<double>? _curve;
  Tween<double>? _tween;
  late ImageConfiguration _configuration;

  @override
  void forEachTween(TweenVisitor<dynamic> visitor) {
    _left = visitor(_left, widget.left, (dynamic value) => Tween<double>(begin: value as double)) as Tween<double>?;
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _configuration = createLocalImageConfiguration(context);
  }

  @override
  Widget build(BuildContext context) {
    if (!identical(_curve, animation) || !identical(_tween, _left)) _leftAnimation = null;
    _curve = animation;
    _tween = _left;
    return _SlidingBoxLeaf(
      left: _leftAnimation ??= _left!.animate(animation),
      width: widget.width,
      decoration: widget.decoration,
      configuration: _configuration,
    );
  }
}

class _SlidingBoxLeaf extends LeafRenderObjectWidget {
  const _SlidingBoxLeaf({
    required this.left,
    required this.width,
    required this.decoration,
    required this.configuration,
  });

  final Animation<double> left;
  final double width;
  final Decoration decoration;
  final ImageConfiguration configuration;

  @override
  _RenderSlidingBox createRenderObject(BuildContext context) =>
      _RenderSlidingBox(left, width, decoration, configuration);

  @override
  void updateRenderObject(BuildContext context, _RenderSlidingBox renderObject) {
    renderObject
      ..left = left
      ..width = width
      ..decoration = decoration
      ..configuration = configuration;
  }
}

class _RenderSlidingBox extends RenderBox {
  _RenderSlidingBox(this._left, this._width, this._decoration, this._configuration);

  Animation<double> _left;

  /// [markNeedsPaint] as one closure, as [_RenderFollowingDecoratedBox] keeps its listener.
  late final VoidCallback _onStep = markNeedsPaint;

  set left(Animation<double> value) {
    if (identical(value, _left)) return;
    if (attached) {
      _left.removeListener(_onStep);
      value.addListener(_onStep);
    }
    _left = value;
    markNeedsPaint();
  }

  double _width;
  set width(double value) {
    if (value == _width) return;
    _width = value;
    markNeedsPaint();
  }

  Decoration _decoration;
  BoxPainter? _painter;
  set decoration(Decoration value) {
    if (value == _decoration) return;
    _painter?.dispose();
    _painter = null;
    _decoration = value;
    markNeedsPaint();
  }

  ImageConfiguration _configuration;
  set configuration(ImageConfiguration value) {
    if (value == _configuration) return;
    _configuration = value;
    markNeedsPaint();
  }

  @override
  bool get sizedByParent => true;

  @override
  Size computeDryLayout(BoxConstraints constraints) => constraints.biggest;

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    _left.addListener(_onStep);
  }

  @override
  void detach() {
    _left.removeListener(_onStep);
    _painter?.dispose();
    _painter = null;
    super.detach();
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    _painter ??= _decoration.createBoxPainter(markNeedsPaint);
    // Where a Stack would draw a child at this left, at the size a Container of this width gets.
    _painter!.paint(
      context.canvas,
      Offset(_left.value + offset.dx, offset.dy),
      _configuration.copyWith(size: Size(_width, size.height)),
    );
  }
}

/// A [Text] whose style animates the way an [AnimatedDefaultTextStyle] around it would.
///
/// It draws what `AnimatedDefaultTextStyle(style: style, child: Text(data, textAlign: textAlign))`
/// draws, with the style handed to the text itself: a [DefaultTextStyle] that changes notifies the
/// text below it in the middle of the build. A step of the animation does not rebuild the text: it
/// takes the step's style when it is drawn.
class AnimatedStyledText extends ImplicitlyAnimatedWidget {
  const AnimatedStyledText(
    this.data, {
    super.key,
    required this.style,
    this.textAlign,
    super.curve,
    required super.duration,
    super.onEnd,
  });

  final String data;
  final TextStyle style;
  final TextAlign? textAlign;

  @override
  ImplicitlyAnimatedWidgetState<AnimatedStyledText> createState() => _AnimatedStyledTextState();
}

class _AnimatedStyledTextState extends ImplicitlyAnimatedWidgetState<AnimatedStyledText> {
  TextStyleTween? _style;

  /// The text, kept while its animation, its curve and its arguments are: a new target only restarts
  /// the animation, which the text follows without being rebuilt.
  ColorCachedText? _text;
  Animation<double>? _curve;
  TextStyleTween? _tween;

  @override
  void forEachTween(TweenVisitor<dynamic> visitor) {
    _style = visitor(_style, widget.style, (dynamic value) => TextStyleTween(begin: value as TextStyle))
        as TextStyleTween?;
  }

  @override
  void didUpdateWidget(AnimatedStyledText oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.data != oldWidget.data || widget.textAlign != oldWidget.textAlign) _text = null;
  }

  @override
  Widget build(BuildContext context) {
    if (!identical(_curve, animation) || !identical(_tween, _style)) _text = null;
    _curve = animation;
    _tween = _style;
    // A DefaultTextStyle hands its style down whole, not merged into the ambient one, and with the
    // wrapping and overflow AnimatedDefaultTextStyle defaults to.
    return _text ??= ColorCachedText.animated(
      widget.data,
      animatedStyle: _Uninherited(_style!).animate(animation),
      textAlign: widget.textAlign,
      softWrap: true,
      overflow: TextOverflow.clip,
    );
  }
}

class _Uninherited extends Animatable<TextStyle> {
  _Uninherited(this.style);

  final TextStyleTween style;

  @override
  TextStyle transform(double t) => style.transform(t).copyWith(inherit: false);
}
