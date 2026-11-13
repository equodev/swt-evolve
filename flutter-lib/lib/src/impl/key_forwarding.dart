import 'package:flutter/services.dart';

import '../gen/event.dart';
import 'key_mapping.dart';
import 'utils/key_edit_order.dart';

/// Whether a whole-tree [DisplaySwt] is mounted and forwarding keystrokes from a single
/// top-level handler (see `display_evolve.dart`).
///
/// In the whole-tree (desk/web) model every keystroke is captured once at the Display root and
/// routed to Java's focused control, reproducing SWT's "the Display sees the key first" contract
/// (which is what Eclipse's `Display.addFilter(SWT.KeyDown/Traverse, …)` command dispatch relies
/// on). While that is active, the per-control forwarders (`ControlImpl.wrap`, Text, Canvas,
/// StyledText) stay silent so the same key isn't dispatched to Java twice.
///
/// In the embedded backend there is no whole-tree Display surface, so this stays `false` and the
/// per-control forwarding remains the sole path — unchanged.
bool get displayLevelKeyForwardingActive => _displayKeyForwarders > 0;

/// Counted, not a flag: a remounted Display's new State starts forwarding before the old one is
/// disposed, so the old one's stop must not end the new one's.
int _displayKeyForwarders = 0;

void startDisplayKeyForwarding() => _displayKeyForwarders++;

void stopDisplayKeyForwarding() => _displayKeyForwarders--;

/// The keys a control forwarded the press of and whose release has not come yet. Shared by all
/// controls: a press can open an editor that takes the focus, and the release then goes to that one.
final Set<PhysicalKeyboardKey> _forwardedPresses = {};

/// The events a control already forwarded, so the controls around it don't.
final Expando<bool> _forwardedKeyEvents = Expando();

/// A control's own key forwarding, the path while no whole-tree Display forwards keys: a press, and
/// each repeat of a held key, goes to Java as KeyDown and its release as KeyUp, the release behind the
/// edit its press typed, as on a native field. A key bubbling up from a control inside this one is that
/// control's alone, as SWT sends a key to the focus control only. A release goes to the control focused
/// when it comes; one whose press no control sent is not sent.
void forwardControlKey(KeyEvent event,
    {required void Function(VEvent) keyDown, required void Function(VEvent) keyUp}) {
  if (_forwardedKeyEvents[event] ?? false) return;
  _forwardedKeyEvents[event] = true;
  if (event is KeyDownEvent || event is KeyRepeatEvent) {
    final v = mapNewKeyEventToSwt(event);
    if (v.keyCode == 0 && v.character == 0) return;
    _forwardedPresses.add(event.physicalKey);
    KeyEditOrder.instance.forward(
      typed: KeyEditOrder.typesCharacter(v.character ?? 0) && KeyEditOrder.instance.editorFocused,
      send: () => keyDown(v),
    );
  } else if (event is KeyUpEvent && _forwardedPresses.remove(event.physicalKey)) {
    final v = mapNewKeyEventToSwt(event);
    KeyEditOrder.instance.forward(typed: false, send: () => keyUp(v));
  }
}

/// The canvas editor (a StyledText) that holds keyboard focus, or `null`.
Object? _focusedCanvasEditor;

/// True while a canvas editor holds keyboard focus; the browser must then not move DOM focus on
/// Tab, since the editor's Traverse listeners decide.
bool get canvasEditorFocused => _focusedCanvasEditor != null;

/// Records that [editor] gained or lost focus. A loss only counts for the current holder, so a blur
/// cannot clear what a newly focused editor just set — the two arrive in no guaranteed order.
void setCanvasEditorFocus(Object editor, bool focused) {
  if (focused) {
    _focusedCanvasEditor = editor;
  } else if (identical(_focusedCanvasEditor, editor)) {
    _focusedCanvasEditor = null;
  }
}

/// The widget that runs its own keyboard pipeline and holds focus, or `null`. Today: a Swing island,
/// whose mirror hands every key to the AWT engine itself.
Object? _keyboardOwner;

/// True while a widget with its own keyboard pipeline holds focus; the Display-level forwarder
/// then stays out, so a key is dispatched to exactly one engine.
bool get keyboardClaimed => _keyboardOwner != null;

/// Claims (or releases) the keyboard for [owner]. A release only counts for the current owner, so
/// a blur cannot clear the claim a newly focused widget just made — the two arrive in no
/// guaranteed order.
void claimKeyboard(Object owner, bool owns) {
  if (owns) {
    _keyboardOwner = owner;
  } else if (identical(_keyboardOwner, owner)) {
    _keyboardOwner = null;
  }
}

/// Open drop-down lists, each deciding which keys it takes. Like the popup of a native one, an open
/// list keeps its navigation keys from SWT: no KeyDown, and no Traverse that would close a dialog or
/// press its default button.
final Set<bool Function(KeyEvent)> _openListKeyClaims = {};

void addOpenListKeyClaim(bool Function(KeyEvent) claims) => _openListKeyClaims.add(claims);

void removeOpenListKeyClaim(bool Function(KeyEvent) claims) => _openListKeyClaims.remove(claims);

bool openListClaimsKey(KeyEvent event) => _openListKeyClaims.any((claims) => claims(event));
