package dev.equo.swt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlutterLibraryLoaderTest {

    @Test
    void firstBundleBaseDir_returnsFirstNonNull() {
        String base = FlutterLibraryLoader.firstBundleBaseDir(List.of(
            () -> "/home/u/.equo/ewt",
            () -> "/ignored"));
        assertThat(base).isEqualTo("/home/u/.equo/ewt");
    }

    @Test
    void firstBundleBaseDir_skipsNullAndBlank() {
        String base = FlutterLibraryLoader.firstBundleBaseDir(List.of(
            () -> null,
            () -> "   ",
            () -> "/home/u/.equo/ewt"));
        assertThat(base).isEqualTo("/home/u/.equo/ewt");
    }

    @Test
    void firstBundleBaseDir_returnsNullWhenNoneProvide() {
        String base = FlutterLibraryLoader.firstBundleBaseDir(List.of(
            () -> null,
            () -> ""));
        assertThat(base).isNull();
    }

    @Test
    void firstBundleBaseDir_returnsNullOnEmpty() {
        assertThat(FlutterLibraryLoader.firstBundleBaseDir(List.of())).isNull();
    }

    @TempDir
    Path tmp;

    @Test
    void bundledDirectory_firstLaunchExtractsTheDirectory() throws IOException {
        File jar = jar("v1", Map.of("runner/engine.dll", "engine-v1", "runner/data/app.so", "app-v1", "other/x", "x"));
        File base = tmp.resolve("lib").toFile();

        File dir = FlutterLibraryLoader.bundledDirectory(jar, "runner", base);

        assertThat(dir).isEqualTo(base);
        assertThat(read(base, "runner/engine.dll")).isEqualTo("engine-v1");
        assertThat(read(base, "runner/data/app.so")).isEqualTo("app-v1");
        assertThat(new File(base, "other")).doesNotExist();
    }

    @Test
    void bundledDirectory_secondLaunchSkipsExtraction() throws IOException {
        File jar = jar("v1", Map.of("runner/engine.dll", "engine-v1"));
        File base = tmp.resolve("lib").toFile();
        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);
        // Same size, different bytes: only a skipped extraction leaves it in place.
        write(base, "runner/engine.dll", "ENGINE-V1");

        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);

        assertThat(read(base, "runner/engine.dll")).isEqualTo("ENGINE-V1");
    }

    @Test
    void bundledDirectory_changedJarReExtracts() throws IOException {
        File base = tmp.resolve("lib").toFile();
        File jar = jar("app", Map.of("runner/engine.dll", "engine-v1"));
        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);
        long built = jar.lastModified();

        jar("app", Map.of("runner/engine.dll", "engine-v2"));
        jar.setLastModified(built + 60_000);
        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);

        assertThat(read(base, "runner/engine.dll")).isEqualTo("engine-v2");
    }

    @Test
    void bundledDirectory_fileKeptBecauseInUseIsReplacedOnTheNextLaunch() throws IOException {
        File base = tmp.resolve("lib").toFile();
        File jar = jar("app", Map.of("runner/bridge.dll", "bridge-v1", "runner/engine.dll", "engine-v1"));
        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);
        long built = jar.lastModified();
        jar("app", Map.of("runner/bridge.dll", "bridge-v2", "runner/engine.dll", "engine-v2"));
        jar.setLastModified(built + 60_000);
        // As on Windows, where a DLL another running process has loaded cannot be replaced.
        FlutterLibraryLoader.Mover bridgeInUse = (from, to) -> {
            if (to.getFileName().toString().equals("bridge.dll")) throw new IOException("in use");
            Files.move(from, to, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        };
        FlutterLibraryLoader.bundledDirectory(jar, "runner", base, bridgeInUse);
        assertThat(read(base, "runner/bridge.dll")).isEqualTo("bridge-v1");

        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);

        assertThat(read(base, "runner/bridge.dll")).isEqualTo("bridge-v2");
    }

    @Test
    void bundledDirectory_missingRequiredFileReExtracts() throws IOException {
        File jar = jar("v1", Map.of("runner/engine.dll", "engine-v1", "runner/bridge.dll", "bridge-v1"));
        File base = tmp.resolve("lib").toFile();
        FlutterLibraryLoader.bundledDirectory(jar, "runner", base, "runner/bridge.dll");
        Files.delete(new File(base, "runner/bridge.dll").toPath());

        FlutterLibraryLoader.bundledDirectory(jar, "runner", base, "runner/bridge.dll");

        assertThat(read(base, "runner/bridge.dll")).isEqualTo("bridge-v1");
    }

    @Test
    void bundledDirectory_interruptedExtractionIsRedone() throws IOException {
        File jar = jar("v1", Map.of("runner/engine.dll", "engine-v1"));
        File base = tmp.resolve("lib").toFile();
        // What an extraction killed before it finished leaves: a same-size stale file and no stamp.
        write(base, "runner/engine.dll", "ENGINE-V1");

        FlutterLibraryLoader.bundledDirectory(jar, "runner", base);

        assertThat(read(base, "runner/engine.dll")).isEqualTo("engine-v1");
    }

    @Test
    void bundledDirectory_explodedBundleLoadsInPlace() throws IOException {
        File bundle = tmp.resolve("plugins/org.eclipse.swt.cocoa.macosx.aarch64_1.0.0").toFile();
        write(bundle, "swtflutter.app/Contents/libflutter_bridge.dylib", "bridge");
        File base = tmp.resolve("lib").toFile();

        File dir = FlutterLibraryLoader.bundledDirectory(bundle, "swtflutter.app", base);

        assertThat(dir).isEqualTo(bundle);
        assertThat(base).doesNotExist();
    }

    @Test
    void bundledDirectory_explodedBundleWithoutTheDirectoryFails() throws IOException {
        File bundle = tmp.resolve("bundle").toFile();
        write(bundle, "other/file", "x");

        assertThatThrownBy(() -> FlutterLibraryLoader.bundledDirectory(bundle, "runner", tmp.resolve("lib").toFile()))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("runner");
    }

    private static final String FRAMEWORK = "swtflutter.app/Contents/Frameworks/App.framework/";
    private static final String LINKS = FRAMEWORK + "Versions/Current\tA\n"
        + FRAMEWORK + "App\tVersions/Current/App\n"
        + FRAMEWORK + "Resources\tVersions/Current/Resources\n";

    private Map<String, String> frameworkJar() {
        Map<String, String> files = new LinkedHashMap<>();
        files.put(FRAMEWORK + "Versions/A/App", "app-binary");
        files.put(FRAMEWORK + "Versions/A/Resources/Info.plist", "plist");
        files.put("swtflutter.app.links", LINKS);
        return files;
    }

    @Test
    void bundledDirectory_extractionCreatesTheListedLinks() throws IOException {
        File jar = jar("mac", frameworkJar());
        File base = tmp.resolve("lib").toFile();

        FlutterLibraryLoader.bundledDirectory(jar, "swtflutter.app", base);

        assertThat(Files.readSymbolicLink(base.toPath().resolve(FRAMEWORK + "Versions/Current"))).isEqualTo(Paths.get("A"));
        assertThat(Files.isSymbolicLink(base.toPath().resolve(FRAMEWORK + "App"))).isTrue();
        assertThat(read(base, FRAMEWORK + "App")).isEqualTo("app-binary");
        assertThat(read(base, FRAMEWORK + "Resources/Info.plist")).isEqualTo("plist");
    }

    @Test
    void bundledDirectory_missingLinkIsRecreated() throws IOException {
        File jar = jar("mac", frameworkJar());
        File base = tmp.resolve("lib").toFile();
        FlutterLibraryLoader.bundledDirectory(jar, "swtflutter.app", base);
        Files.delete(base.toPath().resolve(FRAMEWORK + "Resources"));

        FlutterLibraryLoader.bundledDirectory(jar, "swtflutter.app", base);

        assertThat(read(base, FRAMEWORK + "Resources/Info.plist")).isEqualTo("plist");
    }

    @Test
    void bundledDirectory_directoryAnOlderExtractionCopiedIsReplacedByTheLink() throws IOException {
        File base = tmp.resolve("lib").toFile();
        // An extraction from a jar that stored the linked directories as copies, then a new jar.
        write(base, FRAMEWORK + "Versions/Current/App", "stale-copy");
        write(base, FRAMEWORK + "Resources/Info.plist", "stale-copy");
        File jar = jar("mac", frameworkJar());

        FlutterLibraryLoader.bundledDirectory(jar, "swtflutter.app", base);

        assertThat(Files.isSymbolicLink(base.toPath().resolve(FRAMEWORK + "Versions/Current"))).isTrue();
        assertThat(Files.isSymbolicLink(base.toPath().resolve(FRAMEWORK + "Resources"))).isTrue();
        assertThat(read(base, FRAMEWORK + "Resources/Info.plist")).isEqualTo("plist");
    }

    @Test
    void bundledDirectory_explodedBundleWithItsLinksWritesNothing() throws IOException {
        File bundle = tmp.resolve("plugins/org.eclipse.swt.cocoa.macosx.aarch64_1.0.0").toFile();
        for (Map.Entry<String, String> file : frameworkJar().entrySet()) {
            write(bundle, file.getKey(), file.getValue());
        }
        Path framework = bundle.toPath().resolve(FRAMEWORK);
        Files.createSymbolicLink(framework.resolve("Versions/Current"), Paths.get("A"));
        Files.createSymbolicLink(framework.resolve("App"), Paths.get("Versions/Current/App"));
        Files.createSymbolicLink(framework.resolve("Resources"), Paths.get("Versions/Current/Resources"));
        // What p2 leaves: links in place, in a directory the user may not be able to write.
        framework.toFile().setWritable(false);
        try {
            File dir = FlutterLibraryLoader.bundledDirectory(bundle, "swtflutter.app", tmp.resolve("lib").toFile());

            assertThat(dir).isEqualTo(bundle);
            assertThat(read(bundle, FRAMEWORK + "Resources/Info.plist")).isEqualTo("plist");
        } finally {
            framework.toFile().setWritable(true);
        }
    }

    @Test
    void bundledDirectory_explodedBundleWithoutItsLinksGetsThem() throws IOException {
        File bundle = tmp.resolve("plugins/org.eclipse.swt.cocoa.macosx.aarch64_1.0.0").toFile();
        for (Map.Entry<String, String> file : frameworkJar().entrySet()) {
            write(bundle, file.getKey(), file.getValue());
        }

        FlutterLibraryLoader.bundledDirectory(bundle, "swtflutter.app", tmp.resolve("lib").toFile());

        assertThat(read(bundle, FRAMEWORK + "App")).isEqualTo("app-binary");
    }

    private File jar(String name, Map<String, String> files) throws IOException {
        File jar = tmp.resolve(name + ".jar").toFile();
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar.toPath()))) {
            for (Map.Entry<String, String> file : new LinkedHashMap<>(files).entrySet()) {
                out.putNextEntry(new JarEntry(file.getKey()));
                out.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return jar;
    }

    private static void write(File base, String path, String content) throws IOException {
        File file = new File(base, path);
        file.getParentFile().mkdirs();
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String read(File base, String path) throws IOException {
        return new String(Files.readAllBytes(new File(base, path).toPath()), StandardCharsets.UTF_8);
    }
}
