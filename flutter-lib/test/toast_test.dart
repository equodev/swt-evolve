// The notification popup: a transient card in the corner of the window, raised when a main-toolbar
// item is clicked. SWT has no equivalent, so none of this mirrors a platform -- these tests are the
// specification.
//
// What they pin: it is off unless asked for, it never reaches outside the main toolbar, it never
// stands in the way of what the item actually does, it dismisses itself on schedule, and the stack
// never grows past the theme's cap however many arrive.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/main_toolbar_scope.dart';
import 'package:swtflutter/src/custom/toast.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/toolitem_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// A ToolItem whose outgoing events are captured instead of handed to EquoCommService, which is
/// unavailable in a test.
class _CapturingToolItem extends ToolItemSwt<VToolItem> {
  final void Function(String event) onSend;

  const _CapturingToolItem({required super.value, required this.onSend});

  @override
  void sendEvent(VToolItem val, String ev, VEvent? payload) => onSend(ev);

  @override
  State createState() => ToolItemImpl<_CapturingToolItem, VToolItem>();
}

/// One CHECK item, in or out of the main toolbar -- the only difference the feature may see.
Future<void> _pumpItem(
  WidgetTester tester, {
  required bool inMainToolbar,
  void Function(String)? onSend,
}) async {
  final item = _CapturingToolItem(
    value: VToolItem()
      ..id = 2
      ..style = SWT.CHECK
      ..enabled = true
      ..selection = false
      ..text = 'Open'
      ..toolTipText = 'Open a project (Ctrl+O)',
    onSend: onSend ?? (_) {},
  );
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 60,
      child: inMainToolbar ? MainToolbarScope(child: item) : item,
    ),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

/// Pumps an app and hands back a context that sits under the root overlay.
Future<BuildContext> _host(WidgetTester tester) async {
  late BuildContext captured;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Builder(builder: (context) {
      captured = context;
      return const SizedBox(width: 600, height: 400);
    }),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
  return captured;
}

/// Lets every auto-dismissing toast run out, so no timer outlives the test.
Future<void> _drainToasts(WidgetTester tester) async {
  await tester.pump(const Duration(seconds: 10));
  await tester.pumpAndSettle();
}

void main() {
  setUp(() {
    resetConfigFlags();
    ToastHost.reset();
  });
  tearDown(() {
    ToastHost.reset();
    resetConfigFlags();
  });

  group('raised from a main-toolbar click', () {
    testWidgets('off by default: a click raises nothing', (tester) async {
      await _pumpItem(tester, inMainToolbar: true);

      await tester.tap(find.byType(_CapturingToolItem));
      await tester.pumpAndSettle();

      expect(ToastHost.active, isEmpty);
    });

    testWidgets('on: the card is titled after the item', (tester) async {
      setConfigFlags(ConfigFlags()..notification_popup = true);
      await _pumpItem(tester, inMainToolbar: true);

      await tester.tap(find.byType(_CapturingToolItem));
      await tester.pumpAndSettle();

      // The title is read off the live widget; the body is stand-in wording, so the card reads as
      // a notification rather than as a dump of what Evolve knows about the item.
      expect(find.text('Open'), findsWidgets);
      expect(find.text('This is a test notification.'), findsOneWidget);
      await _drainToasts(tester);
    });

    testWidgets('the item still reports its selection to Java', (tester) async {
      setConfigFlags(ConfigFlags()..notification_popup = true);
      final sent = <String>[];
      await _pumpItem(tester, inMainToolbar: true, onSend: sent.add);

      await tester.tap(find.byType(_CapturingToolItem));
      await tester.pumpAndSettle();

      expect(sent, contains('Selection/Selection'),
          reason: 'the card is raised alongside the action, it does not replace it');
      expect(ToastHost.active, hasLength(1));
      await _drainToasts(tester);
    });

    testWidgets('an item outside the main toolbar raises nothing', (tester) async {
      setConfigFlags(ConfigFlags()..notification_popup = true);
      final sent = <String>[];
      await _pumpItem(tester, inMainToolbar: false, onSend: sent.add);

      await tester.tap(find.byType(_CapturingToolItem));
      await tester.pumpAndSettle();

      expect(ToastHost.active, isEmpty);
      expect(sent, contains('Selection/Selection'), reason: 'the action is untouched either way');
    });
  });

  group('the card itself', () {
    testWidgets('off by default: nothing is shown', (tester) async {
      final context = await _host(tester);

      ToastHost.showNew(context, title: 'Saved');
      await tester.pumpAndSettle();

      expect(find.text('Saved'), findsNothing);
      expect(ToastHost.active, isEmpty);
    });

    testWidgets('on: it appears and then removes itself', (tester) async {
      setConfigFlags(ConfigFlags()..notification_popup = true);
      final context = await _host(tester);

      ToastHost.showNew(
        context,
        title: 'Saved',
        message: 'Written to disk.',
        duration: const Duration(seconds: 2),
      );
      await tester.pumpAndSettle();
      expect(find.text('Saved'), findsOneWidget);
      expect(find.text('Written to disk.'), findsOneWidget);

      await tester.pump(const Duration(seconds: 3));
      await tester.pumpAndSettle();
      expect(find.text('Saved'), findsNothing);
      expect(ToastHost.active, isEmpty);
    });

    testWidgets('a sticky one waits, and its close button dismisses it', (tester) async {
      setConfigFlags(ConfigFlags()..notification_popup = true);
      final context = await _host(tester);

      ToastHost.showNew(context, title: 'Sync failed', duration: Duration.zero);
      await tester.pumpAndSettle();

      await tester.pump(const Duration(seconds: 30));
      await tester.pumpAndSettle();
      expect(find.text('Sync failed'), findsOneWidget);

      await tester.tap(find.byIcon(Icons.close));
      await tester.pumpAndSettle();
      expect(find.text('Sync failed'), findsNothing);
    });

    testWidgets('more cards than the theme shows never draws more than the cap', (tester) async {
      setConfigFlags(ConfigFlags()..notification_popup = true);
      final context = await _host(tester);

      for (var i = 0; i < 9; i++) {
        ToastHost.showNew(context, title: 'Event $i', duration: Duration.zero);
      }
      await tester.pumpAndSettle();

      // The theme's maxVisible is 4; the oldest five are held but not drawn.
      expect(find.textContaining('Event '), findsNWidgets(4));
      expect(find.text('Event 8'), findsOneWidget);
      expect(find.text('Event 0'), findsNothing);
      expect(ToastHost.active.length, 9);
    });
  });
}
