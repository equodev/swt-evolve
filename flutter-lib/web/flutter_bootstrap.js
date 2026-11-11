{{flutter_js}}
{{flutter_build_config}}

// The engine downloads and parses every family of the font manifest before it runs main(). The faces
// fonts an application names are substituted with (WebFontSubstitutions.java, ~4.5 MB) are left out
// of the manifest it reads, and registered once text asks for them (see deferred_fonts.dart). What
// the widgets draw with stays in: Inter is the theme's face, which the Java side measures widgets in.
const deferredFontFamilies = new Set([
  'Liberation Sans',
  'Liberation Serif',
  'Liberation Mono',
  'packages/font_awesome_flutter/FontAwesomeBrands',
]);

// The engine's default face, from the bundle. With a Roboto in the manifest the engine does not
// fetch its own copy from fonts.gstatic.com, and the one face also has the real bold that the
// "System Web" metrics were measured with.
const roboto = {
  family: 'Roboto',
  fonts: [
    { asset: 'assets/fonts/Roboto-Regular.ttf' },
    { asset: 'assets/fonts/Roboto-Bold.ttf', weight: 700 },
  ],
};

// Runs initialize() with the engine reading the manifest without the deferred families, which are
// left in window.evolve.deferredFontManifest (FontManifest.json's format, as a JSON string).
async function withDeferredFonts(initialize) {
  const fetch = window.fetch;
  window.fetch = function (resource) {
    const url = String(resource instanceof Request ? resource.url : resource);
    if (!url.endsWith('assets/FontManifest.json')) return fetch.apply(this, arguments);
    return fetch.apply(this, arguments).then(async function (response) {
      if (!response.ok) return response;
      const families = await response.json();
      const deferred = families.filter((f) => deferredFontFamilies.has(f.family));
      const eager = families.filter((f) => !deferredFontFamilies.has(f.family) && f.family !== roboto.family);
      eager.push(roboto);
      window.evolve = window.evolve || {};
      window.evolve.deferredFontManifest = JSON.stringify(deferred);
      return new Response(JSON.stringify(eager), { headers: { 'Content-Type': 'application/json' } });
    });
  };
  try {
    return await initialize();
  } finally {
    window.fetch = fetch;
  }
}

// The default loader, plus a User Timing mark at each step of the engine's start, which the boot
// trace reads (evolve.test.bootTrace): main.dart.js evaluated, then the engine initialized
// (CanvasKit and the fonts of the font manifest).
_flutter.loader.load({
  serviceWorkerSettings: {
    serviceWorkerVersion: {{flutter_service_worker_version}},
  },
  onEntrypointLoaded: async function (engineInitializer) {
    performance.mark('evolve.entrypointLoaded');
    const appRunner = await withDeferredFonts(() => engineInitializer.initializeEngine());
    performance.mark('evolve.engineInitialized');
    if (window.evolve && window.evolve.commPortReady) await window.evolve.commPortReady;
    await appRunner.runApp();
  },
});
