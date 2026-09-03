// A circular-arrow icon is an arc plus a head drawn as a separate corner path: the corner sits on
// the arc's end, one arm carries on around the circle and the other turns inward. The corner only
// reads as an arrowhead when the arm that carries on around the circle runs into the circle's
// **gap**. When it is drawn on the arc's own side instead it disappears under the arc, and all that
// is left on screen is the inward stub -- a hook curling into the circle rather than an arrow.
//
// So the head's two free ends have to end up clear of the arc's ink. The corner itself does not:
// that is where the head attaches.

import 'dart:math' as math;

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

/// Icons built as one stroked arc plus a corner head. The last three already read correctly and
/// are here as controls: the rule has to describe the construction, not the icons being fixed.
const _family = <String>[
  'restore',
  'reset_proto',
  'revert_edit',
  'history',
  'history_nav',
  'history_list',
  'fastview_restore',
  'last_edit_pos',
  'dep_loop',
  'recursive_co',
  'restart_co',
  'runlast_co',
  'next_edit_pos',
  'refresh',
  'refresh_remote',
  'update_ans',
];

const double _margin = 0.25;

/// A stroked shape, as a densely sampled centreline plus the half-width the stroke adds around it.
class _Ink {
  _Ink(this.corners, this.halfWidth)
      : points = corners.expand(_densify).toList(growable: false);

  final List<List<Offset>> corners;
  final double halfWidth;
  final List<Offset> points;

  /// Gap between this shape's ink and [other]'s ink. Negative when they overlap.
  double gapTo(_Ink other) => points
          .map((p) => other.points.map((q) => (p - q).distance).reduce(math.min))
          .reduce(math.min) -
      halfWidth -
      other.halfWidth;

  double distanceFrom(Offset p) =>
      points.map((q) => (p - q).distance).reduce(math.min);
}

List<Offset> _densify(List<Offset> vertices) {
  if (vertices.length < 2) return vertices;
  final out = <Offset>[vertices.first];
  for (var i = 1; i < vertices.length; i++) {
    final steps = math.max(1, ((vertices[i] - vertices[i - 1]).distance / 0.1).ceil());
    for (var s = 1; s <= steps; s++) {
      out.add(Offset.lerp(vertices[i - 1], vertices[i], s / steps)!);
    }
  }
  return out;
}

/// Samples an SVG elliptical arc, via the endpoint-to-centre conversion in the SVG spec.
List<Offset> _arcPoints(Offset from, Offset to, double rx, double ry, bool largeArc, bool sweep) {
  final dx2 = (from.dx - to.dx) / 2, dy2 = (from.dy - to.dy) / 2;
  final lambda = dx2 * dx2 / (rx * rx) + dy2 * dy2 / (ry * ry);
  if (lambda > 1) {
    rx *= math.sqrt(lambda);
    ry *= math.sqrt(lambda);
  }
  final numerator = rx * rx * ry * ry - rx * rx * dy2 * dy2 - ry * ry * dx2 * dx2;
  final denominator = rx * rx * dy2 * dy2 + ry * ry * dx2 * dx2;
  final factor = (largeArc == sweep ? -1 : 1) * math.sqrt(math.max(0, numerator / denominator));
  final cx2 = factor * rx * dy2 / ry, cy2 = -factor * ry * dx2 / rx;
  final centre = Offset(cx2 + (from.dx + to.dx) / 2, cy2 + (from.dy + to.dy) / 2);

  double angle(double ux, double uy) => math.atan2(uy, ux);
  final start = angle((dx2 - cx2) / rx, (dy2 - cy2) / ry);
  var sweepAngle = angle((-dx2 - cx2) / rx, (-dy2 - cy2) / ry) - start;
  if (!sweep && sweepAngle > 0) sweepAngle -= 2 * math.pi;
  if (sweep && sweepAngle < 0) sweepAngle += 2 * math.pi;

  final steps = math.max(24, (sweepAngle.abs() * math.max(rx, ry) / 0.1).ceil());
  return [
    for (var i = 0; i <= steps; i++)
      Offset(centre.dx + rx * math.cos(start + sweepAngle * i / steps),
          centre.dy + ry * math.sin(start + sweepAngle * i / steps))
  ];
}

/// Reads a path's `d`. Arc flags may be written without separators ("a8 8 0 116-13.5"), so the two
/// flag characters are consumed one at a time rather than as numbers.
({List<List<Offset>> subpaths, bool hasArc}) _parsePath(String d) {
  final subpaths = <List<Offset>>[];
  var current = <Offset>[];
  var cursor = Offset.zero;
  var hasArc = false;
  final number = RegExp(r'\s*,?\s*(-?\d*\.?\d+)');

  for (final token in RegExp(r'[A-Za-z][^A-Za-z]*').allMatches(d)) {
    final command = token.group(0)![0];
    var rest = token.group(0)!.substring(1);
    double next() {
      final m = number.matchAsPrefix(rest)!;
      rest = rest.substring(m.end);
      return double.parse(m.group(1)!);
    }

    bool flag() {
      rest = rest.replaceFirst(RegExp(r'^[\s,]*'), '');
      final f = rest[0] == '1';
      rest = rest.substring(1);
      return f;
    }

    bool more() => number.matchAsPrefix(rest) != null;
    final relative = command.toLowerCase() == command;

    while (more()) {
      switch (command.toUpperCase()) {
        case 'M':
          final p = Offset(next(), next());
          cursor = relative ? cursor + p : p;
          if (current.length > 1) subpaths.add(current);
          current = <Offset>[cursor];
          while (more()) {
            final q = Offset(next(), next());
            cursor = relative ? cursor + q : q;
            current.add(cursor);
          }
        case 'L':
          final p = Offset(next(), next());
          cursor = relative ? cursor + p : p;
          current.add(cursor);
        case 'H':
          final v = next();
          cursor = Offset(relative ? cursor.dx + v : v, cursor.dy);
          current.add(cursor);
        case 'V':
          final v = next();
          cursor = Offset(cursor.dx, relative ? cursor.dy + v : v);
          current.add(cursor);
        case 'A':
          hasArc = true;
          final rx = next(), ry = next();
          next(); // x-axis rotation; every asset here uses 0
          final largeArc = flag(), sweep = flag();
          final p = Offset(next(), next());
          final end = relative ? cursor + p : p;
          current.addAll(_arcPoints(cursor, end, rx, ry, largeArc, sweep).skip(1));
          cursor = end;
        case 'Z':
          break;
        default:
          throw UnsupportedError('path command "$command" in "$d"');
      }
      if (command.toUpperCase() == 'Z') break;
    }
  }
  if (current.length > 1) subpaths.add(current);
  return (subpaths: subpaths, hasArc: hasArc);
}

double? _attribute(String element, String name) {
  final match = RegExp('$name="([^"]*)"').firstMatch(element);
  return match == null ? null : double.tryParse(match.group(1)!);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final icon in _family) {
    test('$icon draws an arrowhead, not a hook lying on the arc', () async {
      final svg = await rootBundle.loadString('assets/icons/$icon.svg');

      _Ink? arc;
      final corners = <_Ink>[];
      for (final element in RegExp(r'<path\b[^>]*>').allMatches(svg)) {
        final source = element.group(0)!;
        final strokeWidth = _attribute(source, 'stroke-width');
        if (strokeWidth == null) continue; // a filled shape, not a stroked one
        final parsed = _parsePath(RegExp(r'd="([^"]*)"').firstMatch(source)!.group(1)!);
        final ink = _Ink(parsed.subpaths, strokeWidth / 2);
        if (parsed.hasArc) {
          arc ??= ink; // the outer circle: the first arc the file draws
        } else if (parsed.subpaths.length == 1 && parsed.subpaths.single.length == 3) {
          corners.add(ink);
        }
      }

      expect(arc, isNotNull, reason: '$icon draws no arc');
      // A three-point corner near the arc's ink is a head -- on the correct construction it sits
      // just past the arc's end, in the gap. One drawn well away from the circle is decoration (a
      // clock hand, a caret) and says nothing about the arrow.
      final heads = corners.where((c) => arc!.distanceFrom(c.corners.single[1]) < 3).toList();
      expect(heads, isNotEmpty, reason: '$icon has no head attached to its arc');

      for (final head in heads) {
        for (final arm in [head.corners.single.first, head.corners.single.last]) {
          expect(
            arc!.distanceFrom(arm) - arc.halfWidth - head.halfWidth,
            greaterThan(_margin),
            reason: '$icon draws a head arm on top of its arc, where it cannot be seen',
          );
        }
      }
    });
  }
}
