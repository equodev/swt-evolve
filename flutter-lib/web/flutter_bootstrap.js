{{flutter_js}}
{{flutter_build_config}}

// The default loader, plus a User Timing mark at each step of the engine's start, which the boot
// trace reads (evolve.test.bootTrace): main.dart.js evaluated, then the engine initialized
// (CanvasKit and the fonts of the font manifest).
_flutter.loader.load({
  serviceWorkerSettings: {
    serviceWorkerVersion: {{flutter_service_worker_version}},
  },
  onEntrypointLoaded: async function (engineInitializer) {
    performance.mark('evolve.entrypointLoaded');
    const appRunner = await engineInitializer.initializeEngine();
    performance.mark('evolve.engineInitialized');
    await appRunner.runApp();
  },
});
