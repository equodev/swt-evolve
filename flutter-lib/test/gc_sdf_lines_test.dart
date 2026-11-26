// Where Impeller draws, a line in a run is shaded from a texture of what Impeller's SDF shader lays
// on a line drawn on its own. Rasterized here as a multisampling GPU does, the run's line paints what
// that shader does, to within four levels: the texture keeps eight bits, 64 luma steps and 16 texels
// a pixel.

import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Impeller's uber_sdf.frag for a line, in device pixels: the rectangle reaching [hl] along and [hw]
/// across from its centre, at least half a pixel each way, its edge faded by smoothstep and the
/// coverage bent towards the colour's [luma].
double _shader(double along, double across, double hl, double hw, double luma) {
  final dx = along.abs() - math.max(hl, 0.5), dy = across.abs() - math.max(hw, 0.5);
  final sdf = math.sqrt(math.pow(math.max(dx, 0.0), 2) + math.pow(math.max(dy, 0.0), 2)) +
      math.min(math.max(dx, dy), 0.0);
  final t = (sdf + 0.5).clamp(0.0, 1.0);
  final c = 1 - t * t * (3 - 2 * t);
  final dark = 1 - math.sqrt(1 - c), light = math.sqrt(c);
  return dark + (light - dark) * luma;
}

double _luma(Color c) => 0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b;

/// The atlas sampled bilinearly at texel coordinates ([u], [v]), clamped at its edges: alpha 0..1.
double _sample(({Uint8List pixels, int width, int height}) atlas, double u, double v) {
  final x = u - 0.5, y = v - 0.5;
  final x0 = x.floor(), y0 = y.floor();
  final fx = x - x0, fy = y - y0;
  double at(int i, int j) {
    i = i.clamp(0, atlas.width - 1);
    j = j.clamp(0, atlas.height - 1);
    return atlas.pixels[(j * atlas.width + i) * 4 + 3] / 255;
  }

  return (at(x0, y0) * (1 - fx) + at(x0 + 1, y0) * fx) * (1 - fy) +
      (at(x0, y0 + 1) * (1 - fx) + at(x0 + 1, y0 + 1) * fx) * fy;
}

/// Metal's four sample positions in a pixel.
const _samples = [(0.375, 0.125), (0.875, 0.375), (0.125, 0.625), (0.625, 0.875)];

/// The barycentric weights of ([x], [y]) in the triangle [t] of [mesh].
(double, double, double) _weights(Float32List p, int a, int b, int c, double x, double y) {
  final ax = p[a * 2], ay = p[a * 2 + 1], bx = p[b * 2], by = p[b * 2 + 1], cx = p[c * 2], cy = p[c * 2 + 1];
  final d = (by - cy) * (ax - cx) + (cx - bx) * (ay - cy);
  if (d.abs() < 1e-12) return (-1, -1, -1);
  final l1 = ((by - cy) * (x - cx) + (cx - bx) * (y - cy)) / d;
  final l2 = ((cy - ay) * (x - cx) + (ax - cx) * (y - cy)) / d;
  return (l1, l2, 1 - l1 - l2);
}

void main() {
  final atlas = sdfLineAtlas();

  test('the texture holds what the shader lays on a line', () {
    final r = math.Random(3);
    var worst = 0.0;
    for (var k = 0; k < 4000; k++) {
      final along = r.nextDouble() * 6 - 3, across = r.nextDouble() * 6 - 3;
      final hw = r.nextBool() ? r.nextDouble() * 0.5 : 1 + r.nextDouble() * 2, hl = 1 + r.nextDouble() * 4;
      final color = Color(0xFF000000 | r.nextInt(0xFFFFFF));
      final mesh = sdfLineMesh(-hl, 0, hl, 0, 2 * hw, SWT.CAP_FLAT, color, 1)!;
      for (var t = 0; t < mesh.indices.length; t += 3) {
        final a = mesh.indices[t], b = mesh.indices[t + 1], c = mesh.indices[t + 2];
        final (l1, l2, l3) = _weights(mesh.positions, a, b, c, along, across);
        if (l1 < 0 || l2 < 0 || l3 < 0) continue;
        final u = l1 * mesh.textures[a * 2] + l2 * mesh.textures[b * 2] + l3 * mesh.textures[c * 2];
        final v = l1 * mesh.textures[a * 2 + 1] + l2 * mesh.textures[b * 2 + 1] + l3 * mesh.textures[c * 2 + 1];
        final got = _sample(atlas, u, v);
        worst = math.max(worst, (got - _shader(along, across, hl, math.max(hw, 0.5), _luma(color))).abs() * 255);
        break;
      }
    }
    expect(worst, lessThan(4), reason: 'worst difference in levels');
  });

  test('a line between one and two device pixels wide is left to be drawn on its own', () {
    expect(sdfLineMesh(0, 0, 20, 10, 1.5, SWT.CAP_FLAT, const Color(0xFF000000), 1), isNull);
    expect(sdfLineMesh(0, 0, 20, 10, 0.75, SWT.CAP_FLAT, const Color(0xFF000000), 0.5), isNull);
    expect(sdfLineMesh(0, 0, 1.5, 0, 1, SWT.CAP_FLAT, const Color(0xFF000000), 1), isNull, reason: 'too short');
  });

  // Every sample a triangle covers takes what that triangle gives at the pixel's centre, inside the
  // triangle or not, as a multisampled GPU shades it; samples no triangle covers keep the canvas.
  for (final px in [1.0, 0.5]) {
    for (final width in [0.0, 0.5, 1.0, 2.0, 3.0, 8.0]) {
      for (final cap in [SWT.CAP_FLAT, SWT.CAP_SQUARE]) {
        test('a line $width wide at $px units a pixel, cap $cap, paints what the shader does', () {
          final r = math.Random(width.hashCode ^ cap);
          var worst = 0.0;
          for (var k = 0; k < 6; k++) {
            final angle = r.nextDouble() * math.pi, length = 3 + r.nextDouble() * 20;
            final x1 = 15 + r.nextDouble(), y1 = 15 + r.nextDouble();
            final x2 = x1 + math.cos(angle) * length, y2 = y1 + math.sin(angle) * length;
            final color = Color(0xFF000000 | r.nextInt(0xFFFFFF));
            final mesh = sdfLineMesh(x1, y1, x2, y2, width, cap, color, px)!;
            final ux = (x2 - x1) / length, uy = (y2 - y1) / length;
            final w = width / 2, l = length / 2 + (cap == SWT.CAP_FLAT ? 0 : w);
            final hw = math.max(w, px / 2) / px, hl = math.max(l, px / 2) / px;
            final scale = width == 0 ? 1.0 : (w * l) / (hw * px * hl * px);
            final mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
            final meshAlpha = ((mesh.colors[0] >> 24) & 0xFF) / 255;
            var left = double.infinity, top = double.infinity, right = -left, bottom = -top;
            for (var i = 0; i < mesh.positions.length; i += 2) {
              left = math.min(left, mesh.positions[i]);
              right = math.max(right, mesh.positions[i]);
              top = math.min(top, mesh.positions[i + 1]);
              bottom = math.max(bottom, mesh.positions[i + 1]);
            }
            for (var py = (top / px).floor() - 2; py < bottom / px + 2; py++) {
              for (var qx = (left / px).floor() - 2; qx < right / px + 2; qx++) {
                final cx = (qx + 0.5) * px, cy = (py + 0.5) * px;
                var sum = 0.0;
                for (final (sx, sy) in _samples) {
                  final x = (qx + sx) * px, y = (py + sy) * px;
                  for (var t = 0; t < mesh.indices.length; t += 3) {
                    final a = mesh.indices[t], b = mesh.indices[t + 1], c = mesh.indices[t + 2];
                    final (l1, l2, l3) = _weights(mesh.positions, a, b, c, x, y);
                    if (l1 < -1e-9 || l2 < -1e-9 || l3 < -1e-9) continue;
                    final (m1, m2, m3) = _weights(mesh.positions, a, b, c, cx, cy);
                    final u = m1 * mesh.textures[a * 2] + m2 * mesh.textures[b * 2] + m3 * mesh.textures[c * 2];
                    final v = m1 * mesh.textures[a * 2 + 1] + m2 * mesh.textures[b * 2 + 1] +
                        m3 * mesh.textures[c * 2 + 1];
                    sum += meshAlpha * _sample(atlas, u, v);
                    break;
                  }
                }
                final along = ((cx - mx) * ux + (cy - my) * uy) / px;
                final across = (-(cx - mx) * uy + (cy - my) * ux) / px;
                final want = scale * _shader(along, across, hl, hw, _luma(color));
                worst = math.max(worst, (sum / _samples.length - want).abs() * 255);
              }
            }
          }
          expect(worst, lessThan(4), reason: 'worst difference in levels');
        });
      }
    }
  }
}
