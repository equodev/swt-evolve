import 'dart:async';
import 'dart:collection';
import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../gen/directorydialog.dart';
import '../gen/filedialog.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widgets.dart' show WireChannel, WireType;
import '../theme/theme_extensions/combo_theme_extension.dart';
import '../theme/theme_extensions/filedialog_theme_extension.dart';
import '../theme/theme_extensions/list_theme_extension.dart';
import '../theme/theme_extensions/text_theme_extension.dart';
import '../theme/theme_settings/list_theme_settings.dart';
import '../theme/theme_settings/text_theme_settings.dart';
import 'utils/dialog_button.dart';
import 'utils/pointer.dart';

/// Ends each part of a chooser's answer; Java reads it in `ServerFiles.choice`.
const String choiceSeparator = '\u0000';

/// What the chooser shows of one directory of the server's file system, as Java lists it
/// (`ServerFiles.listing`).
class ServerListing {
  final String path;
  final String parent;
  final String separator;
  final List<String> roots;
  final List<String> dirs;
  final List<String> files;

  /// False when the New Folder this listing answers could not be made.
  final bool created;

  const ServerListing({
    required this.path,
    this.parent = '',
    this.separator = '/',
    this.roots = const [],
    this.dirs = const [],
    this.files = const [],
    this.created = true,
  });

  factory ServerListing.fromMap(Map<Object?, Object?> m) => ServerListing(
        path: m['path'] as String? ?? '',
        parent: m['parent'] as String? ?? '',
        separator: m['separator'] as String? ?? '/',
        roots: _strings(m['roots']),
        dirs: _strings(m['dirs']),
        files: _strings(m['files']),
        created: m['created'] as bool? ?? true,
      );

  static List<String> _strings(Object? v) =>
      v is List ? v.whereType<String>().toList() : const <String>[];
}

/// What kind of chooser is drawn: SWT.OPEN, SWT.OPEN | SWT.MULTI, SWT.SAVE, or a DirectoryDialog.
enum ChooserMode { open, multi, save, directory }

/// The answer Java reads: the filter index, the directory, then the chosen names.
String encodeChoice(int filterIndex, String directory, List<String> names) =>
    [filterIndex.toString(), directory, ...names].map((p) => '$p$choiceSeparator').join();

/// The chooser mode a FileDialog's style asks for.
ChooserMode fileDialogMode(int style) => (style & SWT.SAVE) != 0
    ? ChooserMode.save
    : (style & SWT.MULTI) != 0
        ? ChooserMode.multi
        : ChooserMode.open;

List<String> _present(List<String?>? values) => values?.whereType<String>().toList() ?? const [];

void showFileDialog(BuildContext context, VFileDialog value, int dialogId) {
  final mode = fileDialogMode(value.style ?? 0);
  _showChooser(
    context,
    WireType.fileDialog,
    dialogId,
    title: (value.text?.isNotEmpty ?? false) ? value.text! : (mode == ChooserMode.save ? 'Save' : 'Open'),
    mode: mode,
    startPath: value.filterPath ?? '',
    fileName: value.fileName ?? '',
    filterNames: _present(value.filterNames),
    filterExtensions: _present(value.filterExtensions),
    filterIndex: math.max(0, value.filterIndex ?? 0),
    overwrite: value.overwrite ?? false,
  );
}

void showDirectoryDialog(BuildContext context, VDirectoryDialog value, int dialogId) {
  _showChooser(
    context,
    WireType.directoryDialog,
    dialogId,
    title: (value.text?.isNotEmpty ?? false) ? value.text! : 'Select Folder',
    mode: ChooserMode.directory,
    message: value.message ?? '',
    startPath: value.filterPath ?? '',
  );
}

/// Opens the chooser for dialog [dialogId]: each directory it shows is asked of Java on
/// `navigateRequest` and arrives on `navigate`, in order; the answer goes back on `close`.
void _showChooser(
  BuildContext context,
  int type,
  int dialogId, {
  required String title,
  required ChooserMode mode,
  String message = '',
  String startPath = '',
  String fileName = '',
  List<String> filterNames = const [],
  List<String> filterExtensions = const [],
  int filterIndex = 0,
  bool overwrite = false,
}) {
  final pending = Queue<Completer<ServerListing>>();
  final token = EquoCommService.on(type, dialogId, WireChannel.navigate, (dynamic payload) {
    if (pending.isEmpty) return;
    pending.removeFirst().complete(
        payload is Map ? ServerListing.fromMap(payload) : const ServerListing(path: ''));
  });
  Future<ServerListing> ask(String request) {
    final reply = Completer<ServerListing>();
    pending.add(reply);
    EquoCommService.sendTo(type, dialogId, WireChannel.navigateRequest, request);
    return reply.future;
  }

  showDialog<void>(
    context: context,
    barrierDismissible: false,
    builder: (ctx) => pointerInterceptor(FileDialogContent(
      title: title,
      mode: mode,
      message: message,
      startPath: startPath,
      fileName: fileName,
      filterNames: filterNames,
      filterExtensions: filterExtensions,
      filterIndex: filterIndex,
      overwrite: overwrite,
      list: (path, index) => ask(encodeChoice(index, path, const [])),
      createFolder: (path, name, index) => ask(encodeChoice(index, path, [name])),
      onResult: (answer) {
        Navigator.pop(ctx);
        EquoCommService.sendTo(type, dialogId, WireChannel.close, answer ?? '');
      },
    )),
  ).whenComplete(() => EquoCommService.remove(type, dialogId, WireChannel.navigate, token));
}

/// A chooser over the server's file system. Transport-free: it asks for each directory through
/// [list], for a New Folder through [createFolder], and reports the encoded answer, or null for a
/// cancel, through [onResult].
class FileDialogContent extends StatefulWidget {
  final String title;
  final ChooserMode mode;
  final String message;
  final String startPath;
  final String fileName;
  final List<String> filterNames;
  final List<String> filterExtensions;
  final int filterIndex;

  /// A SAVE chooser asks before it answers a name that already exists.
  final bool overwrite;
  final Future<ServerListing> Function(String path, int filterIndex) list;

  /// Makes the folder `name` in `path` and answers the listing then shown; with none, there is no
  /// New Folder button.
  final Future<ServerListing> Function(String path, String name, int filterIndex)? createFolder;
  final ValueChanged<String?> onResult;

  const FileDialogContent({
    super.key,
    required this.title,
    required this.mode,
    required this.list,
    required this.onResult,
    this.createFolder,
    this.message = '',
    this.startPath = '',
    this.fileName = '',
    this.filterNames = const [],
    this.filterExtensions = const [],
    this.filterIndex = 0,
    this.overwrite = false,
  });

  @override
  State<FileDialogContent> createState() => FileDialogContentState();
}

class FileDialogContentState extends State<FileDialogContent> {
  ServerListing? _listing;
  late int _filterIndex;
  final Set<String> _selected = {};
  late final TextEditingController _name;
  int _request = 0;

  @override
  void initState() {
    super.initState();
    _filterIndex = widget.filterExtensions.isEmpty
        ? 0
        : widget.filterIndex.clamp(0, widget.filterExtensions.length - 1);
    _name = TextEditingController(text: widget.fileName);
    _go(widget.startPath);
  }

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  Future<void> _go(String path) async {
    final request = ++_request;
    final listing = await widget.list(path, _filterIndex);
    if (!mounted || request != _request) return;
    setState(() {
      _listing = listing;
      _selected.clear();
    });
  }

  void _tapFile(String name) {
    setState(() {
      if (widget.mode == ChooserMode.multi) {
        if (!_selected.remove(name)) _selected.add(name);
      } else {
        _selected
          ..clear()
          ..add(name);
      }
      if (widget.mode == ChooserMode.save) _name.text = name;
    });
  }

  Future<void> _newFolder() async {
    final listing = _listing;
    final createFolder = widget.createFolder;
    if (listing == null || createFolder == null) return;
    final name = await showDialog<String>(context: context, builder: (_) => const _NewFolderPrompt());
    if (name == null || !mounted) return;
    final request = ++_request;
    final shown = await createFolder(listing.path, name, _filterIndex);
    if (!mounted || request != _request) return;
    setState(() {
      _listing = shown;
      _selected.clear();
    });
    if (!shown.created) {
      await showDialog<void>(
        context: context,
        builder: (ctx) => _ChooserFrame.prompt(
          key: const ValueKey('fileDialog.newFolder.failed'),
          message: 'The folder "$name" could not be created.',
          buttons: [DialogButton(label: 'OK', isPrimary: true, onPressed: () => Navigator.pop(ctx))],
        ),
      );
    }
  }

  String _join(String dir, String name) {
    final sep = _listing?.separator ?? '/';
    return dir.endsWith(sep) ? '$dir$name' : '$dir$sep$name';
  }

  bool get _canAccept {
    if (_listing == null) return false;
    if (widget.mode == ChooserMode.directory) return true;
    if (widget.mode == ChooserMode.save) return _name.text.trim().isNotEmpty;
    return _selected.isNotEmpty;
  }

  Future<void> _accept() async {
    final listing = _listing;
    if (listing == null) return;
    if (widget.mode == ChooserMode.directory) {
      final dir = _selected.length == 1 ? _join(listing.path, _selected.first) : listing.path;
      widget.onResult(encodeChoice(_filterIndex, dir, const []));
      return;
    }
    final names = widget.mode == ChooserMode.save
        ? [_name.text.trim()]
        : listing.files.where(_selected.contains).toList();
    if (widget.mode == ChooserMode.save && widget.overwrite && listing.files.contains(names.first)) {
      final replace = await showDialog<bool>(
        context: context,
        builder: (ctx) => _ChooserFrame.prompt(
          message: '${names.first} already exists. Do you want to replace it?',
          buttons: [
            DialogButton(
              key: const ValueKey('fileDialog.overwrite.cancel'),
              label: 'Cancel',
              isPrimary: false,
              onPressed: () => Navigator.pop(ctx, false),
            ),
            DialogButton(
              key: const ValueKey('fileDialog.overwrite.replace'),
              label: 'Replace',
              isPrimary: true,
              onPressed: () => Navigator.pop(ctx, true),
            ),
          ],
        ),
      );
      if (replace != true || !mounted) return;
    }
    widget.onResult(encodeChoice(_filterIndex, listing.path, names));
  }


  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<FileDialogThemeExtension>()!;
    final listing = _listing;
    final dirsOnly = widget.mode == ChooserMode.directory;
    final save = widget.mode == ChooserMode.save;
    return _ChooserFrame(
      title: widget.title,
      width: theme.width,
      content: SizedBox(
        height: theme.height,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (widget.message.isNotEmpty) ...[
              Text(widget.message, style: theme.messageStyle),
              SizedBox(height: theme.sectionSpacing),
            ],
            Row(children: [
              _ChooserIconButton(
                key: const ValueKey('fileDialog.up'),
                tooltip: 'Up',
                icon: Icons.arrow_upward,
                onPressed: listing == null || listing.parent.isEmpty ? null : () => _go(listing.parent),
              ),
              Expanded(
                child: Text(listing?.path ?? '',
                    key: const ValueKey('fileDialog.path'), style: theme.pathStyle, overflow: TextOverflow.ellipsis),
              ),
              if (listing != null && listing.roots.length > 1)
                PopupMenuButton<String>(
                  tooltip: 'Roots',
                  color: theme.backgroundColor,
                  icon: Icon(Icons.storage, color: theme.iconColor, size: theme.iconSize),
                  onSelected: _go,
                  itemBuilder: (_) => [
                    for (final r in listing.roots) PopupMenuItem(value: r, child: Text(r, style: theme.pathStyle)),
                  ],
                ),
            ]),
            SizedBox(height: theme.sectionSpacing),
            Expanded(child: _ListingBox(child: listing == null ? null : _rows(listing, dirsOnly))),
            if (save || (!dirsOnly && widget.filterExtensions.isNotEmpty)) ...[
              SizedBox(height: theme.sectionSpacing),
              Row(children: [
                if (save) ...[
                  Text('Name', style: theme.labelStyle),
                  SizedBox(width: theme.sectionSpacing),
                  Expanded(
                    child: _ChooserTextField(
                      key: const ValueKey('fileDialog.name'),
                      controller: _name,
                      onChanged: (_) => setState(() => _selected.clear()),
                      onSubmitted: (_) => _canAccept ? _accept() : null,
                    ),
                  ),
                ] else
                  const Spacer(),
                if (widget.filterExtensions.isNotEmpty) ...[
                  SizedBox(width: theme.sectionSpacing),
                  _filter(listing),
                ],
              ]),
            ],
          ],
        ),
      ),
      leading: [
        if (widget.createFolder != null)
          DialogButton(
            key: const ValueKey('fileDialog.newFolder'),
            label: 'New Folder',
            isPrimary: false,
            onPressed: listing == null ? null : _newFolder,
          ),
      ],
      buttons: [
        DialogButton(
          key: const ValueKey('fileDialog.cancel'),
          label: 'Cancel',
          isPrimary: false,
          onPressed: () => widget.onResult(null),
        ),
        DialogButton(
          key: const ValueKey('fileDialog.ok'),
          label: save ? 'Save' : (dirsOnly ? 'Choose' : 'Open'),
          isPrimary: true,
          onPressed: _canAccept ? _accept : null,
        ),
      ],
    );
  }

  Widget _rows(ServerListing listing, bool dirsOnly) => ListView(
        key: const ValueKey('fileDialog.list'),
        children: [
          for (final d in listing.dirs)
            _ChooserRow(
              name: d,
              folder: true,
              selected: dirsOnly && _selected.contains(d),
              onTap: dirsOnly
                  ? () => setState(() => _selected
                    ..clear()
                    ..add(d))
                  : () => _go(_join(listing.path, d)),
              onLongPress: () => _go(_join(listing.path, d)),
              trailing: dirsOnly
                  ? _ChooserIconButton(
                      tooltip: 'Open',
                      icon: Icons.chevron_right,
                      onPressed: () => _go(_join(listing.path, d)),
                    )
                  : null,
            ),
          for (final f in listing.files)
            _ChooserRow(name: f, folder: false, selected: _selected.contains(f), onTap: () => _tapFile(f)),
        ],
      );

  Widget _filter(ServerListing? listing) {
    final combo = Theme.of(context).extension<ComboThemeExtension>()!;
    return DropdownButton<int>(
      key: const ValueKey('fileDialog.filter'),
      value: _filterIndex,
      isDense: true,
      style: (combo.textStyle ?? const TextStyle()).copyWith(color: combo.textColor),
      dropdownColor: combo.backgroundColor,
      iconEnabledColor: combo.iconColor,
      iconSize: combo.iconSize,
      borderRadius: BorderRadius.circular(combo.borderRadius),
      underline: Container(height: combo.borderWidth, color: combo.borderColor),
      onChanged: (i) {
        if (i == null || listing == null) return;
        setState(() => _filterIndex = i);
        _go(listing.path);
      },
      items: [
        for (var i = 0; i < widget.filterExtensions.length; i++)
          DropdownMenuItem(
            value: i,
            child: Text(i < widget.filterNames.length ? widget.filterNames[i] : widget.filterExtensions[i]),
          ),
      ],
    );
  }
}

/// The chrome every chooser dialog shares, as the MessageBox and ColorDialog draw theirs: a title,
/// the content, then [leading] buttons on the left and [buttons] on the right.
class _ChooserFrame extends StatelessWidget {
  final String? title;
  final Widget content;
  final double? width;
  final List<Widget> leading;
  final List<Widget> buttons;

  const _ChooserFrame({
    this.title,
    required this.content,
    required this.buttons,
    this.width,
    this.leading = const [],
  });

  /// A small message over its [buttons], for a question the chooser asks.
  _ChooserFrame.prompt({super.key, this.title, String? message, Widget? content, required this.buttons})
      : content = content ?? _PromptMessage(message ?? ''),
        width = null,
        leading = const [];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<FileDialogThemeExtension>()!;
    final title = this.title;
    return Dialog(
      backgroundColor: theme.backgroundColor,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(theme.borderRadius)),
      child: SizedBox(
        width: width ?? theme.promptWidth,
        child: Padding(
          padding: theme.padding,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (title != null) ...[
                Text(title, style: theme.titleStyle),
                SizedBox(height: theme.titleContentSpacing),
              ],
              content,
              SizedBox(height: theme.contentButtonsSpacing),
              Row(children: [
                ...leading,
                const Spacer(),
                for (var i = 0; i < buttons.length; i++) ...[
                  if (i > 0) SizedBox(width: theme.buttonSpacing),
                  buttons[i],
                ],
              ]),
            ],
          ),
        ),
      ),
    );
  }
}

class _PromptMessage extends StatelessWidget {
  final String message;

  const _PromptMessage(this.message);

  @override
  Widget build(BuildContext context) =>
      Text(message, style: Theme.of(context).extension<FileDialogThemeExtension>()!.messageStyle);
}

/// The framed box the listing sits in, drawn like a bordered SWT List; a progress ring while
/// [child] is null.
class _ListingBox extends StatelessWidget {
  final Widget? child;

  const _ListingBox({required this.child});

  @override
  Widget build(BuildContext context) {
    final list = Theme.of(context).extension<ListThemeExtension>()!;
    final theme = Theme.of(context).extension<FileDialogThemeExtension>()!;
    // Container insets the child by the border's width, so the rows paint inside the border.
    return Container(
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        color: list.backgroundColor,
        borderRadius: BorderRadius.circular(list.borderRadius),
        border: Border.all(color: list.borderColor, width: list.borderWidth),
      ),
      child: child ?? Center(child: CircularProgressIndicator(color: theme.progressColor)),
    );
  }
}

/// One entry of the listing, drawn like a row of an SWT List.
class _ChooserRow extends StatefulWidget {
  final String name;
  final bool folder;
  final bool selected;
  final VoidCallback onTap;
  final VoidCallback? onLongPress;
  final Widget? trailing;

  const _ChooserRow({
    required this.name,
    required this.folder,
    required this.selected,
    required this.onTap,
    this.onLongPress,
    this.trailing,
  });

  @override
  State<_ChooserRow> createState() => _ChooserRowState();
}

class _ChooserRowState extends State<_ChooserRow> {
  bool _hovered = false;

  @override
  Widget build(BuildContext context) {
    final list = Theme.of(context).extension<ListThemeExtension>()!;
    final theme = Theme.of(context).extension<FileDialogThemeExtension>()!;
    final overlay = widget.selected
        ? list.selectedItemBackgroundColor.withOpacity(theme.selectedOverlayOpacity)
        : list.hoverItemBackgroundColor.withOpacity(_hovered ? theme.hoverOverlayOpacity : 0);
    final textColor = getListItemTextColor(context, list, widget.selected, true);
    final trailing = widget.trailing;
    return MouseRegion(
      onEnter: (_) => setState(() => _hovered = true),
      onExit: (_) => setState(() => _hovered = false),
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: widget.onTap,
        onLongPress: widget.onLongPress,
        child: Container(
          height: list.itemHeight,
          color: Color.alphaBlend(overlay, list.backgroundColor),
          padding: list.itemPadding,
          child: Row(children: [
            Icon(
              widget.folder ? Icons.folder : Icons.insert_drive_file_outlined,
              size: theme.rowIconSize,
              color: widget.folder ? theme.folderIconColor : theme.fileIconColor,
            ),
            SizedBox(width: theme.rowIconSpacing),
            Expanded(
              child: Text(widget.name,
                  style: (list.textStyle ?? const TextStyle()).copyWith(color: textColor),
                  overflow: TextOverflow.ellipsis),
            ),
            if (trailing != null) trailing,
          ]),
        ),
      ),
    );
  }
}

/// An icon action of the chooser, in the theme's icon colours.
class _ChooserIconButton extends StatelessWidget {
  final String tooltip;
  final IconData icon;
  final VoidCallback? onPressed;

  const _ChooserIconButton({super.key, required this.tooltip, required this.icon, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<FileDialogThemeExtension>()!;
    return IconButton(
      tooltip: tooltip,
      icon: Icon(icon),
      iconSize: theme.iconSize,
      color: theme.iconColor,
      disabledColor: theme.disabledIconColor,
      padding: EdgeInsets.zero,
      constraints: BoxConstraints.tightFor(width: theme.iconButtonSize, height: theme.iconButtonSize),
      onPressed: onPressed,
    );
  }
}

/// The chooser's one-line text field, drawn like a bordered SWT Text.
class _ChooserTextField extends StatelessWidget {
  final TextEditingController controller;
  final bool autofocus;
  final ValueChanged<String>? onChanged;
  final ValueChanged<String>? onSubmitted;

  const _ChooserTextField({
    super.key,
    required this.controller,
    this.autofocus = false,
    this.onChanged,
    this.onSubmitted,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).extension<TextThemeExtension>()!;
    final state = VText()
      ..style = SWT.BORDER | SWT.SINGLE
      ..enabled = true
      ..editable = true;
    return TextField(
      controller: controller,
      autofocus: autofocus,
      style: getTextFieldTextStyle(context, state, text),
      cursorColor: text.textColor,
      decoration: getInputDecoration(context, state, text, controller, controller.clear),
      onChanged: onChanged,
      onSubmitted: onSubmitted,
    );
  }
}

/// Asks the name of a New Folder; answers it, or null for a cancel.
class _NewFolderPrompt extends StatefulWidget {
  const _NewFolderPrompt();

  @override
  State<_NewFolderPrompt> createState() => _NewFolderPromptState();
}

class _NewFolderPromptState extends State<_NewFolderPrompt> {
  final _name = TextEditingController(text: 'untitled folder');

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  void _create() {
    final name = _name.text.trim();
    if (name.isNotEmpty) Navigator.pop(context, name);
  }

  @override
  Widget build(BuildContext context) => _ChooserFrame.prompt(
        title: 'New Folder',
        content: _ChooserTextField(
          key: const ValueKey('fileDialog.newFolder.name'),
          controller: _name,
          autofocus: true,
          onChanged: (_) => setState(() {}),
          onSubmitted: (_) => _create(),
        ),
        buttons: [
          DialogButton(
            key: const ValueKey('fileDialog.newFolder.cancel'),
            label: 'Cancel',
            isPrimary: false,
            onPressed: () => Navigator.pop(context),
          ),
          DialogButton(
            key: const ValueKey('fileDialog.newFolder.create'),
            label: 'Create',
            isPrimary: true,
            onPressed: _name.text.trim().isEmpty ? null : _create,
          ),
        ],
      );
}
