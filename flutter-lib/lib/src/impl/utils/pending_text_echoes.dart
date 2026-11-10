/// Echo suppression shared by the editable-text widgets (Text, CCombo, StyledText).
///
/// These widgets apply edits optimistically on the client and forward each change to Java
/// as an async Modify. Java updates its model and pushes a full-state snapshot back — but
/// that snapshot can land after the user has typed further, still carrying the older text.
/// Adopting it would drop the just-typed characters (a dropped keystroke, seen on slower
/// machines where the round-trip interleaves with typing).
///
/// Each text forwarded to Java is remembered here. When an inbound value matches one that
/// is still in flight it is recognised as our own stale echo and ignored; a value we never
/// sent is treated as a genuine external change (e.g. a programmatic setText, which SWT
/// applies even mid-edit) and is allowed through. The queue is self-draining — a matched
/// echo removes itself and any older superseded entries — so it only ever holds the handful
/// of un-acknowledged keystrokes, never the document history.
mixin PendingTextEchoes {
  final List<String> _pendingEchoes = [];

  /// Record a text value forwarded to Java (a Modify) so its later echo can be recognised.
  void recordSentText(String text) => _pendingEchoes.add(text);

  /// Seed a baseline value — e.g. the text as it stood when an edit session began — so a
  /// stale snapshot carrying it (the one value not covered by [recordSentText]) is also
  /// recognised as an echo. Resets any prior in-flight entries.
  void seedTextEchoBaseline(String text) => _pendingEchoes
    ..clear()
    ..add(text);

  /// Forget all in-flight echoes, e.g. when an edit session ends and Java is authoritative
  /// again.
  void clearSentTextEchoes() => _pendingEchoes.clear();

  /// Whether [incoming] (a text Java just pushed) is a stale echo of our own in-flight edit
  /// that should be ignored: true only when it is a recognised echo *and* the client has
  /// since moved on to [currentLocal]. Returns false for any value we never sent — a genuine
  /// external change to apply.
  ///
  /// The matched value is kept as the queue's new baseline and only older entries are
  /// dropped. Java pushes a full-state snapshot for reasons besides acknowledging a Modify —
  /// a repaint-driven dirty() is the common one — so the same acknowledged text arrives more
  /// than once; draining it on first sight made every later copy look like an external
  /// setText and re-based the editor onto it, discarding whatever had been typed since.
  ///
  /// The queue therefore holds the last acknowledged value plus the un-acknowledged
  /// keystrokes after it, never the document history. One narrow case stays ambiguous: an
  /// undo landing exactly on the last acknowledged value while a newer keystroke is still in
  /// flight is indistinguishable from that keystroke's own echo, and is ignored — the safe
  /// direction, since the alternative discards input the user typed.
  ///
  /// A value we never sent becomes the new baseline: Java has moved past every value queued
  /// before it, so a later push carrying one of them (an application setting the field back to
  /// an earlier value) is a genuine change, not an echo.
  ///
  /// Known transient: that rebase drops the keystrokes queued before the new value, so a late
  /// echo of one of them is applied as an external change, briefly over anything typed since.
  /// Java's next push, which carries the current text, corrects it.
  bool isStaleTextEcho(String incoming, String currentLocal) {
    final index = _pendingEchoes.indexOf(incoming);
    if (index < 0) {
      if (_pendingEchoes.isNotEmpty) seedTextEchoBaseline(incoming);
      return false;
    }
    _pendingEchoes.removeRange(0, index);
    return incoming != currentLocal;
  }
}
