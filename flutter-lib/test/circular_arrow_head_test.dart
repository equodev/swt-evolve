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

import 'support/svg_path.dart';

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
      : points = corners.expand(densify).toList(growable: false);

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
        final parsed = parseSvgPath(RegExp(r'd="([^"]*)"').firstMatch(source)!.group(1)!);
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
