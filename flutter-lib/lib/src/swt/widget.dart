import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'widget.g.dart';

abstract class WidgetSwt<V extends WidgetValue> extends StatefulWidget {
  final V value;

  const WidgetSwt({super.key, required this.value});

  void sendEvent(V val, String ev, Object? payload) {
    print("send ${val.swt}/${val.id}/$ev");
    if (payload == null) {
      EquoCommService.send("${val.swt}/${val.id}/$ev");
    } else {
      EquoCommService.sendPayload("${val.swt}/${val.id}/$ev", payload);
    }
  }

  void sendDisposeDispose(V val, Object? payload) {
    sendEvent(val, "Dispose/Dispose", payload);
  }
}

abstract class WidgetSwtState<T extends WidgetSwt, V extends WidgetValue>
    extends State<T> {
  late V state;

  @override
  void initState() {
    super.initState();
    state = widget.value as V;
    print("${state.swt} ${state.id} initState");
    EquoCommService.on("${state.swt}/${state.id}", _onChange);
  }

  @override
  void didUpdateWidget(covariant T oldWidget) {
    super.didUpdateWidget(oldWidget);
    print("${state.swt} ${state.id} didUpdateWidget");
    state = widget.value as V;
    extraSetState();
  }

  void _onChange(V payload) {
    print('On Widget Change, payload: $payload');

    _setValue(payload);
  }

  void _setValue(V value) {
    setState(() {
      state = value!;
      extraSetState();
    });
  }

  void extraSetState() {}
}

@JsonSerializable()
class WidgetValue {
  WidgetValue() : this.empty();
  WidgetValue.empty()
      : swt = "Widget",
        id = 1,
        style = 0;

  String swt;
  int id;
  List<WidgetValue>? children;
  int style;

  factory WidgetValue.fromJson(Map<String, dynamic> json) =>
      mapWidgetValue(json);
  Map<String, dynamic> toJson() =>
      throw UnsupportedError("Unsupported toJson in Widget");
}
