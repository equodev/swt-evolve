/// Profiler marks behind one import, so generated code need not repeat the conditional import.
library;

export 'perf_marks_stub.dart' if (dart.library.js_interop) 'perf_marks_web.dart';
