import 'dart:async';
import 'dart:typed_data';

import '../testing/boot_trace.dart';
import 'comm_frame.dart';

/// The browser WebSocket as the web comm uses it, kept apart from `package:web` so the socket's
/// lifecycle can be tested on the VM.
abstract class CommSocket {
  bool get isConnecting;
  bool get isOpen;
  void send(Uint8List frame);
  void close();

  /// Replaces whatever handlers the socket had. [onDead] is called on an error and on a close.
  void listen({
    required void Function() onOpen,
    required void Function(Uint8List frame) onMessage,
    required void Function() onDead,
  });
}

/// A socket the page opened at navigation (see `web/index.html`), before the client was running.
class PageSocket {
  PageSocket(this.url, this.socket, this.frames, {required this.everOpened});

  final String url;
  final CommSocket socket;

  /// The binary frames that arrived before the client took the socket over, in arrival order.
  final List<Uint8List> frames;
  final bool everOpened;
}

/// Web comm: a browser WebSocket speaking the binary frame protocol. All protocol logic lives in
/// [EquoCommBase]; this class only owns the socket.
///
/// The socket is opened through [EquoCommBase.openSocket], so its whole lifecycle (retrying a
/// connect that never opened, reopening one that dropped) is the base's capped-backoff loop, and
/// sends buffer instead of reaching a dead wire in between. The first connect can fail transiently
/// (a cold headless browser can refuse the upgrade once), which without a retry strands the queued
/// `ClientReady` forever. A socket that dropped long after boot (an idle timeout, sleep/resume, a
/// network blip) must come back, or every later interaction is lost on a closed socket.
///
/// The first socket is normally the one the page opened at navigation: it is open by the time
/// `main()` runs and already holds what Java sent, where a socket opened here would only open once
/// the engine's first frames let the browser deliver the event.
class BrowserComm extends EquoCommBase {
  BrowserComm(this.url, this._connect, {PageSocket? page}) {
    BootTrace.markOnce('comm.connect');
    if (page != null && page.url == url && (page.everOpened || page.socket.isConnecting)) {
      _adopt(page);
    } else {
      // A socket for another URL, or one refused before it ever opened, is of no use: the client
      // connects on its own.
      page?.socket.close();
      openSocket();
    }
  }

  final String url;
  final CommSocket Function(String url) _connect;
  CommSocket? _socket;
  bool _wasOpen = false;

  @override
  void openSocket() => _listen(_connect(url));

  void _adopt(PageSocket page) {
    final socket = page.socket;
    _listen(socket);
    onTakenOver();
    // Handed over once the code that made this comm has registered its listeners, as a socket's
    // own events would be: a frame's handler is resolved when it arrives. The socket's next event
    // is a task of its own, so it still comes after these.
    scheduleMicrotask(() {
      for (final frame in page.frames) {
        receiveBinary(frame);
      }
      if (socket.isOpen) {
        _opened(socket);
      } else if (!socket.isConnecting) {
        // It opened and dropped before the client ran, so it is reopened as any drop is. Java takes
        // the new socket for a new client and describes everything to it again, and this client has
        // sent nothing yet, so its first ClientReady is a first one anyway: no resync to run.
        _wasOpen = true;
        _onDead(socket);
      }
    });
  }

  void _listen(CommSocket socket) {
    _socket = socket;
    socket.listen(
      onOpen: () => _opened(socket),
      onMessage: receiveBinary,
      // A superseded socket's late close must not tear down the one that replaced it.
      onDead: () => _onDead(socket),
    );
  }

  void _opened(CommSocket socket) {
    if (!identical(_socket, socket)) return;
    _wasOpen = true;
    onOpened();
    markOpen();
  }

  void _onDead(CommSocket socket) {
    if (!identical(_socket, socket)) return;
    markClosed();
    if (_wasOpen) onLost();
  }

  /// The client took the page's socket over.
  void onTakenOver() {}

  /// The socket opened.
  void onOpened() {}

  /// A socket that had been open is gone.
  void onLost() {}

  @override
  void rawSend(Uint8List frame) {
    final socket = _socket;
    // A socket can be CLOSING/CLOSED before its close event has been delivered; sending then
    // throws away the frame and logs an error per attempt. Treat it as the drop it is.
    if (socket == null || !socket.isOpen) {
      bufferUnsent(frame);
      markClosed();
      return;
    }
    socket.send(frame);
  }

  /// A browser socket copies what it is given before send returns, so a view of the buffer will do.
  @override
  void rawSendRange(Uint8List buffer, int length) {
    final socket = _socket;
    if (socket == null || !socket.isOpen) {
      super.rawSendRange(buffer, length);
      return;
    }
    socket.send(Uint8List.sublistView(buffer, 0, length));
  }
}
