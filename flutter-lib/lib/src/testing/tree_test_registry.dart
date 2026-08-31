import 'dart:convert';

import '../gen/treeitem.dart';

typedef TreeItemAction = bool Function(String itemIdentifier);
typedef TreeItemSnapshotProvider = List<Map<String, dynamic>> Function();

class TestTreeHandle {
  TestTreeHandle({
    required this.items,
    required this.expand,
  });

  final TreeItemSnapshotProvider items;
  final TreeItemAction expand;
}

final Map<String, TestTreeHandle> _trees = {};

void registerTestTree(String treeIdentifier, TestTreeHandle handle) {
  _trees[treeIdentifier] = handle;
}

/// Removes [handle] only while it is still the registered one: a re-inflated Tree registers its
/// replacement during the build and disposes the outgoing state at the end of that same frame.
void unregisterTestTree(String treeIdentifier, TestTreeHandle handle) {
  if (identical(_trees[treeIdentifier], handle)) {
    _trees.remove(treeIdentifier);
  }
}

String queryTreeItemsJson() {
  return jsonEncode(_trees.values.expand((tree) => tree.items()).toList());
}

bool expandTreeItem(String itemIdentifier) {
  return _trees.values.any((tree) => tree.expand(itemIdentifier));
}

String treeItemIdentifier(VTreeItem item) => '${item.swt}/${item.id}';

String treeItemText(VTreeItem item) {
  final text = item.text;
  if (text != null && text.isNotEmpty) {
    return text;
  }
  return item.texts?.whereType<String>().firstWhere(
        (text) => text.isNotEmpty,
        orElse: () => '',
      ) ??
      '';
}
