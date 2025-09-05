import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/impl/utils/showdialog.dart';

import '../comm/comm.dart';
import '../swt/shell.dart';
import '../widgets.dart';
import 'decorations_impl.dart';
import '../../native_platform.dart'
    if (dart.library.html) '../../web_platform.dart';

class ShellImpl<T extends ShellSwt<V>, V extends ShellValue>
    extends DecorationsImpl<T, V> {
  @override
  void initState() {
    super.initState();
    EquoCommService.onRaw("MessageBox/open", _onMessageBox);
    EquoCommService.onRaw("${state.swt}/${state.id}/disposed", _onClose);
  }

  void _onMessageBox(dynamic payload) {
    print('OnRaw dialog: $payload');
    showCustomDialog(context, payload);
  }

  void _onClose(Object? payload) {
    print('${state.swt}/${state.id} disposed: $payload');
    close();
  }

  @override
  Widget build(BuildContext context) {
    if (state.children != null) {
      print("ShellImpl.build ${state.children!.length}");
      var children = state.children!;
      return mapLayout(state, state.layout, children);
    }
    return const SizedBox();
//    return Wrap(children: children.map(mapWidget).toList());
//     return Row(
//         // mainAxisAlignment:MainAxisAlignment.start,
//         // verticalDirection: VerticalDirection.down,
//         children: children.map(mapWidget)
//             .map((e) => Expanded(child: e)).toList());
  }
}
