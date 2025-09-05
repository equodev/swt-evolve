import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

final Map<String, IconData> iconMap = {
  // Basic UI Icons
  'home': Icons.home,
  'menu': Icons.menu,
  'search': Icons.search,
  'settings': Icons.settings,
  'person': Icons.person,

  // Navigation Icons
  'arrow_back': Icons.arrow_back,
  'arrow_forward': Icons.arrow_forward,
  'close': Icons.close,
  'more_vert': Icons.more_vert,
  'more_horiz': Icons.more_horiz,
  'check': Icons.check,
  'done': Icons.done,
  'navigate_next': Icons.navigate_next,
  'navigate_before': Icons.navigate_before,

  // Actions Icons
  'add': Icons.add,
  'remove': Icons.remove,
  'edit': Icons.edit,
  'delete': Icons.delete,
  'favorite': Icons.favorite,
  'favorite_border': Icons.favorite_border,
  'share': Icons.share,
  'bookmark': Icons.bookmark,
  'bookmark_border': Icons.bookmark_border,

  // Communication Icons
  'email': Icons.email,
  'phone': Icons.phone,
  'chat': Icons.chat,
  'message': Icons.message,
  'notifications': Icons.notifications,
  'notifications_none': Icons.notifications_none,

  // Media Icons
  'play_arrow': Icons.play_arrow,
  'pause': Icons.pause,
  'stop': Icons.stop,
  'skip_next': Icons.skip_next,
  'skip_previous': Icons.skip_previous,
  'volume_up': Icons.volume_up,
  'volume_off': Icons.volume_off,
  'image': Icons.image,
  'video_library': Icons.video_library,

  // Content Icons
  'attach_file': Icons.attach_file,
  'copy': Icons.copy,
  'text_fields': Icons.text_fields,
  'format_list_bulleted': Icons.format_list_bulleted,
  'folder': Icons.folder,
  'file_present': Icons.file_present,

  // Device Icons
  'bluetooth': Icons.bluetooth,
  'wifi': Icons.wifi,
  'battery_full': Icons.battery_full,
  'brightness_high': Icons.brightness_high,
  'location_on': Icons.location_on,

  // Social Icons
  'thumb_up': Icons.thumb_up,
  'thumb_down': Icons.thumb_down,
  'star': Icons.star,
  'star_border': Icons.star_border,
  'group': Icons.group,

  // Shopping Icons
  'shopping_cart': Icons.shopping_cart,
  'local_offer': Icons.local_offer,
  'credit_card': Icons.credit_card,
  'store': Icons.store,

  // Alert Icons
  'error': Icons.error,
  'warning': Icons.warning,
  'info': Icons.info,
  'help': Icons.help,

  'view_menu': Icons.more_vert,
  'collapseall': Icons.unfold_less,
  'synced': Icons.sync_alt,
  'filter_ps': Icons.filter_alt,
  'focus-disabled': Icons.adjust,
  'alphab_sort_co': Icons.sort_by_alpha,
  'rem_co': Icons.clear,
  'rem_all_co': Icons.clear_all,
  'debugt_obj': Icons.bug_report,
  'gotoobj_tsk': Icons.plagiarism,
  'skip_brkp': Icons.do_not_disturb_off,
  'expandall': Icons.unfold_more,
  'focus': Icons.adjust,
  'tnames_co': Icons.type_specimen,
  'var_cntnt_prvdr': Icons.schema,
  'monitorexpression_tsk': Icons.add,
  'backward_nav': Icons.arrow_back,
  'forward_nav': Icons.arrow_forward,
  'home_nav': Icons.home,

  // ===== Eclipse UI – elcl16 / etool16 / eview16 / obj16 / ovr16 =====
  'close_view': Icons.close,
  'min_view': Icons.minimize,
  'max_view': Icons.maximize,
  'pin_view': Icons.push_pin,
  'refresh_nav': Icons.refresh,
  'linkto_help': Icons.help_outline,
  'button_menu': Icons.more_horiz,

  'flatLayout': Icons.view_stream,
  'hierarchicalLayout': Icons.account_tree,
  'selected_mode': Icons.radio_button_checked,
  'showchild_mode': Icons.subdirectory_arrow_right,

  'showcomplete_tsk': Icons.check_circle,
  'showtsk_tsk': Icons.checklist,
  'showerr_tsk': Icons.error_outline,
  'showwarn_tsk': Icons.warning_amber_outlined,
  'usearch_obj': Icons.search,
  'excludeMode_filter': Icons.filter_alt_off,
  'includeMode_filter': Icons.filter_alt,
  'fileType_filter': Icons.insert_drive_file,
  'fileFolderType_filter': Icons.folder_copy,
  'folderType_filter': Icons.create_new_folder,

  'problems_view': Icons.rule_folder,
  'problems_view_error': Icons.error,
  'problems_view_warning': Icons.warning,
  'problems_view_info': Icons.info_outline,
  'tasks_tsk': Icons.task_alt,
  'bkmrk_tsk': Icons.bookmark,
  'taskmrk_tsk': Icons.check_circle_outline,
  'complete_tsk': Icons.verified,
  'incomplete_tsk': Icons.radio_button_unchecked,
  'hprio_tsk': Icons.priority_high,
  'lprio_tsk': Icons.low_priority,
  'info_tsk': Icons.info,
  'warn_tsk': Icons.warning,
  'error_tsk': Icons.error,

  'prj_obj': Icons.folder_open,
  'cprj_obj': Icons.folder,
  'workset': Icons.workspaces,
  'exportpref_obj': Icons.upload_file,
  'importpref_obj': Icons.download,
  'keyspref_obj': Icons.keyboard,
  'quickfix_error_obj': Icons.tips_and_updates,
  'quickfix_warning_obj': Icons.tips_and_updates_outlined,
  'quickfix_info_obj': Icons.tips_and_updates_outlined,

  'filterapplied_ovr': Icons.filter_alt,
  'link_ovr': Icons.link,
  'linkwarn_ovr': Icons.link_off,
  'symlink_ovr': Icons.link,
  'virt_ovr': Icons.cloud,

  'search_src': Icons.search,
  'next_nav': Icons.navigate_next,
  'prev_nav': Icons.navigate_before,

  'newfile_wiz': Icons.note_add,
  'newfolder_wiz': Icons.create_new_folder,
  'newgroup_wiz': Icons.groups_2,
  'newprj_wiz': Icons.folder_open,
  'export_wiz': Icons.upload_file,
  'exportdir_wiz': Icons.drive_folder_upload,
  'exportzip_wiz': Icons.archive,
  'import_wiz': Icons.download,
  'importdir_wiz': Icons.drive_folder_upload,
  'importzip_wiz': Icons.unarchive,

  'bkmrk_nav': Icons.bookmarks,
  'filenav_nav': Icons.description,
  'pview': Icons.view_compact_alt,

  'smartmode_co': Icons.auto_fix_high,
  'step_current': Icons.circle,
  'step_done': Icons.check_circle,

  'resume_co': Icons.play_arrow,
  'suspend_co': Icons.pause,
  'terminate_co': Icons.stop,
  'terminate_all_co': Icons.stop_circle,
  'disconnect_co': Icons.link_off,
  'runlast_co': Icons.replay,
  'stepinto_co': Icons.keyboard_return,
  'stepover_co': Icons.skip_next,
  'stepreturn_co': Icons.subdirectory_arrow_left,
  'stepfilters_co': Icons.filter_alt,

  'toggle_brkp': Icons.fiber_manual_record,
  'disabled_co': Icons.block,
  'rem_all_brkp': Icons.clear_all,

  'configs': Icons.tune,
  //'workingsets_view': Icons.workspaces,
  'goto_input': Icons.input,
  'header_priority': Icons.priority_high,
  'header_complete': Icons.done_all,
  'welcome_editor': Icons.emoji_objects,
  'welcome_item': Icons.emoji_objects_outlined,
  'welcome_banner': Icons.flag,

  'fields': Icons.data_object,
  'static': Icons.bolt,
  'public': Icons.public,
  'localtypes': Icons.category,
  //'breakpoint_view': Icons.workspaces,
  'jcu_obj': Icons.data_object,
  'classf_obj': Icons.insert_drive_file,
  'file_obj': Icons.insert_drive_file_outlined,
  'text': Icons.text_snippet,
  'xmldoc': FontAwesomeIcons.code,
  //'sample': Icons.find_in_page_outlined,
  //'plugin_depend': Icons.extension_outlined,
  'plugin_javasearch': Icons.extension,
  //'package': FontAwesomeIcons.folderTree,
};

IconData? getIconByName(String name) => iconMap[name];

/// Normalizes a key:
/// - Converts to lower case
/// - Removes .png/.gif/.svg extension
/// - Removes folder segments like ".../elcl16/" if present in paths
/// - Replaces spaces with '_' and normalizes '-' → '_'
String _normalizeKey(String raw) {
  var s = raw.trim().toLowerCase();
  // Remove path
  final lastSlash = s.lastIndexOf('/');
  if (lastSlash >= 0) s = s.substring(lastSlash + 1);
  // Remove extension
  for (final ext in const ['.png', '.gif', '.svg', '.jpg', '.jpeg']) {
    if (s.endsWith(ext)) {
      s = s.substring(0, s.length - ext.length);
      break;
    }
  }
  s = s.replaceAll(' ', '_').replaceAll('-', '_');
  return s;
}

const List<String> _eclipseSuffixes = [
  '_co',
  '_wiz',
  '_tsk',
  '_obj',
  '_ovr',
  '_nav',
  '_ps',
  '_view',
  '_editor',
];

final List<MapEntry<RegExp, IconData>> _fallbackPatterns = [
  MapEntry(RegExp(r'^(collapse|collapseall)$'), Icons.unfold_less),
  MapEntry(RegExp(r'^(expand|expandall)$'), Icons.unfold_more),
  MapEntry(RegExp(r'^refresh(_.*)?$'), Icons.refresh),
  MapEntry(RegExp(r'^(filter|.*_filter)$'), Icons.filter_alt),
  MapEntry(RegExp(r'^(search|find|usearch|search_src)$'), Icons.search),
  MapEntry(RegExp(r'^(next|.*_next|next_nav)$'), Icons.navigate_next),
  MapEntry(
      RegExp(r'^(prev|previous|.*_prev|prev_nav)$'), Icons.navigate_before),
  MapEntry(RegExp(r'^(close|close_view)$'), Icons.close),
  MapEntry(RegExp(r'^(pin|pin_view)$'), Icons.push_pin),
  MapEntry(RegExp(r'^(min|min_view|minimize)$'), Icons.minimize),
  MapEntry(RegExp(r'^(max|max_view|maximize)$'), Icons.maximize),
  MapEntry(RegExp(r'^(home|home_nav)$'), Icons.home),
  MapEntry(RegExp(r'^(back|backward|backward_nav)$'), Icons.arrow_back),
  MapEntry(RegExp(r'^(forward|forward_nav)$'), Icons.arrow_forward),
  MapEntry(RegExp(r'^(warn|warning|.*_warn.*)$'), Icons.warning),
  MapEntry(RegExp(r'^(error|.*_err.*)$'), Icons.error),
  MapEntry(RegExp(r'^(info|.*_info.*)$'), Icons.info_outline),
  MapEntry(RegExp(r'^(task|tasks|.*_tsk)$'), Icons.task_alt),
  MapEntry(RegExp(r'^(bookmark|bkmrk.*)$'), Icons.bookmark),
  MapEntry(RegExp(r'^(problem|problems.*)$'), Icons.rule_folder),
  MapEntry(RegExp(r'^(project|prj_.*|cprj_.*)$'), Icons.folder_open),
  MapEntry(RegExp(r'^(folder|.*folder.*)$'), Icons.folder),
  MapEntry(RegExp(r'^(file|.*file.*)$'), Icons.insert_drive_file),
  MapEntry(RegExp(r'^(link|symlink|.*_link.*)$'), Icons.link),
  MapEntry(RegExp(r'^(linkoff|linkwarn|.*link.*off.*)$'), Icons.link_off),
  MapEntry(RegExp(r'^(cloud|virt.*)$'), Icons.cloud),
  MapEntry(RegExp(r'^(debug|bug|.*debug.*)$'), Icons.bug_report),
  MapEntry(RegExp(r'^(resume|run|play.*)$'), Icons.play_arrow),
  MapEntry(RegExp(r'^(suspend|pause.*)$'), Icons.pause),
  MapEntry(RegExp(r'^(terminate|stop.*)$'), Icons.stop),
  MapEntry(RegExp(r'^terminate_all.*$'), Icons.stop_circle),
  MapEntry(RegExp(r'^(disconnect.*)$'), Icons.link_off),
  MapEntry(RegExp(r'^(runlast.*)$'), Icons.replay),
  MapEntry(RegExp(r'^(stepinto.*)$'), Icons.keyboard_return),
  MapEntry(RegExp(r'^(stepover.*)$'), Icons.skip_next),
  MapEntry(RegExp(r'^(stepreturn.*)$'), Icons.subdirectory_arrow_left),
  MapEntry(RegExp(r'^(stepfilters.*)$'), Icons.filter_alt),
  MapEntry(RegExp(r'^(toggle.*brk.*|.*brkp.*|.*breakpoint.*)$'),
      Icons.fiber_manual_record),
  MapEntry(RegExp(r'^(disabled|disable.*)$'), Icons.block),
  MapEntry(RegExp(r'^(remove_all|rem_all.*)$'), Icons.clear_all),
  MapEntry(RegExp(r'^(remove|rem.*)$'), Icons.clear),
  MapEntry(RegExp(r'^(add|new|create.*)$'), Icons.add),
  MapEntry(RegExp(r'^(edit|rename|properties.*)$'), Icons.edit),
  MapEntry(RegExp(r'^(import.*)$'), Icons.download),
  MapEntry(RegExp(r'^(export.*)$'), Icons.upload_file),
  MapEntry(RegExp(r'^(archive|zip.*)$'), Icons.archive),
  MapEntry(RegExp(r'^(unarchive|unzip.*)$'), Icons.unarchive),
  MapEntry(RegExp(r'^(schema|var_cntnt_prvdr)$'), FontAwesomeIcons.folderTree),
  MapEntry(RegExp(r'^(smart|smartmode.*)$'), Icons.auto_fix_high),
  MapEntry(RegExp(r'^(focus|focus_disabled|focus-disabled)$'), Icons.adjust),
];

/// Returns an icon for an Eclipse or generic key.
/// 1) Looks for an exact (normalized) match in iconMap.
/// 2) Tries without common suffixes (_co, _wiz, etc.).
/// 3) Applies pattern-based fallback.
IconData? getEclipseIcon(String rawKey) {
  final key = _normalizeKey(rawKey);

  final direct = iconMap[key];
  if (direct != null) return direct;

  for (final suf in _eclipseSuffixes) {
    if (key.endsWith(suf)) {
      final trimmed = key.substring(0, key.length - suf.length);
      final m2 = iconMap[trimmed];
      if (m2 != null) return m2;
    }
  }

  for (final entry in _fallbackPatterns) {
    if (entry.key.hasMatch(key)) return entry.value;
  }

  return null;
}

IconData? suggestIconForKey(String rawKey) {
  final key = _normalizeKey(rawKey);

  for (final entry in _fallbackPatterns) {
    if (entry.key.hasMatch(key)) return entry.value;
  }
  return null;
}

class IconValidationReport {
  final List<String> missingKeys;
  final Map<String, IconData?> suggestions;
  IconValidationReport({required this.missingKeys, required this.suggestions});
  bool get isOk => missingKeys.isEmpty;
}

IconValidationReport validateIconKeys(Iterable<String> keysInUse) {
  final missing = <String>[];
  final sugg = <String, IconData?>{};

  for (final raw in keysInUse) {
    final exact = getEclipseIcon(raw);
    if (exact == null) {
      missing.add(raw);
      sugg[raw] = suggestIconForKey(raw);
    }
  }

  return IconValidationReport(missingKeys: missing, suggestions: sugg);
}
