package dev.equo.swt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Manages the extraction and loading of native Flutter libraries.
 * From a jar, libraries are extracted to a subdirectory within the user's home directory, once per
 * jar content; from an exploded bundle directory (an Eclipse install), they load in place.
 */
public class FlutterLibraryLoader {

    static final String SEP = File.separator;
    static final String USER_HOME = System.getProperty("user.home");

    // OS and Architecture Constants
    private static final String OS_LINUX = "linux";
    private static final String OS_MACOSX = "macosx";
    public static final String OS_WIN = "win32";
    private static final String OS_WINDOWS_PREFIX = "Win";
    private static final String ARCH_AMD64 = "amd64";
    private static final String ARCH_X86_64 = "x86_64";

    // Directory and File Name Constants
    static final String EQUO_BASE_DIR_NAME = ".equo";
    static final String LIB_SUB_DIR_NAME = "lib";
    static final String SWT_DIR_NAME = "swt";
    private static final String MACOS_LIB_NAME = "libflutter_bridge.dylib";
    public static final String CONTENTS = "Contents";
    public static final String SWTFLUTTER_APP = "swtflutter.app";
    private static final String LINKS_SUFFIX = ".links";
    // With -Ddev.equo.swt.dartDebug=true the desktop Flutter frameworks were built with --debug (JIT)
    // so the embedded engine exposes a Dart VM Service (DTD/MCP introspection). Flutter writes those
    // artifacts under its Debug/debug output dirs instead of Release/release. Only the dev (build-dir)
    // loading below needs to follow suit — the packaged-jar path is mode-agnostic (it just contains
    // swtflutter.app / runner, with no Release|Debug segment). See docs/design/flutter-dtd-introspection.md.
    // -Ddev.equo.swt.dartProfile=true picks the --profile build the same way.
    private static final boolean DART_DEBUG = Boolean.getBoolean("dev.equo.swt.dartDebug");
    private static final boolean DART_PROFILE = !DART_DEBUG && Boolean.getBoolean("dev.equo.swt.dartProfile");
    private static final String BUILD_MODE_CAP = DART_DEBUG ? "Debug" : DART_PROFILE ? "Profile" : "Release";     // macOS & Windows
    private static final String BUILD_MODE_LOWER = DART_DEBUG ? "debug" : DART_PROFILE ? "profile" : "release";   // Linux
    public static final String SWTFLUTTER_APP_CONTENTS = "macos/Build/Products/" + BUILD_MODE_CAP + "/" + SWTFLUTTER_APP + SEP + CONTENTS;
    private static final String RUNNER_DIR_NAME = "runner";
    private static final String LINUX_BUNDLE_DIR_NAME = "bundle";
    private static final String LINUX_LIB_NAME = "libflutter_bridge.so";
    private static final String LINUX_ENGINE_LIB_NAME = "libflutter_linux_gtk.so";
    // Flutter plugin that libflutter_bridge.so links against; must be pre-loaded (see below).
    private static final String LINUX_WEBVIEW_PLUGIN_NAME = "libwebview_all_linux_plugin.so";
    private static final String WIN_LIB1_NAME = "flutter_windows.dll";
    private static final String WIN_LIB_NAME = "flutter_bridge.dll";
    // Flutter's own name for the target arch in its build output dirs (build/linux/<arch>, build/windows/<arch>).
    private static final String FLUTTER_ARCH = "aarch64".equals(System.getProperty("os.arch")) ? "arm64" : "x64";
    public static final String LINUX_RELEASE = "linux/" + FLUTTER_ARCH + "/" + BUILD_MODE_LOWER;
    public static final String WIN_RELEASE = "windows/" + FLUTTER_ARCH + "/runner/" + BUILD_MODE_CAP;

    private static final String EQUO_LIB_PATH_SUFFIX =
            EQUO_BASE_DIR_NAME + SEP + SWT_DIR_NAME + SEP + LIB_SUB_DIR_NAME + SEP + getOS() + SEP + getArch();
    private static boolean loaded;

    /**
     * Initializes the Flutter library loader. This is the main public entry point.
     * It orchestrates the extraction and loading, wrapping any failure in a single, clear exception.
     * This method is synchronized and ensures that extraction and loading occur only once.
     *
     * @throws LibraryLoaderException if initialization fails for any reason.
     */
    public static void initialize() {
        try {
            if (loaded) return;
            loaded = true;
            try (BootTrace.Span span = BootTrace.span("natives")) {
                extractAndLoadFlutterLibraries();
            }
        } catch (Exception e) {
            throw new LibraryLoaderException("Failed to initialize Flutter libraries", e);
        }
    }

    /**
     * Main method to orchestrate library extraction and loading based on OS.
     * This method now throws IOException to be handled by the public entry point.
     *
     * @throws IOException if any file or directory operation fails.
     * @throws UnsupportedOperationException if the OS is not supported.
     */
    private static void extractAndLoadFlutterLibraries() throws IOException {
        String os = getOS();
        File equoLibDir = new File(USER_HOME, EQUO_LIB_PATH_SUFFIX);

        boolean isDevelopmentMode = isDevelopmentMode();
        System.out.println("Running in development: " + isDevelopmentMode);

        if (OS_MACOSX.equals(os)) {
            extractAndLoadMacOSLibraries(equoLibDir, isDevelopmentMode);
        } else if (OS_LINUX.equals(os)) {
            extractAndLoadLinuxLibraries(equoLibDir, isDevelopmentMode);
        } else if (OS_WIN.equals(os)) {
            extractAndLoadWinLibraries(equoLibDir, isDevelopmentMode);
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os + ". Equo SWT currently supports macOS, Windows and Linux.");
        }
        try (BootTrace.Span span = BootTrace.span("externalBundleOverride")) {
            applyExternalBundleOverride();
        }
    }

    private static void extractAndLoadMacOSLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            String lib = SWTFLUTTER_APP + SEP + CONTENTS + SEP + MACOS_LIB_NAME;
            String frameworks = SWTFLUTTER_APP + SEP + CONTENTS + SEP + "Frameworks" + SEP;
            File baseDir = bundledDirectory(SWTFLUTTER_APP, targetDir, lib,
                    frameworks + "FlutterMacOS.framework/Versions/A/FlutterMacOS",
                    frameworks + "FlutterMacOS.framework/Versions/A/Resources/icudtl.dat",
                    frameworks + "App.framework/Versions/A/App",
                    SWTFLUTTER_APP + LINKS_SUFFIX);
            File libFile = new File(baseDir, SWTFLUTTER_APP + SEP + CONTENTS + SEP + MACOS_LIB_NAME);
            loadLibrary(libFile.getAbsolutePath());
        } else {
            loadOSLibraries(SWTFLUTTER_APP_CONTENTS, MACOS_LIB_NAME);
        }
    }

    private static void extractAndLoadLinuxLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            String bundleLib = LINUX_BUNDLE_DIR_NAME + SEP + LIB_SUB_DIR_NAME + SEP;
            File baseDir = bundledDirectory(RUNNER_DIR_NAME, targetDir, RUNNER_DIR_NAME + SEP + LINUX_LIB_NAME);
            bundledDirectory(LINUX_BUNDLE_DIR_NAME, targetDir, bundleLib + LINUX_ENGINE_LIB_NAME, bundleLib + LINUX_WEBVIEW_PLUGIN_NAME,
                    bundleLib + "libapp.so", LINUX_BUNDLE_DIR_NAME + SEP + "data" + SEP + "icudtl.dat");

            File bundleLibDir = new File(baseDir, LINUX_BUNDLE_DIR_NAME + SEP + LIB_SUB_DIR_NAME);
            File flutFile = new File(bundleLibDir, LINUX_ENGINE_LIB_NAME);
            File webviewFile = new File(bundleLibDir, LINUX_WEBVIEW_PLUGIN_NAME);
            File libFile = new File(baseDir, RUNNER_DIR_NAME + SEP + LINUX_LIB_NAME);
            for (File required : new File[] { flutFile, webviewFile, libFile }) {
                if (!required.exists())
                    throw new IOException("Essential Linux library not found after extraction: " + required.getAbsolutePath());
                // A bundle directory may be read-only (a shared Eclipse install); dlopen does not need the execute bit.
                if (baseDir.equals(targetDir))
                    setExecutablePermission(required);
            }
            // Pre-load the engine, then the bridge (which links against it). The webview plugin is not
            // pre-loaded: the bridge dlopens it lazily, so a missing libwebkit2gtk-4.1 leaves the Browser
            // inactive instead of aborting Display creation. It is still extracted above.
            loadLibrary(flutFile.getAbsolutePath());
            loadLibrary(libFile.getAbsolutePath());
        } else {
            String bundleLib = LINUX_BUNDLE_DIR_NAME + SEP + LIB_SUB_DIR_NAME + SEP;
            loadOSLibraries(LINUX_RELEASE, bundleLib + LINUX_ENGINE_LIB_NAME);
            loadOSLibraries(LINUX_RELEASE, bundleLib + LINUX_WEBVIEW_PLUGIN_NAME);
            loadOSLibraries(LINUX_RELEASE, RUNNER_DIR_NAME + SEP + LINUX_LIB_NAME);
        }
    }

    private static void extractAndLoadWinLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            File baseDir = bundledDirectory(RUNNER_DIR_NAME, targetDir,
                    RUNNER_DIR_NAME + SEP + WIN_LIB1_NAME, RUNNER_DIR_NAME + SEP + WIN_LIB_NAME,
                    RUNNER_DIR_NAME + SEP + "data" + SEP + "app.so", RUNNER_DIR_NAME + SEP + "data" + SEP + "icudtl.dat");

            File lib1File = new File(baseDir, RUNNER_DIR_NAME + SEP + WIN_LIB1_NAME);
            File libFile = new File(baseDir, RUNNER_DIR_NAME + SEP + WIN_LIB_NAME);
            File libWebFile1 = new File(baseDir, RUNNER_DIR_NAME + SEP + "WebView2Loader.dll");
            File libWebFile = new File(baseDir, RUNNER_DIR_NAME + SEP + "webview_all_windows_plugin.dll");
            if (!libFile.exists() || !lib1File.exists()) {
                throw new IOException("Essential Windows library not found after extraction: " + libFile.getAbsolutePath() + ", "+lib1File.getAbsolutePath());
            }
            loadLibrary(lib1File.getAbsolutePath());
            loadLibrary(libWebFile1.getAbsolutePath());
            loadLibrary(libWebFile.getAbsolutePath());
            loadLibrary(libFile.getAbsolutePath());
        } else {
            loadOSLibraries(WIN_RELEASE, SEP + WIN_LIB1_NAME);
            loadOSLibraries(WIN_RELEASE, SEP + "WebView2Loader.dll");
            loadOSLibraries(WIN_RELEASE, SEP + "webview_all_windows_plugin.dll");
            loadOSLibraries(WIN_RELEASE, SEP + WIN_LIB_NAME);
        }
    }

    private static void loadOSLibraries(String libDirectoryPath, String libName) throws IOException {
        System.out.println("Development mode: loading Flutter libraries directly from build directory");
        File flutterBuildDir = findFlutterBuildDirectory();
        if (flutterBuildDir == null) {
            throw new IOException("Flutter build directory not found. Searched in common locations like 'flutter-lib/build'. Please build the Flutter app first.");
        }

        File releaseDir = new File(flutterBuildDir, libDirectoryPath);
        if (!releaseDir.exists() || !releaseDir.isDirectory()) {
            throw new IOException("Flutter Release build not found at: " + releaseDir.getAbsolutePath());
        }

        File libFile = new File(releaseDir, libName);
        if (!libFile.exists()) {
            throw new IOException("Essential library not found at: " + libFile.getAbsolutePath());
        }
        setExecutablePermission(libFile);
        loadLibrary(libFile.getAbsolutePath());
    }

    /**
     * Returns the directory that holds {@code directoryPath}: the bundle directory itself when this
     * class loads from an exploded bundle, else {@code extractionBase}, after extracting the
     * directory from the jar into it. {@code requiredFiles}, relative to the returned directory, are
     * the files the caller loads.
     */
    static File bundledDirectory(String directoryPath, File extractionBase, String... requiredFiles) throws IOException {
        return bundledDirectory(codeSourceLocation(), directoryPath, extractionBase, requiredFiles);
    }

    static File bundledDirectory(File codeSource, String directoryPath, File extractionBase, String... requiredFiles) throws IOException {
        return bundledDirectory(codeSource, directoryPath, extractionBase, MOVE, requiredFiles);
    }

    /** Moves an extracted file into place; a test substitutes one that fails, as a file in use does on Windows. */
    interface Mover {
        void move(Path from, Path to) throws IOException;
    }

    private static final Mover MOVE = (from, to) ->
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

    static File bundledDirectory(File codeSource, String directoryPath, File extractionBase, Mover mover,
                                 String... requiredFiles) throws IOException {
        if (codeSource.isDirectory()) {
            if (!new File(codeSource, directoryPath).isDirectory()) {
                throw new IOException("'" + directoryPath + "' not found in bundle directory " + codeSource.getAbsolutePath());
            }
            System.out.println("Loading '" + directoryPath + "' in place from " + codeSource.getAbsolutePath());
            createLinks(codeSource, directoryPath);
            return codeSource;
        }
        extractDirectoryFromJar(codeSource, directoryPath, extractionBase, mover, requiredFiles);
        return extractionBase;
    }

    private static File codeSourceLocation() throws IOException {
        URL classUrl = FlutterLibraryLoader.class.getProtectionDomain().getCodeSource().getLocation();
        if (classUrl == null) {
            throw new IOException("Cannot determine the location of the Flutter libraries");
        }
        try {
            return Paths.get(classUrl.toURI()).toFile();
        } catch (URISyntaxException | IllegalArgumentException e) {
            return new File(classUrl.getPath());
        }
    }

    /**
     * Extracts a directory and its contents from the jar to a target directory, unless a previous
     * launch already extracted it from this same jar. The jar is identified by its size and
     * modification time, so a launch that skips the extraction never opens it. The stamp is
     * written last, and only when every file was replaced, so an interrupted extraction, or one
     * that had to keep a file another process holds, is redone on the next launch.
     */
    private static void extractDirectoryFromJar(File jarFileSource, String directoryPathInJar, File targetBaseDir,
                                                Mover mover, String... requiredFiles) throws IOException {
        try (BootTrace.Span span = BootTrace.span("extract " + directoryPathInJar)) {
            String stamp = jarFileSource.length() + ":" + jarFileSource.lastModified();
            File stampFile = new File(targetBaseDir, directoryPathInJar + ".stamp");
            if (isExtracted(stampFile, stamp, targetBaseDir, directoryPathInJar, requiredFiles)) {
                return;
            }

            System.out.println("Extracting '" + directoryPathInJar + "' from " + jarFileSource.getAbsolutePath() + " to " + targetBaseDir.getAbsolutePath());
            ensureDirectoryExists(targetBaseDir);
            Files.deleteIfExists(stampFile.toPath());
            boolean allReplaced = true;
            try (JarFile jarFile = new JarFile(jarFileSource)) {
                String normalizedPath = directoryPathInJar.endsWith("/") ? directoryPathInJar : directoryPathInJar + "/";
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith(normalizedPath) && !entry.isDirectory()) {
                        File targetFile = new File(targetBaseDir, entry.getName());
                        ensureDirectoryExists(targetFile.getParentFile());
                        allReplaced &= extractAtomically(jarFile, entry, targetFile, mover);
                    }
                }
                JarEntry links = jarFile.getJarEntry(directoryPathInJar + LINKS_SUFFIX);
                if (links != null) {
                    allReplaced &= extractAtomically(jarFile, links, new File(targetBaseDir, links.getName()), mover);
                }
            }
            createLinks(targetBaseDir, directoryPathInJar);
            if (allReplaced) {
                writeAtomically(stampFile, stamp);
            }
        }
    }

    private static boolean isExtracted(File stampFile, String stamp, File targetBaseDir, String directoryPath,
                                       String... requiredFiles) throws IOException {
        if (!stampFile.isFile() || !stamp.equals(new String(Files.readAllBytes(stampFile.toPath()), StandardCharsets.UTF_8))) {
            return false;
        }
        for (String required : requiredFiles) {
            if (!new File(targetBaseDir, required).isFile()) {
                return false;
            }
        }
        for (Map.Entry<String, String> link : readLinks(targetBaseDir, directoryPath).entrySet()) {
            if (!isLink(targetBaseDir.toPath().resolve(link.getKey()), link.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * The symlinks a jar cannot hold (a macOS framework's {@code Versions/Current}, and the links to it),
     * listed in {@code <directoryPath>.links} as {@code link<TAB>target} lines relative to {@code base}.
     */
    private static Map<String, String> readLinks(File base, String directoryPath) throws IOException {
        Map<String, String> links = new LinkedHashMap<>();
        File list = new File(base, directoryPath + LINKS_SUFFIX);
        if (!list.isFile()) {
            return links;
        }
        for (String line : Files.readAllLines(list.toPath(), StandardCharsets.UTF_8)) {
            int tab = line.indexOf('\t');
            if (tab > 0) {
                links.put(line.substring(0, tab), line.substring(tab + 1));
            }
        }
        return links;
    }

    /**
     * Creates the links {@link #readLinks} lists that are not already in place. Each one is created
     * under a temporary name and renamed over its path, so a process starting at the same time sees
     * either the old entry or the link. A directory in its place, a copy an older extraction made by
     * following the link, is deleted first.
     */
    static void createLinks(File base, String directoryPath) throws IOException {
        for (Map.Entry<String, String> link : readLinks(base, directoryPath).entrySet()) {
            Path path = base.toPath().resolve(link.getKey());
            if (isLink(path, link.getValue())) {
                continue;
            }
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                deleteRecursively(path);
            }
            ensureDirectoryExists(path.getParent().toFile());
            Path temp = path.resolveSibling(path.getFileName() + "." + UUID.randomUUID() + ".link");
            try {
                Files.createSymbolicLink(temp, Paths.get(link.getValue()));
                Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                if (!isLink(path, link.getValue())) {
                    throw new IOException("Cannot create the link " + path + " -> " + link.getValue(), e);
                }
            } finally {
                Files.deleteIfExists(temp);
            }
        }
    }

    private static boolean isLink(Path path, String target) throws IOException {
        return Files.isSymbolicLink(path) && Files.readSymbolicLink(path).equals(Paths.get(target));
    }

    private static void deleteRecursively(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
                Files.deleteIfExists(path);
            }
        }
    }

    /**
     * Sets execute permission on the given file. No-op on Windows.
     * Simplified logic that throws an exception on failure.
     *
     * @param file The file to set as executable.
     * @throws IOException if the permission cannot be set.
     */
    private static void setExecutablePermission(File file) throws IOException {
        if (getOS().startsWith(OS_WINDOWS_PREFIX) || !file.exists()) {
            return;
        }

        // Try Java's built-in method first.
        if (file.canExecute()) {
            return; // Already executable
        }

        if (file.setExecutable(true, false)) {
            System.out.println("Successfully set execute permission for: " + file.getAbsolutePath());
            return; // Success
        }

        // Fallback to chmod command only if the Java method fails.
        System.out.println("setExecutable() failed, attempting chmod fallback...");
        try {
            Process chmodProcess = Runtime.getRuntime().exec(new String[]{"chmod", "755", file.getAbsolutePath()});
            if (chmodProcess.waitFor() == 0 && file.canExecute()) {
                System.out.println("Execute permission set via chmod for: " + file.getAbsolutePath());
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interrupted status
            throw new IOException("Chmod process interrupted while setting permissions for " + file.getAbsolutePath(), e);
        }

        // If we're still here, all attempts failed.
        throw new IOException("Failed to set execute permission for: " + file.getAbsolutePath());
    }

    /**
     * Loads a native library from the given absolute path.
     * This method is a low-level boundary; it catches the specific `UnsatisfiedLinkError`
     * and wraps it with helpful diagnostic information.
     *
     * @param absoluteLibPath The absolute path to the native library.
     * @throws LibraryLoaderException if loading the library fails.
     */
    private static void loadLibrary(String absoluteLibPath) {
        try (BootTrace.Span span = BootTrace.span(BootTrace.enabled() ? "load " + new File(absoluteLibPath).getName() : null)) {
            System.load(absoluteLibPath);
            System.out.println("Successfully loaded library: " + absoluteLibPath);
        } catch (UnsatisfiedLinkError e) {
            String errorMessage = String.format("Failed to load library %s. Architecture: %s. OS: %s. Error: %s",
                    absoluteLibPath, getArch(), getOS(), e.getMessage());
            throw new LibraryLoaderException(errorMessage, e);
        }
    }

    private static String getOS() {
        String osName = System.getProperty("os.name");
        if (osName.equalsIgnoreCase(OS_LINUX)) return OS_LINUX;
        if (osName.equalsIgnoreCase("Mac OS X")) return OS_MACOSX;
        if (osName.startsWith(OS_WINDOWS_PREFIX)) return OS_WIN;
        return osName.toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", "");
    }

    private static String getArch() {
        String osArch = System.getProperty("os.arch");
        return osArch.equalsIgnoreCase(ARCH_AMD64) ? ARCH_X86_64 : osArch;
    }

    static boolean isDevelopmentMode() {
        try (BootTrace.Span span = BootTrace.span("findFlutterBuildDirectory")) {
            return findFlutterBuildDirectory() != null;
        }
    }

    /**
     * Writes the entry next to its target and moves it into place, so a process starting at the
     * same time never loads a half-written file. If the move fails while the target exists (another
     * process put it there first, and it may already be loaded), that file is used.
     *
     * @return false when the existing file was kept instead
     */
    private static boolean extractAtomically(JarFile jarFile, JarEntry entry, File targetFile, Mover mover) throws IOException {
        Path target = targetFile.toPath();
        Path temp = Files.createTempFile(target.getParent(), targetFile.getName(), ".part");
        try {
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                mover.move(temp, target);
                return true;
            } catch (IOException moveFailed) {
                if (!Files.isRegularFile(target)) {
                    throw moveFailed;
                }
                return false;
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void writeAtomically(File targetFile, String content) throws IOException {
        Path target = targetFile.toPath();
        Path temp = Files.createTempFile(target.getParent(), targetFile.getName(), ".part");
        try {
            Files.write(temp, content.getBytes(StandardCharsets.UTF_8));
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void ensureDirectoryExists(File directory) throws IOException {
        // mkdirs() is also false when another process created the directory first, e.g. two apps
        // starting together on a fresh machine; only a directory that still is not there is a failure.
        if (!directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }
    }

    // NOTE: dev.equo.ewt.bundleDir is intentionally NOT consulted here. It names the
    // EXTERNAL app-bundle dir (EWT-owned combined bundle), not where Evolve loads its own
    // native bridge/engine from. The bundle path is handed to the bridge via setBundleDir
    // in applyExternalBundleOverride() (called from extractAndLoadFlutterLibraries after the
    // natives load); Evolve's own natives load from its build/jar as in standalone.
    static File findFlutterBuildDirectory() {
        String[] possiblePaths = {
                "flutter-lib/build",
                "../flutter-lib/build",
                "../../flutter-lib/build",
                "swt-evolve/flutter-lib/build",
                "../swt-evolve/flutter-lib/build",
                "../../swt-evolve/flutter-lib/build"
        };
        for (String path : possiblePaths) {
            File buildDir = new File(path);
            if (buildDir.exists() && buildDir.isDirectory()) {
                return buildDir;
            }
        }
        return null;
    }

    // The EWT-owned combined app bundle (libapp.so + flutter_assets). Names the external
    // bundle; Evolve loads its own native bridge/engine separately.
    private static final String EXTERNAL_BUNDLE_DIR = "dev.equo.ewt.bundleDir";

    /**
     * Returns the first provider's non-blank bundle base, or null if none supply one.
     * Package-private + Iterable-typed so it is unit-testable with a fixed list; the real
     * caller passes {@code ServiceLoader.load(ExternalBundleProvider.class)}.
     */
    static String firstBundleBaseDir(Iterable<ExternalBundleProvider> providers) {
        for (ExternalBundleProvider p : providers) {
            String base = p.extractAndGetBundleBaseDir();
            if (base != null && !base.trim().isEmpty()) {
                return base;
            }
        }
        return null;
    }

    /**
     * When an external combined bundle is configured, hand its path to the native bridge so
     * the engine boots that libapp.so / flutter_assets instead of the bridge-relative one.
     * No-op (legacy behavior) when the property is absent.
     */
    private static void applyExternalBundleOverride() {
        String dir = System.getProperty(EXTERNAL_BUNDLE_DIR);
        if (dir == null || dir.trim().isEmpty()) {
            // Production: no dev build dir. Discover the external bundle owner (EWT) via SPI,
            // which extracts the combined bundle and returns its base. Publish it back into the
            // property so EWT's NativeLibLoader (which reads the same property to attach-load
            // libwidgets) sees it too — the two consumers stay in sync without extra wiring.
            dir = firstBundleBaseDir(ServiceLoader.load(ExternalBundleProvider.class));
            if (dir == null) return;   // standalone Evolve — no external bundle, unchanged
            System.setProperty(EXTERNAL_BUNDLE_DIR, dir);
        }
        String os = getOS();
        // The native bridge (GetSharedLibraryPath) appends "/bundle/lib/libapp.so" and
        // "/bundle/data/..." to whatever it is given, so we must hand it the directory that
        // CONTAINS bundle/ (the per-OS "release" dir), NOT the bundle dir itself — otherwise
        // the engine gets a doubled ".../bundle/bundle/lib/libapp.so" and fails with
        // "Invalid ELF path". Only the Linux path is verified; Win/mac follow their own layout.
        String bundleBase;
        if (OS_LINUX.equals(os)) {
            bundleBase = new File(dir, LINUX_RELEASE).getAbsolutePath();
        } else if (OS_WIN.equals(os)) {
            bundleBase = new File(dir, WIN_RELEASE).getAbsolutePath();
        } else { // macOS
            bundleBase = new File(dir, SWTFLUTTER_APP_CONTENTS).getAbsolutePath();
        }
        if (!new File(bundleBase).isDirectory()) {
            System.err.println("[Equo] WARNING: " + EXTERNAL_BUNDLE_DIR + " resolved bundle base does not exist or is not a directory: " + bundleBase);
            return;
        }
        System.out.println("[Equo] external app bundle base: " + bundleBase);
        FlutterNative.setBundleDir(bundleBase);
    }

    /**
     * Custom exception for library loading and extraction failures.
     */
    static class LibraryLoaderException extends RuntimeException {
        public LibraryLoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}