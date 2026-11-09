#ifndef RUNNER_BOOT_TRACE_H_
#define RUNNER_BOOT_TRACE_H_

#include <flutter/encodable_value.h>
#include <windows.h>

#include <cstdint>
#include <string>
#include <type_traits>

// Steps of the native side's start, in microseconds since the epoch (the clock the Java and Dart
// sides of the boot trace read), answered on the window channel's `bootTrace`. Only the first
// window's start is the boot; windows opened later record nothing.
namespace boot_trace {

inline flutter::EncodableList& Steps() {
  static flutter::EncodableList steps;
  return steps;
}

inline bool& Recording() {
  static bool recording = true;
  return recording;
}

inline int64_t NowMicros() {
  FILETIME ft;
  ::GetSystemTimePreciseAsFileTime(&ft);
  ULARGE_INTEGER t;
  t.LowPart = ft.dwLowDateTime;
  t.HighPart = ft.dwHighDateTime;
  // FILETIME counts 100 ns ticks from 1601-01-01.
  return static_cast<int64_t>((t.QuadPart - 116444736000000000ULL) / 10);
}

inline void Record(const char* name, int64_t start_us) {
  if (!Recording()) return;
  Steps().push_back(flutter::EncodableValue(flutter::EncodableMap{
      {flutter::EncodableValue("name"), flutter::EncodableValue(std::string(name))},
      {flutter::EncodableValue("depth"), flutter::EncodableValue(1)},
      {flutter::EncodableValue("startUs"), flutter::EncodableValue(start_us)},
      {flutter::EncodableValue("endUs"), flutter::EncodableValue(NowMicros())},
  }));
}

template <typename F>
auto Step(const char* name, F&& body) -> decltype(body()) {
  const int64_t start = NowMicros();
  if constexpr (std::is_void_v<decltype(body())>) {
    body();
    Record(name, start);
  } else {
    auto value = body();
    Record(name, start);
    return value;
  }
}

}  // namespace boot_trace

#endif  // RUNNER_BOOT_TRACE_H_
