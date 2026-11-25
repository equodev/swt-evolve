// The magnifier icons are composites: one shared base glyph -- a lens and a diagonal handle --
// with a badge on top of it that says which action the icon stands for. A badge only reads as its
// own mark where the base leaves the box empty, so it has to clear the handle band, and it has to
// sit either wholly inside the lens or wholly outside it -- a shape that crosses the rim reads as
// part of the lens. A badge placed on the handle's line y = x is bisected by it.
//
// A direction badge carries one more requirement: a bare segment points nowhere, so next and
// previous need an arrowhead to be told apart at all.

import 'dart:math' as math;

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'support/svg_path.dart';

/// The whole family, including the three icons whose badge already sat clear.
const _family = <String>[
  'search_rem',
  'search_next',
  'search_prev',
  'search_goto',
  'search_remall',
  'search_history',
];

/// Icons whose badge has to point somewhere.
const _directionIcons = <String>['search_next', 'search_prev'];

/// Ink is allowed this close to another shape before the two read as one.
const double _margin = 0.25;

const double _viewBox = 24;

/// A stroked shape, as its centreline plus the half-width the stroke adds around it. Every
/// asset in the family strokes with a round cap and join, so the ink is exactly the set of
/// points within [halfWidth] of the centreline -- caps included.
class _Ink {
  _Ink(this.subpaths, this.halfWidth)
      : points = subpaths.expand(densify).toList(growable: false);

  /// One polyline of corner points per subpath.
  final List<List<Offset>> subpaths;
  final double halfWidth;

  /// The centreline, sampled finely enough to measure distances off.
  final List<Offset> points;

  double distanceTo(Offset a, Offset b) =>
      points.map((p) => pointToSegment(p, a, b)).reduce(math.min) - halfWidth;

  double get minRadius =>
      points.map((p) => (p - _lensCentre).distance).reduce(math.min) - halfWidth;

  double get maxRadius =>
      points.map((p) => (p - _lensCentre).distance).reduce(math.max) + halfWidth;

  /// True when a subpath turns -- three corners that are not in a line, i.e. an arrowhead.
  bool get hasCorner => subpaths.any((p) {
        for (var i = 2; i < p.length; i++) {
          final u = p[i - 1] - p[i - 2];
          final v = p[i] - p[i - 1];
          if ((u.dx * v.dy - u.dy * v.dx).abs() > 0.01) return true;
        }
        return false;
      });
}

const _lensCentre = Offset(9, 9);
const _lensRadius = 5.0;
const _handleStart = Offset(13, 13);
const _handleEnd = Offset(20, 20);

double _attribute(String element, String name) {
  final match = RegExp('$name="([^"]*)"').firstMatch(element);
  expect(match, isNotNull, reason: 'no $name on $element');
  return double.parse(match!.group(1)!);
}

/// Every stroked shape in the document, in source order.
List<_Ink> _shapes(String svg) {
  final shapes = <_Ink>[];
  for (final element in RegExp(r'<(circle|path)\b[^>]*>').allMatches(svg)) {
    final source = element.group(0)!;
    final halfWidth = _attribute(source, 'stroke-width') / 2;
    if (element.group(1) == 'circle') {
      final centre = Offset(_attribute(source, 'cx'), _attribute(source, 'cy'));
      final radius = _attribute(source, 'r');
      shapes.add(_Ink([
        [
          for (var i = 0; i <= 720; i++)
            centre + Offset(math.cos(i * math.pi / 360), math.sin(i * math.pi / 360)) * radius
        ]
      ], halfWidth));
    } else {
      final d = RegExp(r'd="([^"]*)"').firstMatch(source)!.group(1)!;
      shapes.add(_Ink(parseSvgPath(d).subpaths, halfWidth));
    }
  }
  return shapes;
}

bool _isLens(_Ink shape) =>
    shape.subpaths.length == 1 &&
    (shape.minRadius - (_lensRadius - shape.halfWidth)).abs() < 0.01 &&
    (shape.maxRadius - (_lensRadius + shape.halfWidth)).abs() < 0.01;

bool _isHandle(_Ink shape) =>
    shape.subpaths.length == 1 &&
    (shape.subpaths.single.first - _handleStart).distance < 0.01 &&
    (shape.subpaths.single.last - _handleEnd).distance < 0.01;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final icon in _family) {
    test('$icon draws its badge clear of the magnifier underneath it', () async {
      final shapes = _shapes(await rootBundle.loadString('assets/icons/$icon.svg'));

      expect(shapes.where(_isLens), hasLength(1), reason: '$icon has no magnifier lens');
      expect(shapes.where(_isHandle), hasLength(1), reason: '$icon has no magnifier handle');
      final lens = shapes.firstWhere(_isLens);
      final handle = shapes.firstWhere(_isHandle);
      final badge = shapes.where((s) => !_isLens(s) && !_isHandle(s)).toList();
      expect(badge, isNotEmpty, reason: '$icon draws nothing but the bare magnifier');

      for (final shape in badge) {
        expect(
          shape.distanceTo(_handleStart, _handleEnd) - handle.halfWidth,
          greaterThan(_margin),
          reason: '$icon draws its badge on the magnifier handle',
        );

        final insideLens = shape.maxRadius < _lensRadius - lens.halfWidth - _margin;
        final outsideLens = shape.minRadius > _lensRadius + lens.halfWidth + _margin;
        expect(insideLens || outsideLens, isTrue,
            reason: '$icon draws its badge across the lens rim');

        for (final p in shape.points) {
          expect(p.dx - shape.halfWidth, greaterThanOrEqualTo(0), reason: '$icon leaves the viewBox');
          expect(p.dy - shape.halfWidth, greaterThanOrEqualTo(0), reason: '$icon leaves the viewBox');
          expect(p.dx + shape.halfWidth, lessThanOrEqualTo(_viewBox), reason: '$icon leaves the viewBox');
          expect(p.dy + shape.halfWidth, lessThanOrEqualTo(_viewBox), reason: '$icon leaves the viewBox');
        }
      }
    });
  }

  for (final icon in _directionIcons) {
    test('$icon marks a direction with an arrowhead', () async {
      final shapes = _shapes(await rootBundle.loadString('assets/icons/$icon.svg'));
      final badge = shapes.where((s) => !_isLens(s) && !_isHandle(s));

      expect(badge.any((s) => s.hasCorner), isTrue,
          reason: '$icon marks its direction with a bare segment, which points nowhere');
    });
  }
}
