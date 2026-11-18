import 'dart:ui' show Size;

import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/assets_manager.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// Application icons named like these are replaced by Evolve's own drawing: version-control
/// actions, file-tree status decorations, diff navigation, common actions, settings-style
/// commands and model-diagram elements.
const _names = [
  'git_16', 'github_16', 'create_repo', 'clone', 'browse_repo', 'repo', 'checkout',
  'diff', 'diff_with_conflict', 'blank_folder', 'blank_file', 'archive_folder',
  'none_2x', 'none_2x_2x', 'error_2x', 'error_2x_2x', 'all_good_2x', 'all_good_2x_2x',
  'sync', 'sync_upload', 'sync_download', 'sync_download_2x', 'sync_download_2x_2x', 'question',
  'overlay_added', 'overlay_removed', 'overlay_modified', 'overlay_conflicted',
  'overlay_untracked', 'overlay_ignored', 'overlay_unmodified', 'overlay_calculating',
  'branch_create', 'branch_delete', 'branch_edit', 'branch_move', 'branch_create_move',
  'branch_remote', 'branch_remote_add', 'branch_remote_remove',
  'cherry_pick', 'rebase', 'squash', 'revert_to_parent', 'tag_create', 'tag_delete',
  'gh_code', 'issue_open', 'issue_closed', 'pr_open', 'pr_merged', 'pr_closed', 'pr_draft',
  'change_next', 'change_prev', 'change_first', 'change_last', 'change_to_left',
  'change_to_right', 'change_all_to_left', 'change_all_to_right',
  'split_horizontal', 'split_vertical', 'adjacent', 'overlap', 'split_monitors',
  'zoom_in', 'zoom_out', 'zoom_fit', 'zoom_100', 'collapse_all', 'expand_all', 'expand_all_deep',
  'plus', 'minus', 'check_green', 'close_red', 'close_white', 'export', 'update', 'internet',
  'camera', 'lock', 'cut', 'paste', 'undo', 'redo', 'save',
  'config', 'KeyRing', 'license', 'dp', 'action',
  'simulink_icon', 'aux_simulink_icon', 'block', 'eml', 'reference_16', 'arrow_up', 'arrow_down',
];

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  setUp(resetConfigFlags);

  for (final name in _names) {
    test('an application icon named $name.png is replaced by a vector drawing in the set\'s style',
        () async {
      final replacement = await AssetsManager.loadReplacement('icons/$name.png');

      expect(replacement, isA<String>(), reason: 'assets/icons/$name.svg is in the set');
      final svg = replacement as String;
      expect(svg, contains('viewBox="0 0 24 24"'));
      expect(RegExp(r'#[0-9a-fA-F]{3,6}').allMatches(svg).map((m) => m[0]).toSet(), {'#000000'},
          reason: 'one ink colour, so the theme tints it');
      expect(svg, isNot(contains('<text')), reason: 'no fonts');
      expect(svg, isNot(contains('<image')), reason: 'no embedded rasters');
      final info = await vg.loadPicture(SvgStringLoader(svg), null);
      expect(info.size, const Size(24, 24));
      info.picture.dispose();
    });
  }
}
