/// Measurement-only: where a GC paint's time goes on this side, and what it asks the canvas to draw.
/// Off unless [start] is called; production pays one static read per paint frame, commit and run.
///
/// - frames: GC paint frames received (ops, state and the end of a paint), their bytes, and the time
///   their ops took to apply into shapes;
/// - commits: paints made what a control shows, and the shapes they left it holding;
/// - paints: scenes recorded into the canvas, and the time recording them took;
/// - runs: triangle lists drawn, and the shapes they held; shapes drawn on their own, by kind; the
///   clips a scene changed to; the shapes a run reached past, drawn after it.
class GcStats {
  static bool on = false;
  static final Stopwatch _clock = Stopwatch()..start();

  static int frames = 0;
  static int bytes = 0;
  static int applyMicros = 0;
  static int commits = 0;
  static int commitMicros = 0;
  static int shapesHeld = 0;
  static int paints = 0;
  static int recordMicros = 0;
  static int runs = 0;
  static int shapesInRuns = 0;
  static int clips = 0;
  static int drawnAfterRuns = 0;
  static final Map<Type, int> alone = {};

  static int now() => _clock.elapsedMicroseconds;

  static void start() {
    frames = bytes = applyMicros = commits = commitMicros = shapesHeld = 0;
    paints = recordMicros = runs = shapesInRuns = clips = drawnAfterRuns = 0;
    alone.clear();
    on = true;
  }

  /// One frame of [length] bytes whose ops started applying at [startedAt].
  static void frame(int length, int startedAt) {
    frames++;
    bytes += length;
    applyMicros += now() - startedAt;
  }

  static void drawnAlone(Object shape) {
    final type = shape.runtimeType;
    alone[type] = (alone[type] ?? 0) + 1;
  }

  static Map<String, dynamic> stop() {
    on = false;
    final out = <String, dynamic>{
      'frames': frames,
      'bytes': bytes,
      'applyMs': applyMicros / 1000.0,
      'commits': commits,
      'commitMs': commitMicros / 1000.0,
      'shapesHeld': shapesHeld,
      'paints': paints,
      'recordMs': recordMicros / 1000.0,
      'runs': runs,
      'shapesInRuns': shapesInRuns,
      'clips': clips,
      'drawnAfterRuns': drawnAfterRuns,
    };
    alone.forEach((type, n) => out['alone $type'] = n);
    return out;
  }
}
