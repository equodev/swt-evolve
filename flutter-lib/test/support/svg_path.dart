// SVG path geometry for the icon tests that measure an asset's strokes.

import 'dart:math' as math;
import 'dart:ui';

/// Reads a path's `d` into one polyline per subpath; arcs are sampled. Supports `M L H V A Z`,
/// absolute and relative; anything else throws, so an asset the caller cannot read fails its test
/// instead of passing it by default. Arc flags may be written without separators
/// ("a8 8 0 116-13.5"), so the two flag characters are consumed one at a time.
({List<List<Offset>> subpaths, bool hasArc}) parseSvgPath(String d) {
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

/// Distance from [p] to the segment [a]-[b].
double pointToSegment(Offset p, Offset a, Offset b) {
  final ab = b - a;
  final lengthSquared = ab.dx * ab.dx + ab.dy * ab.dy;
  if (lengthSquared == 0) return (p - a).distance;
  final t = (((p - a).dx * ab.dx + (p - a).dy * ab.dy) / lengthSquared).clamp(0.0, 1.0);
  return (p - (a + ab * t)).distance;
}

/// Samples a polyline every 0.1 units, so a nearest-point search over the samples measures ink.
List<Offset> densify(List<Offset> vertices) {
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
      Offset(
        centre.dx + rx * math.cos(start + sweepAngle * i / steps),
        centre.dy + ry * math.sin(start + sweepAngle * i / steps),
      ),
  ];
}
