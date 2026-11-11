import 'dart:async';
import 'dart:typed_data';

import 'package:fake_async/fake_async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_browser.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel, wireChannelNames;
import 'support/channels.dart';

/// The comm socket `web/index.html` opens at navigation, taken over by the client once `main()`
/// runs: what it buffered is delivered in order, its state is the client's, and without one the
/// client connects on its own.
const _url = 'ws://localhost:4321';

class _FakeSocket implements CommSocket {
  _FakeSocket({this.state = _State.connecting});

  _State state;
  final List<String> sent = [];
  bool closed = false;
  void Function()? _onOpen;
  void Function(Uint8List)? _onMessage;
  void Function()? _onDead;

  @override
  bool get isConnecting => state == _State.connecting;

  @override
  bool get isOpen => state == _State.open;

  @override
  void send(Uint8List frame) => sent.add(EquoCommBase.decodeFrame(frame).$1);

  @override
  void close() {
    closed = true;
    state = _State.dead;
  }

  @override
  void listen({
    required void Function() onOpen,
    required void Function(Uint8List frame) onMessage,
    required void Function() onDead,
  }) {
    _onOpen = onOpen;
    _onMessage = onMessage;
    _onDead = onDead;
  }

  void open() {
    state = _State.open;
    _onOpen?.call();
  }

  /// A browser delivers a message as a task of its own.
  void receive(int n) => Timer.run(() => _onMessage?.call(_frame(n)));

  void drop() {
    state = _State.dead;
    _onDead?.call();
  }
}

enum _State { connecting, open, dead }

/// A frame on one raw-bytes channel carrying the single byte [n].
Uint8List _frame(int n) => frame(_channel, Uint8List.fromList([n]));

const _channel = 'GC/7/imageInit';

class _Connector {
  final List<_FakeSocket> opened = [];
  _State next = _State.connecting;

  CommSocket call(String url) {
    expect(url, _url);
    final socket = _FakeSocket(state: next);
    opened.add(socket);
    return socket;
  }
}

void main() {
  test('what the page socket buffered is delivered in arrival order, ahead of later frames', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.open);
      final connect = _Connector();
      final comm = BrowserComm(_url, connect.call,
          page: PageSocket(_url, page, [_frame(1), _frame(2)],
              everOpened: true));
      page.receive(3);

      final received = <int>[];
      comm.onBytesNamed(_channel, (bytes) => received.add(bytes.single));
      async.elapse(Duration.zero);

      expect(received, [1, 2, 3]);
      expect(connect.opened, isEmpty, reason: 'the page socket is used, no second one is opened');
    });
  });

  test('what the page socket buffered arrives after the listeners registered right after the comm', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.open);
      final comm = BrowserComm(_url, _Connector().call,
          page: PageSocket(_url, page, [frame(wireChannelNames[WireChannel.gcCreate]!, Uint8List.fromList([9]))],
              everOpened: true));
      // A channel answered as it arrives: a frame that comes before its handler takes another path.
      final arrived = <int>[];
      comm.onBytesAt(WireChannel.gcCreate, (data, start, end) => arrived.add(data[start]));
      async.flushMicrotasks();

      expect(arrived, [9]);
    });
  });

  test('an open page socket counts as open at once, and sends go out on it', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.open);
      final comm = BrowserComm(_url, _Connector().call, page: PageSocket(_url, page, [], everOpened: true));
      var firstOpen = false;
      comm.firstOpen.then((_) => firstOpen = true);
      comm.sendNamed('Display/1/ClientReady');
      async.flushMicrotasks();

      expect(firstOpen, isTrue);
      expect(page.sent, ['Display/1/ClientReady']);
    });
  });

  test('a page socket still connecting is waited for, not replaced', () {
    fakeAsync((async) {
      final page = _FakeSocket();
      final connect = _Connector();
      final comm = BrowserComm(_url, connect.call, page: PageSocket(_url, page, [], everOpened: false));
      comm.sendNamed('Display/1/ClientReady');
      async.elapse(const Duration(seconds: 1));
      expect(page.sent, isEmpty);
      expect(connect.opened, isEmpty);

      page.open();
      expect(page.sent, ['Display/1/ClientReady']);
    });
  });

  test('a page socket that drops is reopened, and the client resyncs', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.open);
      final connect = _Connector()..next = _State.open;
      final comm = BrowserComm(_url, connect.call, page: PageSocket(_url, page, [], everOpened: true));
      var reconnects = 0;
      comm.onReconnected = () => reconnects++;
      async.flushMicrotasks();

      page.drop();
      comm.sendNamed('Button/1/Selection/Selection');
      async.elapse(const Duration(seconds: 1));
      expect(connect.opened, hasLength(1));
      connect.opened.single.open();

      expect(reconnects, 1);
      expect(page.sent, isEmpty);
      expect(connect.opened.single.sent, ['Button/1/Selection/Selection']);
    });
  });

  test('a page socket that opened and dropped before the client ran still hands over its frames', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.dead);
      final connect = _Connector();
      final comm = BrowserComm(_url, connect.call,
          page: PageSocket(_url, page, [_frame(1)], everOpened: true));
      final received = <int>[];
      comm.onBytesNamed(_channel, (bytes) => received.add(bytes.single));
      async.flushMicrotasks();
      expect(received, [1]);

      async.elapse(const Duration(seconds: 1));
      expect(connect.opened, hasLength(1));
    });
  });

  test('without a page socket the client connects on its own', () {
    fakeAsync((async) {
      final connect = _Connector();
      final comm = BrowserComm(_url, connect.call);
      expect(connect.opened, hasLength(1));

      comm.sendNamed('Display/1/ClientReady');
      connect.opened.single.open();
      expect(connect.opened.single.sent, ['Display/1/ClientReady']);
    });
  });

  test('a page socket refused before it opened is dropped for one of the client\'s own, at once', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.dead);
      final connect = _Connector();
      BrowserComm(_url, connect.call, page: PageSocket(_url, page, [], everOpened: false));
      expect(connect.opened, hasLength(1));
    });
  });

  test('a page socket for another URL is closed and the client connects to its own', () {
    fakeAsync((async) {
      final page = _FakeSocket(state: _State.open);
      final connect = _Connector();
      BrowserComm(_url, connect.call,
          page: PageSocket('ws://localhost:1', page, [_frame(1)], everOpened: true));
      expect(page.closed, isTrue);
      expect(connect.opened, hasLength(1));
    });
  });
}
