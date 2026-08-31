// Re-inflating a Tree must leave it readable from the test harness. Flutter deactivates the old
// element during the build and disposes it in finalizeTree, so the replacement's initState runs
// BEFORE the outgoing dispose: an unregister keyed only by identifier removes the handle the new
// state just registered, and the tree keeps painting while queryTreeItemsJson() reports nothing.
// The register/unregister pair mirrors TreeImpl's initState and dispose.

import 'dart:convert';

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/testing/tree_test_registry.dart';

class _TreeHost extends StatefulWidget {
  const _TreeHost({super.key, required this.treeId, required this.rowText});

  final int treeId;
  final String rowText;

  @override
  State<_TreeHost> createState() => _TreeHostState();
}

class _TreeHostState extends State<_TreeHost> {
  late final String _testTreeKey;
  late final TestTreeHandle _handle;

  @override
  void initState() {
    super.initState();
    _testTreeKey = 'Tree/${widget.treeId}';
    _handle = TestTreeHandle(items: _items, expand: (_) => false);
    registerTestTree(_testTreeKey, _handle);
  }

  List<Map<String, dynamic>> _items() => [
        {
          'treeIdentifier': _testTreeKey,
          'identifier': 'TreeItem/1',
          'parentIdentifier': null,
          'text': widget.rowText,
          'level': 0,
          'expanded': false,
          'visible': true,
        }
      ];

  @override
  void dispose() {
    unregisterTestTree(_testTreeKey, _handle);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => const SizedBox.shrink();
}

List<String> _rowsOf(String treeIdentifier) {
  final List<dynamic> rows = jsonDecode(queryTreeItemsJson()) as List<dynamic>;
  return rows
      .cast<Map<String, dynamic>>()
      .where((row) => row['treeIdentifier'] == treeIdentifier)
      .map((row) => row['text'] as String)
      .toList();
}

void main() {
  testWidgets('a re-inflated Tree is still readable from the harness', (WidgetTester tester) async {
    // A wizard page that is left and returned to comes back as a new element under the same SWT id.
    await tester.pumpWidget(const _TreeHost(key: ValueKey('page-1'), treeId: 5, rowText: 'Java'));
    expect(_rowsOf('Tree/5'), ['Java']);

    await tester.pumpWidget(const _TreeHost(key: ValueKey('page-1-again'), treeId: 5, rowText: 'Java'));
    await tester.pump();

    expect(_rowsOf('Tree/5'), ['Java'],
        reason: 'the outgoing state unregistered the handle its replacement had already registered');
  });

  testWidgets('disposing a Tree for good removes it', (WidgetTester tester) async {
    await tester.pumpWidget(const _TreeHost(key: ValueKey('only'), treeId: 9, rowText: 'General'));
    expect(_rowsOf('Tree/9'), ['General']);

    await tester.pumpWidget(const SizedBox.shrink());
    await tester.pump();

    expect(_rowsOf('Tree/9'), isEmpty);
  });
}
