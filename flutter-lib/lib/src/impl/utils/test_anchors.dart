import 'dart:ui' as ui;

import 'package:flutter/foundation.dart' show mergeSort;
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

import '../../gen/widget.dart' show VWidget;

/// [child], tagged as [node] (`{swt}/{id}`) so a test can locate it, or as [id] for a part of
/// [node] drawn by its parent (a tab, a tool item's arrow). The child itself unless the E2E flag
/// turned [TestAnchors] on.
Widget tagSemantics(VWidget? node, Widget child, {String? id}) =>
    TestAnchors._wrap(id ?? '${node!.swt}/${node.id}', node, child);

/// Where each tagged widget is mounted, by its `{swt}/{id}` identifier: what a test locates a
/// widget by, read from the render tree rather than from the semantics tree. Off unless a test
/// harness turns it on, and then every tagged widget registers here as it mounts.
class TestAnchors {
  TestAnchors._();

  static bool enabled = false;

  static final Map<String, _TestAnchorState> _byIdentifier = {};

  /// The same anchors by kind, the identifier up to its last '/' ("Label/", "chevron:CTabFolder/"):
  /// a look-up by kind reads one of these, not the whole registry.
  static final Map<String, Set<_TestAnchorState>> _byKind = {};

  static String _kindOf(String identifier) => identifier.substring(0, identifier.lastIndexOf('/') + 1);

  static void _register(String identifier, _TestAnchorState anchor) {
    _byIdentifier[identifier] = anchor;
    (_byKind[_kindOf(identifier)] ??= <_TestAnchorState>{}).add(anchor);
  }

  static void _unregister(String identifier, _TestAnchorState anchor) {
    if (identical(_byIdentifier[identifier], anchor)) _byIdentifier.remove(identifier);
    final kind = _byKind[_kindOf(identifier)];
    kind?.remove(anchor);
    if (kind != null && kind.isEmpty) _byKind.remove(_kindOf(identifier));
  }

  /// The anchors whose identifier starts with [prefix]: one bucket when it names a kind.
  static Iterable<_TestAnchorState> _withPrefix(String prefix) {
    if (prefix.endsWith('/')) return _byKind[prefix] ?? const <_TestAnchorState>{};
    return _byIdentifier.entries.where((e) => e.key.startsWith(prefix)).map((e) => e.value);
  }

  static Widget _wrap(String identifier, VWidget? node, Widget child) => enabled
      ? _TestAnchor(identifier: identifier, node: node, child: child)
      : child;

  /// Where [identifier] is on the page (the part its ancestors' clips leave on screen) and, with
  /// [hit], whether a tap at its centre reaches it, or null when it is not laid out:
  /// `{x, y, width, height, paints}`, plus `hit`.
  ///
  /// `hit`: a tap at the centre lands on the widget, see [_reaches]. `paints`: it has a size, overlaps its view, and nothing above it is offstage or
  /// fully transparent.
  static Map<String, Object>? locate(String identifier, {bool hit = true}) {
    final anchor = _byIdentifier[identifier];
    final box = anchor?.context.findRenderObject();
    if (box is! RenderBox || !box.attached || !box.hasSize) return null;
    final origin = box.localToGlobal(Offset.zero);
    if (!origin.isFinite) return null;
    final size = box.size;
    final view = View.maybeOf(anchor!.context);
    var reached = false;
    var paints = false;
    var visible = Rect.zero;
    if (view != null && !size.isEmpty) {
      final viewRect =
          Offset.zero & (view.physicalSize / view.devicePixelRatio);
      visible = _visibleRect(box, viewRect);
      paints = _shown(box) && !visible.isEmpty;
      if (hit) {
        final result = HitTestResult();
        RendererBinding.instance.hitTestInView(
          result,
          (paints ? visible : origin & size).center,
          view.viewId,
        );
        reached = _reaches(result, box);
      }
    }
    // Where it is on screen, as the semantics tree gave a node's rectangle: what its ancestors' clips
    // leave of it; the whole box only when none of it shows.
    final shownRect = paints ? visible : origin & size;
    return {
      'x': shownRect.left,
      'y': shownRect.top,
      'width': shownRect.width,
      'height': shownRect.height,
      'paints': paints,
      if (hit) 'hit': reached,
    };
  }

  /// Every mounted widget whose identifier starts with one of [prefixes] (all of them when it is
  /// empty), inside the widget [within] when given, in reading order, with its text and where it is:
  /// `{id, text, bounds, paints}`. Those not laid out are left out. No hit test: [locate] makes it
  /// for the one widget a click aims at.
  ///
  /// With [paintedText], `text` is what the widget paints: its paragraphs' text in paint order,
  /// mnemonic markers gone, as the semantics DOM's text read. Otherwise it is the widget's own text.
  static List<Map<String, Object>> nodes(
    List<String> prefixes, {
    String? within,
    bool paintedText = false,
  }) {
    // One identifier asked for by itself is read from the registry, not found by a walk.
    if (within == null && prefixes.length == 1 && prefixes.first.contains(RegExp(r'/-?\d+$'))) {
      final anchor = _byIdentifier[prefixes.first];
      return [if (anchor != null) ?_describe(anchor, paintedText: paintedText)];
    }
    final Iterable<_TestAnchorState> candidates;
    if (within == null) {
      candidates = prefixes.isEmpty ? _byIdentifier.values : prefixes.expand(_withPrefix);
    } else {
      // Inside one widget: only its own subtree is walked.
      final root = _byIdentifier[within]?.context as Element?;
      if (root == null) return const [];
      final inside = <_TestAnchorState>[];
      void visit(Element element) {
        if (element is StatefulElement && element.state is _TestAnchorState) {
          inside.add(element.state as _TestAnchorState);
        }
        element.visitChildElements(visit);
      }

      root.visitChildElements(visit);
      candidates = inside.where((a) => prefixes.isEmpty || prefixes.any(a.widget.identifier.startsWith));
    }
    final listed = [
      for (final anchor in candidates)
        if (_describe(anchor, paintedText: paintedText) case final node?) node,
    ];
    // In reading order, top to bottom then left to right, as the semantics tree ordered siblings.
    double at(Map<String, Object> node, String key) => (node['bounds'] as Map<String, Object>)[key] as double;
    mergeSort(listed, compare: (a, b) {
      final dy = at(a, 'y').round().compareTo(at(b, 'y').round());
      return dy != 0 ? dy : at(a, 'x').round().compareTo(at(b, 'x').round());
    });
    return listed;
  }

  /// [anchor] as a listing gives it, or null when it is not laid out.
  static Map<String, Object>? _describe(_TestAnchorState anchor, {bool paintedText = false}) {
    final located = locate(anchor.widget.identifier, hit: false);
    if (located == null) return null;
    return {
      'id': anchor.widget.identifier,
      // A part with no state of its own (a MessageBox's button) answers what it paints.
      'text': paintedText || anchor.widget.node == null
          ? _paintedText(anchor)
          : anchor.widget.node?.semanticsText ?? '',
      'bounds': {
        'x': located['x']!,
        'y': located['y']!,
        'width': located['width']!,
        'height': located['height']!,
      },
      'paints': located['paints']!,
    };
  }

  /// Every paragraph laid out with [text] inside a widget whose identifier starts with [within]
  /// (a kind, "Table/"), in paint order: `{id, text, bounds, paints, hit, cut}`, `id` being
  /// `text:<n>` by position, `cut` whether the paragraph was elided. Only those widgets' subtrees
  /// are walked. [exact] matches the whole text; otherwise it is contained, case aside. Text drawn
  /// inline by its owner (a list's items, a menu's) is found too, with no anchor of its own.
  static List<Map<String, Object>> texts(String text, {required bool exact, required String within}) {
    final wanted = exact ? text.trim() : text.trim().toLowerCase();
    final found = <Map<String, Object>>[];
    final walked = <RenderObject>{};
    void visit(RenderObject node) {
      // A widget of the kind nested in another is walked once, with the outer one.
      if (!walked.add(node)) return;
      final String? text = node is RenderParagraph
          ? node.text.toPlainText(includeSemanticsLabels: false)
          : node is PaintedTextSource
              ? (node as PaintedTextSource).paintedText
              : null;
      if (text != null && node is RenderBox && node.attached && node.hasSize) {
        final painted = text.trim();
        final matches = exact ? painted == wanted : painted.toLowerCase().contains(wanted);
        if (matches) {
          final origin = node.localToGlobal(Offset.zero);
          final size = node.size;
          final view = _viewOf(node);
          var paints = false;
          var hit = false;
          if (origin.isFinite && view != null && !size.isEmpty) {
            final viewRect = Offset.zero & (view.physicalSize / view.devicePixelRatio);
            paints = _shown(node) && !_visibleRect(node, viewRect).isEmpty;
            final result = HitTestResult();
            RendererBinding.instance.hitTestInView(result, origin + size.center(Offset.zero), view.viewId);
            hit = _reaches(result, node);
          }
          found.add({
            'id': 'text:${found.length}',
            'text': painted,
            'bounds': {'x': origin.dx, 'y': origin.dy, 'width': size.width, 'height': size.height},
            'paints': paints,
            'hit': hit,
            'cut': node is RenderParagraph && node.didExceedMaxLines,
          });
        }
      }
      node.visitChildren(visit);
    }

    for (final anchor in _withPrefix(within)) {
      final root = anchor.context.findRenderObject();
      if (root != null && root.attached) visit(root);
    }
    return found;
  }

  static ui.FlutterView? _viewOf(RenderObject node) {
    RenderObject? at = node;
    while (at != null && at is! RenderView) {
      at = at.parent;
    }
    return at is RenderView ? at.flutterView : null;
  }

  /// The part of [box] left on screen, in global coordinates, once every ancestor's paint clip
  /// (a scrolled area, a clipped composite) and the view have cut it: what the semantics tree kept
  /// of a widget, which leaves out one clipped away.
  static Rect _visibleRect(RenderBox box, Rect viewRect) {
    var rect = Offset.zero & box.size;
    RenderObject child = box;
    // Up to the view's own child: the view's transform is the device pixel ratio, and [viewRect]
    // is in logical pixels.
    for (RenderObject? parent = box.parent; parent != null && parent is! RenderView; parent = parent.parent) {
      rect = MatrixUtils.transformRect(child.getTransformTo(parent), rect);
      // A scrolling viewport clips to its own bounds; the clip it describes for a sliver child is
      // in the sliver's scroll terms, not a rectangle in its own coordinates.
      final Rect? clip = parent is RenderViewportBase
          ? Offset.zero & parent.size
          : parent is RenderSliver
              ? null
              : parent.describeApproximatePaintClip(child);
      if (clip != null) {
        rect = rect.intersect(clip);
        if (rect.isEmpty) return Rect.zero;
      }
      child = parent;
    }
    return rect.intersect(viewRect);
  }

  /// Whether a tap whose hit test gave [result] lands on [target]: the deepest render object hit
  /// is [target], inside it, or one of its ancestors (a row taking the hit for its text). Anything
  /// else covers it.
  static bool _reaches(HitTestResult result, RenderObject target) {
    for (final entry in result.path) {
      final hit = entry.target;
      if (hit is! RenderObject) continue;
      return identical(hit, target) || _isAncestor(target, hit) || _isAncestor(hit, target);
    }
    return false;
  }

  static bool _isAncestor(RenderObject ancestor, RenderObject node) {
    for (RenderObject? at = node.parent; at != null; at = at.parent) {
      if (identical(at, ancestor)) return true;
    }
    return false;
  }

  /// The text [anchor]'s widget paints: its paragraphs in paint order, joined by spaces.
  static String _paintedText(_TestAnchorState anchor) {
    final box = anchor.context.findRenderObject();
    if (box == null) return '';
    final parts = <String>[];
    void visit(RenderObject node) {
      if (node is RenderParagraph) {
        final text = node.text.toPlainText(includeSemanticsLabels: false).trim();
        if (text.isNotEmpty) parts.add(text);
      } else if (node is PaintedTextSource) {
        final text = (node as PaintedTextSource).paintedText?.trim();
        if (text != null && text.isNotEmpty) parts.add(text);
      } else if (node is RenderCustomPaint && node.painter is PaintedTextSource) {
        final text = (node.painter as PaintedTextSource).paintedText;
        if (text != null && text.isNotEmpty) parts.add(text);
      }
      node.visitChildren(visit);
    }

    visit(box);
    return parts.join(' ');
  }

  /// Scrolls every scrollable around [identifier] so the widget comes into view, as a click by a
  /// user's pointer needs it to be. False when it is not mounted.
  static bool reveal(String identifier) {
    final anchor = _byIdentifier[identifier];
    if (anchor == null) return false;
    Scrollable.ensureVisible(anchor.context, alignment: 0.5);
    return true;
  }

  /// Nothing between [box] and the root keeps it from painting.
  static bool _shown(RenderObject box) {
    for (RenderObject? node = box; node != null; node = node.parent) {
      if (node is RenderOffstage && node.offstage) return false;
      if (node is RenderOpacity && node.opacity == 0) return false;
      if (node is RenderAnimatedOpacity && node.opacity.value == 0) {
        return false;
      }
    }
    return true;
  }
}

/// A painter or render object that can say what text it draws: a canvas drawn through a GC, or
/// text drawn without a paragraph, has none to read.
abstract interface class PaintedTextSource {
  String? get paintedText;
}

class _TestAnchor extends StatefulWidget {
  const _TestAnchor({
    required this.identifier,
    required this.node,
    required this.child,
  });

  final String identifier;
  final VWidget? node;
  final Widget child;

  @override
  State<_TestAnchor> createState() => _TestAnchorState();
}

class _TestAnchorState extends State<_TestAnchor> {
  @override
  void initState() {
    super.initState();
    TestAnchors._register(widget.identifier, this);
  }

  @override
  void didUpdateWidget(_TestAnchor oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.identifier != widget.identifier) {
      TestAnchors._unregister(oldWidget.identifier, this);
      TestAnchors._register(widget.identifier, this);
    }
  }

  @override
  void dispose() {
    TestAnchors._unregister(widget.identifier, this);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => widget.child;
}
