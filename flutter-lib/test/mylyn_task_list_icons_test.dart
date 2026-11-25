// The Eclipse Task List view sends these image names for its toolbar and view menu items. Each
// shows a replacement icon: a bundled SVG, or failing that an entry of the icon map, the two
// lookups an image with a filename goes through before its original bitmap is drawn.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/assets_manager.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/icons_map.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _toolbar = [
  'task-new',
  'presentation-categorized',
  'presentation-scheduled',
  'filter-complete',
  'filter-mytasks',
  'repository-synchronize',
];

const _viewMenu = [
  'go-up',
  'filter-priority',
  'task-search',
  'repositories',
  'icons-legend',
  'link-editor',
];

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    AssetsManager.resetForTesting();
    applyConfigFlags(ConfigFlags());
  });

  for (final name in [..._toolbar, ..._viewMenu]) {
    test('the Task List image $name shows a replacement icon', () async {
      final replacement = await AssetsManager.loadReplacement(name);
      expect(replacement != null || getIconByName(name) != null, isTrue,
          reason: '$name has neither a bundled SVG nor an icon map entry');
    });
  }
}
