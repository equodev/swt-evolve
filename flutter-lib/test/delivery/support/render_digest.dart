/// A canonical form for what is actually on screen.
///
/// Convergence of value objects is not the property the user cares about: the client can hold the
/// right state and still render the wrong tree, because the widget that owns it never rebuilt. The
/// delivery suite therefore needs a second oracle, over the render tree rather than over the data.
///
/// The digest is compared **A against B inside one test run** - "the tree after applying diffs"
/// against "the tree built fresh from the merged state" - never against a checked-in baseline. That
/// choice is what makes a dump this detailed usable: it only has to be *stable within a process*,
/// so it can stay maximally sensitive without anyone having to maintain golden files, and an
/// unrelated Flutter upgrade cannot turn the suite red.
///
/// Sensitivity is the whole point. A digest that ignores geometry, order or nesting would pass a
/// tree that renders the right words in the wrong places - which is precisely the failure a
/// text-only comparison misses.
library;

import 'package:flutter/rendering.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';

/// Identity that varies per run and says nothing about what was rendered: Flutter's short object
/// hashes (`RenderParagraph#a1b2c`, `[GlobalKey#3f9e1]`).
final _hash = RegExp(r'#[0-9a-f]{5}');

/// Semantics node ids are an allocation counter, so the same tree reached by a rebuild and by a
/// fresh mount numbers its nodes differently. Matched by name rather than by stripping every
/// `#<digits>`: a label whose text happens to contain `#42` is content, and blanket-stripping it
/// would make the digest blind to that text changing.
final _semanticsId = RegExp(r'SemanticsNode#\d+');

/// Pipeline bookkeeping that depends on *when* the dump was taken rather than on what is on screen.
/// Two trees that render identically can disagree on these purely because one was reached by a
/// rebuild and the other by a fresh mount - exactly the two paths a convergence probe compares.
final _transient = RegExp(
    r'\s+(NEEDS-LAYOUT|NEEDS-PAINT|NEEDS-COMPOSITING-BITS-UPDATE|DETACHED|DISPOSED)');

String _normalize(String dump) => dump
    .replaceAll(_semanticsId, 'SemanticsNode#')
    .replaceAll(_hash, '#')
    .replaceAll(_transient, '');

/// The rendered subtree under [finder] - structure, geometry and paint properties - reduced to a
/// comparable string.
String renderDigest(WidgetTester tester, Finder finder) {
  final RenderObject render = tester.renderObject(finder);
  return _normalize(render.toStringDeep());
}

/// [renderDigest] once the tree has stopped changing shape.
///
/// `pumpAndSettle` is not enough on its own. Parts of this UI attach *after* the first settled
/// frame - a control's GC overlay swaps from `Offstage` to a painted `IgnorePointer` once its
/// channel hands it a value, which arrives on a microtask. So a tree that was just mounted and one
/// that has been alive for a while can render the same state through a different structure.
///
/// That difference is not a delivery bug, but it is exactly the shape a round-trip probe compares:
/// "updated in place" against "built fresh". Taking the digest at a fixpoint - pump until two
/// consecutive digests agree - puts both sides in the same lifecycle state, so a reported
/// difference is about state rather than about how recently the tree was built.
///
/// Throws rather than returning an unsettled digest: a probe that silently compared moving trees
/// would be flaky in one direction and falsely green in the other.
Future<String> stableRenderDigest(WidgetTester tester, Finder finder,
    {int maxRounds = 10}) async {
  String previous = renderDigest(tester, finder);
  for (int round = 0; round < maxRounds; round++) {
    await tester.pumpAndSettle();
    final String current = renderDigest(tester, finder);
    if (current == previous) return current;
    previous = current;
  }
  throw StateError('render tree still changing after $maxRounds settle rounds; '
      'a digest taken here would compare two moving targets');
}

/// The digest of everything currently mounted.
String renderDigestAll(WidgetTester tester) {
  final RenderObject render = tester.binding.rootElement!.findRenderObject()!;
  return _normalize(render.toStringDeep());
}

/// The semantics tree, for the properties the render tree does not carry in a comparable form -
/// notably `enabled`, labels and the `flt-semantics-identifier` each widget is tagged with.
///
/// Separate from [renderDigest] and off by default because building semantics changes behaviour:
/// the widget base skips a rebuild when an incoming value is identical only while semantics are on.
/// A probe that needs this must opt in, and know it is measuring that mode.
String semanticsDigest(WidgetTester tester) {
  final SemanticsNode? root =
      tester.binding.pipelineOwner.semanticsOwner?.rootSemanticsNode;
  if (root == null) {
    throw StateError('no semantics tree - call tester.ensureSemantics() first');
  }
  return _normalize(root.toStringDeep());
}
