// Collapse All and Expand All are drawn as two stacked chevrons. At toolbar size they only read
// as chevrons while the two strokes stay apart: chevrons whose apexes meet draw an x, which sits
// next to a view's close button. Collapse points both chevrons towards the middle, expand points
// them away from it.

import 'dart:math' as math;

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'support/svg_path.dart';

const _collapse = <String>['collapseall', 'collapse_all_mini'];
const _expand = <String>['expandall'];

/// Ink is allowed this close to the other chevron before the two read as one shape.
const double _margin = 0.5;

double _segmentDistance(Offset a, Offset b, Offset c, Offset d) => [
      pointToSegment(a, c, d),
      pointToSegment(b, c, d),
      pointToSegment(c, a, b),
      pointToSegment(d, a, b),
    ].reduce(math.min);

bool _segmentsCross(Offset a, Offset b, Offset c, Offset d) {
  double cross(Offset o, Offset p, Offset q) =>
      (p.dx - o.dx) * (q.dy - o.dy) - (p.dy - o.dy) * (q.dx - o.dx);
  return cross(a, b, c) * cross(a, b, d) < 0 && cross(c, d, a) * cross(c, d, b) < 0;
}

/// Shortest distance between the centrelines of two polylines; zero where they cross.
double _polylineDistance(List<Offset> p, List<Offset> q) {
  var best = double.infinity;
  for (var i = 1; i < p.length; i++) {
    for (var j = 1; j < q.length; j++) {
      if (_segmentsCross(p[i - 1], p[i], q[j - 1], q[j])) return 0;
      best = math.min(best, _segmentDistance(p[i - 1], p[i], q[j - 1], q[j]));
    }
  }
  return best;
}

class _Chevrons {
  _Chevrons(this.top, this.bottom, this.strokeWidth);

  final List<Offset> top;
  final List<Offset> bottom;
  final double strokeWidth;

  /// Gap between the two strokes' ink.
  double get clearance => _polylineDistance(top, bottom) - strokeWidth;

  /// Positive when the chevron's apex points down, negative when it points up.
  static double _direction(List<Offset> c) => c[1].dy - (c.first.dy + c.last.dy) / 2;

  bool get pointsInward => _direction(top) > 0 && _direction(bottom) < 0;
  bool get pointsOutward => _direction(top) < 0 && _direction(bottom) > 0;
}

Future<_Chevrons> _load(String icon) async {
  final svg = await rootBundle.loadString('assets/icons/$icon.svg');
  final path = RegExp(r'<path\b[^>]*>').firstMatch(svg)!.group(0)!;
  final subpaths = parseSvgPath(RegExp(r'\bd="([^"]*)"').firstMatch(path)!.group(1)!).subpaths;
  final strokeWidth = double.parse(RegExp(r'stroke-width="([^"]*)"').firstMatch(path)!.group(1)!);
  expect(subpaths, hasLength(2), reason: '$icon is two chevrons');
  for (final c in subpaths) {
    expect(c, hasLength(3), reason: '$icon: a chevron is two strokes');
  }
  subpaths.sort((a, b) => (a.first.dy + a.last.dy).compareTo(b.first.dy + b.last.dy));
  return _Chevrons(subpaths[0], subpaths[1], strokeWidth);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final icon in [..._collapse, ..._expand]) {
    test('$icon keeps its two chevrons apart', () async {
      final chevrons = await _load(icon);
      expect(chevrons.clearance, greaterThanOrEqualTo(_margin),
          reason: '$icon: chevrons that touch draw an x');
    });
  }

  for (final icon in _collapse) {
    test('$icon points both chevrons towards the middle', () async {
      expect((await _load(icon)).pointsInward, isTrue);
    });
  }

  for (final icon in _expand) {
    test('$icon points both chevrons away from the middle', () async {
      expect((await _load(icon)).pointsOutward, isTrue);
    });
  }
}
