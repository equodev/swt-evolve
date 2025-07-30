package dev.equo.swt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Manages the extraction and loading of native Flutter libraries.
 * Libraries are extracted to a subdirectory within the user's home directory.
 */
public class FlutterLibraryLoader {

    private static final String SEPARATOR = File.separator;
    private static final String USER_HOME = System.getProperty("user.home");

    // OS and Architecture Constants
    private static final String OS_LINUX = "linux";
    private static final String OS_MACOSX = "macosx";
    private static final String OS_WINDOWS_PREFIX = "Win";
    private static final String ARCH_AMD64 = "amd64";
    private static final String ARCH_X86_64 = "x86_64";

    // Directory and File Name Constants
    private static final String EQUO_BASE_DIR_NAME = ".equo";
    private static final String LIB_SUB_DIR_NAME = "lib";
    private static final String SWT_DIR_NAME = "swt";
    private static final String MACOS_LIB_NAME = "libFlutterBridge.dylib";
    public static final String CONTENTS = "Contents";
    public static final String SWTFLUTTER_APP = "swtflutter.app";
    public static final String SWTFLUTTER_APP_CONTENTS = "macos/Build/Products/Release/" + SWTFLUTTER_APP + SEPARATOR + CONTENTS;
    private static final String RUNNER_DIR_NAME = "runner";
    private static final String LINUX_BUNDLE_DIR_NAME = "bundle";
    private static final String LINUX_LIB_NAME = "libflutter_library.so";
    private static final String WIN_LIB1_NAME = "flutter_windows.dll";
    private static final String WIN_LIB_NAME = "flutter_library.dll";
    public static final String LINUX_X64_RELEASE = "linux/x64/release";
    public static final String WIN_X64_RELEASE = "windows/x64/runner/Release";

    private static final String EQUO_LIB_PATH_SUFFIX =
            EQUO_BASE_DIR_NAME + SEPARATOR + SWT_DIR_NAME + SEPARATOR + LIB_SUB_DIR_NAME + SEPARATOR + getOS() + SEPARATOR + getArch();

    /**
     * Initializes the Flutter library loader. This is the main public entry point.
     * It orchestrates the extraction and loading, wrapping any failure in a single, clear exception.
     * This method is synchronized and ensures that extraction and loading occur only once.
     *
     * @throws LibraryLoaderException if initialization fails for any reason.
     */
    public static void initialize() {
        try {
            extractAndLoadFlutterLibraries();
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

        ensureDirectoryExists(equoLibDir);

        boolean isDevelopmentMode = isDevelopmentMode();
        System.out.println("Running in development: " + isDevelopmentMode);

        if (OS_MACOSX.equals(os)) {
            extractAndLoadMacOSLibraries(equoLibDir, isDevelopmentMode);
        } else if (OS_LINUX.equals(os)) {
            extractAndLoadLinuxLibraries(equoLibDir, isDevelopmentMode);
        } else if ("win32".equals(os)) {
            extractAndLoadWinLibraries(equoLibDir, isDevelopmentMode);
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os + ". Equo SWT currently supports macOS, Windows and Linux.");
        }
    }

    private static void extractAndLoadMacOSLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            extractDirectoryFromJar(SWTFLUTTER_APP, targetDir);
            File libFile = new File(targetDir, SWTFLUTTER_APP + SEPARATOR + CONTENTS + SEPARATOR + MACOS_LIB_NAME);
            loadLibrary(libFile.getAbsolutePath());
        } else {
            loadOSLibraries(SWTFLUTTER_APP_CONTENTS, MACOS_LIB_NAME);
        }
    }

    private static void extractAndLoadLinuxLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            extractDirectoryFromJar(RUNNER_DIR_NAME, targetDir);
            extractDirectoryFromJar(LINUX_BUNDLE_DIR_NAME, targetDir);

            File libFile = new File(targetDir, RUNNER_DIR_NAME + SEPARATOR + LINUX_LIB_NAME);
            if (!libFile.exists()) {
                throw new IOException("Essential Linux library not found after extraction: " + libFile.getAbsolutePath());
            }
            setExecutablePermission(libFile);
            loadLibrary(libFile.getAbsolutePath());
        } else {
            loadOSLibraries(LINUX_X64_RELEASE, RUNNER_DIR_NAME + SEPARATOR + LINUX_LIB_NAME);
        }
    }

    private static void extractAndLoadWinLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            extractDirectoryFromJar(RUNNER_DIR_NAME, targetDir);

            File lib1File = new File(targetDir, RUNNER_DIR_NAME + SEPARATOR + WIN_LIB1_NAME);
            File libFile = new File(targetDir, RUNNER_DIR_NAME + SEPARATOR + WIN_LIB_NAME);
            if (!libFile.exists() || !lib1File.exists()) {
                throw new IOException("Essential Windows library not found after extraction: " + libFile.getAbsolutePath() + ", "+lib1File.getAbsolutePath());
            }
            loadLibrary(lib1File.getAbsolutePath());
            loadLibrary(libFile.getAbsolutePath());
        } else {
            loadOSLibraries(WIN_X64_RELEASE, SEPARATOR + WIN_LIB1_NAME);
            loadOSLibraries(WIN_X64_RELEASE, SEPARATOR + WIN_LIB_NAME);
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
     * Extracts a directory and its contents from the JAR file to a target directory.
     * This method now fails fast if any file cannot be extracted.
     */
    private static void extractDirectoryFromJar(String directoryPathInJar, File targetBaseDir) throws IOException {
        URL classUrl = FlutterLibraryLoader.class.getProtectionDomain().getCodeSource().getLocation();
        if (classUrl == null) {
            throw new IOException("Cannot determine JAR location to extract directory: " + directoryPathInJar);
        }
        File jarFileSource = new File(classUrl.getPath());

        System.out.println("Extracting '" + directoryPathInJar + "' from " + jarFileSource.getAbsolutePath() + " to " + targetBaseDir.getAbsolutePath());

        try (JarFile jarFile = new JarFile(jarFileSource)) {
            String normalizedPath = directoryPathInJar.endsWith("/") ? directoryPathInJar : directoryPathInJar + "/";
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(normalizedPath)) {
                    File targetFile = new File(targetBaseDir, entry.getName());
                    if (entry.isDirectory()) {
                        ensureDirectoryExists(targetFile);
                    } else {
                        ensureDirectoryExists(targetFile.getParentFile());
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
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
        try {
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
        if (osName.startsWith(OS_WINDOWS_PREFIX)) return "win32";
        return osName.toLowerCase().replaceAll("\\s+", "");
    }

    private static String getArch() {
        String osArch = System.getProperty("os.arch");
        return osArch.equalsIgnoreCase(ARCH_AMD64) ? ARCH_X86_64 : osArch;
    }

    private static boolean isDevelopmentMode() {
        return findFlutterBuildDirectory() != null;
    }

    private static void ensureDirectoryExists(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }
    }

    private static File findFlutterBuildDirectory() {
        String[] possiblePaths = {"flutter-lib/build", "../flutter-lib/build", "../../flutter-lib/build"};
        for (String path : possiblePaths) {
            File buildDir = new File(path);
            if (buildDir.exists() && buildDir.isDirectory()) {
                return buildDir;
            }
        }
        return null;
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
