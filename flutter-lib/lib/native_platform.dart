// import 'package:bitsdojo_window/bitsdojo_window.dart';
import 'src/comm/comm.dart';

//late final ShellListener? listener;

int? getWidgetId(List<String> args) {
  if (args.isNotEmpty) {
    return int.parse(args[1]);
  }
  return null;
}

String? getWidgetName(List<String> args) {
    if (args.length >= 2) {
        return args[2];
    }
    return null;
}

String? getTheme(List<String> args) {
//  if (args.length >= 2) {
//    return args[2];
//  }
//  return null;
}

void close() {
  //if (listener != null) {
  //  windowManager.removeListener(listener!);
  //}
  //windowManager.close();
}

//class ShellListener extends WindowListener {
//  @override
//  void onWindowClose() {
//    print("onWindowClose");
//    EquoCommService.send("close");
//  }
//}
