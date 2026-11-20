// An icon key whose name has no drawing of its own, but whose meaning matches an icon the pack has,
// gets that icon: `icon://IMG_ADDITEM` is named `additem` and shows `add`.

import 'dart:ui' show Size;

import 'package:flutter/services.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/assets_manager.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _aliases = {
  'additem': 'add',
  'removeitem': 'minus',
  'question_mark': 'question',
  'lock_closed': 'lock',
  'tick': 'check',
  'ok': 'check',
  'checkbox_unchecked': 'uncheck',
  'disk': 'save',
  'telephone': 'phone',
  'mobilephone': 'phone',
  'star_empty': 'star_border',
  'next': 'next_nav',
  'previous': 'prev_nav',
  'gear': 'settings',
  'link': 'link_obj',
  'mail': 'email',
  'bell': 'notifications',
  'printer': 'print_edit',
  'document': 'file_obj',
  'cart': 'shopping_cart',
  'import': 'import_co',
};

/// Lookup names (`icon://IMG_FOO` is `foo`) of a desktop app's icon keys; each shows a pack
/// icon. The app's brand logo is left out: it keeps the app's own bitmap.
const _appIconKeys = [
  'add', 'achtung', 'additem', 'adressetikette', 'arrowdown', 'arrowdowntorect', 'arrowup',
  'arrowstop', 'arrowstop_wo_shadow', 'ausrufez', 'ausrufez_rot', 'baggage_cart_box',
  'bell_exclamation', 'bomb', 'book', 'bookmark_pencil', 'bullet_green', 'bullet_grey',
  'bullet_red', 'bullet_yellow', 'calendar', 'cards', 'chart_curve', 'clear', 'clipboard',
  'comment_add', 'contact_detail', 'delete', 'disk', 'drawer', 'drawer_arrow', 'edit',
  'edit_done', 'edit_abort', 'empty_transparent', 'export', 'eye_wo_shadow', 'fax', 'fehler',
  'filter', 'frau', 'gofurther', 'group', 'home', 'import', 'link', 'lock_closed',
  'lock_closed_yellow', 'lock_closed_green', 'lock_closed_grey', 'lock_open', 'lock_co', 'mail',
  'mail_send', 'mann', 'mobilephone', 'money', 'movetolowerlist', 'movetoupperlist', 'network',
  'new', 'next', 'next_wo_shadow', 'ok', 'organisation', 'organisation_add', 'patientetikette',
  'pencil_8px', 'person', 'person_add', 'person_grey', 'person_ok', 'perspective_articles',
  'perspective_bbs', 'perspective_bills', 'perspective_contacts', 'perspective_kons',
  'perspective_leistungen', 'perspective_letters', 'perspective_orders', 'perspective_reminders',
  'pill', 'pill_exclamation_wo_shadow', 'pills', 'previous', 'printer', 'printer_big',
  'print_fix', 'print_reserve', 'print_symptomatic', 'refresh', 'removeitem', 'telephone',
  'tick', 'undo', 'versionedetikette', 'vip', 'vip_overlay', 'web', 'wizard', 'document_convert',
  'document_pdf', 'document_text', 'document_add', 'document_write', 'document_remove',
  'document_stack', 'document_stand_up', 'document', 'menubar', 'toolbar', 'bill',
  'view_work_incapable', 'view_consultation_detail', 'view_laboratory', 'view_patient_detail',
  'view_recipes', 'recipe_fix', 'recipe_reserve', 'recipe_symptomatic', 'database', 'conflict',
  'question_mark', 'flag_at', 'flag_de', 'flag_ch', 'flag_it', 'flag_fr', 'flag_fl',
  'category_group', 'syringe', 'blocks_small', 'sync', 'star', 'star_empty', 'stop', 'covercard',
  'copy', 'user_silhouette', 'table', 'node', 'nw_status', 'gear', 'checkbox',
  'checkbox_unchecked', 'doc_stand', 'doc_sys', 'jar', 'fix_medi', 'reserve_medi',
  'symptom_medi', 'need_medi', 'wiz_day', 'sort_star', 'data', 'balance_edit', 'cart', 'folder',
  'counter_stop', 'user_idle', 'system_monitor', 'sort_date_descending', 'sort_date',
  'nobilling', 'hand', 'clock', 'task', 'scanner', 'scanner_barcode', 'inbox', 'outbox',
  'server_pencil', '2e',
];

/// The same app's image files, reached by path (view and perspective icons, images its code loads
/// from its bundle): each shows a pack icon. Brand logos are left out.
const _appIconFiles = [
  '2E_icon.png', '8_em_pencil.png', 'abfall.png',
  'achtung.ico', 'add.gif', 'address-book-new-2.png', 'address_book.png', 'adressetikette.ico',
  'agenda.ico', 'arrow-stop.png', 'arrow-stop_woShadow.png', 'arrow_down.png',
  'arrow_down_to_rect.png', 'arrow_next.png', 'arrow_prev.png', 'arrow_up.png',
  'arrow_wo_shadow.png', 'artikel_perspective', 'arztkoffer.ico', 'at.png', 'attach.png',
  'auf.png', 'auf_view.ico', 'auftragsetikette.ico', 'ausrufez_rot.ico', 'ausrufezeichen.png',
  'ausrufezeichen_rot.ico', 'baggage-cart-box.png', 'balance-edit.png', 'basket.png',
  'bbs_perspective', 'bell.png', 'bell_exclamation.png', 'bestell_perspective', 'bestellen.gif',
  'bigprinter.png', 'block-small.png', 'bomb.png', 'book.png', 'book_open.png',
  'book_open_view.png', 'bookmark--pencil.png', 'box_label.png', 'brief2.ico',
  'briefe_perspective', 'buch.png', 'bulletGreen.png', 'bulletGrey.png', 'bulletRed.png',
  'bulletYellow.png', 'calendar.png', 'calendar_day.png', 'calendar_view_day.png', 'cards.png',
  'cart.png', 'category-group.png', 'ch.png', 'chart_curve.png', 'checkbox-unchecked.png',
  'checkbox.png', 'clipboard.png', 'clock.png', 'coins.png', 'collapseall.png', 'comment_add.png',
  'conflict.png', 'consult_view.png', 'contact_perspective', 'copy_edit.png', 'counter-stop.png',
  'covercard.png', 'cross_small.png', 'cut_edit.png', 'database.png', 'daten.gif', 'dayview.png',
  'de.png', 'delete.png', 'delete_edit.png', 'diskette.png', 'documennt-sys.png',
  'document-convert.png', 'document-copy.png', 'document-stand.png', 'document-stand_up.png',
  'document-sys.png', 'document.png', 'documentStack.png', 'document__minus.png',
  'document__pencil.png', 'document__plus.png', 'document_new.png', 'document_stack.png',
  'document_text.png', 'documents_stack.png', 'documents_text.png', 'dokument.gif',
  'drawer--arrow.png', 'drawer.png', 'drucker.ico', 'edit.png', 'edit_abort.png', 'edit_done.png',
  'emblem-symbolic-link-2.png', 'emptyTransparent.png', 'expandall.png', 'export.ico',
  'export_2.ico', 'export_wiz.png', 'eye_wo_shadow.png', 'face-monkey-2.png', 'factory.png',
  'fax.gif', 'fehler.ico', 'filter_ps.png', 'fix.png', 'fl.png', 'folder.png', 'fr.png',
  'frau.png', 'gear.png', 'gofurther.png', 'group.png', 'hand.png', 'help.gif',
  'help_contents.png', 'help_search.png', 'home.png', 'import.gif', 'import_wiz.png',
  'inbox_into.png', 'information.png', 'internal_browser.png', 'it.png', 'jar.png',
  'kons_perspective', 'kontakt_perspective.png', 'kontaktdetail.png', 'kontaktdetail_view.png',
  'kontakte_view.png', 'labor_view.png', 'leistung.png', 'leistung_perspective', 'link.png',
  'lock_co.png', 'mail-send.png', 'mail.png', 'mann.png', 'menubar.ico', 'minus.ico',
  'mobilephone.png', 'money.png', 'moveToLowerList.png', 'moveToUpperList.png', 'need.png',
  'network-status.png', 'netzwerk.ico', 'new.png', 'new_persp.png', 'new_wiz.gif',
  'nobillinng.png', 'node.png', 'ok.ico', 'organisationAdd.png', 'outbox_into.png',
  'outline_co.png', 'page_copy.png', 'page_green.png', 'page_pdf.png', 'page_red.png',
  'paste_edit.png', 'patientdetail_view.png', 'patientenetikette.png', 'person.ico',
  'personAdd.png', 'personGrey.png', 'personal.png', 'personen.png', 'personok.ico',
  'pill--exclamation_wo_shadow.png', 'pill.png', 'pills.png', 'platzhalter.gif', 'print_edit.png',
  'print_fix.png', 'print_reserve.png', 'print_symptomatic.png', 'printer.png', 'prop_ps.png',
  'question.png', 'reagensask.ico', 'rechnung.gif', 'rechnung_perspective', 'rechnungen.ico',
  'redo_edit.png', 'refresh.png', 'refresh_nav.png', 'reminder_perspective', 'report.png',
  'reserve.png', 'rezept_fix.png', 'rezept_reserve.png', 'rezept_symptomatic.png',
  'rezept_view.png', 'save_edit.png', 'saveall_edit.png', 'saveas_edit.png', 'scanner-barcode.png',
  'scanner.png', 'schlos_zu_grey.png', 'schloss_offen.png', 'schloss_zu.png',
  'schloss_zu_green.png', 'schloss_zu_grey.png', 'schloss_zu_yellow.png', 'script_code.png',
  'search.png', 'server--pencil.png', 'sort-date-descending.png', 'sort-date.png', 'sort-star.png',
  'star-empty.png', 'star.png', 'stethoskop.gif', 'stop.png', 'symptom.png', 'synced.png',
  'syringe.png', 'system-search-3.png', 'table.png', 'tafel.jpg', 'task.png', 'telephone.png',
  'tick.png', 'toolbar.ico', 'undo.png', 'undo_edit.png', 'user-idle-16x16.png',
  'user-silhouette.png', 'utilities-system-monitor-6.png', 'vip.png', 'vip_overlay.png', 'web.png',
  'wiz_day.png', 'wizard.ico',
];

/// An IDE plugin set's image files (explorer, dashboard, wizard and view icons its bundles load by
/// path): each shows a pack icon in the set's style. Brand logos, overlays and animation frames
/// are left out.
const _idePluginIconFiles = [
  'add_target.png', 'add_target_disabled.png', 'advice_obj.gif', 'advises_obj.gif',
  'after_advice_obj.gif', 'alias_obj.gif', 'aop_config_obj.gif', 'around_advice_obj.gif',
  'arrow_obj.gif', 'aspect_obj.gif', 'bean_obj.gif', 'beanref_obj.gif', 'before_advice_obj.gif',
  'bracket.svg', 'callhierarchy-incoming.svg', 'cheatsheet_task.gif', 'check_greyedout.png',
  'circuit-board.svg', 'close_hot.gif', 'cloud-inactive.png', 'cloud-ready.png', 'cloud_obj.png',
  'coffee.svg', 'collection_obj.gif', 'configset_obj.gif', 'configure.png', 'constant_obj.gif',
  'constructor_obj.gif', 'container_debugging.png', 'container_paused.png',
  'container_started.png', 'container_stopped.png', 'content_assist_separator_obj.gif',
  'context_obj.gif', 'debug-breakpoint-data-unverified.svg', 'debug-disconnect.svg',
  'debug-on-cloud.png', 'delete_app.png', 'delete_app_disabled.png', 'description_obj.gif',
  'download.png', 'ejb_obj.gif', 'event.png', 'feed.png', 'file-binary.svg', 'generic_editor.png',
  'globe.svg', 'image_debugging.png', 'image_paused.png', 'image_started.png',
  'image_stopped.png', 'import_obj.gif', 'introduction_obj.gif', 'jms_obj.gif', 'key_obj.gif',
  'layers.svg', 'library.svg', 'light-bulb-disabled.png', 'light-bulb.png', 'lightbulb.svg',
  'list_obj.gif', 'live-beans-graph.png', 'map_obj.gif', 'method_override_obj.gif',
  'namespace.png', 'namespace_bean_obj.gif', 'namespace_component_obj.gif',
  'new_ajdt_project.gif', 'new_java_project.gif', 'new_roo_project.png', 'new_spring_project.gif',
  'new_template_project.png', 'new_template_project_obj.png', 'newconfig_wiz.gif', 'news.png',
  'next_error_nav.gif', 'open_bean_obj.gif', 'open_browser_disabled.gif', 'open_console.png',
  'open_console_disabled.png', 'pointcut_obj.gif', 'prev.gif', 'prev_error_nav.gif',
  'priority-1.png', 'project_obj.gif', 'property_obj.gif', 'property_path_obj.gif', 'rebug.png',
  'rebug_disabled.png', 'record.svg', 'remove_target.png', 'remove_target_disabled.png',
  'report-bug.gif', 'request_mapping_obj.gif', 'research.png', 'restart.png',
  'restart_disabled.png', 'resume.gif', 'resumed.gif', 'rs_crashed.gif', 'rs_debugging.png',
  'rs_flapping.gif', 'rs_inactive.png', 'rs_paused.png', 'rs_running.png', 'rs_unknown.gif',
  'rss.png', 'rss_configure.png', 'run-on-cloud.png', 'script_obj.gif', 'selectmanifest.gif',
  'service-inactive.png', 'service.png', 'show_line_numbers.png', 'stop_disabled.png',
  'suspend.gif', 'suspend_disabled.gif', 'symbol-class.svg', 'symbol-field.svg',
  'symbol-interface.svg', 'symbol-method.svg', 'symbol-property.svg', 'symbol-value.svg',
  'target.svg', 'term_restart_disabled.png', 'text-size.svg', 'tutorials.png', 'tx_obj.gif',
  'update_password.png', 'value_obj.gif', 'verified.svg', 'virtual_folder_obj.gif', 'webinar.png',
  'xmlfile.gif', 'xsd_obj.gif', 'zap.svg',
];

/// A trace viewer's image files (its views, charts, toolbars, tracing sessions and targets, and the
/// platform plugins it bundles): each shows a pack icon in the set's style. Brand logos, overlays,
/// cursor masks and @2x/@4x variants are left out.
const _traceViewerIconFiles = [
  'add-context.gif', 'add_bookmark.gif', 'add_button.gif', 'add_obj.gif', 'addchild_button.gif',
  'addmany_button.gif', 'addnext_button.gif', 'addtoworkset.svg', 'analysis.png',
  'analysisprovider_obj.gif', 'arraypartition_obj.gif', 'arrow_up.bmp', 'backward_nav.gif',
  'barchart.png', 'binaries_obj.gif', 'binary_mapping_file.gif', 'bkmrk_nav.svg', 'bkmrk_tsk.svg',
  'block_selection_mode.svg', 'bookmark_obj.png', 'bp_label.svg', 'breakpoint_view.svg',
  'brkp_grp.svg', 'brkp_grp4.svg', 'brkp_obj.svg', 'brkp_type.svg', 'btf.png', 'bullet.gif',
  'callgraph.png', 'callstack.png', 'callstack_view.gif', 'channel.gif', 'channel_disabled.gif',
  'cheatsheet_task.svg', 'cheatsheet_view.svg', 'class.gif', 'classobject_obj.gif', 'clear.gif',
  'collapse_all.gif', 'collapseall.svg', 'collapsed_obj.gif', 'colors_view.gif', 'comment_obj.gif',
  'compare_view.svg', 'connect.gif', 'connection.gif', 'connection_node.gif', 'console_view.svg',
  'constrainon.gif', 'contextswitch.png', 'control_flow_view.gif', 'control_view.gif',
  'copy_edit.svg', 'copy_edit_co.svg', 'counter.png', 'cpu-usage.png', 'cpuusage.png',
  'cpyqual_menu.svg', 'critical-path.png', 'customparser_wizard.gif', 'cut_edit.svg',
  'debug_exc.svg', 'debug_persp.svg', 'debug_view.svg', 'debuglast_co.svg', 'debugt_obj.gif',
  'delete.gif', 'delete_button.gif', 'delete_edit.svg', 'delete_trace.gif', 'density.png',
  'disable.gif', 'disable_event.gif', 'disabled_co.svg', 'disconnect.gif', 'disconnect_co.svg',
  'display_selected_mb.svg', 'domain.gif', 'down_button.gif', 'drop_to_frame.svg', 'edit.gif',
  'element_icon.gif', 'enable.gif', 'enable_event.gif', 'enabled_co.svg', 'error_log.svg',
  'event_disabled.gif', 'event_enabled.gif', 'eventmatching.png', 'events_view.gif',
  'expand_all.gif', 'expandall.svg', 'experiment.gif', 'export.gif', 'export_brkpts.svg',
  'export_button.png', 'export_config_obj.svg', 'export_projectset.svg', 'export_wiz.svg',
  'exportdir_wiz.svg', 'exportpref_obj.svg', 'exportzip_wiz.svg', 'external_tools.svg',
  'fetch_log_wiz.gif', 'file_obj.svg', 'filter_add.gif', 'filter_items.gif', 'filter_ps.gif',
  'filters_view.gif', 'flame.png', 'fldr_obj.svg', 'follow_arrow_bwd.gif', 'follow_arrow_fwd.gif',
  'forward_nav.gif', 'funcdensity.png', 'gantt.png', 'ganttxml.png', 'garland16.png',
  'generic_editor.svg', 'gotoobj_tsk.svg', 'gotopage_menu.gif', 'group_by.gif',
  'header_complete.svg', 'header_priority.svg', 'help.gif', 'help_button.gif', 'help_contents.svg',
  'help_search.svg', 'help_view.svg', 'hide.gif', 'hide_arrows.gif', 'hide_empty_rows.png',
  'hide_lost_events.gif', 'hierarchy.gif', 'histogram.gif', 'history_view.svg', 'hunk_obj.svg',
  'ignoreprefs.svg', 'import.gif', 'import_brkpts.svg', 'import_button.png',
  'import_config_obj.svg', 'import_projectset.svg', 'import_trace.gif', 'import_wiz.svg',
  'importdir_wiz.svg', 'importpref_obj.svg', 'importzip_wiz.svg', 'index_co.svg', 'inst_ptr.svg',
  'inst_ptr_top.svg', 'install-handler.png', 'internal_browser.svg', 'ioanalysis.png', 'iu_obj.svg',
  'iu_update_obj.svg', 'jar_obj.svg', 'keyspref_obj.svg', 'last_edit_pos.svg', 'latency.png',
  'launchConfiguration.svg', 'lgroup_obj.svg', 'line_icon.gif', 'linechart.png', 'linechartxml.png',
  'link_ovr.gif', 'logger_disabled.gif', 'logger_enabled.gif', 'mark_occurrences.svg',
  'memory-usage.png', 'memory_view.svg', 'memoryreset_tsk.svg', 'memusage.png', 'minimap.svg',
  'module_view.svg', 'monitorexpression_tsk.svg', 'movedown_button.gif', 'moveup_button.gif',
  'nested_projects.svg', 'network.png', 'new.gif', 'new_con.svg', 'new_persp.svg',
  'new_tracing_prj.gif', 'new_untitled_text_file.svg', 'new_view.png', 'new_wiz.svg', 'new_xml.gif',
  'newfile_wiz.svg', 'newfolder_wiz.svg', 'newprj_wiz.svg', 'newSampleProject_wiz.gif',
  'next_bookmark.gif', 'next_edit_pos.svg', 'next_event.gif', 'next_item.gif', 'next_menu.gif',
  'next_nav.svg', 'node_end.gif', 'node_obj.gif', 'node_start.gif', 'ok_tbl.gif',
  'onboarding_resource_persp.png', 'open.gif', 'Optimization.png', 'os_overview.png',
  'outline_co.svg', 'package_obj.gif', 'paste_edit.svg',
  'pause_trace.gif', 'pcap.png', 'pcapng.png', 'pin.svg', 'pin_view.gif', 'plus_obj.gif',
  'prev_bookmark.gif', 'prev_event.gif', 'prev_item.gif', 'prev_menu.gif', 'prev_nav.svg',
  'print_edit.svg', 'prj_obj.svg', 'problems_view.svg', 'process_obj.gif', 'profile.gif',
  'profile_exc.svg', 'project_view.gif', 'prop_ps.svg', 'providers.gif', 'pview.svg',
  'readwrite_obj.svg', 'redo_edit.svg', 'refresh.gif', 'refresh_nav.svg', 'register_view.svg',
  'rem_all_co.svg', 'rem_all_triggers.svg', 'rem_co.svg', 'remove.gif', 'remove_bookmark.gif',
  'removefromworkset.svg', 'removememory_tsk.svg', 'rename.gif', 'resource_persp.svg',
  'resources_view.gif', 'resume_co.svg', 'rldgrmr.gif', 'run_exc.svg', 'runlast_co.svg',
  'runtime.gif', 'runtoline_co.svg', 'save_button.gif', 'save_edit.svg', 'saveall_edit.svg',
  'saveas_edit.svg', 'scatter.png', 'scatterchart.png', 'search.svg', 'search_match.gif',
  'search_match_bookmark.gif', 'search_menu.gif', 'search_seqdiag_menu.gif', 'searchm_obj.svg',
  'searchres.svg', 'segment_edit.svg', 'select_all.svg', 'select_menu.gif',
  'sequencediagram_view.gif', 'session_active.gif', 'session_destroyed.gif', 'session_inactive.gif',
  'sessions.gif', 'set_trace_type.gif', 'shift_l_edit.gif', 'shift_r_edit.gif',
  'show_hist_traces.gif', 'show_legend.gif', 'show_whitespace_chars.svg', 'sigma.gif',
  'sinfocenter_obj.svg', 'skip_brkp.svg', 'slocalhelp_obj.svg', 'snapshot.gif', 'sort_alpha.gif',
  'sort_alpha_rev.gif', 'sort_num.gif', 'sort_num_rev.gif', 'sort_time.gif', 'sort_time_rev.gif',
  'split_horizontal.svg', 'split_vertical.svg', 'start_trace.gif', 'statistics.png',
  'statistics_view.gif', 'stckframe_obj.svg', 'stepbystep_co.svg', 'stepinto_co.svg',
  'stepover_co.svg', 'stepreturn_co.svg', 'stop_trace.gif', 'stream_list_view.gif',
  'suspend_co.svg', 'sweb_obj.svg', 'swslatency.png', 'synced.svg', 'synch_participants.svg',
  'synch_synch.svg', 'systemcalls.png', 'tag-generic.gif', 'tag-macro.gif', 'target_add.gif',
  'target_connected.gif', 'target_delete.gif', 'target_disconnected.gif', 'targets.gif',
  'tasks_tsk.svg', 'terminate_all_co.svg', 'terminate_co.svg', 'text_mapping_file.gif',
  'thread_obj.gif', 'threads.png', 'timechart_view.gif', 'toggledetailpane_co.svg', 'toLower.svg',
  'toUpper.svg', 'trace.gif', 'trace_group.gif', 'trace_import_wiz.png',
  'tracing_pers.gif', 'tree_explorer.svg', 'tsearch_obj.svg', 'undo_edit.svg',
  'unknown_parser.gif', 'unpinned_view.gif', 'up_button.gif',
  'var_cntnt_prvdr.svg', 'variable_view.svg', 'watch_exp.svg', 'watchlist_view.svg',
  'welcome16.svg', 'welcome_editor.svg', 'wordwrap.svg', 'workingsets.svg', 'workset.svg',
  'workspace_obj.svg', 'xml_perspective.gif', 'xmldoc.gif', 'zoomIn.svg', 'zoomin_nav.gif',
  'zoomin_obj.bmp', 'zoomOut.svg', 'zoomout_nav.gif', 'zoomout_obj.bmp',
];

/// A file whose icon key has a drawing shows that drawing: the toolbar's label printing and the
/// search field's clear button load their images by file, other parts by `icon://` key.
const _filesOfKeys = {
  'patientenetikette.png': 'patientetikette',
  'auftragsetikette.ico': 'versionedetikette',
  'cross_small.png': 'clear',
  'documents_stack.png': 'document_stack',
  'bbs_perspective.png': 'perspective_bbs',
  'kontakt_perspective.png': 'perspective_contacts',
  'leistung_perspective.png': 'perspective_leistungen',
};

/// Read from the asset manifest: a case-insensitive file system would also find `moveToLowerList`
/// under `movetolowerlist.svg`.
Future<bool> _inPack(String name) async {
  final manifest = await AssetManifest.loadFromAssetBundle(rootBundle);
  return manifest.listAssets().contains('assets/icons/$name.svg');
}

/// [replacement] is a pack SVG Evolve can draw in the theme's ink.
Future<void> _expectPackStyle(Object? replacement, String name) async {
  expect(replacement, isA<String>(), reason: '$name has a pack icon');
  final svg = replacement as String;
  expect(svg, contains('viewBox="0 0 24 24"'));
  expect(RegExp(r'#[0-9a-fA-F]{3,6}').allMatches(svg).map((m) => m[0]).toSet(),
      anyOf(isEmpty, {'#000000'}),
      reason: 'one ink colour, so the theme tints it');
  final info = await vg.loadPicture(SvgStringLoader(svg), null);
  expect(info.size, const Size(24, 24));
  info.picture.dispose();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    AssetsManager.resetForTesting();
    applyConfigFlags(ConfigFlags());
  });

  for (final MapEntry(key: alias, value: icon) in _aliases.entries) {
    test('$alias shows the pack\'s $icon', () async {
      final expected = await AssetsManager.loadReplacement(icon);
      expect(expected, isA<String>(), reason: '$icon is in the pack');
      expect(await AssetsManager.loadReplacement(alias), expected);
    });
  }

  for (final name in _appIconKeys) {
    test('icon://IMG_${name.toUpperCase()} shows a pack icon in the set\'s style', () async {
      await _expectPackStyle(await AssetsManager.loadReplacement(name), name);
    });
  }

  for (final file in _appIconFiles) {
    test('a bundle file named $file shows a pack icon', () async {
      final replacement = await AssetsManager.loadReplacement('icons/16x16/$file');

      expect(replacement, isA<String>(), reason: '$file has a pack icon');
      expect(replacement as String, contains('<svg'));
    });
  }

  for (final file in _idePluginIconFiles) {
    test('a plugin image named $file shows a pack icon in the set\'s style', () async {
      await _expectPackStyle(await AssetsManager.loadReplacement('icons/full/obj16/$file'), file);
    });
  }

  for (final file in _traceViewerIconFiles) {
    test('a trace viewer image named $file shows a pack icon in the set\'s style', () async {
      await _expectPackStyle(await AssetsManager.loadReplacement('icons/obj16/$file'), file);
    });
  }

  for (final MapEntry(key: file, value: key) in _filesOfKeys.entries) {
    test('a bundle file named $file shows the drawing of icon key $key', () async {
      final expected = await AssetsManager.loadReplacement(key);
      expect(expected, isA<String>(), reason: '$key is in the pack');
      expect(await AssetsManager.loadReplacement('icons/16x16/$file'), expected);
    });
  }

  test('the (re)start icon draws its start arrow, not an empty frame', () async {
    for (final name in ['term_restart', 'restart']) {
      final svg = await AssetsManager.loadReplacement(name) as String;
      expect(svg, contains('fill="#000000"'), reason: '$name fills its start arrow');
    }
  });

  test('every alias names a drawing the pack has', () async {
    for (final MapEntry(key: alias, value: icon) in AssetsManager.iconAliases.entries) {
      expect(await _inPack(icon), isTrue, reason: '$alias -> $icon');
    }
  });

  test('no alias hides a drawing the pack has under that name', () async {
    expect(await _inPack('add'), isTrue);
    for (final alias in AssetsManager.iconAliases.keys) {
      expect(await _inPack(alias), isFalse, reason: '$alias has its own SVG');
    }
  });
}
