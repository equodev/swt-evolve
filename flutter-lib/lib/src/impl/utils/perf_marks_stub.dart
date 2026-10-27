/// Off everywhere but the web, where the browser's profiler is the tool that reads these.
bool get perfMarksEnabled => false;

bool get perfDetailEnabled => false;

void perfMark(String name, void Function() body) => body();

void perfCount(String name) {}

void perfMarkDetail(String name, void Function() body) => body();

void perfSpan(String name, double startMs, double endMs) {}

void perfSpanSampled(String name, double startMs, double endMs, {int every = 20}) {}

double perfNow() => 0;

T perfMarkValue<T>(String name, T Function() body) => body();

T perfMarkValueDetail<T>(String name, T Function() body) => body();
