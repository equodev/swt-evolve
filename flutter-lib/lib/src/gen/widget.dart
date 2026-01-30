import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import 'event.dart';
import 'widgets.dart';

part 'widget.g.dart';

abstract class WidgetSwt<V extends VWidget> extends StatefulWidget {
  final V value;
  const WidgetSwt({super.key, required this.value});

  void sendEvent(V val, String ev, VEvent? payload) {
    // print("send ${val.swt}/${val.id}/$ev");
    if (payload == null) {
      EquoCommService.send("${val.swt}/${val.id}/$ev");
    } else {
      EquoCommService.sendPayload("${val.swt}/${val.id}/$ev", payload);
    }
  }

  void sendDisposeDispose(V val, VEvent? payload) {
    sendEvent(val, "Dispose/Dispose", payload);
  }
}

abstract class WidgetSwtState<T extends WidgetSwt, V extends VWidget>
    extends State<T> {
  late V state;

  @override
  void initState() {
    super.initState();
    state = widget.value as V;
    // print("${state.swt} ${state.id} initState");
    EquoCommService.on("${state.swt}/${state.id}", _onChange);
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    // print("${state.swt} ${state.id} didUpdateWidget");
    state = widget.value as V;
    extraSetState();
  }

  @protected
  void setValue(V value) {
    setState(() {
      state = value!;
      extraSetState();
    });
  }

  void _onChange(V payload) {
    // print('On Widget Change, payload: $payload');
    setValue(payload);
  }

  void extraSetState() {}

  void onOp(String op, void Function(dynamic) handler) {
    EquoCommService.onRaw("${state.swt}/${state.id}/$op", (opArgs) {
      print('OnOp: "${state.swt}/${state.id}/$op" args: ${opArgs}');
      handler(jsonDecode(opArgs as String));
    });
  }
}

@JsonSerializable()
class VWidget {
  VWidget() : this.empty();
  VWidget.empty() : swt = "Widget", id = 1, style = 0;

  String swt;
  int id;
  int style;

  factory VWidget.fromJson(Map<String, dynamic> json) => mapWidgetValue(json);
  Map<String, dynamic> toJson() =>
      throw UnsupportedError("Unsupported toJson in Widget");
}
