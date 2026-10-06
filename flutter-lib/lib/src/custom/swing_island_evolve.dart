/// An SWT_AWT island under swing-evolve's engine. Java describes the composite `SWT_AWT.new_Frame`
/// creates as a "SwingIsland" and, asked, answers with the AWT frame's windowId and the engine's
/// port, or a negative one when the engine's traffic rides Evolve's own connection; the mirror of
/// that window comes from [swingMirrorBuilder].
///
/// Evolve does not depend on swing-evolve. A host whose Flutter bundle includes swing-evolve's
/// package sets [swingMirrorBuilder] at startup; while it is null the island renders empty, which
/// is why flutter-lib builds without that package.
///
/// The region owns what only the embedder can:
///  * focus: an SWT-side [FocusNode]. Focused, [SwingIslandWindow.focused] turns true, so the
///    mirror claims keys and Java's window gains focus; Java is told `Focus/FocusIn` so
///    `Display.getFocusControl()` is the island; and the island claims the keyboard from the
///    Display-level forwarder ([claimKeyboard]) so each key is dispatched to exactly one engine.
///    Focus leaving reverses all three.
///  * where the mirror goes: the builder only builds it, inside the region's [Focus].
library;

import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../gen/composite.dart';
import '../impl/composite_evolve.dart';
import '../impl/focus_requests.dart';
import '../impl/key_forwarding.dart';

/// The AWT window an island mirrors, as Java described it.
@immutable
class SwingIslandWindow {
  const SwingIslandWindow({required this.windowId, required this.port, required this.focused});

  /// The engine's id of the frame `SWT_AWT.new_Frame` returned.
  final int windowId;

  /// The engine's loopback comm port, or negative when the engine has no socket of its own and its
  /// traffic rides Evolve's connection instead ([overEvolveConnection]).
  final int port;

  /// Whether the engine's traffic rides Evolve's own connection rather than a socket of its own.
  bool get overEvolveConnection => port < 0;

  /// True while the island holds focus: the mirror claims keys and Java's window gains focus.
  final ValueListenable<bool> focused;
}

/// Builds the mirror of [window] inside an island.
typedef SwingMirrorBuilder = Widget Function(BuildContext context, SwingIslandWindow window);

/// Set by a host that bundles swing-evolve's Flutter package; null renders every island empty.
SwingMirrorBuilder? swingMirrorBuilder;

/// The composite `SWT_AWT.new_Frame` was given, described by Java as a "SwingIsland".
class SwingIslandSwt extends CompositeSwt<VComposite> {
  const SwingIslandSwt({super.key, required super.value});

  @override
  State<StatefulWidget> createState() => SwingIslandImpl();
}

class SwingIslandImpl extends CompositeImpl<SwingIslandSwt, VComposite> {
  @override
  Widget buildComposite() => ClipRect(
    child: SwingIslandRegion(
      key: ValueKey('swing-island-${state.id}'),
      id: state.id,
      channel: '${state.swt}/${state.id}',
      send: (event) => widget.sendEvent(state, event, null),
    ),
  );
}

/// The mirror of one island's AWT frame, once Java has said which window it is.
class SwingIslandRegion extends StatefulWidget {
  const SwingIslandRegion({super.key, required this.id, required this.channel, required this.send});

  /// The island's SWT widget id, the one Java's focus requests name.
  final int id;

  /// `SwingIsland/<id>`: the channel Java answers [ask] on.
  final String channel;

  /// Sends an event of this island to Java, named relative to [channel].
  final void Function(String event) send;

  /// The request for the frame's windowId and the engine's port, answered under the same name.
  static const String ask = 'swingIsland';

  @override
  State<SwingIslandRegion> createState() => _SwingIslandRegionState();
}

class _SwingIslandRegionState extends State<SwingIslandRegion> {
  final FocusNode _node = FocusNode(debugLabel: 'SwingIsland');
  final ValueNotifier<bool> _focused = ValueNotifier(false);
  int? _windowId;
  int? _port;
  Object? _token;

  String get _askChannel => '${widget.channel}/${SwingIslandRegion.ask}';

  @override
  void initState() {
    super.initState();
    _node.addListener(_onNodeFocus);
    FocusRequests.instance.addListener(_applyFocusRequest);
    _applyFocusRequest();
    _token = EquoCommService.onRaw(_askChannel, _onIsland);
    // Java's push may have gone out before this listener existed.
    widget.send(SwingIslandRegion.ask);
  }

  void _onIsland(Object? raw) {
    final decoded = raw is String ? jsonDecode(raw) : raw;
    if (decoded is! Map) return;
    final windowId = (decoded['windowId'] as num?)?.toInt();
    final port = (decoded['port'] as num?)?.toInt();
    if (windowId == null || port == null || !mounted) return;
    setState(() {
      _windowId = windowId;
      _port = port;
    });
  }

  /// Java focused the island (Control.setFocus, Tab traversal into it): take the node.
  void _applyFocusRequest() {
    if (!FocusRequests.instance.claim(widget.id)) return;
    if (_node.context != null) {
      _node.requestFocus();
    } else {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _node.requestFocus();
      });
    }
  }

  void _onNodeFocus() {
    final has = _node.hasFocus;
    if (_focused.value == has) return;
    _focused.value = has;
    claimKeyboard(this, has);
    widget.send('Focus/${has ? 'FocusIn' : 'FocusOut'}');
  }

  @override
  void dispose() {
    claimKeyboard(this, false);
    FocusRequests.instance.removeListener(_applyFocusRequest);
    _node.removeListener(_onNodeFocus);
    _node.dispose();
    _focused.dispose();
    EquoCommService.remove(_askChannel, _token);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final windowId = _windowId;
    final port = _port;
    final builder = swingMirrorBuilder;
    if (windowId == null || port == null || builder == null) return const SizedBox.expand();
    return Focus(
      focusNode: _node,
      child: Listener(
        behavior: HitTestBehavior.translucent,
        onPointerDown: (_) => _node.requestFocus(),
        child: builder(
          context,
          SwingIslandWindow(windowId: windowId, port: port, focused: _focused),
        ),
      ),
    );
  }
}
