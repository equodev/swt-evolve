#include "engine_vsync.h"

#include <algorithm>
#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <cstring>

#include "flutter_embedder/embedder.h"

namespace {

// FlEngine's instance up to its embedder proc table, as fl_engine.cc declares it in the Flutter
// release whose embedder.h sits next to this file. EngineVsyncInstall checks the guess against the
// engine's public getters before it writes anything.
struct FlEngineInstance {
  GObject parent_instance;
  GThread *thread;
  FlDartProject *project;
  gpointer display_monitor;
  FlutterRendererType renderer_type;
  gpointer opengl_manager;
  FlBinaryMessenger *binary_messenger;
  gpointer settings_handler;
  gpointer platform_handler;
  gpointer accessibility_handler;
  gpointer keyboard_manager;
  gpointer text_input_handler;
  gpointer keyboard_handler;
  gpointer mouse_cursor_handler;
  FlTextureRegistrar *texture_registrar;
  gpointer task_runner;
  FlutterEngineAOTData aot_data;
  FLUTTER_API_SYMBOL(FlutterEngine) engine;
  FlutterEngineProcTable embedder_api;
};

constexpr char kStateKey[] = "dev.equo.swt.vsync";
constexpr uint64_t kFallbackRefreshMilliHz = 60000;

struct VsyncState {
  // One refresh interval of the window's monitor. Written on the GTK thread, read on the engine's.
  std::atomic<uint64_t> period_ns{1000000000000ull / kFallbackRefreshMilliHz};
  // When the allowance is spent: one interval past the last start, or past now after an idle
  // spell. Only the engine's UI thread, which makes every vsync request, touches it.
  uint64_t spent_until_ns = 0;
};

// The engine's own entry points, the same for every engine in the process.
FlutterEngineInitializeFnPtr g_initialize = nullptr;
FlutterEngineOnVsyncFnPtr g_on_vsync = nullptr;
FlutterEngineGetCurrentTimeFnPtr g_current_time = nullptr;
FlutterEngineTraceEventDurationBeginFnPtr g_trace_begin = nullptr;
FlutterEngineTraceEventDurationEndFnPtr g_trace_end = nullptr;

// Vsync requests that may start at once after an idle spell. Not every request draws a frame: the
// engine asks for one more whenever a pointer event arrives, for its own trace bookkeeping, and the
// frame that event causes follows right after. A real display's vblank would serve both; here the
// allowance does.
constexpr uint64_t kBurst = 2;

// The engine asks for the next vsync. It starts at once while the allowance lasts, otherwise when
// it refills, one interval after the last start; the engine holds a start in the future until then.
void OnVsyncRequest(void *user_data, intptr_t baton) {
  auto *instance = static_cast<FlEngineInstance *>(user_data);
  auto *state = static_cast<VsyncState *>(g_object_get_data(G_OBJECT(user_data), kStateKey));
  uint64_t period = state->period_ns.load(std::memory_order_relaxed);
  uint64_t now = g_current_time();
  uint64_t tolerance = (kBurst - 1) * period;
  uint64_t spent = state->spent_until_ns;
  uint64_t start = spent > now + tolerance ? spent - tolerance : now;
  state->spent_until_ns = std::max(spent, start) + period;
  g_on_vsync(instance->engine, baton, start, start + period);
}

FlutterEngineResult InitializeWithVsync(size_t version, const FlutterRendererConfig *config,
                                        const FlutterProjectArgs *args, void *user_data,
                                        FLUTTER_API_SYMBOL(FlutterEngine) * engine_out) {
  FlutterProjectArgs paced = {};
  std::memcpy(&paced, args, std::min(args->struct_size, sizeof(paced)));
  if (args->struct_size >= offsetof(FlutterProjectArgs, vsync_callback) + sizeof(VsyncCallback)) {
    paced.vsync_callback = OnVsyncRequest;
  }
  return g_initialize(version, config, &paced, user_data, engine_out);
}

void UpdatePeriod(GtkWidget *window, VsyncState *state) {
  GdkDisplay *display = gtk_widget_get_display(window);
  GdkWindow *gdk_window = gtk_widget_get_window(window);
  GdkMonitor *monitor = gdk_window != nullptr ? gdk_display_get_monitor_at_window(display, gdk_window)
                                              : gdk_display_get_primary_monitor(display);
  if (monitor == nullptr && gdk_display_get_n_monitors(display) > 0) {
    monitor = gdk_display_get_monitor(display, 0);
  }
  int milli_hz = monitor != nullptr ? gdk_monitor_get_refresh_rate(monitor) : 0;
  uint64_t rate = milli_hz > 0 ? static_cast<uint64_t>(milli_hz) : kFallbackRefreshMilliHz;
  state->period_ns.store(1000000000000ull / rate, std::memory_order_relaxed);
}

// A window that moves may land on a monitor with another refresh rate.
gboolean OnWindowConfigure(GtkWidget *window, GdkEvent *, gpointer engine) {
  auto *state = static_cast<VsyncState *>(g_object_get_data(G_OBJECT(engine), kStateKey));
  if (state != nullptr) UpdatePeriod(window, state);
  return FALSE;
}

bool LooksLikeTheInstance(FlEngine *engine, FlEngineInstance *instance) {
  return instance->embedder_api.struct_size == sizeof(FlutterEngineProcTable) &&
         instance->binary_messenger == fl_engine_get_binary_messenger(engine) &&
         instance->texture_registrar == fl_engine_get_texture_registrar(engine) &&
         instance->binary_messenger != nullptr && instance->texture_registrar != nullptr &&
         instance->engine == nullptr && instance->embedder_api.Initialize != nullptr &&
         instance->embedder_api.OnVsync != nullptr && instance->embedder_api.GetCurrentTime != nullptr;
}

} // namespace

GObject *EngineMouseCursorHandler(FlEngine *engine) {
  if (engine == nullptr) return nullptr;
  auto *instance = reinterpret_cast<FlEngineInstance *>(engine);
  if (instance->binary_messenger == nullptr ||
      instance->binary_messenger != fl_engine_get_binary_messenger(engine) ||
      instance->texture_registrar != fl_engine_get_texture_registrar(engine) ||
      instance->embedder_api.struct_size != sizeof(FlutterEngineProcTable)) {
    return nullptr;
  }
  auto *handler = static_cast<GObject *>(instance->mouse_cursor_handler);
  if (handler == nullptr || !G_IS_OBJECT(handler) ||
      strcmp(G_OBJECT_TYPE_NAME(handler), "FlMouseCursorHandler") != 0) {
    return nullptr;
  }
  return handler;
}

void EngineTraceBegin(const char *name) {
  if (g_trace_begin != nullptr) g_trace_begin(name);
}

void EngineTraceEnd(const char *name) {
  if (g_trace_end != nullptr) g_trace_end(name);
}

bool EngineVsyncInstall(FlEngine *engine, GtkWidget *window) {
  if (engine == nullptr || window == nullptr) return false;
  auto *instance = reinterpret_cast<FlEngineInstance *>(engine);
  if (!LooksLikeTheInstance(engine, instance)) {
    g_warning("Engine layout not recognized; frames stay on the engine's 60 Hz timer");
    return false;
  }
  if (instance->embedder_api.Initialize == InitializeWithVsync) return true;
  g_trace_begin = instance->embedder_api.TraceEventDurationBegin;
  g_trace_end = instance->embedder_api.TraceEventDurationEnd;
  // EQUO_SWT_VSYNC=timer keeps the engine's own 60 Hz timer.
  const char *mode = getenv("EQUO_SWT_VSYNC");
  if (mode != nullptr && strcmp(mode, "timer") == 0) return false;
  g_initialize = instance->embedder_api.Initialize;
  g_on_vsync = instance->embedder_api.OnVsync;
  g_current_time = instance->embedder_api.GetCurrentTime;

  auto *state = new VsyncState();
  UpdatePeriod(window, state);
  g_object_set_data_full(G_OBJECT(engine), kStateKey, state,
                         [](gpointer p) { delete static_cast<VsyncState *>(p); });
  g_signal_connect_object(window, "configure-event", G_CALLBACK(OnWindowConfigure), engine,
                          static_cast<GConnectFlags>(0));
  instance->embedder_api.Initialize = InitializeWithVsync;
  return true;
}
