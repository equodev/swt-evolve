import 'dart:async';
import 'dart:js_interop';

import 'package:flutter/semantics.dart';
import 'package:web/web.dart' as web;

/// Neutralizes a Flutter Web engine defect (observed on stable 3.44.4 through 3.47.1, reproduced
/// in a standalone app with none of this repo's code): once the semantics tree is
/// built, the engine sometimes leaves an empty `<flt-semantics>` element sized to the whole
/// viewport with `pointer-events: auto`. A `Slider`/`Scale`'s drag surface is the case seen so
/// far -- the element is `aria-owns`ed by the control's real node, but sized to the page instead
/// of the control. Sitting last in DOM order, it paints over everything else and swallows every
/// click on the page, real or driven by a test tool -- not only on its own widget.
///
/// This only ever touches an element that is simultaneously full-viewport-sized, empty of both
/// text and child elements, and pointer-permeable -- no legitimate control (interactive or not)
/// matches all three at once, so real accessibility nodes are left untouched. It trades away that
/// one control's own click-through-the-DOM affordance to stop it from blocking every other
/// control on the page; see UiDriver's click helper for how a real click still reaches a control
/// whose own proxy this disarms.
Timer? _sweepTimer;
Timer? _debounce;
web.MutationObserver? _observer;
bool _listening = false;

/// Follows the semantics tree rather than the E2E flag: the ghost node appears whenever the engine
/// builds the semantics DOM, which assistive technology turns on by itself. Nothing is swept while
/// semantics is off, so an app that never enables it pays nothing.
void startSemanticsGhostNodeGuard() {
  if (_listening) return;
  _listening = true;
  SemanticsBinding.instance.addSemanticsEnabledListener(_syncToSemantics);
  _syncToSemantics();
}

void _syncToSemantics() {
  if (SemanticsBinding.instance.semanticsEnabled) {
    _arm();
  } else {
    _disarm();
  }
}

void _arm() {
  if (_observer != null) return;
  _sweep();
  final observer = web.MutationObserver(_onMutation.toJS);
  final body = web.document.body;
  if (body != null) {
    observer.observe(
      body,
      web.MutationObserverInit(
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: <JSString>['style'.toJS].toJS,
      ),
    );
  }
  _observer = observer;
  // Backstop for whatever the observer's childList/attributes filter doesn't catch -- cheap at
  // this DOM size.
  _sweepTimer ??= Timer.periodic(const Duration(milliseconds: 500), (_) => _sweep());
}

// The engine tears the semantics DOM down with it, so the elements this disarmed are gone; there
// is nothing to restore.
void _disarm() {
  _observer?.disconnect();
  _observer = null;
  _sweepTimer?.cancel();
  _sweepTimer = null;
  _debounce?.cancel();
  _debounce = null;
}

// Coalesced, not per-mutation: a drag or an expand/collapse fires this many times a second, each
// a forced layout read (getBoundingClientRect/getComputedStyle) over the whole semantics tree --
// cheap once, not worth paying on every single one of a mutation burst.
void _onMutation(JSArray<web.MutationRecord> records, web.MutationObserver observer) {
  _debounce?.cancel();
  _debounce = Timer(const Duration(milliseconds: 150), _sweep);
}

void _sweep() {
  final nodes = web.document.querySelectorAll('flt-semantics');
  final vw = web.window.innerWidth;
  final vh = web.window.innerHeight;
  for (var i = 0; i < nodes.length; i++) {
    final node = nodes.item(i);
    if (node == null || !node.isA<web.HTMLElement>()) continue;
    final el = node as web.HTMLElement;
    if (_isGhost(el, vw, vh)) {
      el.style.pointerEvents = 'none';
    }
  }
}

bool _isGhost(web.HTMLElement el, int viewportWidth, int viewportHeight) {
  if (el.childElementCount != 0) return false;
  if ((el.textContent ?? '').trim().isNotEmpty) return false;
  final rect = el.getBoundingClientRect();
  if (rect.width < viewportWidth - 5 || rect.height < viewportHeight - 5) return false;
  return web.window.getComputedStyle(el).pointerEvents != 'none';
}
